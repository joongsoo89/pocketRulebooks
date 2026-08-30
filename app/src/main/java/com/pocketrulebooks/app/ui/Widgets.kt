package com.pocketrulebooks.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketrulebooks.app.data.CustomTab
import com.pocketrulebooks.app.data.RuleSection
import com.pocketrulebooks.app.data.SelectedTab
import com.pocketrulebooks.app.ui.theme.Burgundy
import com.pocketrulebooks.app.ui.theme.Cream
import com.pocketrulebooks.app.ui.theme.Muted

@Composable
fun LanguageBar(lang: Lang, onChange: (Lang) -> Unit) {
    LangSwitcher(lang, onChange)
}

@Composable
fun LangSwitcher(lang: Lang, onChange: (Lang) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(androidx.compose.ui.graphics.Color(0xFFEFE4D2))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf(Lang.Ko to "KO", Lang.Ja to "JP", Lang.En to "EN").forEach { (value, label) ->
            val selected = value == lang
            Button(
                onClick = { onChange(value) },
                modifier = Modifier.height(34.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) Cream else androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = if (selected) Burgundy else Muted,
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                Text(label, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SectionTabBar(
    lang: Lang,
    selected: SelectedTab,
    customTabs: List<CustomTab>,
    showAdd: Boolean,
    onSelect: (SelectedTab) -> Unit,
    onAdd: () -> Unit,
) {
    val t = stringsForLang(lang)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.ui.graphics.Color(0xFFEFE4D2))
            .horizontalScroll(rememberScrollState())
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RuleSection.entries.forEach { item ->
            val tab = SelectedTab.Builtin(item)
            TabChip(
                label = t.tabShort.getValue(item),
                selected = selected == tab,
                onClick = { onSelect(tab) },
            )
        }
        customTabs.forEach { item ->
            val tab = SelectedTab.Custom(item.id)
            TabChip(
                label = item.title.ifBlank { t.newTabTitle },
                selected = selected == tab,
                onClick = { onSelect(tab) },
            )
        }
        if (showAdd) {
            TabChip(
                label = t.addTab,
                selected = false,
                onClick = onAdd,
            )
        }
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(42.dp)
            .widthIn(min = 48.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Cream else androidx.compose.ui.graphics.Color.Transparent,
            contentColor = if (selected) Burgundy else Muted,
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp),
    ) {
        Text(label, fontSize = 13.sp, maxLines = 1)
    }
}

@Composable
fun AddTabDialog(
    lang: Lang,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val t = stringsForLang(lang)
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(t.newTabTitle) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(t.newTabHint) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val title = name.trim()
                    if (title.isNotEmpty()) onConfirm(title)
                },
            ) { Text(t.addAction) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(t.cancel) }
        },
    )
}
