# Barangay Management System

## Module 1: Authentication & Security

- First-run setup:
  - If no SUPER_ADMIN exists, show a simple setup form (username, password, full name)
  - Basic password validation (min length and character mix) and BCrypt hashing
- Login:
  - Username/password form with BCrypt verification
  - Role-based redirection after login (SUPER_ADMIN, ADMIN, CLERK, RESIDENT)

## Module 2: User Management

- SUPER_ADMIN:
  - Create ADMIN and CLERK accounts; view/edit/deactivate/reactivate any user; reset CLERK/RESIDENT passwords
- ADMIN:
  - Create CLERK and RESIDENT accounts; view/edit/deactivate/reactivate; reset passwords
- CLERK:
  - Create RESIDENT accounts; reset RESIDENT passwords
- Account lifecycle:
  - Account creation/activation/deactivation

## Module 3: Resident Management

- Registration (CLERK/ADMIN/SUPER_ADMIN):
  - Core fields: name, birth date/place, gender, civil status, nationality, contact, address, occupation/employment, income bracket (optional), education (optional)
  - Resident ID auto-generation (BR-YYYY-XXXXX) and basic duplicate detection (name + birthdate)
- Search & View:
  - Quick search by name; filters: gender, age range; sort by name/registration date; pagination
  - View profile and key details
- Update:
  - Edit fields; except resident_id; track last updated timestamp
- Deactivation/Archive (ADMIN/SUPER_ADMIN):
  - Soft delete with reason; reactivate archived residents
- Statistics:
  - Basic counters only: total residents, by gender

## Module 4: Document Issuance

- Document Selection:
  - Pick resident and choose document type: Barangay ID, Barangay Clearance, Certificate of Residency
- Barangay ID:
  - Generate unique ID number (BID-YYYY-XXXXX), validity field (input), optional emergency contact, basic printable preview; record issuance
- Barangay Clearance:
  - Purpose, reference number (BC-YYYY-XXXXX), validity field (default value), basic printable preview; record issuance
- Certificate of Residency:
  - Purpose, residency computation (from registration date), reference number (CR-YYYY-XXXXX), validity field, basic printable preview; record issuance
- Document Management:
  - View issued documents; search by reference/resident/date; filter by type; reprint

### Module 5: Document Request (Resident-facing)

**Purpose:**
Allow RESIDENT users to request barangay documents online, track status, and download once issued, while staff still control approval and issuance in Module 4.

#### 5.1 Resident: create request

- Entry points:
    - From RESIDENT Dashboard → “Request Document” button.
    - From “My Documents” → “New Request”.
- Flow:
    - System loads the resident’s profile (read-only: name, address, civil status, residency dates, etc.).
    - Resident selects **Document Type**: Barangay ID / Barangay Clearance / Certificate of Residency.
    - Additional fields depending on type:
        - For Barangay Clearance / Certificate of Residency: purpose, optional notes, desired validity (if allowed).
        - For Barangay ID: preferred validity, optional emergency contact (overrides or confirms stored data).
    - Resident confirms and submits.
- System behavior:
    - Creates a **DocumentRequest** record with fields like:
        - `request_id` (DR-YYYY-XXXXX)
        - `resident_id` (link to Module 3)
        - `document_type` (ID / Clearance / Residency)
        - `purpose`, `requested_valid_until`, `notes`
        - `status` = "PENDING" (or "FOR_REVIEW")
        - `created_at`, `updated_at`
    - No official document reference (BC-…, CR-…, BID-…) is generated yet; that happens only on issuance in Module 4.


#### 5.2 Resident: track request status

- On the RESIDENT Dashboard, under **My Documents / Requests**, show a list with:
    - Request ID, document type, purpose, current status, last update.
- Status flow (recommended):
    - PENDING → UNDER_REVIEW → APPROVED → ISSUED
    - Or PENDING → UNDER_REVIEW → REJECTED
- Each request has a detail view with a simple timeline showing status changes and staff notes (e.g., “Approved by Clerk Juan Dela Cruz; please pay at barangay cashier”).


#### 5.3 Resident: download document

- Once the corresponding issuance is created in Module 4 and linked to the request, the request status becomes “ISSUED”.
- The request detail view now shows:
    - Issued document reference (BID-…, BC-…, CR-…).
    - Issued date and validity/expiry.
    - Buttons: “View / Download / Print”.
- The issued document also appears in **My Documents** (same record as Module 4 Document Management, but filtered to the logged-in resident).

***

### Changes needed in Module 4: Document Issuance

**New linkage with Document Request:**

- When staff open Module 4, they should see:
    - Tab A: “Walk-in Issuance” (no request; staff just pick resident and issue).
    - Tab B: “From Requests” (list of approved requests from Module 5).

**Processing a request-based issuance:**

- Staff select a pending/under-review request from the “From Requests” tab.
- They review resident details, confirm purpose, and check any compliance (payment, records, etc.).[^5][^2]
- Actions:
    - **Approve \& Issue**
        - System generates the actual document issuance with reference number (BID/BC/CR), issue date, validity.
        - Links the issuance to the originating `request_id`.
        - Updates request status to "ISSUED".
    - **Reject**
        - Staff enter rejection reason.
        - Request status changes to “REJECTED”; no issuance record is created.

***

### Dashboard impacts per role

**RESIDENT Dashboard (Module 9):**

- Add:
    - “My Requests / Documents” section showing:
        - Active requests with status (PENDING, UNDER_REVIEW, ISSUED, REJECTED).
        - Issued documents with download/print links.
- Quick action: “Request Document” → opens Module 5 request form.

**CLERK Dashboard:**

- Add a card: “Pending Document Requests” showing count of requests in PENDING or UNDER_REVIEW.
- Clicking opens Module 5 staff-side view: filter/search list, then drill-down to review and either forward to Issuance (Module 4) or reject.

**ADMIN Dashboard:**

- Similar card: “Pending Document Requests” plus “Documents Issued Today/This Month” sourced from Module 4, but now with breakdown by source (Walk-in vs Online Request) if you want.

**SUPER_ADMIN Dashboard:**

- No need for direct request handling, but may show high-level counters (total online requests, approval rate, average processing time) in Reports later.

## Module 6: Voter Application System

RESIDENT Features:

1. Submit Application

- Fill application form
- Upload valid ID (front/back)
- Select application type (new/transfer/reactivation)
- Provide current registration details (if transfer)
- Submit for review

2. Track Application Status

- View current status with timeline:
  - Pending > Under Review > Approved/Rejected > (If Approved) Scheduled > Verified

3. Download Appointment Slip

- Available once status = "SCHEDULED"
- Contains:
  - Appointment date/time
  - Venue (COMELEC office or Barangay Hall)
  - Requirements to bring
  - Contact person

CLERK/ADMIN Features:

1. Review Applications

- List all pending applications
- Filter by date, status
- View resident details and uploaded documents
- Approve/Reject with notes

2. Schedule COMELEC Verification

- Set appointment date/time
- Generate appointment slip
- Auto-update status to "SCHEDULED"

3. Generate Appointment Slip

- Unique slip reference number
- Include barangay contact info

4. Update Application Status

- Mark as "VERIFIED" after COMELEC confirmation
- Update resident's voter status in residents table

## Module 7: Barangay Officials Management

- Officials Registration (ADMIN/SUPER_ADMIN):
  - Select resident; assign position (Captain, 7 Kagawads, SK Chair, Secretary, Treasurer), term start/end, is_current;
- Officials List (All roles view-only):
  - Simple directory of current officials; print via browser
- Management (ADMIN/SUPER_ADMIN):
  - Edit info; end terms; basic term history

## Module 8: Reports & Analytics

- Simple dashboard counters:
  - Total residents; residents by gender; registered voters; documents issued (today/this month)

## Module 9: System Administration

- Barangay Information (ADMIN/SUPER_ADMIN):
  - Edit barangay name, city/municipality, province, region, address, contact;

## Module 10: Dashboard & Home

- SUPER_ADMIN Dashboard:
  - Cards: total users (by role), total residents; quick links to user mgmt and system settings; minimal recent actions
- ADMIN Dashboard:
  - Cards: total population, registered voters, documents issued (today/this month), pending voter applications; quick actions
- CLERK Dashboard:
  - Cards: residents registered (today), documents issued (today), pending tasks; quick actions
- RESIDENT Dashboard:
  - My profile, my documents (download/print), voter application status

## Module 11: Profile Management

- User Profile (All roles):
  - View: username, role, linked resident (if any), created date, last login
  - Change password
