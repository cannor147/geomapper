package com.github.cannor147.geomapper

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import kotlin.collections.ArrayList

private const val QUOTE = "\""
private const val COMMA_DELIMITER = ','

@Throws(IOException::class)
fun readCsv(file: File, column: Int): List<String?> = readCsv(file).asSequence()
    .map { getSafely(it, column) }
    .toList()

@Throws(IOException::class)
fun readCsv(file: File, firstColumn: Int, secondColumn: Int): List<Pair<String?, String?>> = readCsv(file).asSequence()
    .map { row: List<String> -> getSafely(row, firstColumn) to getSafely(row, secondColumn) }
    .toList()

@Suppress("unused")
@Throws(IOException::class)
fun readCsv(file: File, vararg columns: Int): List<List<String?>> = readCsv(file).asSequence()
    .map { columns.map { column -> getSafely(it, column) } }
    .toList()

@Throws(IOException::class)
fun readCsv(file: File): List<List<String>> = BufferedReader(FileReader(file)).lineSequence()
    .map {
        it.foldIndexed(ArrayList<String>() to StringBuilder()) { i, (records, sb), ch ->
            val record = sb.toString()
            if (i == it.length - 1 || ch == COMMA_DELIMITER && (!record.startsWith(QUOTE) || record.endsWith(QUOTE))) {
                records.add(unquoteString(record))
                sb.clear()
            } else {
                sb.append(ch)
            }
            return@foldIndexed records to sb
        }
    }
    .map { it.first }
    .toList()

private fun unquoteString(recordString: String): String = when {
    recordString.length == 1 -> recordString
    recordString.startsWith(QUOTE) -> recordString.substring(1, recordString.length - 1)
    else -> recordString
}

private fun <T> getSafely(list: List<T>, index: Int): T? = index
    .takeIf { it >= 0 && it < list.size }
    ?.let { list[it] }
