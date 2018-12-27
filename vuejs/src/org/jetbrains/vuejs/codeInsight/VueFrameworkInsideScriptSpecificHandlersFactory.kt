package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandlersFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSCompositeTypeImpl
import com.intellij.lang.javascript.psi.types.JSContextualUnionTypeImpl
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.codeInsight.completion.vuex.VueStoreUtils
import org.jetbrains.vuejs.codeInsight.completion.vuex.VueStoreUtils.hasVuex
import org.jetbrains.vuejs.codeInsight.completion.vuex.VueStoreUtils.normalizeName
import org.jetbrains.vuejs.index.DELIMITER
import org.jetbrains.vuejs.index.VueStoreIndex
import org.jetbrains.vuejs.index.getForAllKeys
import org.jetbrains.vuejs.language.VueJSLanguage

/**
 * @author Irina.Chernushina on 11/10/2017.
 */
class VueFrameworkInsideScriptSpecificHandlersFactory : JSFrameworkSpecificHandlersFactory {
  companion object {
    fun isInsideScript(element: PsiElement): Boolean {
      val tag = PsiTreeUtil.getParentOfType(element, XmlTag::class.java) ?: return false
      return HtmlUtil.isScriptTag(tag)
    }
  }

  override fun findExpectedType(parent: JSExpression, expectedTypeKind: JSExpectedTypeKind): JSType? {
    val language = DialectDetector.languageOfElement(parent)
    if (VueFileType.INSTANCE == parent.containingFile?.fileType && isInsideScript(parent) && VueJSLanguage.INSTANCE != language) {
      val obj = parent as? JSObjectLiteralExpression
      if (obj?.parent !is ES6ExportDefaultAssignment) {
        if (!hasVuex(parent)) return null
        val expression = PsiTreeUtil.getParentOfType(parent, JSCallExpression::class.java)
        if (expression == null || expression.methodExpression == null) return null
        val keys = getForAllKeys(GlobalSearchScope.projectScope(expression.project), VueStoreIndex.KEY)
        if (keys.isEmpty()) return null
        val map = mutableListOf<JSStringLiteralTypeImpl>()
        val expressionText = expression.methodExpression!!.text!!
        when {
          expressionText.endsWith("dispatch") || expressionText == "mapActions" -> processVuex(keys, map, VueStoreUtils.ACTION)
          expressionText == "commit" || expressionText == "mapMutations" -> processVuex(keys, map, VueStoreUtils.MUTATION)
          expressionText.endsWith("getters") || expressionText == "mapGetters" -> processVuex(keys, map, VueStoreUtils.GETTER)
          expressionText == "mapState" -> processVuex(keys, map, VueStoreUtils.STATE)
        }
        if (map.isEmpty()) return null
        return JSCompositeTypeImpl(map[0].source, map)
      }
      return createExportedObjectLiteralTypeEvaluator(obj)
    }
    return null
  }

  private fun processVuex(keys: Collection<JSImplicitElement>,
                          map: MutableList<JSStringLiteralTypeImpl>,
                          vueStoreAction: String) {
    keys.forEach {
      if (it.typeString!!.startsWith("0$DELIMITER$vueStoreAction")) {
        val typeSource = JSTypeSource(it.containingFile, it, JSTypeSource.SourceLanguage.JS, false)
        val jsStringLiteralTypeImpl = JSStringLiteralTypeImpl(normalizeName(it.typeString!!), false, typeSource)
        map.add(jsStringLiteralTypeImpl)
      }
    }
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

    var typeFromTypeScript = typeAlias.jsType
    if (typeFromTypeScript is JSCompositeTypeImpl) {
      typeFromTypeScript = JSContextualUnionTypeImpl.getContextualUnionType(typeFromTypeScript.types, typeFromTypeScript.source)
    }
    return typeFromTypeScript
  }
}