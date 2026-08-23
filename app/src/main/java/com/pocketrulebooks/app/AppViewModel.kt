package com.pocketrulebooks.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketrulebooks.app.data.Game
import com.pocketrulebooks.app.data.GameStore
import com.pocketrulebooks.app.data.RulebookCodec
import com.pocketrulebooks.app.ui.Lang
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val store = GameStore(application)
    private val prefs = application.getSharedPreferences("pocket_rulebooks", 0)

    private val _games = MutableStateFlow<List<Game>>(emptyList())
    val games: StateFlow<List<Game>> = _games.asStateFlow()

    private val _lang = MutableStateFlow(readLang())
    val lang: StateFlow<Lang> = _lang.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            _games.value = store.load()
        }
    }

    fun setLang(lang: Lang) {
        _lang.value = lang
        prefs.edit().putString("lang", lang.name).apply()
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun upsert(game: Game) {
        val next = game.copy(updatedAt = System.currentTimeMillis())
        _games.update { current ->
            val without = current.filterNot { it.id == next.id }
            listOf(next) + without
        }
        persist()
    }

    fun delete(id: String) {
        _games.update { it.filterNot { game -> game.id == id } }
        persist()
    }

    fun game(id: String?): Game? = _games.value.find { it.id == id }

    fun importText(text: String, targetId: String? = null): Game? {
        return runCatching {
            val parsed = RulebookCodec.decode(text, fallbackId = targetId)
            val existing = targetId?.let { id -> _games.value.find { it.id == id } }
            val merged = if (existing != null) {
                parsed.copy(
                    id = existing.id,
                    createdAt = existing.createdAt,
                    emoji = parsed.emoji.ifBlank { existing.emoji },
                    title = parsed.title.ifBlank { existing.title },
                )
            } else {
                val byId = _games.value.find { it.id == parsed.id }
                if (byId != null) parsed.copy(createdAt = byId.createdAt) else parsed
            }
            if (merged.title.isBlank()) {
                _message.value = "empty-title"
                return null
            }
            upsert(merged)
            _message.value = "import-ok"
            merged
        }.getOrElse {
            _message.value = "import-fail"
            null
        }
    }

    fun exportText(game: Game): String = RulebookCodec.encode(game)

    fun notify(code: String) {
        _message.value = code
    }

    private fun persist() {
        val snapshot = _games.value
        viewModelScope.launch { store.save(snapshot) }
    }

    private fun readLang(): Lang {
        val stored = prefs.getString("lang", null)
        return Lang.entries.find { it.name == stored } ?: Lang.fromSystem()
    }
}
