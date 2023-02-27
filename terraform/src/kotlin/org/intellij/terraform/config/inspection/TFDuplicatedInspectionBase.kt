/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.refactoring.actions.RenameElementAction
import com.intellij.usageView.UsageInfo
import com.intellij.usages.*
import com.intellij.util.NullableFunction
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.config.model.getTerraformSearchScope
import org.intellij.terraform.config.patterns.TerraformPatterns


abstract class TFDuplicatedInspectionBase : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = holder.file
    if (!TerraformPatterns.TerraformConfigFile.accepts(file)) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return createVisitor(holder)
  }

  companion object {
    abstract class RenameQuickFix : LocalQuickFix {
      override fun startInWriteAction(): Boolean = false

      protected fun invokeRenameRefactoring(project: Project, element: PsiElement) {
        val editor = getEditor(element, project) ?: return
        val dataContext = SimpleDataContext.builder()
          .add(CommonDataKeys.PSI_ELEMENT, element)
          .add(CommonDataKeys.EDITOR, editor)
          .setParent(EditorUtil.getEditorDataContext(editor))
          .build()
        val action = RenameElementAction()
        val event = AnActionEvent.createFromAnAction(action, null, "", dataContext)
        event.setInjectedContext(false) // ensure our map/element is used, not some other element
        action.actionPerformed(event)
      }

      fun getEditor(element: PsiElement, project: Project): Editor? {
        return if (InjectedLanguageUtil.findInjectionHost(element) != null)
          InjectedLanguageUtil.openEditorFor(element.containingFile, project)
        else
          FileEditorManager.getInstance(project).selectedTextEditor
      }
    }
  }

  abstract fun createVisitor(holder: ProblemsHolder): PsiElementVisitor

  protected fun createNavigateToDupeFix(element: PsiElement, single: Boolean): LocalQuickFix {
    return object : LocalQuickFix {
      override fun startInWriteAction(): Boolean = false

      override fun getFamilyName(): String {
        val first = if (!single) HCLBundle.message("duplicated.inspection.base.navigate.to.duplicate.quick.fix.name.first") else ""
        return HCLBundle.message("duplicated.inspection.base.navigate.to.duplicate.quick.fix.name", first)
      }

      override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        if (element is Navigatable && (element as Navigatable).canNavigate()) {
          (element as Navigatable).navigate(true)
        }
        else {
          OpenFileDescriptor(project, element.containingFile.originalFile.virtualFile, element.textOffset).navigate(true)
        }
      }
    }
  }

  protected fun createShowOtherDupesFix(element: PsiNamedElement, duplicates: NullableFunction<PsiElement, List<PsiElement>?>): LocalQuickFix {

    return object : LocalQuickFix {
      var myTitle: String? = null

      override fun startInWriteAction(): Boolean = false
      override fun getFamilyName(): String = HCLBundle.message("duplicated.inspection.base.show.other.duplicates.quick.fix.name")

      override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        @Suppress("NAME_SHADOWING")
        val duplicates = ApplicationManager.getApplication().runReadAction<List<PsiElement>?> {
          duplicates.`fun`(descriptor.psiElement)
        } ?: return

        val presentation = UsageViewPresentation()
        val target = PsiElement2UsageTargetAdapter(element, true)
        if (myTitle == null) myTitle = "Duplicate of " + target.presentableText
        val title = myTitle!!
        presentation.searchString = title
        presentation.tabName = title
        presentation.tabText = title
        val scope = descriptor.psiElement.getTerraformSearchScope()
        presentation.scopeText = scope.displayName

        UsageViewManager.getInstance(project).searchAndShowUsages(arrayOf<UsageTarget>(target), {
          UsageSearcher { processor ->
            val infos = ApplicationManager.getApplication().runReadAction<List<UsageInfo>> {
              duplicates.map { dup -> UsageInfo(dup) }
            }
            for (info in infos) {
              processor.process(UsageInfo2UsageAdapter(info))
            }
          }
        }, false, false, presentation, null)
      }
    }
  }
}

