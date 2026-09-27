package com.geecee.escapelauncher.feature.appslist

import com.geecee.escapelauncher.core.model.InstalledApp

class AppSearchTrie {
    class Node {
        val children = mutableMapOf<Char, Node>()
        val apps = mutableSetOf<InstalledApp>()
    }
    
    private val root = Node()

    fun insert(app: InstalledApp, normalizedName: String) {
        // Insert exact matches (prefix of the entire name)
        insertWord(app, normalizedName)
        
        // Insert prefixes for each word in the app name
        val words = normalizedName.split(" ")
        if (words.size > 1) {
            for (i in 1 until words.size) {
                // Insert each subsequent word so searching "browser" matches "elril browser"
                insertWord(app, words.drop(i).joinToString(" "))
            }
        }
        
        // Advanced: Insert acronyms (e.g., "ps" for "Play Store")
        val acronym = words.mapNotNull { it.firstOrNull() }.joinToString("")
        if (acronym.length > 1) {
            insertWord(app, acronym)
        }
    }

    private fun insertWord(app: InstalledApp, word: String) {
        var current = root
        for (char in word) {
            current = current.children.getOrPut(char) { Node() }
            current.apps.add(app)
        }
    }

    // O(K) lookup time where K is the length of the search query (instant)
    fun search(prefix: String): Set<InstalledApp> {
        var current = root
        for (char in prefix) {
            current = current.children[char] ?: return emptySet()
        }
        return current.apps
    }
}