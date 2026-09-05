package com.pocketrulebooks.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketrulebooks.app.data.Game
import com.pocketrulebooks.app.data.RuleSection
import com.pocketrulebooks.app.data.SelectedTab
import com.pocketrulebooks.app.data.hasTab
import com.pocketrulebooks.app.data.storageKey
import com.pocketrulebooks.app.data.tabFromKey
import com.pocketrulebooks.app.data.tabText
import com.pocketrulebooks.app.ui.theme.Burgundy
import com.pocketrulebooks.app.ui.theme.Cream
import com.pocketrulebooks.app.ui.theme.Ink
import com.pocketrulebooks.app.ui.theme.Muted
import com.pocketrulebooks.app.ui.theme.Paper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    lang: Lang,
    game: Game,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onLang: (Lang) -> Unit,
) {
    val t = stringsForLang(lang)
    var tabKey by rememberSaveable { mutableStateOf(SelectedTab.Builtin(RuleSection.Overview).storageKey()) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val selected = run {
        val current = tabFromKey(tabKey)
        if (game.hasTab(current)) current else SelectedTab.Builtin(RuleSection.Overview)
    }
    val hint = (selected as? SelectedTab.Builtin)?.let { t.tabHint[it.section].orEmpty() }.orEmpty()
    val heading = when (selected) {
        is SelectedTab.Builtin -> t.tabFull.getValue(selected.section)
        is SelectedTab.Custom -> game.customTabs.find { it.id == selected.id }?.title.orEmpty()
    }
    val body = game.tabText(selected).ifBlank { t.noContent }
    val bits = listOf(game.players, game.playTime).filter { it.isNotBlank() }

    Scaffold(
        containerColor = Paper,
        topBar = {
            TopAppBar(
                title = { Text(game.title.ifBlank { t.appName }, maxLines = 1) },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } },
                actions = { LanguageBar(lang, onLang) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper, titleContentColor = Ink),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Cream),
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CoverThumb(game.id, game.photoFileName, Modifier.size(72.dp), placeholderSize = 28.sp)
                        Column {
                            Text(game.title, color = Ink, fontSize = 20.sp)
                            if (bits.isNotEmpty()) Text(bits.joinToString(" · "), color = Muted)
                        }
                    }
                    LabelChips(game.labels)
                    SectionTabBar(
                        lang = lang,
                        selected = selected,
                        customTabs = game.customTabs,
                        showAdd = false,
                        onSelect = { tabKey = it.storageKey() },
                        onAdd = {},
                    )
                }
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(heading.ifBlank { t.newTabTitle }, color = Ink, fontSize = 22.sp)
                if (hint.isNotBlank()) Text("($hint)", color = Muted)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Cream),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    body,
                    modifier = Modifier.padding(18.dp),
                    color = Ink,
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onEdit, colors = ButtonDefaults.buttonColors(containerColor = Burgundy)) {
                    Text(t.edit)
                }
                TextButton(onClick = onExport) { Text(t.exportFile, color = Burgundy) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onImport) { Text(t.importIntoGame, color = Burgundy) }
                TextButton(onClick = { confirmDelete = true }) { Text(t.delete, color = Burgundy) }
            }
            CreditLine(t.credit)
            Box(Modifier.padding(bottom = 12.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(t.delete) },
            text = { Text(t.confirmDelete) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text(t.delete, color = Burgundy) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(t.cancel) }
            },
        )
    }
}
