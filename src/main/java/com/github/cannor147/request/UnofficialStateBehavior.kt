package com.github.cannor147.request

enum class UnofficialStateBehavior(
    val includeUnmentioned: Boolean,
    val includeMentioned: Boolean
) {
    INCLUDE_ALL(true, true),
    INCLUDE_UNMENTIONED(true, false),
    EXCLUDE_ALL(false, false),
    ;
}