package com.github.cannor147.namer

fun findNames(obj: Synonymized): List<String> = obj.synonyms.asSequence()
    .plus(obj.name)
    .toList()

fun <T : Synonymized> createMap(items: Array<T>): Map<String, T> = createMap(listOf(*items))

fun <T : Synonymized> createMap(items: Iterable<T>): Map<String, T> = items.asSequence()
    .map { item: T -> findNames(item) to item }
    .flatMap {
        it.first
            .asSequence()
            .map { name: String -> normalize(name) to it.second }
    }
    .associateBy(Pair<String, T>::first, Pair<String, T>::second)

fun normalize(name: String): String = name
    .toLowerCase()
    .replace('–', '-')
    .replace('—', '-')
    .trim { it <= ' ' }