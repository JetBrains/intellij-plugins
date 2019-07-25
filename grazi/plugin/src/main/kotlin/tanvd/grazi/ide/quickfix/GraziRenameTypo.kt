package tanvd.grazi.ide.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.ide.DataManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.refactoring.RefactoringActionHandlerFactory
import com.intellij.refactoring.rename.NameSuggestionProvider
import com.intellij.refactoring.rename.RenameHandlerRegistry
import icons.SpellcheckerIcons
import tanvd.grazi.grammar.Typo
import tanvd.grazi.spellcheck.SpellCheckRenameSuggestions
import tanvd.kex.trimToNull
import tanvd.kex.untilNotNull
import javax.swing.Icon

open class GraziRenameTypo(private val typo: Typo) : LocalQuickFix, Iconable, PriorityAction {
    override fun getIcon(flags: Int): Icon = SpellcheckerIcons.Spellcheck

    override fun getName() = "Fix ${(typo.info.match.shortMessage.trimToNull() ?: typo.info.category.description).toLowerCase()}"

    override fun getFamilyName() = "Fix mistake with rename"

    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return

        val provider = NameSuggestionProvider.EP_NAME.extensionList.untilNotNull { it as? SpellCheckRenameSuggestions }
        try {
            provider?.active = true

            val runnable = Runnable {
                DataManager.getInstance().dataContextFromFocusAsync.onSuccess { dataContext ->
                    val renameHandler = RenameHandlerRegistry.getInstance().getRenameHandler(dataContext)
                    if (renameHandler != null) {
                        renameHandler.invoke(project, arrayOf(element), dataContext)
                    } else {
                        val renameRefactoringHandler = RefactoringActionHandlerFactory.getInstance().createRenameHandler()
                        renameRefactoringHandler.invoke(project, arrayOf(element), dataContext)
                    }
                }
            }

            if (ApplicationManager.getApplication().isUnitTestMode) {
                runnable.run()
            } else {
                TransactionGuard.submitTransaction(project, runnable)
            }
        } finally {
            provider?.active = false
        }
    }
}
