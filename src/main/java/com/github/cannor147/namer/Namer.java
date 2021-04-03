package com.github.cannor147.namer;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@UtilityClass
public class Namer {
    public static List<String> findNames(Synonymized synonymized) {
        return Stream.concat(Arrays.stream(synonymized.getSynonyms()), Stream.of(synonymized.getName()))
                .collect(Collectors.toList());
    }

    public static <T extends Synonymized> Map<String, T> createMap(T[] items) {
        return createMap(Arrays.asList(items));
    }

    public static <T extends Synonymized> Map<String, T> createMap(Iterable<T> items) {
        return StreamSupport.stream(items.spliterator(), false)
                .map(item -> Pair.of(findNames(item), item))
                .flatMap(pair -> pair.getKey().stream().map(name -> Pair.of(normalize(name), pair.getRight())))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    public static String normalize(String name) {
        return name.toLowerCase().replace('–', '-').replace('—', '-').trim();
    }
}
