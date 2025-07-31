package com.admin.event_management_backend_java_spring.academic.model;

public enum Semester {
    SEMESTER_1("Kỳ 1", 1),
    SEMESTER_2("Kỳ 2", 2),
    SEMESTER_3("Kỳ 3", 3),
    SEMESTER_4("Kỳ 4", 4),
    SEMESTER_5("Kỳ 5", 5),
    SEMESTER_6("Kỳ 6", 6),
    SEMESTER_7("Kỳ 7", 7),
    SEMESTER_8("Kỳ 8", 8);
    
    private final String displayName;
    private final int semesterNumber;
    
    Semester(String displayName, int semesterNumber) {
        this.displayName = displayName;
        this.semesterNumber = semesterNumber;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getSemesterNumber() {
        return semesterNumber;
    }
    
    public static Semester fromNumber(int number) {
        for (Semester semester : values()) {
            if (semester.semesterNumber == number) {
                return semester;
            }
        }
        throw new IllegalArgumentException("Invalid semester number: " + number);
    }
    
    public String getTrainingPointsFieldName() {
        return "trainingPoints" + semesterNumber;
    }
}