package com.pocketrulebooks.app.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CustomTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val body: String = "",
)

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
    val customTabs: List<CustomTab> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

enum class RuleSection {
    Overview,
    Setup,
    Play,
    End,
}

sealed class SelectedTab {
    data class Builtin(val section: RuleSection) : SelectedTab()
    data class Custom(val id: String) : SelectedTab()
}

fun SelectedTab.storageKey(): String = when (this) {
    is SelectedTab.Builtin -> "b:${section.name}"
    is SelectedTab.Custom -> "c:$id"
}

fun tabFromKey(key: String): SelectedTab {
    return when {
        key.startsWith("c:") -> SelectedTab.Custom(key.removePrefix("c:"))
        key.startsWith("b:") -> {
            val name = key.removePrefix("b:")
            val section = RuleSection.entries.find { it.name == name } ?: RuleSection.Overview
            SelectedTab.Builtin(section)
        }
        else -> SelectedTab.Builtin(RuleSection.Overview)
    }
}

fun Game.migrated(): Game {
    val leftover = extra.trim()
    if (leftover.isEmpty()) return copy(extra = "")
    if (customTabs.any { it.body.trim() == leftover }) return copy(extra = "")
    return copy(
        extra = "",
        customTabs = customTabs + CustomTab(title = "기타", body = leftover),
    )
}

fun Game.sectionText(section: RuleSection): String = when (section) {
    RuleSection.Overview -> overview
    RuleSection.Setup -> setup
    RuleSection.Play -> play
    RuleSection.End -> ending
}

fun Game.tabText(tab: SelectedTab): String = when (tab) {
    is SelectedTab.Builtin -> sectionText(tab.section)
    is SelectedTab.Custom -> customTabs.find { it.id == tab.id }?.body.orEmpty()
}

fun Game.withSection(section: RuleSection, value: String): Game = when (section) {
    RuleSection.Overview -> copy(overview = value, updatedAt = System.currentTimeMillis())
    RuleSection.Setup -> copy(setup = value, updatedAt = System.currentTimeMillis())
    RuleSection.Play -> copy(play = value, updatedAt = System.currentTimeMillis())
    RuleSection.End -> copy(ending = value, updatedAt = System.currentTimeMillis())
}

fun Game.withTabText(tab: SelectedTab, value: String): Game = when (tab) {
    is SelectedTab.Builtin -> withSection(tab.section, value)
    is SelectedTab.Custom -> copy(
        customTabs = customTabs.map { if (it.id == tab.id) it.copy(body = value) else it },
        updatedAt = System.currentTimeMillis(),
    )
}

fun Game.withCustomTitle(id: String, title: String): Game = copy(
    customTabs = customTabs.map { if (it.id == id) it.copy(title = title) else it },
    updatedAt = System.currentTimeMillis(),
)

fun Game.addCustomTab(title: String): Pair<Game, CustomTab> {
    val tab = CustomTab(title = title.trim())
    return copy(
        customTabs = customTabs + tab,
        updatedAt = System.currentTimeMillis(),
    ) to tab
}

fun Game.removeCustomTab(id: String): Game = copy(
    customTabs = customTabs.filterNot { it.id == id },
    updatedAt = System.currentTimeMillis(),
)

fun Game.hasTab(tab: SelectedTab): Boolean = when (tab) {
    is SelectedTab.Builtin -> true
    is SelectedTab.Custom -> customTabs.any { it.id == tab.id }
}

private val EMOJIS = listOf("🎲", "♟️", "🃏", "🧩", "🎯", "🪙", "🏰", "🚂", "🐉", "🚀", "🧙", "🦊")

fun randomEmoji(): String = EMOJIS.random()

fun Game.fileName(): String {
    val base = title.trim().ifBlank { "rulebook" }
        .replace(Regex("""[\\/:*?"<>|]"""), "_")
        .take(40)
    return "$base.txt"
}
