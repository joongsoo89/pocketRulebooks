package com.pocketrulebooks.app.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Game(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String = randomEmoji(),
    val title: String = "",
    val players: String = "",
    val playTime: String = "",
    val overview: String = "",
    val setup: String = "",
    val play: String = "",
    val ending: String = "",
    val extra: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

enum class RuleSection {
    Overview,
    Setup,
    Play,
    End,
    Extra,
}

fun Game.sectionText(section: RuleSection): String = when (section) {
    RuleSection.Overview -> overview
    RuleSection.Setup -> setup
    RuleSection.Play -> play
    RuleSection.End -> ending
    RuleSection.Extra -> extra
}

fun Game.withSection(section: RuleSection, value: String): Game = when (section) {
    RuleSection.Overview -> copy(overview = value, updatedAt = System.currentTimeMillis())
    RuleSection.Setup -> copy(setup = value, updatedAt = System.currentTimeMillis())
    RuleSection.Play -> copy(play = value, updatedAt = System.currentTimeMillis())
    RuleSection.End -> copy(ending = value, updatedAt = System.currentTimeMillis())
    RuleSection.Extra -> copy(extra = value, updatedAt = System.currentTimeMillis())
}

private val EMOJIS = listOf("🎲", "♟️", "🃏", "🧩", "🎯", "🪙", "🏰", "🚂", "🐉", "🚀", "🧙", "🦊")

fun randomEmoji(): String = EMOJIS.random()

fun Game.fileName(): String {
    val base = title.trim().ifBlank { "rulebook" }
        .replace(Regex("""[\\/:*?"<>|]"""), "_")
        .take(40)
    return "$base.txt"
}
