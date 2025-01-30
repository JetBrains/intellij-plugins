package org.angular2.findUsages

import com.intellij.ide.DataManager
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.findUsages.JSSearchForComponentUsageAction
import com.intellij.lang.javascript.findUsages.JavaScriptFindUsagesConfiguration
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.usages.Usage
import com.intellij.usages.UsageTarget
import com.intellij.usages.UsageView
import com.intellij.usages.rules.PsiElementUsage
import com.intellij.usages.rules.UsageFilteringRule
import com.intellij.usages.rules.UsageFilteringRuleProvider
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC
import org.angular2.lang.Angular2LangUtil
import org.jetbrains.annotations.NonNls

private class Angular2UsageFilteringRuleProvider : UsageFilteringRuleProvider {
  @Suppress("OVERRIDE_DEPRECATION")
  override fun getActiveRules(project: Project): Array<out UsageFilteringRule> =
    arrayOf(Angular2ComponentUsageInTemplateFilteringRule(project))

  @Deprecated("Deprecated in Java")
  override fun createFilteringActions(view: UsageView): Array<out AnAction?> {
    val dataContext = DataManager.getInstance().getDataContext()
    val psiFile = dataContext.getData(CommonDataKeys.PSI_FILE)
    if (psiFile !is JSFile || !Angular2LangUtil.isAngular2Context(psiFile)) return AnAction.EMPTY_ARRAY
    var hasComponent = false
    val psiElement = dataContext.getData(CommonDataKeys.PSI_ELEMENT)
    if (psiElement is TypeScriptClass) {
      hasComponent = Angular2DecoratorUtil.findDecorator(psiElement, COMPONENT_DEC, DIRECTIVE_DEC) != null
    }
    else if (psiElement == null && dataContext.getData(CommonDataKeys.SYMBOLS)?.isEmpty() != false) {
      psiFile.acceptChildren(object : JSElementVisitor() {
        override fun visitTypeScriptClass(typeScriptClass: TypeScriptClass) {
          if (Angular2DecoratorUtil.findDecorator(typeScriptClass, COMPONENT_DEC, DIRECTIVE_DEC) != null) {
            hasComponent = true
          }
        }

        override fun visitES6ExportDefaultAssignment(node: ES6ExportDefaultAssignment) {
          node.acceptChildren(this)
        }
      })
    }
    return if (hasComponent) {
      arrayOf<AnAction>(JSSearchForComponentUsageAction())
    }
    else {
      AnAction.EMPTY_ARRAY
    }
  }

  private class Angular2ComponentUsageInTemplateFilteringRule(private val project: Project) : UsageFilteringRule {

    override fun getActionId(): String =
      "UsageFiltering.Angular2ComponentUsageInTemplate"

    override fun getRuleId(): @NonNls String =
      "Angular2ComponentUsageInTemplateFilteringRule"

    override fun isVisible(usage: Usage, targets: Array<out UsageTarget>): Boolean =
      usage !is PsiElementUsage
      || usage.referenceClass != Angular2ComponentClassInTemplateUsageSearcher.ClassUsageInTemplateFile::class.java
      || JavaScriptFindUsagesConfiguration.getFindUsagesOptions(project, null).isShowComponentUsages
  }

}