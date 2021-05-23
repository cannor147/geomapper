package com.github.cannor147.geomapper.namer

interface Synonymized : Named {
    val synonyms: Array<String>
}