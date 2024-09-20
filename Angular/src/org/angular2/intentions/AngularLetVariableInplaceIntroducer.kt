package org.angular2.intentions

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.refactoring.introduce.BaseIntroduceSettings
import com.intellij.lang.javascript.refactoring.introduce.IntroducedEntityInfoProvider
import com.intellij.lang.javascript.refactoring.introduce.JSBaseIntroduceDialog
import com.intellij.lang.javascript.refactoring.introduce.JSBaseIntroduceHandler
import com.intellij.lang.javascript.refactoring.introduceVariable.JSVariableInplaceIntroducerBase
import com.intellij.lang.javascript.refactoring.introduceVariable.Settings
import com.intellij.lang.javascript.refactoring.introduceVariable.Settings.IntroducedVarType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import javax.swing.JComponent

class AngularLetVariableInplaceIntroducer(
  project: Project, editor: Editor, occurrences: Array<out JSExpression>,
  fileType: FileType,
  handler: JSBaseIntroduceHandler<out PsiElement, BaseIntroduceSettings, out JSBaseIntroduceDialog<out IntroducedEntityInfoProvider>>,
  context: JSBaseIntroduceHandler.BaseIntroduceContext<BaseIntroduceSettings>,
  private val callback: Runnable,
) : JSVariableInplaceIntroducerBase(project, editor, occurrences, fileType, handler, context) {

  override fun getComponent(): JComponent? {
    return null
  }

  override fun getInplaceIntroduceSettings(name: String?): BaseIntroduceSettings {
    return object : Settings {
      override fun getIntroducedVarType(): IntroducedVarType {
        return IntroducedVarType.LET
      }

      override fun isReplaceAllOccurrences(): Boolean {
        return myInitialSettings.isReplaceAllOccurrences
      }

      override fun getVariableName(): String? {
        return name
      }

      override fun getVariableType(): String? {
        return null
      }
    }
  }

  override fun performPostIntroduceTasks() {
    super.performPostIntroduceTasks()
    callback.run()
  }
}