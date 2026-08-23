package com.pocketrulebooks.app.data

object RulebookCodec {
    private const val MAGIC = "POCKET_RULEBOOKS/1"

    fun encode(game: Game): String {
        val body = buildString {
            appendLine(MAGIC)
            appendLine("id: ${game.id}")
            appendLine("emoji: ${game.emoji}")
            appendLine("title: ${game.title}")
            appendLine("players: ${game.players}")
            appendLine("playTime: ${game.playTime}")
            appendLine()
            appendSection("overview", game.overview)
            appendSection("setup", game.setup)
            appendSection("play", game.play)
            appendSection("end", game.ending)
            appendSection("extra", game.extra)
        }
        return body.trimEnd() + "\n"
    }

    fun decode(raw: String, fallbackId: String? = null): Game {
        val text = raw.replace("\r\n", "\n").replace('\r', '\n')
        val lines = text.lines()
        val firstSection = lines.indexOfFirst { it.trim().startsWith("===") }.let {
            if (it < 0) lines.size else it
        }
        val header = lines.take(firstSection)
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.equals(MAGIC, ignoreCase = true) }
            .mapNotNull { line ->
                val idx = line.indexOf(':')
                if (idx <= 0) null
                else line.substring(0, idx).trim().lowercase() to line.substring(idx + 1).trim()
            }
            .toMap()

        val sections = linkedMapOf<String, StringBuilder>()
        var current: String? = null
        for (line in lines.drop(firstSection)) {
            val marker = parseMarker(line)
            if (marker != null) {
                current = marker
                sections.putIfAbsent(marker, StringBuilder())
            } else if (current != null) {
                val buf = sections.getValue(current)
                if (buf.isNotEmpty()) buf.append('\n')
                buf.append(line)
            }
        }

        fun sec(vararg keys: String): String {
            val found = keys.firstNotNullOfOrNull { key ->
                sections[key]?.toString()?.trim()
            }
            return found.orEmpty()
        }

        return Game(
            id = header["id"]?.ifBlank { null } ?: fallbackId ?: java.util.UUID.randomUUID().toString(),
            emoji = header["emoji"]?.ifBlank { null } ?: randomEmoji(),
            title = header["title"].orEmpty(),
            players = header["players"] ?: header["player"].orEmpty(),
            playTime = header["playtime"] ?: header["time"].orEmpty(),
            overview = sec("overview", "게임개요", "개요", "目標", "概要"),
            setup = sec("setup", "게임준비", "준비", "準備"),
            play = sec("play", "게임진행", "진행", "進行"),
            ending = sec("end", "ending", "게임종료", "종료", "終了"),
            extra = sec("extra", "기타", "その他", "other"),
        )
    }

    private fun StringBuilder.appendSection(key: String, value: String) {
        appendLine("=== $key ===")
        appendLine(value.trim())
        appendLine()
    }

    private fun parseMarker(line: String): String? {
        val trimmed = line.trim()
        if (!trimmed.startsWith("===") || !trimmed.endsWith("===")) return null
        val inner = trimmed.removePrefix("===").removeSuffix("===").trim().lowercase()
        if (inner.isEmpty()) return null
        val primary = inner.split("|").first().trim()
        return when (primary) {
            "게임개요", "개요", "overview", "goal", "目標", "概要" -> "overview"
            "게임준비", "준비", "setup", "準備" -> "setup"
            "게임진행", "진행", "play", "進行" -> "play"
            "게임종료", "종료", "end", "ending", "scoring", "終了" -> "end"
            "기타", "extra", "other", "その他" -> "extra"
            else -> primary
        }
    }
}
