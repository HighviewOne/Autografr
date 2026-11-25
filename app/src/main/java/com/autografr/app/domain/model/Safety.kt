package com.autografr.app.domain.model

/** What a report is about. Stored as the enum name in reports/{id}.targetType. */
enum class ReportTargetType { PHOTO, USER, REQUEST }

/** Reasons a user can pick when reporting. Names must match the list in firestore.rules. */
enum class ReportReason(val label: String) {
    SPAM("Spam or misleading"),
    HARASSMENT("Harassment or bullying"),
    SEXUAL("Sexual or nude content"),
    VIOLENCE("Violence or dangerous content"),
    HATE("Hate speech"),
    IMPERSONATION("Impersonation or fake autograph"),
    SCAM("Scam or fraud"),
    OTHER("Something else")
}

data class BlockedUser(
    val userId: String,
    val displayName: String
)
