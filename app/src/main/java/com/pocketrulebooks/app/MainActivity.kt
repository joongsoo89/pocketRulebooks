package com.pocketrulebooks.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pocketrulebooks.app.data.Game
import com.pocketrulebooks.app.data.fileName
import com.pocketrulebooks.app.ui.GameDetailScreen
import com.pocketrulebooks.app.ui.GameEditScreen
import com.pocketrulebooks.app.ui.GameListScreen
import com.pocketrulebooks.app.ui.stringsForLang
import com.pocketrulebooks.app.ui.theme.PocketTheme
import kotlinx.coroutines.launch
import java.nio.charset.Charset

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<AppViewModel>()
    private var pendingViewUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingViewUri = intent.data.takeIf { intent.action == Intent.ACTION_VIEW }
        setContent {
            PocketTheme {
                PocketApp(viewModel, pendingViewUri) { pendingViewUri = null }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_VIEW) {
            pendingViewUri = intent.data
        }
    }
}

@Composable
private fun PocketApp(
    viewModel: AppViewModel,
    incomingUri: Uri?,
    onIncomingConsumed: () -> Unit,
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    val lang by viewModel.lang.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val t = stringsForLang(lang)
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var exportPayload by remember { mutableStateOf<Pair<String, String>?>(null) }
    var importTargetId by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        val payload = exportPayload
        exportPayload = null
        if (uri == null || payload == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(payload.second.toByteArray(Charset.forName("UTF-8")))
            } ?: error("no stream")
            viewModel.notify("export-ok")
        }.onFailure { viewModel.notify("import-fail") }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = readUri(context, uri)
        if (text == null) {
            viewModel.notify("import-fail")
            return@rememberLauncherForActivityResult
        }
        val imported = viewModel.importText(text, importTargetId)
        importTargetId = null
        if (imported != null) {
            nav.navigate("detail/${imported.id}")
        }
    }

    fun startExport(game: Game) {
        exportPayload = game.fileName() to viewModel.exportText(game)
        exportLauncher.launch(game.fileName())
    }

    fun startImport(targetId: String?) {
        importTargetId = targetId
        importLauncher.launch(arrayOf("text/plain", "text/*"))
    }

    LaunchedEffect(incomingUri) {
        val uri = incomingUri ?: return@LaunchedEffect
        val text = readUri(context, uri)
        onIncomingConsumed()
        if (text == null) {
            viewModel.notify("import-fail")
        } else {
            viewModel.importText(text)?.let { nav.navigate("detail/${it.id}") }
        }
    }

    LaunchedEffect(message) {
        val code = message ?: return@LaunchedEffect
        val text = when (code) {
            "empty-title" -> t.needTitle
            "import-fail" -> t.importFail
            "import-ok" -> t.importOk
            "export-ok" -> t.exportOk
            else -> null
        }
        viewModel.consumeMessage()
        if (text != null) snackbar.showSnackbar(text)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "list",
            modifier = Modifier.padding(padding),
        ) {
            composable("list") {
                GameListScreen(
                    lang = lang,
                    games = games,
                    onLang = viewModel::setLang,
                    onOpen = { nav.navigate("detail/$it") },
                    onAdd = { nav.navigate("new") },
                    onImport = { startImport(null) },
                )
            }
            composable(
                "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id")
                val game = viewModel.game(id)
                if (game == null) {
                    LaunchedEffect(id) { nav.popBackStack() }
                } else {
                    GameDetailScreen(
                        lang = lang,
                        game = game,
                        onBack = { nav.popBackStack() },
                        onEdit = { nav.navigate("edit/${game.id}") },
                        onDelete = {
                            viewModel.delete(game.id)
                            nav.popBackStack()
                        },
                        onExport = { startExport(game) },
                        onImport = { startImport(game.id) },
                        onLang = viewModel::setLang,
                    )
                }
            }
            composable("new") {
                GameEditScreen(
                    lang = lang,
                    initial = remember { Game() },
                    onSave = { game ->
                        viewModel.upsert(game)
                        nav.navigate("detail/${game.id}") {
                            popUpTo("list")
                        }
                    },
                    onCancel = { nav.popBackStack() },
                    onLang = viewModel::setLang,
                    onNeedTitle = { viewModel.notify("empty-title") },
                )
            }
            composable(
                "edit/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id")
                val game = viewModel.game(id)
                if (game == null) {
                    LaunchedEffect(id) { nav.popBackStack() }
                } else {
                    GameEditScreen(
                        lang = lang,
                        initial = game,
                        onSave = { updated ->
                            viewModel.upsert(updated)
                            nav.popBackStack()
                        },
                        onCancel = { nav.popBackStack() },
                        onLang = viewModel::setLang,
                        onNeedTitle = { viewModel.notify("empty-title") },
                    )
                }
            }
        }
    }
}

private fun readUri(context: android.content.Context, uri: Uri): String? {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.reader(Charset.forName("UTF-8")).readText()
        }
    }.getOrNull()
}
