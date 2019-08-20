package tanvd.grazi.ide

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.ide.msg.GraziStateLifecycle
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.isInjectedFragment

class GraziInspection : LocalInspectionTool() {
    companion object : GraziStateLifecycle {
        override fun update(prevState: GraziConfig.State, newState: GraziConfig.State, project: Project) {
            if (prevState == newState) return

            ProjectManager.getInstance().openProjects.forEach {
                DaemonCodeAnalyzer.getInstance(it).restart()
            }
        }
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement?) {
                if (element == null || element.isInjectedFragment()) return

                val typos = HashSet<Typo>()
                for (ext in LanguageSupport.allForLanguageOrAny(element.language).filter { it.isRelevant(element) }) {
                    typos.addAll(ext.getTypos(element))
                }

                if (GraziConfig.get().enabledSpellcheck) {
                    typos.addAll(GraziSpellchecker.getTypos(element))
                }

                typos.map { GraziProblemDescriptor(it, isOnTheFly) }.forEach {
                    holder.registerProblem(it)
                }

                super.visitElement(element)
            }
        }
    }

    override fun getDisplayName() = "Grazi proofreading inspection"
}
