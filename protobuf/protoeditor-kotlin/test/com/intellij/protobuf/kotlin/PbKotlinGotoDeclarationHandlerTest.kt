package com.intellij.protobuf.kotlin

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.protobuf.lang.psi.PbMessageType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

class PbKotlinGotoDeclarationHandlerTest : PbKotlinTestBase() {

    fun testKotlinGotoDeclarationHandlerIsRegistered() {
        val handlers = GotoDeclarationHandler.EP_NAME.extensionList

        val handlerNames = handlers.joinToString(separator = "\n") {
            it.javaClass.name
        }

        assertTrue(
            """
        PbKotlinGotoDeclarationHandler is not registered.

        Registered handlers:
        $handlerNames
      """.trimIndent(),
            handlers.any { it.javaClass.name == KOTLIN_GOTO_HANDLER_CLASS_NAME }
        )
    }

    fun testRegisteredHandlerDoesNotCrashOnKotlinFile() {
        addProjectFiles()

        myFixture.configureByText(
            "Main.kt",
            """
        package demo

        import demo.proto.User

        fun main() {
          val user = Use<caret>r.newBuilder().build()
        }
      """.trimIndent()
        )

        registeredKotlinGotoTargets(elementAtCaret())
    }

    fun testIdeNavigationPipelineIncludesProtoTargetForJavaApiMessageClassFromKotlin() {
        addProjectFiles()

        myFixture.configureByText(
            "Main.kt",
            """
        package demo

        import demo.proto.User

        fun main() {
          val user = Use<caret>r.newBuilder().build()
        }
      """.trimIndent()
        )

        val targets = GotoDeclarationAction.findAllTargetElements(
            project,
            myFixture.editor,
            myFixture.caretOffset
        )

        assertNotNull(
            "Expected IDEA navigation pipeline to return targets",
            targets
        )

        assertTrue(
            "Expected IDEA navigation pipeline to include proto message User, but got:\n${describeTargets(targets)}",
            targets.any { it is PbMessageType && it.name == "User" }
        )
    }

    private fun registeredKotlinGotoTargets(sourceElement: PsiElement): Array<PsiElement>? {
        val handler = registeredKotlinGotoHandler()

        return handler.getGotoDeclarationTargets(
            sourceElement,
            myFixture.caretOffset,
            myFixture.editor
        )
    }

    private fun registeredKotlinGotoHandler(): GotoDeclarationHandler {
        return GotoDeclarationHandler.EP_NAME.extensionList
            .single { it.javaClass.name == KOTLIN_GOTO_HANDLER_CLASS_NAME }
    }

    private fun elementAtCaret(): PsiElement {
        return myFixture.file.findElementAt(myFixture.caretOffset)
            ?: error("No source element at caret")
    }

    private fun targetName(target: PsiElement): String? {
        return when (target) {
            is PsiNamedElement -> target.name
            else -> null
        }
    }

    private fun describeTargets(targets: Array<PsiElement>?): String {
        if (targets == null) {
            return "<null>"
        }

        if (targets.isEmpty()) {
            return "<empty>"
        }

        return targets.joinToString(separator = "\n") { target ->
            val name = targetName(target)
            val file = target.containingFile?.virtualFile?.path
                ?: target.containingFile?.name
                ?: "<no file>"

            "${target.javaClass.name}, name=$name, text='${target.text.take(80)}', file=$file"
        }
    }

    companion object {
        private const val KOTLIN_GOTO_HANDLER_CLASS_NAME =
            "com.intellij.protobuf.kotlin.PbKotlinGotoDeclarationHandler"
    }
}
