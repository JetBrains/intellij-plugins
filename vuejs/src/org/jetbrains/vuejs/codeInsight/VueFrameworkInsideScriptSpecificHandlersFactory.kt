package org.jetbrains.vuejs.codeInsight

import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.ModuleType
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactoryI
import com.intellij.lang.javascript.dialects.JSFrameworkSpecificHandlersFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.stubs.JSSymbolIndex2
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
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
  override fun findFactory(element: PsiElement): JSDialectSpecificHandlersFactoryI? {
    if (VueFileType.INSTANCE == element.containingFile?.fileType && isInsideScript(element)) {
      return DynamicDelegatingFactory(getDelegate(element))
    }
    return null
  }

  companion object {
    fun isInsideScript(element: PsiElement) : Boolean {
      val tag = PsiTreeUtil.getParentOfType(element, XmlTag::class.java) ?: return false
      return HtmlUtil.isScriptTag(tag)
    }

    private fun getDelegate(context : PsiElement?) : JSDialectSpecificHandlersFactoryI {
      val default = JSDialectSpecificHandlersFactory.getDefault()
      context ?: return default
      val languageOfElement = DialectDetector.languageOfElement(context)
      if (VueJSLanguage.INSTANCE == languageOfElement) return default
      return JSDialectSpecificHandlersFactory.forLanguage(languageOfElement)
    }
  }

  private class DynamicDelegatingFactory(val delegate: JSDialectSpecificHandlersFactoryI) : JSDialectSpecificHandlersFactoryI by delegate {
    override fun newExpectedTypeEvaluator(parent: JSExpression?, expectedTypeKind: JSExpectedTypeKind?): ExpectedTypeEvaluator {
      parent ?: return delegate.newExpectedTypeEvaluator(parent, expectedTypeKind)

      val languageOfElement = DialectDetector.languageOfElement(parent)
      if (VueJSLanguage.INSTANCE == languageOfElement) {
        return delegate.newExpectedTypeEvaluator(parent, expectedTypeKind)
      }

      return object : JSBaseExpectedTypeEvaluator(parent, expectedTypeKind) {
        override fun findExpectedType(): JSType? {
          return findVueType(parent) ?: return delegate.newExpectedTypeEvaluator(parent, expectedTypeKind).findExpectedType()
        }

        private fun findVueType(parent: JSExpression?): JSType? {
          val obj = parent as? JSObjectLiteralExpression
          obj?.parent as? ES6ExportDefaultAssignment ?: return null

          val project = parent!!.project

          var modules: MutableList<CompletionModuleInfo> = mutableListOf()
          val interpreter = NodeJsInterpreterRef.createProjectRef().resolve(project)
          NodeModuleSearchUtil.findModulesWithName(modules, "vue", obj.containingFile.originalFile.viewProvider.virtualFile, true,
                                                   interpreter)
          if (modules.isEmpty()) return null

          val selected = modules.firstOrNull { ModuleType.NODE_MODULES_DIR == it.type }
          val cacheHolder: VirtualFile?
          if (selected != null && selected.virtualFile != null) {
            modules = mutableListOf(selected)
            cacheHolder = selected.virtualFile
          }
          else {
            cacheHolder = project.baseDir
          }
          val cacheHolderPsi = PsiManager.getInstance(project).findDirectory(cacheHolder!!) ?: return getTypeFromModule(modules, project)
          return CachedValuesManager.getManager(project).getCachedValue(cacheHolderPsi, {
            CachedValueProvider.Result.create(getTypeFromModule(modules, project), cacheHolderPsi)
          })
        }

        private fun getTypeFromModule(modules: MutableList<CompletionModuleInfo>,
                                      project: Project): JSType? {
          val array = modules.mapNotNull { it.virtualFile }.toTypedArray()
          val scope = GlobalSearchScopesCore.directoriesScope(project, true, *array)
          val elementsToProcess = ArrayList<JSElement>()
          if (StubIndex.getInstance().processElements(JSSymbolIndex2.KEY, "Component", project, scope, JSElement::class.java,
                                                      Processors.cancelableCollectProcessor(elementsToProcess))) {
            val element = (elementsToProcess.firstOrNull {
              TypeScriptFileType.INSTANCE == it.containingFile?.fileType &&
              it is TypeScriptTypeAlias
            } as? TypeScriptTypeAlias)?.typeDeclaration ?: return null
            return TypeScriptTypeParser.buildTypeFromTypeScript(element)
          }
          return null
        }
      }
    }
  }
}