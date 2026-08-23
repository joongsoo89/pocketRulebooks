package com.pocketrulebooks.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import com.pocketrulebooks.app.data.Game
import com.pocketrulebooks.app.data.RuleSection
import com.pocketrulebooks.app.data.randomEmoji
import com.pocketrulebooks.app.data.sectionText
import com.pocketrulebooks.app.data.withSection
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
    var game by remember(initial.id) { mutableStateOf(initial) }
    var section by rememberSaveable { mutableStateOf(RuleSection.Overview) }
    val hint = t.tabHint.getValue(section)

    Scaffold(
        containerColor = Paper,
        topBar = {
            TopAppBar(
                title = { Text(if (initial.title.isBlank()) t.add else t.edit) },
                navigationIcon = { TextButton(onClick = onCancel) { Text("←") } },
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { game = game.copy(emoji = randomEmoji()) },
                    colors = ButtonDefaults.buttonColors(containerColor = Cream, contentColor = Ink),
                    shape = RoundedCornerShape(16.dp),
                ) { Text(game.emoji) }
                Field(
                    label = t.title,
                    value = game.title,
                    hint = t.titleHint,
                    modifier = Modifier.weight(1f),
                    onChange = { game = game.copy(title = it) },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Field(t.players, game.players, t.playersHint, Modifier.weight(1f)) {
                    game = game.copy(players = it)
                }
                Field(t.playTime, game.playTime, t.playTimeHint, Modifier.weight(1f)) {
                    game = game.copy(playTime = it)
                }
            }
            SectionTabBar(lang, section, onChange = { section = it })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                Text(t.tabFull.getValue(section), color = Ink)
                if (hint.isNotBlank()) Text("($hint)", color = Muted)
            }
            OutlinedTextField(
                value = game.sectionText(section),
                onValueChange = { game = game.withSection(section, it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                placeholder = { Text(t.noContent) },
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 28.dp)) {
                Button(
                    onClick = {
                        if (game.title.isBlank()) onNeedTitle()
                        else onSave(game.copy(updatedAt = System.currentTimeMillis()))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                ) { Text(t.save) }
                TextButton(onClick = onCancel) { Text(t.cancel, color = Burgundy) }
            }
        }
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
