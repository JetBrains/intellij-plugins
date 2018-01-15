package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandlersFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.types.JSCompositeTypeImpl
import com.intellij.lang.javascript.psi.types.JSContextualUnionTypeImpl
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.language.VueJSLanguage

/**
 * @author Irina.Chernushina on 11/10/2017.
 */
class VueFrameworkInsideScriptSpecificHandlersFactory : JSFrameworkSpecificHandlersFactory {
  companion object {
    fun isInsideScript(element: PsiElement) : Boolean {
      val tag = PsiTreeUtil.getParentOfType(element, XmlTag::class.java) ?: return false
      return HtmlUtil.isScriptTag(tag)
    }
  }

  override fun findExpectedType(parent: JSExpression, expectedTypeKind: JSExpectedTypeKind): JSType? {
    val language = DialectDetector.languageOfElement(parent)
    if (VueFileType.INSTANCE == parent.containingFile?.fileType && isInsideScript(parent) && VueJSLanguage.INSTANCE != language) {
      val obj = parent as? JSObjectLiteralExpression
      obj?.parent as? ES6ExportDefaultAssignment ?: return null
      return createExportedObjectLiteralTypeEvaluator(obj)
    }
    return null
  }

  private fun createExportedObjectLiteralTypeEvaluator(obj: JSObjectLiteralExpression): JSType? {
    val typeAlias = JSFileReferencesUtil.resolveModuleReference(obj.containingFile, "vue")
                      .mapNotNull {
                        it as? JSElement ?: return@mapNotNull null
                        ES6PsiUtil.resolveSymbolInModule("Component", obj, it).filter { it.isValidResult }
                      }
                      .flatten()
                      .filter {
                        val psiFile = it.element?.containingFile
                        psiFile != null && TypeScriptUtil.isDefinitionFile(psiFile)
                      }
                      .mapNotNull { (it.element as? TypeScriptTypeAlias)?.typeDeclaration }.firstOrNull() ?: return null

    var typeFromTypeScript = TypeScriptTypeParser.buildTypeFromTypeScript(typeAlias)
    if (typeFromTypeScript is JSCompositeTypeImpl) {
      typeFromTypeScript = JSContextualUnionTypeImpl.getContextualUnionType(typeFromTypeScript.types, typeFromTypeScript.source)
    }
    return typeFromTypeScript
  }
}