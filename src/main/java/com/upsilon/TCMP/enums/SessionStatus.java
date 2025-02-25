package com.upsilon.TCMP.enums;

public enum SessionStatus {
    SCHEDULED,      // Session is scheduled but hasn't started
    IN_PROGRESS,    // Session is currently ongoing
    COMPLETED,      // Session was completed successfully
    CANCELLED,      // Session was cancelled
    PENDING,        // Waiting for confirmation
    NO_SHOW,        // Student or tutor didn't show up
    RESCHEDULED     // Session was rescheduled
}