package tanvd.grazi.ide.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.ide.DataManager
import com.intellij.injected.editor.EditorWindow
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil.getInjectedEditorForInjectedFile
import icons.SpellcheckerIcons
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.utils.toAbsoluteSelectionRange
import tanvd.kex.trimToNull
import javax.swing.Icon
import kotlin.math.min

class GraziReplaceTypo(private val typo: Typo) : LocalQuickFix, Iconable, PriorityAction {
    override fun getFamilyName() = msg("grazi.quickfix.replacetypo.family")

    override fun getName() = msg("grazi.quickfix.replacetypo.text", (typo.info.match.shortMessage.trimToNull() ?: typo.info.category.description).toLowerCase())

    override fun getIcon(flags: Int): Icon = SpellcheckerIcons.Spellcheck

    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        DataManager.getInstance().dataContextFromFocusAsync.onSuccess { context ->
            var editor: Editor = CommonDataKeys.EDITOR.getData(context) ?: return@onSuccess
            val element = typo.location.element!!
            if (InjectedLanguageManager.getInstance(project).getInjectionHost(element) != null && editor !is EditorWindow) {
                editor = getInjectedEditorForInjectedFile(editor, element.containingFile)
            }

            val selectionRange = typo.toAbsoluteSelectionRange()
            editor.selectionModel.setSelection(selectionRange.startOffset, min(selectionRange.endOffset, editor.document.textLength))

            val items = typo.fixes.map { LookupElementBuilder.create(it) }
            LookupManager.getInstance(project).showLookup(editor, *items.toTypedArray())
        }
    }
}
