package com.pocketrulebooks.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketrulebooks.app.data.CoverStore
import com.pocketrulebooks.app.data.Game
import com.pocketrulebooks.app.data.RuleSection
import com.pocketrulebooks.app.data.SelectedTab
import com.pocketrulebooks.app.data.addCustomTab
import com.pocketrulebooks.app.data.hasTab
import com.pocketrulebooks.app.data.parseLabels
import com.pocketrulebooks.app.data.removeCustomTab
import com.pocketrulebooks.app.data.storageKey
import com.pocketrulebooks.app.data.tabFromKey
import com.pocketrulebooks.app.data.tabText
import com.pocketrulebooks.app.data.withCustomTitle
import com.pocketrulebooks.app.data.withLabels
import com.pocketrulebooks.app.data.withTabText
import com.pocketrulebooks.app.ui.theme.Burgundy
import com.pocketrulebooks.app.ui.theme.Cream
import com.pocketrulebooks.app.ui.theme.Ink
import com.pocketrulebooks.app.ui.theme.Line
import com.pocketrulebooks.app.ui.theme.Muted
import com.pocketrulebooks.app.ui.theme.Paper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameEditScreen(
    lang: Lang,
    initial: Game,
    onSave: (Game) -> Unit,
    onCancel: () -> Unit,
    onLang: (Lang) -> Unit,
    onNeedTitle: () -> Unit,
) {
    val t = stringsForLang(lang)
    val context = LocalContext.current
    val covers = remember { CoverStore(context) }
    var game by remember(initial.id) { mutableStateOf(initial) }
    var tabKey by rememberSaveable { mutableStateOf(SelectedTab.Builtin(RuleSection.Overview).storageKey()) }
    var bodyFocused by remember { mutableStateOf(false) }
    var showAddTab by remember { mutableStateOf(false) }
    var confirmDeleteTab by remember { mutableStateOf(false) }
    var labelDraft by remember { mutableStateOf("") }
    var photoTick by remember { mutableStateOf(0) }
    val selected = run {
        val current = tabFromKey(tabKey)
        if (game.hasTab(current)) current else SelectedTab.Builtin(RuleSection.Overview)
    }
    val customSelected = selected as? SelectedTab.Custom
    val hint = (selected as? SelectedTab.Builtin)?.let { t.tabHint[it.section].orEmpty() }.orEmpty()
    val heading = when (selected) {
        is SelectedTab.Builtin -> t.tabFull.getValue(selected.section)
        is SelectedTab.Custom -> game.customTabs.find { it.id == selected.id }?.title.orEmpty()
    }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        if (covers.saveFromUri(game.id, uri)) {
            game = game.copy(photoFileName = "${game.id}.jpg", emoji = "")
            photoTick += 1
        }
    }

    fun addDraftLabels() {
        val extra = parseLabels(labelDraft)
        if (extra.isEmpty()) return
        game = game.withLabels(game.labels + extra)
        labelDraft = ""
    }

    Scaffold(
        containerColor = Paper,
        topBar = {
            if (!bodyFocused) {
                TopAppBar(
                    title = { Text(if (initial.title.isBlank()) t.add else t.edit) },
                    navigationIcon = { TextButton(onClick = onCancel) { Text("←") } },
                    actions = { LanguageBar(lang, onLang) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Paper,
                        titleContentColor = Ink,
                    ),
                )
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp)
                .padding(top = if (bodyFocused) 8.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!bodyFocused) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CoverThumb(
                        gameId = game.id,
                        photoFileName = "${game.photoFileName}-$photoTick",
                        modifier = Modifier.size(72.dp),
                        placeholderSize = 28.sp,
                    )
                    Column(Modifier.weight(1f)) {
                        Field(
                            label = t.title,
                            value = game.title,
                            hint = t.titleHint,
                            onChange = { game = game.copy(title = it) },
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { pickPhoto.launch("image/*") }) {
                                Text(if (game.photoFileName.isBlank()) t.addPhoto else t.changePhoto, color = Burgundy)
                            }
                            if (game.photoFileName.isNotBlank() || covers.hasCover(game.id)) {
                                TextButton(onClick = {
                                    covers.delete(game.id)
                                    game = game.copy(photoFileName = "")
                                    photoTick += 1
                                }) { Text(t.removePhoto, color = Burgundy) }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Field(t.players, game.players, t.playersHint, Modifier.weight(1f)) {
                        game = game.copy(players = it)
                    }
                    Field(t.playTime, game.playTime, t.playTimeHint, Modifier.weight(1f)) {
                        game = game.copy(playTime = it)
                    }
                }
                Text(t.labels, color = Muted)
                LabelChips(game.labels, onRemove = { name ->
                    game = game.copy(labels = game.labels.filterNot { it.equals(name, ignoreCase = true) })
                })
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = labelDraft,
                        onValueChange = { labelDraft = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(t.labelsHint) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors(),
                    )
                    TextButton(onClick = { addDraftLabels() }) { Text(t.addLabel, color = Burgundy) }
                }
            }

            SectionTabBar(
                lang = lang,
                selected = selected,
                customTabs = game.customTabs,
                showAdd = true,
                onSelect = { tabKey = it.storageKey() },
                onAdd = { showAddTab = true },
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (customSelected != null) {
                    OutlinedTextField(
                        value = heading,
                        onValueChange = { game = game.withCustomTitle(customSelected.id, it) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text(t.newTabTitle) },
                        colors = fieldColors(),
                    )
                    TextButton(onClick = { confirmDeleteTab = true }) {
                        Text(t.deleteTab, color = Burgundy)
                    }
                } else {
                    Text(heading, color = Ink)
                    if (hint.isNotBlank()) Text("($hint)", color = Muted)
                }
            }
            OutlinedTextField(
                value = game.tabText(selected),
                onValueChange = { game = game.withTabText(selected, it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onFocusChanged { bodyFocused = it.isFocused },
                placeholder = { Text(t.noContent) },
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                Button(
                    onClick = {
                        addDraftLabels()
                        if (game.title.isBlank()) onNeedTitle()
                        else onSave(game.copy(updatedAt = System.currentTimeMillis()))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Burgundy, contentColor = Color.White),
                ) { Text(t.save, color = Color.White) }
                TextButton(onClick = onCancel) { Text(t.cancel, color = Burgundy) }
            }
        }
    }

    if (showAddTab) {
        AddTabDialog(
            lang = lang,
            onConfirm = { title ->
                val (next, tab) = game.addCustomTab(title)
                game = next
                tabKey = SelectedTab.Custom(tab.id).storageKey()
                showAddTab = false
            },
            onDismiss = { showAddTab = false },
        )
    }
    if (confirmDeleteTab && customSelected != null) {
        AlertDialog(
            onDismissRequest = { confirmDeleteTab = false },
            title = { Text(t.deleteTab) },
            text = { Text(t.confirmDeleteTab) },
            confirmButton = {
                TextButton(onClick = {
                    game = game.removeCustomTab(customSelected.id)
                    tabKey = SelectedTab.Builtin(RuleSection.Overview).storageKey()
                    confirmDeleteTab = false
                }) { Text(t.delete, color = Burgundy) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteTab = false }) { Text(t.cancel) }
            },
        )
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    hint: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Muted)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(hint) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Cream,
    unfocusedContainerColor = Cream,
    focusedBorderColor = Line,
    unfocusedBorderColor = Line,
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
)
