package com.pocketrulebooks.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketrulebooks.app.data.Game
import com.pocketrulebooks.app.data.matches
import com.pocketrulebooks.app.ui.theme.Burgundy
import com.pocketrulebooks.app.ui.theme.Cream
import com.pocketrulebooks.app.ui.theme.Ink
import com.pocketrulebooks.app.ui.theme.Line
import com.pocketrulebooks.app.ui.theme.Muted
import com.pocketrulebooks.app.ui.theme.Paper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListScreen(
    lang: Lang,
    games: List<Game>,
    onLang: (Lang) -> Unit,
    onOpen: (String) -> Unit,
    onAdd: () -> Unit,
    onImport: () -> Unit,
) {
    val t = stringsForLang(lang)
    var query by rememberSaveable { mutableStateOf("") }
    var selectedLabel by rememberSaveable { mutableStateOf("") }
    val allLabels = games.flatMap { it.labels }.distinctBy { it.lowercase() }.sortedBy { it.lowercase() }
    val filtered = games.filter { it.matches(query, selectedLabel) }

    Scaffold(
        containerColor = Paper,
        topBar = {
            TopAppBar(
                title = { Text(t.appName) },
                actions = { LanguageBar(lang, onLang) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper, titleContentColor = Ink),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = Burgundy, contentColor = Color.White) {
                Text("+ ${t.add}", modifier = Modifier.padding(horizontal = 8.dp), color = Color.White)
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(t.search) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Cream,
                    unfocusedContainerColor = Cream,
                    focusedBorderColor = Line,
                    unfocusedBorderColor = Line,
                ),
            )
            if (allLabels.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FilterChip(
                        selected = selectedLabel.isBlank(),
                        onClick = { selectedLabel = "" },
                        label = { Text(t.allLabels) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Burgundy,
                            selectedLabelColor = Color.White,
                        ),
                    )
                    allLabels.forEach { label ->
                        FilterChip(
                            selected = selectedLabel.equals(label, ignoreCase = true),
                            onClick = {
                                selectedLabel = if (selectedLabel.equals(label, ignoreCase = true)) "" else label
                            },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Burgundy,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
            }
            TextButton(onClick = onImport) { Text(t.importFile, color = Burgundy) }
            if (games.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CoverThumb("empty", "", Modifier.size(88.dp), placeholderSize = 36.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(t.emptyTitle, color = Ink, fontSize = 20.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(t.emptyHint, color = Muted)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filtered, key = { it.id }) { game ->
                        GameCard(game, onClick = { onOpen(game.id) })
                    }
                }
            }
            CreditLine(t.credit)
        }
    }
}

@Composable
private fun GameCard(game: Game, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CoverThumb(game.id, game.photoFileName, Modifier.size(56.dp), placeholderSize = 26.sp)
            Column(Modifier.weight(1f)) {
                Text(game.title.ifBlank { "—" }, color = Ink, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val bits = listOf(game.players, game.playTime).filter { it.isNotBlank() }
                if (bits.isNotEmpty()) {
                    Text(bits.joinToString(" · "), color = Muted, fontSize = 13.sp)
                }
                if (game.labels.isNotEmpty()) {
                    Text(game.labels.joinToString(" · "), color = Burgundy, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (game.overview.isNotBlank()) {
                    Text(game.overview, color = Muted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
