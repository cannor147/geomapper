package com.github.cannor147.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UnofficialStateBehavior {
    INCLUDE_ALL(true, true),
    INCLUDE_UNMENTIONED(true, false),
    EXCLUDE_ALL(false, false)
    ;

    private final boolean includeUnmentioned;
    private final boolean includeMentioned;
}
