#!/usr/bin/env python3
"""Populate barangay.db with mock data using Faker.

This script mirrors the schema defined in DatabaseConnection.java so it can be
used standalone (outside the JavaFX app) to spin up a realistic playground
database.  Default record counts intentionally stay modest, but you can tweak
all of them via CLI flags.
"""

from __future__ import annotations

import argparse
import json
import random
import sqlite3
import sys
import uuid
from dataclasses import dataclass
from datetime import UTC, date, datetime, timedelta
from pathlib import Path
from typing import Callable, Dict, List, Sequence, Set, Tuple

import bcrypt
from faker import Faker

ROOT_DIR = Path(__file__).parent
DEFAULT_DB = ROOT_DIR / "barangay_mock.db"
DOCUMENT_PREFIX = {
    "BARANGAY_ID": "BID",
    "BARANGAY_CLEARANCE": "BC",
    "CERTIFICATE_OF_RESIDENCY": "CR",
}
OFFICIAL_POSITIONS = ["CAPTAIN", "KAGAWAD",
                      "SK_CHAIRMAN", "SECRETARY", "TREASURER"]
DOCUMENT_TYPES = list(DOCUMENT_PREFIX.keys())
DOCUMENT_REQUEST_STATUSES = ["PENDING",
                             "UNDER_REVIEW", "APPROVED", "REJECTED", "ISSUED"]
APPLICATION_STATUSES = [
    "PENDING",
    "UNDER_REVIEW",
    "APPROVED",
    "REJECTED",
    "SCHEDULED",
    "VERIFIED",
]
APPLICATION_TYPES = ["NEW_REGISTRATION", "TRANSFER", "REACTIVATION"]
GENDERS = ["MALE", "FEMALE"]
CIVIL_STATUSES = ["SINGLE", "MARRIED", "WIDOWED", "SEPARATED", "DIVORCED"]
INCOME_BRACKETS = [
    "BELOW_10K",
    "TEN_TO_20K",
    "TWENTY_TO_30K",
    "THIRTY_TO_50K",
    "ABOVE_50K",
]
EDUCATION_LEVELS = [
    "NO_FORMAL_EDUCATION",
    "ELEMENTARY",
    "ELEMENTARY_GRADUATE",
    "HIGH_SCHOOL",
    "HIGH_SCHOOL_GRADUATE",
    "VOCATIONAL",
    "COLLEGE",
    "COLLEGE_GRADUATE",
    "POST_GRADUATE",
]

PH_CITIES = [
    "Quezon City",
    "Manila",
    "Cebu City",
    "Davao City",
    "Iloilo City",
    "Baguio",
    "Cagayan de Oro",
    "General Santos",
]
PH_PROVINCES = [
    "Laguna",
    "Bulacan",
    "Cebu",
    "Davao del Sur",
    "Pampanga",
    "Batangas",
    "Rizal",
    "Zamboanga del Sur",
]
PH_STREETS = [
    "Rizal Ave",
    "Bonifacio St",
    "Mabini St",
    "Roxas Blvd",
    "Luna St",
    "Aguinaldo Hwy",
    "Quezon Ave",
]


@dataclass
class ResidentRecord:
    resident_id: str
    full_name: str
    is_active: bool


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Populate barangay.db with mock data")
    parser.add_argument("--db", type=Path, default=DEFAULT_DB,
                        help="Path to SQLite database (default: barangay.db)")
    parser.add_argument("--residents", type=int, default=50,
                        help="Number of resident records to create")
    parser.add_argument("--users", type=int, default=12,
                        help="Number of user accounts to create")
    parser.add_argument("--document-requests", type=int,
                        default=25, help="Number of document requests to create")
    parser.add_argument("--documents", type=int, default=15,
                        help="Minimum number of issued documents to create")
    parser.add_argument("--voter-applications", type=int,
                        default=15, help="Number of voter applications to create")
    parser.add_argument("--officials", type=int, default=8,
                        help="Number of barangay officials to create")
    parser.add_argument("--seed", type=int, default=None,
                        help="Optional random seed for reproducible data")
    parser.add_argument("--reset", action="store_true",
                        help="Delete existing rows before seeding")
    parser.add_argument("--password", type=str, default="password",
                        help="Plaintext password applied to every generated user (default: password)")
    return parser.parse_args()


def ensure_schema(conn: sqlite3.Connection) -> None:
    conn.executescript(
        """
        PRAGMA foreign_keys = ON;
        CREATE TABLE IF NOT EXISTS users (
            id TEXT PRIMARY KEY,
            username TEXT UNIQUE NOT NULL,
            password_hash TEXT NOT NULL,
            role TEXT NOT NULL,
            linked_resident_id TEXT,
            is_active INTEGER DEFAULT 1,
            created_at TEXT NOT NULL,
            last_login_at TEXT,
            updated_at TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS residents (
            id TEXT PRIMARY KEY,
            first_name TEXT NOT NULL,
            middle_name TEXT,
            last_name TEXT NOT NULL,
            suffix TEXT,
            birth_date TEXT NOT NULL,
            birth_place TEXT,
            gender TEXT NOT NULL,
            civil_status TEXT,
            nationality TEXT,
            contact TEXT,
            house_number TEXT,
            street TEXT,
            purok TEXT,
            barangay TEXT,
            city TEXT,
            province TEXT,
            occupation TEXT,
            employment TEXT,
            income_bracket TEXT,
            education_level TEXT,
            is_voter INTEGER DEFAULT 0,
            is_active INTEGER DEFAULT 1,
            deactivation_reason TEXT,
            registered_at TEXT NOT NULL,
            updated_at TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS document_requests (
            id TEXT PRIMARY KEY,
            resident_id TEXT NOT NULL,
            document_type TEXT NOT NULL,
            purpose TEXT,
            requested_valid_until TEXT,
            notes TEXT,
            additional_info TEXT,
            status TEXT NOT NULL,
            staff_notes TEXT,
            handled_by TEXT,
            linked_document_reference TEXT,
            created_at TEXT NOT NULL,
            updated_at TEXT NOT NULL,
            FOREIGN KEY (resident_id) REFERENCES residents(id)
        );
        CREATE TABLE IF NOT EXISTS documents (
            reference TEXT PRIMARY KEY,
            resident_id TEXT NOT NULL,
            type TEXT NOT NULL,
            purpose TEXT,
            issued_date TEXT NOT NULL,
            valid_until TEXT,
            issued_by TEXT NOT NULL,
            additional_info TEXT,
            request_id TEXT,
            created_at TEXT NOT NULL,
            FOREIGN KEY (resident_id) REFERENCES residents(id),
            FOREIGN KEY (request_id) REFERENCES document_requests(id)
        );
        CREATE TABLE IF NOT EXISTS voter_applications (
            id TEXT PRIMARY KEY,
            resident_id TEXT NOT NULL,
            application_type TEXT NOT NULL,
            current_registration_details TEXT,
            valid_id_front_path TEXT,
            valid_id_back_path TEXT,
            status TEXT NOT NULL,
            review_notes TEXT,
            reviewed_by TEXT,
            appointment_datetime TEXT,
            appointment_venue TEXT,
            appointment_slip_reference TEXT,
            submitted_at TEXT NOT NULL,
            reviewed_at TEXT,
            updated_at TEXT NOT NULL,
            FOREIGN KEY (resident_id) REFERENCES residents(id)
        );
        CREATE TABLE IF NOT EXISTS barangay_officials (
            id TEXT PRIMARY KEY,
            resident_id TEXT NOT NULL,
            official_name TEXT NOT NULL,
            position TEXT NOT NULL,
            term_start TEXT NOT NULL,
            term_end TEXT NOT NULL,
            is_current INTEGER DEFAULT 1,
            created_at TEXT NOT NULL,
            updated_at TEXT NOT NULL,
            FOREIGN KEY (resident_id) REFERENCES residents(id)
        );
        """
    )


def reset_tables(conn: sqlite3.Connection) -> None:
    conn.execute("PRAGMA foreign_keys = OFF;")
    try:
        for table in [
            "documents",
            "document_requests",
            "voter_applications",
            "barangay_officials",
            "users",
            "residents",
        ]:
            conn.execute(f"DELETE FROM {table};")
    finally:
        conn.execute("PRAGMA foreign_keys = ON;")


def utc_now() -> datetime:
    # Return a naive datetime string representing UTC time (no offset)
    # so Java's LocalDateTime.parse(...) can consume it directly.
    return datetime.now(UTC).replace(tzinfo=None)


def iso_now(offset_days: int = 0) -> str:
    return (utc_now() + timedelta(days=offset_days)).isoformat(timespec="seconds")


def random_birth_date() -> date:
    today = date.today()
    start = today - timedelta(days=70 * 365)
    end = today - timedelta(days=18 * 365)
    return start + timedelta(days=random.randint(0, (end - start).days))


def make_resident_id(counter: int) -> str:
    year = utc_now().year
    return f"BR-{year}-{counter:010d}"


def fake_contact_number(fake: Faker) -> str:
    phone_func = getattr(fake, "phone_number", None)
    if callable(phone_func):
        try:
            return phone_func()
        except AttributeError:
            pass
    # Fall back to a deterministic mobile-style pattern when locale lacks phone numbers
    return fake.numerify("+63 9## ### ####")


def faker_with_fallback(fake: Faker, attr: str, fallback: Callable[[], str]) -> str:
    func = getattr(fake, attr, None)
    if callable(func):
        try:
            value = func()
            if value:
                return value
        except AttributeError:
            pass
    return fallback()


def fake_city_name(fake: Faker) -> str:
    return faker_with_fallback(fake, "city", lambda: random.choice(PH_CITIES))


def fake_province_name(fake: Faker) -> str:
    return faker_with_fallback(fake, "state", lambda: random.choice(PH_PROVINCES))


def fake_street_name(fake: Faker) -> str:
    return faker_with_fallback(fake, "street_name", lambda: random.choice(PH_STREETS))


def fake_street_address(fake: Faker) -> str:
    return faker_with_fallback(
        fake,
        "street_address",
        lambda: f"{fake.numerify('###')} {fake_street_name(fake)}",
    )


def fake_json_blob(fake: Faker, elements: int = 3) -> str:
    data = fake.pydict(nb_elements=elements, value_types=[
                       str, int, float, bool])
    return json.dumps(data)


def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt(10)).decode("utf-8")


def sanitize_username(raw: str) -> str:
    cleaned = "".join(ch for ch in raw.lower() if ch.isalnum())
    return cleaned or f"user{random.randint(1000, 9999)}"


def generate_username(fake: Faker, used: set[str]) -> str:
    for _ in range(10):
        candidate = sanitize_username(fake.name())
        if candidate not in used:
            used.add(candidate)
            return candidate
    fallback = f"user{random.randint(10000, 99999)}"
    used.add(fallback)
    return fallback


def add_residents(conn: sqlite3.Connection, fake: Faker, count: int) -> List[ResidentRecord]:
    if count <= 0:
        return []
    rows: List[Tuple] = []
    residents: List[ResidentRecord] = []
    for idx in range(1, count + 1):
        rid = make_resident_id(idx)
        first = fake.first_name()
        last = fake.last_name()
        middle = fake.first_name() if random.random() < 0.6 else None
        suffix = random.choice([None, "Jr.", "Sr.", "III", "IV"])
        birth_dt = random_birth_date()
        gender = random.choice(GENDERS)
        civil_status = random.choice(CIVIL_STATUSES)
        contact = fake_contact_number(fake)
        address = fake_street_address(fake).split("\n")
        purok = f"Zone {random.randint(1, 7)}"
        barangay = fake_street_name(fake)
        city = fake_city_name(fake)
        province = fake_province_name(fake)
        occupation = fake.job()
        employment = random.choice(
            ["Employed", "Self-employed", "Unemployed", "Student"])
        income = random.choice(INCOME_BRACKETS)
        education = random.choice(EDUCATION_LEVELS)
        is_voter = random.random() < 0.7
        is_active = random.random() < 0.9
        now_iso = iso_now(-random.randint(0, 90))
        rows.append(
            (
                rid,
                first,
                middle,
                last,
                suffix,
                birth_dt.isoformat(),
                fake_city_name(fake),
                gender,
                civil_status,
                "Filipino",
                contact,
                address[0] if address else None,
                fake_street_name(fake),
                purok,
                barangay,
                city,
                province,
                occupation,
                employment,
                income,
                education,
                int(is_voter),
                int(is_active),
                None if is_active else "Relocated",
                now_iso,
                now_iso,
            )
        )
        residents.append(ResidentRecord(rid, f"{first} {last}", is_active))
    conn.executemany(
        """
        INSERT INTO residents (
            id, first_name, middle_name, last_name, suffix, birth_date, birth_place, gender,
            civil_status, nationality, contact, house_number, street, purok, barangay, city,
            province, occupation, employment, income_bracket, education_level, is_voter,
            is_active, deactivation_reason, registered_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        rows,
    )
    return residents


def add_users(
    conn: sqlite3.Connection,
    fake: Faker,
    residents: List[ResidentRecord],
    target: int,
    password_hash: str,
) -> None:
    if target <= 0 or not residents:
        return
    selected = residents.copy()
    random.shuffle(selected)
    target = min(target, len(selected))
    # Exclude SUPER_ADMIN from generated users as requested
    roles_cycle = ["ADMIN", "CLERK", "RESIDENT"]
    rows = []
    used_usernames: Set[str] = set()
    for idx in range(target):
        resident = selected[idx]
        role = roles_cycle[idx] if idx < len(
            roles_cycle) else random.choice(roles_cycle)
        username = generate_username(fake, used_usernames)
        now_iso = iso_now(-random.randint(0, 30))
        rows.append(
            (
                str(uuid.uuid4()),
                username,
                password_hash,
                role,
                resident.resident_id,
                1,
                now_iso,
                None,
                now_iso,
            )
        )
    conn.executemany(
        """
        INSERT INTO users (
            id, username, password_hash, role, linked_resident_id, is_active,
            created_at, last_login_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        rows,
    )


def make_request_id(counter: int) -> str:
    year = utc_now().year
    return f"REQ-{year}-{counter:06d}"


def make_document_reference(doc_type: str, counter: int) -> str:
    prefix = DOCUMENT_PREFIX[doc_type]
    year = utc_now().year
    return f"{prefix}-{year}-{counter:010d}"


def add_document_requests(
    conn: sqlite3.Connection,
    fake: Faker,
    residents: List[ResidentRecord],
    target: int,
) -> List[Dict[str, str]]:
    if target <= 0 or not residents:
        return []
    rows = []
    issuable: List[Dict[str, str]] = []
    for idx in range(1, target + 1):
        resident = random.choice(residents)
        doc_type = random.choice(DOCUMENT_TYPES)
        status = random.choice(DOCUMENT_REQUEST_STATUSES)
        created_at = iso_now(-random.randint(1, 45))
        updated_at = created_at
        linked_reference = None
        handled_by = None
        staff_notes = None
        if status in {"UNDER_REVIEW", "APPROVED", "REJECTED", "ISSUED"}:
            handled_by = fake.user_name()
            staff_notes = fake.sentence(nb_words=8)
            updated_at = iso_now(-random.randint(0, 10))
        if status == "ISSUED":
            issuable.append({"resident_id": resident.resident_id,
                            "document_type": doc_type, "request_id": make_request_id(idx)})
        rows.append(
            (
                make_request_id(idx),
                resident.resident_id,
                doc_type,
                fake.sentence(nb_words=6),
                (date.today() + timedelta(days=random.randint(10, 60))).isoformat(),
                fake.sentence(nb_words=10),
                fake_json_blob(fake),
                status,
                staff_notes,
                handled_by,
                linked_reference,
                created_at,
                updated_at,
            )
        )
    conn.executemany(
        """
        INSERT INTO document_requests (
            id, resident_id, document_type, purpose, requested_valid_until, notes,
            additional_info, status, staff_notes, handled_by, linked_document_reference,
            created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        rows,
    )
    return issuable


def add_documents(
    conn: sqlite3.Connection,
    fake: Faker,
    residents: List[ResidentRecord],
    issuable_from_requests: List[Dict[str, str]],
    minimum_total: int,
) -> None:
    rows = []
    counter = 1
    for payload in issuable_from_requests:
        reference = make_document_reference(payload["document_type"], counter)
        rows.append(
            (
                reference,
                payload["resident_id"],
                payload["document_type"],
                fake.sentence(nb_words=5),
                date.today().isoformat(),
                (date.today() + timedelta(days=90)).isoformat(),
                fake.user_name(),
                fake_json_blob(fake, elements=2),
                payload["request_id"],
                iso_now(),
            )
        )
        counter += 1
    while len(rows) < max(minimum_total, len(rows)):
        resident = random.choice(residents)
        doc_type = random.choice(DOCUMENT_TYPES)
        reference = make_document_reference(doc_type, counter)
        rows.append(
            (
                reference,
                resident.resident_id,
                doc_type,
                fake.sentence(nb_words=5),
                (date.today() - timedelta(days=random.randint(0, 30))).isoformat(),
                (date.today() + timedelta(days=random.randint(30, 180))).isoformat(),
                fake.user_name(),
                fake_json_blob(fake, elements=2),
                None,
                iso_now(),
            )
        )
        counter += 1
    if rows:
        conn.executemany(
            """
            INSERT INTO documents (
                reference, resident_id, type, purpose, issued_date, valid_until,
                issued_by, additional_info, request_id, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            rows,
        )


def add_voter_applications(
    conn: sqlite3.Connection,
    fake: Faker,
    residents: List[ResidentRecord],
    target: int,
) -> None:
    if target <= 0 or not residents:
        return
    rows = []
    for idx in range(1, target + 1):
        resident = random.choice(residents)
        app_id = f"VA-{utc_now().year}-{idx:05d}"
        status = random.choice(APPLICATION_STATUSES)
        submitted_at = iso_now(-random.randint(10, 60))
        updated_at = iso_now(-random.randint(0, 9))
        appointment_dt = None
        appointment_venue = None
        slip_ref = None
        review_notes = None
        reviewed_by = None
        if status in {"UNDER_REVIEW", "APPROVED", "REJECTED", "SCHEDULED", "VERIFIED"}:
            review_notes = fake.sentence(nb_words=8)
            reviewed_by = fake.user_name()
        if status in {"SCHEDULED", "VERIFIED"}:
            appointment_dt = (
                utc_now() + timedelta(days=random.randint(1, 14))
            ).isoformat(timespec="seconds")
            appointment_venue = f"Barangay Hall Room {random.randint(1, 5)}"
            slip_ref = f"SLIP-{idx:05d}"
        rows.append(
            (
                app_id,
                resident.resident_id,
                random.choice(APPLICATION_TYPES),
                fake.sentence(nb_words=6),
                None,
                None,
                status,
                review_notes,
                reviewed_by,
                appointment_dt,
                appointment_venue,
                slip_ref,
                submitted_at,
                None,
                updated_at,
            )
        )
    conn.executemany(
        """
        INSERT INTO voter_applications (
            id, resident_id, application_type, current_registration_details,
            valid_id_front_path, valid_id_back_path, status, review_notes,
            reviewed_by, appointment_datetime, appointment_venue,
            appointment_slip_reference, submitted_at, reviewed_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        rows,
    )


def add_officials(
    conn: sqlite3.Connection,
    fake: Faker,
    residents: List[ResidentRecord],
    target: int,
) -> None:
    if target <= 0 or not residents:
        return
    unique_residents = residents.copy()
    random.shuffle(unique_residents)
    rows = []
    year = utc_now().year
    for idx in range(min(target, len(unique_residents))):
        resident = unique_residents[idx]
        start = date(year - 1, random.randint(1, 12), random.randint(1, 28))
        end = start + timedelta(days=365)
        is_current = int(end >= date.today())
        now_iso = iso_now(-random.randint(0, 20))
        rows.append(
            (
                f"OFF-{uuid.uuid4().hex[:10]}",
                resident.resident_id,
                resident.full_name,
                random.choice(OFFICIAL_POSITIONS),
                start.isoformat(),
                end.isoformat(),
                is_current,
                now_iso,
                now_iso,
            )
        )
    conn.executemany(
        """
        INSERT INTO barangay_officials (
            id, resident_id, official_name, position, term_start, term_end,
            is_current, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        rows,
    )


def main() -> None:
    args = parse_args()
    if args.seed is not None:
        random.seed(args.seed)
        Faker.seed(args.seed)
    fake = Faker("en_PH")
    db_path = args.db
    db_path.parent.mkdir(parents=True, exist_ok=True)
    conn = sqlite3.connect(db_path)
    try:
        ensure_schema(conn)
        if args.reset:
            reset_tables(conn)
        password_hash = hash_password(args.password)
        residents = add_residents(conn, fake, args.residents)
        add_users(conn, fake, residents, args.users, password_hash)
        issuable = add_document_requests(
            conn, fake, residents, args.document_requests)
        add_documents(conn, fake, residents, issuable, args.documents)
        add_voter_applications(conn, fake, residents, args.voter_applications)
        add_officials(conn, fake, residents, args.officials)
        conn.commit()
    finally:
        conn.close()
    print(
        f"Mock data inserted successfully. All generated users share the password '{args.password}'."
    )


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        sys.exit(1)
