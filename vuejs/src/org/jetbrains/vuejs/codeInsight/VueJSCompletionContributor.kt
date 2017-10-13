package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.psi.xml.XmlFile

/**
 * @author Irina.Chernushina on 7/31/2017.
 */
class VueJSCompletionContributor : CompletionContributor() {
  override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
    val xmlFile = InjectedLanguageManager.getInstance(parameters.position.project).getTopLevelFile(
      parameters.position) as? XmlFile ?: return

    val embeddedScriptContents = org.jetbrains.vuejs.codeInsight.findModule(xmlFile) ?: return
    val defaultExport = com.intellij.lang.ecmascript6.resolve.ES6PsiUtil.findDefaultExport(embeddedScriptContents)
    if (defaultExport is ES6ExportDefaultAssignment && defaultExport.stubSafeElement is JSObjectLiteralExpression) {
      getComponentInnerDetailsFromObjectLiteral(defaultExport.stubSafeElement as JSObjectLiteralExpression)
        .forEach { result.addElement(LookupElementBuilder.create(it.name)) }
    }

    super.fillCompletionVariants(parameters, result)
  }
}