package com.github.cannor147.util;

import lombok.experimental.UtilityClass;
import com.github.cannor147.model.named.Synonymized;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class SynonymizedUtils {
    public static List<String> findNames(Synonymized synonymized) {
        return Stream.concat(Arrays.stream(synonymized.getSynonyms()), Stream.of(synonymized.getName()))
                .collect(Collectors.toList());
    }
}
