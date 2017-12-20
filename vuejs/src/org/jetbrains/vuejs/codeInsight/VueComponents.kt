package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.ES6QualifiedNameResolver
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.putValue
import org.jetbrains.vuejs.GLOBAL_BINDING_MARK
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.index.VueComponentsIndex
import org.jetbrains.vuejs.index.getForAllKeys

/**
 * @author Irina.Chernushina on 9/26/2017.
 */
class VueComponents {
  companion object {
    fun onlyLocal(elements: Collection<JSImplicitElement>): List<JSImplicitElement> {
      return elements.filter(this::isNotInLibrary)
    }

    fun isNotInLibrary(element : JSImplicitElement): Boolean {
      val file = element.containingFile.viewProvider.virtualFile
      return !JSProjectUtil.isInLibrary(file, element.project) && !JSLibraryUtil.isProbableLibraryFile(file)
    }

    fun findComponentDescriptor(element: JSImplicitElement): JSObjectLiteralExpression? {
      val parent = element.parent

      if (parent is JSCallExpression) {
        val reference = element.typeString ?: return null

        return resolveReferenceToObjectLiteral(element, reference)
      }
      return (parent as? JSProperty)?.context as? JSObjectLiteralExpression
    }

    private fun resolveReferenceToObjectLiteral(element: JSImplicitElement, reference: String): JSObjectLiteralExpression? {
      val scope = PsiTreeUtil.getContextOfType(element, JSCatchBlock::class.java, JSClass::class.java, JSExecutionScope::class.java)
                  ?: element.containingFile

      val resolvedLocally = JSStubBasedPsiTreeUtil.resolveLocally(reference, scope)
      if (resolvedLocally != null) {
        return getLiteralFromResolve(listOf(resolvedLocally))
      }

      val elements = ES6QualifiedNameResolver(scope).resolveQualifiedName(reference)
      return getLiteralFromResolve(elements)
    }

    private fun getLiteralFromResolve(result: Collection<PsiElement>): JSObjectLiteralExpression? {
      return result.mapNotNull(fun(it: PsiElement): JSObjectLiteralExpression? {
        val element: PsiElement? = (it as? JSVariable)?.initializerOrStub ?: it
        if (element is JSObjectLiteralExpression) return element
        return JSStubBasedPsiTreeUtil.calculateMeaningfulElement(element!!) as? JSObjectLiteralExpression
      }).firstOrNull()
    }

    fun isGlobal(it: JSImplicitElement) = it.typeString != null

    fun vueMixinDescriptorFinder(implicitElement: JSImplicitElement): JSObjectLiteralExpression? {
      if (!StringUtil.isEmptyOrSpaces(implicitElement.typeString)) {
        val expression = VueComponents.resolveReferenceToObjectLiteral(implicitElement, implicitElement.typeString!!)
        if (expression != null) {
          return expression
        }
      }
      val mixinObj = (implicitElement.parent as? JSProperty)?.parent as? JSObjectLiteralExpression
      if (mixinObj != null) return mixinObj

      val call = implicitElement.parent as? JSCallExpression
      if (call != null) {
        return JSStubBasedPsiTreeUtil.findDescendants(call, JSStubElementTypes.OBJECT_LITERAL_EXPRESSION)
          .firstOrNull { (it.context as? JSArgumentList)?.context == call || (it.context == call) }
      }
      return null
    }

    fun isGlobalLibraryComponent(name: String, component: JSImplicitElement): Boolean =
      getLibComponentsMappings(component)[fromAsset(name)] != null

    private fun getLibComponentsMappings(component: JSImplicitElement): Map<String, String> {
      val virtualFile = component.containingFile.viewProvider.virtualFile
      val packageJson = PackageJsonUtil.findUpPackageJson(virtualFile)
      val project = component.project
      if (packageJson != null && packageJson.parent != null &&
          PackageJsonUtil.getOrCreateData(packageJson).isDependencyOfAnyType("vue")) {
        val folder = PsiManager.getInstance(project).findDirectory(packageJson.parent)
        if (folder != null) {
          return CachedValuesManager.getCachedValue(folder,
                                                    CachedValueProvider {
                                                      val scope = GlobalSearchScopesCore.directoryScope(project, packageJson.parent, true)
                                                      val value = calculateAllComponents(scope, null, true)
                                                      CachedValueProvider.Result(value.libCompResolveMap, packageJson.parent)
                                                    })
        }
      }
      return getAllComponents(project, null, true).libCompResolveMap
    }

    fun getAllComponents(project: Project, filter: ((String) -> Boolean)?, onlyGlobal: Boolean): ComponentsData {
      return CachedValuesManager.getManager(project).getCachedValue(project, {
        val componentsData = calculateAllComponents(GlobalSearchScope.allScope(project), filter, onlyGlobal)
        CachedValueProvider.Result(componentsData, PsiManager.getInstance(project).modificationTracker)
      })
    }

    private fun calculateAllComponents(scope: GlobalSearchScope,
                                       filter: ((String) -> Boolean)?,
                                       onlyGlobal: Boolean): ComponentsData {
      val allValues = getForAllKeys(scope, VueComponentsIndex.KEY, filter)
      val libCompResolveMap = mutableMapOf<String, String>()

      val componentData = mutableMapOf<String, MutableList<Pair<PsiElement, Boolean>>>()
      for (value in allValues) {
        val isGlobal = isGlobal(value)
        if (isGlobal && value.name.endsWith(GLOBAL_BINDING_MARK)) {
          val pair = doAdditionalLibResolve(value) ?: continue
          val normalizedName = fromAsset(pair.first)
          libCompResolveMap.put(normalizedName, fromAsset(value.name.substringBefore(GLOBAL_BINDING_MARK)))
          componentData.putValue(normalizedName, Pair(pair.second, true))
        }
        else if (!onlyGlobal || isGlobal) {
          componentData.putValue(fromAsset(value.name), Pair(value, isGlobal))
        }
      }

      val componentsMap = mutableMapOf<String, Pair<PsiElement, Boolean>>()
      for (entry in componentData) {
        componentsMap.put(entry.key, selectComponentDefinition(entry.value))
      }
      return ComponentsData(componentsMap, libCompResolveMap)
    }

    private fun selectComponentDefinition(list: List<Pair<PsiElement, Boolean>>): Pair<PsiElement, Boolean> {
      var selected: Pair<PsiElement, Boolean>? = null
      for (componentData in list) {
        val isVue = VueFileType.INSTANCE == componentData.first.containingFile.fileType
        if (componentData.second) {
          if (isVue) return componentData
          selected = componentData
        }
        else if (selected == null && isVue) selected = componentData
      }
      return selected ?: list[0]
    }

    private fun doAdditionalLibResolve(element: JSImplicitElement): Pair<String, PsiElement>? {
      val descriptor = VueComponents.findComponentDescriptor(element)
      if (descriptor == null) {
        return Pair(element.name.substringBefore(GLOBAL_BINDING_MARK), element)
      }
      val name = getTextIfLiteral(descriptor.findProperty("name")?.value) ?: element.name.substringBefore(GLOBAL_BINDING_MARK)
      return Pair(name, descriptor)
    }

    class ComponentsData(val map: Map<String, Pair<PsiElement, Boolean>>, val libCompResolveMap: Map<String, String>)
  }
}