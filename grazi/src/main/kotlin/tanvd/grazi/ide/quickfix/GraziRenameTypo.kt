package tanvd.grazi.ide.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.ide.DataManager
import com.intellij.injected.editor.EditorWindow
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.impl.text.TextEditorPsiDataProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil.findInjectionHost
import com.intellij.refactoring.actions.RenameElementAction
import com.intellij.refactoring.rename.NameSuggestionProvider
import com.intellij.refactoring.rename.RenameHandlerRegistry
import tanvd.grazi.grammar.Typo
import tanvd.grazi.spellcheck.SpellCheckSuggestions
import java.util.*


open class GraziRenameTypo(private val typo: Typo) : LocalQuickFix, PriorityAction {

    override fun getName(): String {
        return "Fix ${typo.info.category.description} mistake"
    }

    override fun getFamilyName(): String = "Fix mistake"

    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val provider = findProvider()
        provider?.active = true

        val map = HashMap<String, Any>()
        val psiElement = descriptor.psiElement ?: return
        val editor = getEditor(psiElement, project) ?: return

        if (editor is EditorWindow) {
            map[CommonDataKeys.EDITOR.name] = editor
            map[CommonDataKeys.PSI_ELEMENT.name] = psiElement
        } else if (ApplicationManager.getApplication().isUnitTestMode) {
            map[CommonDataKeys.PSI_ELEMENT.name] = TextEditorPsiDataProvider().getData(CommonDataKeys.PSI_ELEMENT.name, editor, editor.caretModel.currentCaret)!!
        }

        val selectAll = editor.getUserData(RenameHandlerRegistry.SELECT_ALL)
        try {
            editor.putUserData(RenameHandlerRegistry.SELECT_ALL, true)
            val dataContext = SimpleDataContext.getSimpleContext(map, DataManager.getInstance().getDataContext(editor.component))
            val action = RenameElementAction()
            val event = AnActionEvent.createFromAnAction(action, null, "", dataContext)
            action.actionPerformed(event)
            provider?.active = false
        } finally {
            editor.putUserData(RenameHandlerRegistry.SELECT_ALL, selectAll)
        }
    }


    private fun getEditor(element: PsiElement, project: Project): Editor? {
        return if (findInjectionHost(element) != null)
            InjectedLanguageUtil.openEditorFor(element.containingFile, project)
        else
            FileEditorManager.getInstance(project).selectedTextEditor
    }

    private fun findProvider(): SpellCheckSuggestions? {
        for (extension in NameSuggestionProvider.EP_NAME.extensionList) {
            if (extension is SpellCheckSuggestions) {
                return extension
            }
        }
        return null
    }
}
