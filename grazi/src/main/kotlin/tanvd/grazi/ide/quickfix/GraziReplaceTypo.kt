package tanvd.grazi.ide.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.ide.DataManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icons.SpellcheckerIcons
import tanvd.grazi.grammar.Typo
import tanvd.grazi.utils.toAbsoluteSelectionRange
import javax.swing.Icon
import kotlin.math.min


class GraziReplaceTypo(private val typo: Typo) : LocalQuickFixAndIntentionActionOnPsiElement(typo.location.element,
        typo.location.element), PriorityAction, Iconable {
    override fun getIcon(flags: Int): Icon = SpellcheckerIcons.Spellcheck

    override fun getText() =  "Fix ${typo.info.match.shortMessage.toLowerCase()}"

    override fun getFamilyName() = "Fix mistake with replace"

    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (editor != null) {
            DataManager.getInstance().dataContextFromFocusAsync.onSuccess {
                val selectionRange = typo.toAbsoluteSelectionRange()
                editor.selectionModel.setSelection(selectionRange.startOffset, min(selectionRange.endOffset, editor.document.textLength))

                val items = typo.fixes.map { LookupElementBuilder.create(it) }
                LookupManager.getInstance(project).showLookup(editor, *items.toTypedArray())
            }
        }
    }
}
