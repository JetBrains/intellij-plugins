package tanvd.grazi.ide.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInspection.*
import com.intellij.ide.DataManager
import com.intellij.injected.editor.EditorWindow
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil.getInjectedEditorForInjectedFile
import tanvd.grazi.grammar.Typo


class GraziReplaceTypo(private val typo: Typo) : LocalQuickFix, PriorityAction {

    override fun getName(): String {
        return "Fix ${typo.info.category.description}"
    }

    override fun getFamilyName(): String = "Fix mistake with replace"

    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        DataManager.getInstance()
                .dataContextFromFocusAsync
                .onSuccess { context ->
                    var editor = CommonDataKeys.EDITOR.getData(context)
                    if (editor != null) {
                        if (InjectedLanguageManager.getInstance(project).getInjectionHost(element) != null && editor !is EditorWindow) {
                            editor = getInjectedEditorForInjectedFile(editor, element.containingFile)
                        }

                        val textRange = (descriptor as ProblemDescriptorBase).textRange
                        val documentLength = editor.document.textLength
                        val endOffset = getDocumentOffset(textRange!!.endOffset, documentLength)
                        val startOffset = getDocumentOffset(textRange.startOffset, documentLength)
                        editor.selectionModel.setSelection(startOffset, endOffset)

                        val items = typo.fixes.map { LookupElementBuilder.create(it) }
                        LookupManager.getInstance(project).showLookup(editor, *items.toTypedArray())
                    }
                }
    }

    private fun getDocumentOffset(offset: Int, documentLength: Int): Int {
        return if (offset in 0..documentLength) offset else documentLength
    }
}
