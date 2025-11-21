package com.barangay.domain.entities;

import com.barangay.presentation.util.StringUtil;

public enum EducationLevel {
    NO_FORMAL_EDUCATION,
    ELEMENTARY,
    ELEMENTARY_GRADUATE,
    HIGH_SCHOOL,
    HIGH_SCHOOL_GRADUATE,
    VOCATIONAL,
    COLLEGE,
    COLLEGE_GRADUATE,
    POST_GRADUATE;

    @Override
    public String toString() {
        return StringUtil.toTitleCase(super.toString());
    }
}
