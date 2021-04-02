package com.github.cannor147.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@SuppressWarnings("unused")
@UtilityClass
public class ReadUtils {
    private static final char COMMA_DELIMITER = ',';
    private static final String QUOTE = "\"";

    public static List<String> readCsv(File file, int column) throws IOException {
        return readCsv(file).stream()
                .map(row -> safeGet(row, column))
                .collect(Collectors.toList());
    }

    public static List<Pair<String, String>> readCsv(File file, int firstColumn, int secondColumn) throws IOException {
        return readCsv(file).stream()
                .map(row -> Pair.of(safeGet(row, firstColumn), safeGet(row, secondColumn)))
                .collect(Collectors.toList());
    }

    public static List<List<String>> readCsv(File file, int... columns) throws IOException {
        return readCsv(file).stream()
                .map(row -> stream(columns).mapToObj(column -> safeGet(row, column)).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public static List<List<String>> readCsv(File file) throws IOException {
        final List<List<String>> rows = new ArrayList<>();
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final List<String> columns = new ArrayList<>();
                StringBuilder record = new StringBuilder();
                for (int i = 0; i < line.length(); i++) {
                    final char c = line.charAt(i);
                    final String recordString = record.toString();
                    if (c == COMMA_DELIMITER && (!recordString.startsWith(QUOTE) || recordString.endsWith(QUOTE))) {
                        columns.add(unquoteString(recordString));
                        record = new StringBuilder();
                    } else {
                        record.append(c);
                    }
                }
                columns.add(unquoteString(record.toString()));
                rows.add(columns);
            }
        }
        return rows;
    }

    private static String unquoteString(String recordString) {
        if (recordString.length() == 1) {
            return recordString;
        }
        return recordString.startsWith(QUOTE) ? recordString.substring(1, recordString.length() - 1) : recordString;
    }

    private static <T> T safeGet(List<T> list, int index) {
        return index >= 0 && index < list.size() ? list.get(index) : null;
    }
}
