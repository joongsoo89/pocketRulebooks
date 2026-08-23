package com.pocketrulebooks.app.ui

import com.pocketrulebooks.app.data.RuleSection
import java.util.Locale

enum class Lang { Ko, Ja, En;

    companion object {
        fun fromSystem(): Lang {
            val tag = Locale.getDefault().language.lowercase(Locale.ROOT)
            return when {
                tag.startsWith("ja") -> Ja
                tag.startsWith("en") -> En
                else -> Ko
            }
        }
    }
}

data class Copy(
    val appName: String,
    val search: String,
    val emptyTitle: String,
    val emptyHint: String,
    val add: String,
    val edit: String,
    val save: String,
    val cancel: String,
    val delete: String,
    val confirmDelete: String,
    val title: String,
    val titleHint: String,
    val players: String,
    val playersHint: String,
    val playTime: String,
    val playTimeHint: String,
    val noContent: String,
    val needTitle: String,
    val importFile: String,
    val exportFile: String,
    val importIntoGame: String,
    val importOk: String,
    val importFail: String,
    val exportOk: String,
    val tabShort: Map<RuleSection, String>,
    val tabFull: Map<RuleSection, String>,
    val tabHint: Map<RuleSection, String>,
)

fun stringsForLang(lang: Lang): Copy = when (lang) {
    Lang.Ko -> Copy(
        appName = "Pocket Rulebooks",
        search = "게임 검색",
        emptyTitle = "아직 게임이 없습니다",
        emptyHint = "보드게임 룰을 적어 두고, 필요할 때 꺼내 보세요.",
        add = "게임 추가",
        edit = "편집",
        save = "저장",
        cancel = "취소",
        delete = "삭제",
        confirmDelete = "이 게임을 삭제할까요?",
        title = "게임 이름",
        titleHint = "예: 아줄",
        players = "인원",
        playersHint = "예: 2–4명",
        playTime = "플레이 시간",
        playTimeHint = "예: 30–45분",
        noContent = "아직 작성된 내용이 없습니다.",
        needTitle = "게임 이름을 입력해 주세요.",
        importFile = "텍스트 가져오기",
        exportFile = "텍스트로 내보내기",
        importIntoGame = "이 게임에 가져오기",
        importOk = "룰북을 가져왔습니다.",
        importFail = "텍스트 파일을 읽지 못했습니다.",
        exportOk = "텍스트 파일로 저장했습니다.",
        tabShort = mapOf(
            RuleSection.Overview to "개요",
            RuleSection.Setup to "준비",
            RuleSection.Play to "진행",
            RuleSection.End to "종료",
            RuleSection.Extra to "기타",
        ),
        tabFull = mapOf(
            RuleSection.Overview to "게임개요",
            RuleSection.Setup to "게임준비",
            RuleSection.Play to "게임진행",
            RuleSection.End to "게임종료",
            RuleSection.Extra to "기타",
        ),
        tabHint = mapOf(
            RuleSection.Overview to "목표",
            RuleSection.Setup to "",
            RuleSection.Play to "",
            RuleSection.End to "점수계산",
            RuleSection.Extra to "",
        ),
    )
    Lang.Ja -> Copy(
        appName = "Pocket Rulebooks",
        search = "ゲームを検索",
        emptyTitle = "まだゲームがありません",
        emptyHint = "ルールを書いておき、必要なときにすぐ開けます。",
        add = "ゲームを追加",
        edit = "編集",
        save = "保存",
        cancel = "キャンセル",
        delete = "削除",
        confirmDelete = "このゲームを削除しますか？",
        title = "ゲーム名",
        titleHint = "例: アズール",
        players = "人数",
        playersHint = "例: 2–4人",
        playTime = "プレイ時間",
        playTimeHint = "例: 30–45分",
        noContent = "まだ内容がありません。",
        needTitle = "ゲーム名を入力してください。",
        importFile = "テキストを読み込む",
        exportFile = "テキストで書き出す",
        importIntoGame = "このゲームに読み込む",
        importOk = "ルールブックを読み込みました。",
        importFail = "テキストファイルを読めませんでした。",
        exportOk = "テキストファイルに保存しました。",
        tabShort = mapOf(
            RuleSection.Overview to "概要",
            RuleSection.Setup to "準備",
            RuleSection.Play to "進行",
            RuleSection.End to "終了",
            RuleSection.Extra to "その他",
        ),
        tabFull = mapOf(
            RuleSection.Overview to "ゲーム概要",
            RuleSection.Setup to "ゲーム準備",
            RuleSection.Play to "ゲーム進行",
            RuleSection.End to "ゲーム終了",
            RuleSection.Extra to "その他",
        ),
        tabHint = mapOf(
            RuleSection.Overview to "目標",
            RuleSection.Setup to "",
            RuleSection.Play to "",
            RuleSection.End to "得点計算",
            RuleSection.Extra to "",
        ),
    )
    Lang.En -> Copy(
        appName = "Pocket Rulebooks",
        search = "Search games",
        emptyTitle = "No games yet",
        emptyHint = "Write the rules down, then open them whenever you need.",
        add = "Add game",
        edit = "Edit",
        save = "Save",
        cancel = "Cancel",
        delete = "Delete",
        confirmDelete = "Delete this game?",
        title = "Game title",
        titleHint = "e.g. Azul",
        players = "Players",
        playersHint = "e.g. 2–4",
        playTime = "Play time",
        playTimeHint = "e.g. 30–45 min",
        noContent = "Nothing written here yet.",
        needTitle = "Please enter a game title.",
        importFile = "Import text",
        exportFile = "Export text",
        importIntoGame = "Import into this game",
        importOk = "Rulebook imported.",
        importFail = "Could not read the text file.",
        exportOk = "Saved as a text file.",
        tabShort = mapOf(
            RuleSection.Overview to "Goal",
            RuleSection.Setup to "Setup",
            RuleSection.Play to "Play",
            RuleSection.End to "End",
            RuleSection.Extra to "More",
        ),
        tabFull = mapOf(
            RuleSection.Overview to "Overview",
            RuleSection.Setup to "Setup",
            RuleSection.Play to "Play",
            RuleSection.End to "Game end",
            RuleSection.Extra to "Other",
        ),
        tabHint = mapOf(
            RuleSection.Overview to "Goal",
            RuleSection.Setup to "",
            RuleSection.Play to "",
            RuleSection.End to "Scoring",
            RuleSection.Extra to "",
        ),
    )
}
