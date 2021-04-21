package com.github.cannor147.util

import org.apache.commons.lang3.tuple.Pair
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*
import java.util.stream.Collectors
import kotlin.streams.asSequence

private const val QUOTE = "\""
private const val COMMA_DELIMITER = ','

@Throws(IOException::class)
fun readCsv(file: File, column: Int): List<String?> = readCsv(file)
    .asSequence()
    .map { safeGet(it, column) }
    .toList()

@Throws(IOException::class)
fun readCsv(file: File, firstColumn: Int, secondColumn: Int): List<Pair<String?, String?>> = readCsv(file)
    .asSequence()
    .map { row: List<String> -> Pair.of(safeGet(row, firstColumn), safeGet(row, secondColumn)) }
    .toList()

@Suppress("unused")
@Throws(IOException::class)
fun readCsv(file: File, vararg columns: Int): List<MutableList<String?>> = readCsv(file)
    .asSequence()
    .map { Arrays.stream(columns).mapToObj { column: Int -> safeGet(it, column) }.collect(Collectors.toList()) }
    .toList()

@Throws(IOException::class)
fun readCsv(file: File): List<List<String>> = BufferedReader(FileReader(file)).lines()
    .asSequence()
    .map { line ->
        val columns: MutableList<String> = ArrayList()
        val record = StringBuilder()
        line.forEach { ch ->
            val recordString = record.toString()
            if (ch == COMMA_DELIMITER && (!recordString.startsWith(QUOTE) || recordString.endsWith(QUOTE))) {
                columns.add(unquoteString(recordString))
                record.clear()
            } else {
                record.append(ch)
            }
        }
        columns.add(unquoteString(record.toString()))
        return@map columns
    }
    .toList()

private fun unquoteString(recordString: String): String = when {
    recordString.length == 1 -> recordString
    recordString.startsWith(QUOTE) -> recordString.substring(1, recordString.length - 1)
    else -> recordString
}

private fun <T> safeGet(list: List<T>, index: Int): T? = index
    .takeIf { it >= 0 && it < list.size }
    ?.let { list[it] }
