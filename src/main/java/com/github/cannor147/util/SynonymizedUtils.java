package com.github.cannor147.util;

import lombok.experimental.UtilityClass;
import com.github.cannor147.model.named.Synonymized;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@UtilityClass
public class SynonymizedUtils {
    public static List<String> findNames(Synonymized synonymized) {
        return Stream.concat(Arrays.stream(synonymized.getSynonyms()), Stream.of(synonymized.getName()))
                .collect(Collectors.toList());
    }
}
