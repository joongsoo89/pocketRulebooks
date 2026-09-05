package com.pocketrulebooks.app.data

object RulebookCodec {
    private const val MAGIC = "POCKET_RULEBOOKS/1"

    fun encode(game: Game): String {
        val normalized = game.migrated()
        val body = buildString {
            appendLine(MAGIC)
            appendLine("id: ${normalized.id}")
            appendLine("title: ${normalized.title}")
            appendLine("players: ${normalized.players}")
            appendLine("playTime: ${normalized.playTime}")
            appendLine("labels: ${normalized.labels.joinToString(", ")}")
            appendLine()
            appendSection("overview", normalized.overview)
            appendSection("setup", normalized.setup)
            appendSection("play", normalized.play)
            appendSection("end", normalized.ending)
            for (tab in normalized.customTabs) {
                appendSection("tab:${tab.title.ifBlank { "untitled" }}", tab.body)
            }
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

        data class Block(val key: String, val title: String, val body: StringBuilder = StringBuilder())
        val blocks = mutableListOf<Block>()
        var current: Block? = null
        for (line in lines.drop(firstSection)) {
            val marker = parseMarker(line)
            if (marker != null) {
                current = Block(marker.first, marker.second)
                blocks += current
            } else if (current != null) {
                if (current.body.isNotEmpty()) current.body.append('\n')
                current.body.append(line)
            }
        }

        fun sec(vararg keys: String): String {
            return blocks.firstOrNull { it.key in keys }?.body?.toString()?.trim().orEmpty()
        }

        val extraBody = sec("extra")
        val customTabs = blocks
            .filter { it.key == "custom" }
            .map { CustomTab(title = it.title, body = it.body.toString().trim()) }
            .let { tabs ->
                if (extraBody.isBlank()) tabs
                else tabs + CustomTab(title = "기타", body = extraBody)
            }

        return Game(
            id = header["id"]?.ifBlank { null } ?: fallbackId ?: java.util.UUID.randomUUID().toString(),
            emoji = header["emoji"].orEmpty(),
            labels = parseLabels(header["labels"].orEmpty()),
            title = header["title"].orEmpty(),
            players = header["players"] ?: header["player"].orEmpty(),
            playTime = header["playtime"] ?: header["time"].orEmpty(),
            overview = sec("overview"),
            setup = sec("setup"),
            play = sec("play"),
            ending = sec("end"),
            extra = "",
            customTabs = customTabs,
        ).migrated()
    }

    private fun StringBuilder.appendSection(key: String, value: String) {
        appendLine("=== $key ===")
        appendLine(value.trim())
        appendLine()
    }

    private fun parseMarker(line: String): Pair<String, String>? {
        val trimmed = line.trim()
        if (!trimmed.startsWith("===") || !trimmed.endsWith("===")) return null
        val inner = trimmed.removePrefix("===").removeSuffix("===").trim()
        if (inner.isEmpty()) return null
        val primary = inner.split("|").first().trim()
        val lower = primary.lowercase()
        val key = when {
            lower in setOf("게임개요", "개요", "overview", "goal", "目標", "概要") -> "overview"
            lower in setOf("게임준비", "준비", "setup", "準備") -> "setup"
            lower in setOf("게임진행", "진행", "play", "進行") -> "play"
            lower in setOf("게임종료", "종료", "end", "ending", "scoring", "終了") -> "end"
            lower in setOf("기타", "extra", "other", "その他") -> "extra"
            lower.startsWith("tab:") -> "custom"
            else -> "custom"
        }
        val title = if (key == "custom") {
            primary.removePrefix("tab:").removePrefix("TAB:").trim().ifBlank { primary }
        } else {
            primary
        }
        return key to title
    }
}
