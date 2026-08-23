package com.pocketrulebooks.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketrulebooks.app.data.RuleSection
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
    section: RuleSection,
    onChange: (RuleSection) -> Unit,
) {
    val t = stringsForLang(lang)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.ui.graphics.Color(0xFFEFE4D2))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RuleSection.entries.forEach { item ->
            val selected = item == section
            Button(
                onClick = { onChange(item) },
                modifier = Modifier
                    .height(42.dp)
                    .weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) Cream else androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = if (selected) Burgundy else Muted,
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                Text(t.tabShort.getValue(item), fontSize = 13.sp, maxLines = 1)
            }
        }
    }
}
