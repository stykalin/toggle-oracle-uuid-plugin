package com.github.stykalin.tou.action

import com.github.stykalin.tou.settings.ToggleOracleUuidAppSettingsState
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actions.EditorActionUtil
import com.intellij.openapi.project.Project
import java.util.regex.Matcher
import java.util.regex.Pattern


class ToggleOracleUuidAction : AnAction() {

    private val settings: ToggleOracleUuidAppSettingsState = ToggleOracleUuidAppSettingsState.getInstance()

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project: Project = e.getData(CommonDataKeys.PROJECT) ?: return
        val document: Document = editor.document

        var selectedText = editor.selectionModel.selectedText
        if (selectedText.isNullOrEmpty()) {
            editor.selectionModel.selectWordAtCaret(true)
            var moveLeft = true
            var moveRight = true
            var start = editor.selectionModel.selectionStart
            var end = editor.selectionModel.selectionEnd
            val p: Pattern = Pattern.compile("[^A-z0-9\\-]")

            // move caret left
            while (moveLeft && start != 0) {
                start--
                EditorActionUtil.moveCaret(editor.caretModel.currentCaret, start, true)
                val m: Matcher = p.matcher(editor.selectionModel.selectedText!!)
                if (m.find()) {
                    start++
                    moveLeft = false
                }
            }
            editor.selectionModel.setSelection(end - 1, end)

            // move caret right
            while (moveRight && end != editor.document.textLength) {
                end++
                EditorActionUtil.moveCaret(editor.caretModel.currentCaret, end, true)
                val m: Matcher = p.matcher(editor.selectionModel.selectedText!!)
                if (m.find()) {
                    end--
                    moveRight = false
                }
            }
            editor.selectionModel.setSelection(start, end)
            selectedText = editor.selectionModel.selectedText
        }

        if (selectedText == null) return

        val replacementText = convertUuid(selectedText)

        if (selectedText == replacementText) {
            editor.selectionModel.removeSelection()
            return
        }

        val start = editor.selectionModel.selectionStart
        val end = editor.selectionModel.selectionEnd

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(start, end, replacementText)
        }
        editor.selectionModel.removeSelection()
    }

    override fun update(e: AnActionEvent) {
        val project: Project? = e.project
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = project != null && editor != null
    }

    private fun convertUuid(uuidText: String): String {
        return when {
            uuidText.matches(UUID_PATTERN) && settings.useRaw16Mode -> convertUuidToRaw16(uuidText)
            uuidText.matches(UUID_PATTERN) -> convertToDb(uuidText)
            uuidText.matches(ORACLE_UUID_PATTERN) && settings.useRaw16Mode -> convertRaw16ToUuid(uuidText)
            uuidText.matches(ORACLE_UUID_PATTERN) -> convertToUuid(uuidText)
            else -> uuidText
        }
    }

    /**
     * Covert from "DD6575AFC6B6A149840E6008432D7D92" to "dd6575af-c6b6-a149-840e-6008432d7d92"
     */
    private fun convertToUuid(uuidText: String): String {
        val charList = uuidText.replace("-", "").replace("\"", "").lowercase().split("").toMutableList()
        charList.add(9, "-")
        charList.add(14, "-")
        charList.add(19, "-")
        charList.add(24, "-")
        return charList.joinToString("").let { if (settings.useUpperCase) it.uppercase() else it }
    }

    /**
     * Covert from "dd6575af-c6b6-a149-840e-6008432d7d92" to "DD6575AFC6B6A149840E6008432D7D92"
     */
    private fun convertToDb(uuidText: String): String {
        return uuidText.replace("-", "").replace("\"", "").uppercase()
    }

    /**
     * Covert from "AF7565DDB6C649A1840E6008432D7D92" to "DD6575AF-C6B6-A149-840E-6008432D7D92"
     */
    private fun convertRaw16ToUuid(raw16Text: String): String {
        val result = raw16Text.split("").let {
            it[7] + it[8] + it[5] + it[6] + it[3] + it[4] + it[1] + it[2] + "-" +            //DD6575AF-
                    it[11] + it[12] + it[9] + it[10] + "-" +                                 //C6B6-
                    it[15] + it[16] + it[13] + it[14] + "-" +                                //A149-
                    it.subList(17, 21).joinToString("") + "-" + //840E-
                    it.subList(21, 33).joinToString("")         //6008432D7D92
        }
        return result.let { if (settings.useUpperCase) it.uppercase() else it.lowercase() }
    }

    /**
     * Covert from "DD6575AF-C6B6-A149-840E-6008432D7D92" to "AF7565DDB6C649A1840E6008432D7D92"
     */
    fun convertUuidToRaw16(uuidText: String): String {
        val result = uuidText.replace("-", "").split("").let {
            it[7] + it[8] + it[5] + it[6] + it[3] + it[4] + it[1] + it[2] +
                    it[11] + it[12] + it[9] + it[10] +
                    it[15] + it[16] + it[13] + it[14] +
                    it.subList(17, 21).joinToString("") +
                    it.subList(21, 33).joinToString("")
        }
        return result.let { if (settings.useUpperCase) it.uppercase() else it.lowercase() }
    }


    companion object {
        private val UUID_PATTERN = "[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12}".toRegex()
        private val ORACLE_UUID_PATTERN = "[A-Z0-9]{32}".toRegex()
    }

}