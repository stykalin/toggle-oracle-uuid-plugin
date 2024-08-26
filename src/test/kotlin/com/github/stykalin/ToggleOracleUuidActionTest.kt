package com.github.stykalin


import com.intellij.testFramework.EditorTestUtil.CARET_TAG
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat


class ToggleOracleUuidActionTest : BasePlatformTestCase() {

    companion object {
        private const val ACTION_ID = "com.github.stykalin.ToggleOracleUuidAction"
    }

    fun testRawToUUID() {
        myFixture.configureByText("Example.txt", "\"AF7565DDB6C649A1840E6008432D7D92${CARET_TAG}\"")
        myFixture.performEditorAction(ACTION_ID)

        val textAfterAction = myFixture.editor.document.text
        assertThat(textAfterAction, equalTo("\"af7565dd-b6c6-49a1-840e-6008432d7d92\""))
    }

    fun testUUIDToRaw() {
        myFixture.configureByText("Example.kt", "val str = \"${CARET_TAG}66dbcc0a-09a3-44d5-9f46-900eda5cf632\"")

        myFixture.performEditorAction(ACTION_ID)

        val textAfterAction = myFixture.editor.document.text
        assertThat(textAfterAction, equalTo("val str = \"66DBCC0A09A344D59F46900EDA5CF632\""))
    }

}