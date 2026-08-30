package com.pocketrulebooks.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class GameStore(context: Context) {
    private val file = File(context.filesDir, "games.json")
    private val mutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    suspend fun load(): List<Game> = mutex.withLock {
        withContext(Dispatchers.IO) {
            if (!file.exists()) return@withContext emptyList()
            runCatching {
                json.decodeFromString<List<Game>>(file.readText(Charsets.UTF_8))
                    .map { it.migrated() }
                    .sortedByDescending { it.updatedAt }
            }.getOrDefault(emptyList())
        }
    }

    suspend fun save(games: List<Game>) = mutex.withLock {
        withContext(Dispatchers.IO) {
            file.writeText(json.encodeToString(games), Charsets.UTF_8)
        }
    }
}
