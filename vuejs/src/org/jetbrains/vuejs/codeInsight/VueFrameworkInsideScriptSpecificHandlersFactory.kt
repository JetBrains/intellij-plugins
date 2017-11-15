package org.jetbrains.vuejs.codeInsight

import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.ModuleType
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandlersFactory
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.stubs.JSSymbolIndex2
import com.intellij.lang.javascript.psi.types.JSCompositeTypeImpl
import com.intellij.lang.javascript.psi.types.JSContextualUnionTypeImpl
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.Processors
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

  override fun newExpectedType(parent: JSExpression, expectedTypeKind: JSExpectedTypeKind): JSType? {
    val language = DialectDetector.languageOfElement(parent)
    if (VueFileType.INSTANCE == parent.containingFile?.fileType && isInsideScript(parent) && VueJSLanguage.INSTANCE != language) {
      val obj = parent as? JSObjectLiteralExpression
      obj?.parent as? ES6ExportDefaultAssignment ?: return null
      return createExportedObjectLiteralTypeEvaluator(obj)
    }
    return null
  }

  private fun createExportedObjectLiteralTypeEvaluator(obj: JSObjectLiteralExpression): JSType? {
    val project = obj.project

    var modules = JSFileReferencesUtil.resolveModuleReference(obj.containingFile, "vue")
      .filterNotNull()
      .mapNotNull { JSLibraryUtil.findAncestorLibraryDir(it.containingFile.viewProvider.virtualFile, "vue") }
    if (modules.isEmpty()) return null

    val projectScope = GlobalSearchScope.projectScope(project)
    val selected = modules.firstOrNull { projectScope.contains(it) }
    val cacheHolder = selected ?: project.baseDir
    if (selected != null) {
      modules = mutableListOf(selected)
    }
    val cacheHolderPsi = PsiManager.getInstance(project).findDirectory(cacheHolder!!) ?: return null
    return CachedValuesManager.getManager(project).getCachedValue(cacheHolderPsi, {
      CachedValueProvider.Result.create(getTypeFromModule(modules, project), cacheHolderPsi)
    })
  }

  private fun getTypeFromModule(modules: List<VirtualFile>,
                                project: Project): JSType? {
    val scope = GlobalSearchScopesCore.directoriesScope(project, true, *modules.toTypedArray())
    val elementsToProcess = ArrayList<JSElement>()
    if (StubIndex.getInstance().processElements(JSSymbolIndex2.KEY, "Component", project, scope, JSElement::class.java,
                                                Processors.cancelableCollectProcessor(elementsToProcess))) {
      val element = (elementsToProcess.firstOrNull {
        TypeScriptFileType.INSTANCE == it.containingFile?.fileType &&
        it is TypeScriptTypeAlias
      } as? TypeScriptTypeAlias)?.typeDeclaration ?: return null
      var typeFromTypeScript = TypeScriptTypeParser.buildTypeFromTypeScript(element)
      if (typeFromTypeScript is JSCompositeTypeImpl) {
        typeFromTypeScript = JSContextualUnionTypeImpl.getContextualUnionType(typeFromTypeScript.types, typeFromTypeScript.source)
      }
      return typeFromTypeScript
    }
    return null
  }
}