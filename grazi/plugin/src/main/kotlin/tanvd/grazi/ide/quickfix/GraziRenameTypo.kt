package tanvd.grazi.ide.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.RefactoringQuickFix
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.RefactoringActionHandlerFactory
import com.intellij.refactoring.rename.NameSuggestionProvider
import com.intellij.refactoring.rename.RenameHandlerRegistry
import icons.SpellcheckerIcons
import org.slf4j.LoggerFactory
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.spellcheck.SpellCheckRenameSuggestions
import tanvd.kex.trimToNull
import tanvd.kex.untilNotNull
import javax.swing.Icon

open class GraziRenameTypo(private val typo: Typo) : RefactoringQuickFix, Iconable, PriorityAction {
    companion object {
        private val logger = LoggerFactory.getLogger(GraziRenameTypo::class.java)
    }

    override fun getFamilyName() = msg("grazi.quickfix.renametypo.family")

    override fun getName() = msg("grazi.quickfix.renametypo.text", (typo.info.match.shortMessage.trimToNull() ?: typo.info.category.description).toLowerCase())

    override fun getIcon(flags: Int): Icon = SpellcheckerIcons.Spellcheck

    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun getHandler(): RefactoringActionHandler = RefactoringActionHandlerFactory.getInstance().createRenameHandler()
    override fun getHandler(context: DataContext): RefactoringActionHandler = RenameHandlerRegistry.getInstance().getRenameHandler(context) ?: handler

    override fun getElementToRefactor(element: PsiElement): PsiElement = if (element is PsiNamedElement) element else super.getElementToRefactor(element)

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return

        with(NameSuggestionProvider.EP_NAME.extensionList.untilNotNull { it as? SpellCheckRenameSuggestions }) {
            try {
                this?.active = true
                TransactionGuard.submitTransaction(project, Runnable { doFix(element) })
            } catch (t: Throwable) {
                logger.warn("Got exception during rename refactoring", t)
            } finally {
                this?.active = false
            }
        }
    }
}
