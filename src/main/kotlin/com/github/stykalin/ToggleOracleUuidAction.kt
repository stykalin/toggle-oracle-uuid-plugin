package com.github.stykalin

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

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val project: Project = e.getRequiredData(CommonDataKeys.PROJECT)
        val document: Document = editor.document

        var selectedText = editor.selectionModel.selectedText
        if (selectedText == null || selectedText.isEmpty()) {
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
            uuidText.matches(UUID_PATTERN) -> convertToDb(uuidText)
            uuidText.matches(ORACLE_UUID_PATTERN) -> convertToUuid(uuidText)
            else -> uuidText
        }
    }


    private fun convertToDb(uuidText: String): String {
        return uuidText.replace("-", "").replace("\"", "").uppercase()
    }

    private fun convertToUuid(uuidText: String): String {
        val charList = uuidText.replace("-", "").replace("\"", "").lowercase().split("").toMutableList()
        charList.add(9, "-")
        charList.add(14, "-")
        charList.add(19, "-")
        charList.add(24, "-")
        return charList.joinToString("")
    }

    companion object {
        private val UUID_PATTERN = "[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}".toRegex()
        private val ORACLE_UUID_PATTERN = "[A-Z0-9]{32}".toRegex()
    }

}