package com.github.cannor147.namer

interface Synonymized : Named {
    val synonyms: Array<String>
}