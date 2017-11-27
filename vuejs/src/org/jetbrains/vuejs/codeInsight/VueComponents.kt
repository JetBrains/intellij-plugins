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
import com.intellij.openapi.util.Trinity
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.index.VueComponentsIndex
import org.jetbrains.vuejs.index.getForAllKeys

/**
 * @author Irina.Chernushina on 9/26/2017.
 */
class VueComponents {
  companion object {
    fun selectComponent(elements: Collection<JSImplicitElement>?, ignoreLibraries: Boolean): JSImplicitElement? {
      elements ?: return null
      var filtered: Collection<JSImplicitElement> = onlyLocal(elements)
      if (filtered.isEmpty()) {
        if (ignoreLibraries) return null
        filtered = elements
      }

      return filtered.firstOrNull { it.typeString != null } ?: elements.firstOrNull()
    }

    fun onlyLocal(elements: Collection<JSImplicitElement>): List<JSImplicitElement> {
      return elements.filter {
        val file = it.containingFile.viewProvider.virtualFile
        !JSProjectUtil.isInLibrary(file, it.project) && !JSLibraryUtil.isProbableLibraryFile(file)
      }
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

      val componentsList = allValues.mapNotNull {
        val isGlobal = isGlobal(it)
        if (isGlobal && it.name.endsWith("*")) {
          val pair = doAdditionalLibResolve(it) ?: return@mapNotNull null
          libCompResolveMap.put(fromAsset(pair.first), fromAsset(it.name.substringBefore("*")))
          Trinity(pair.first, pair.second, true)
        }
        else if (!onlyGlobal) {
          Trinity(it.name, it, isGlobal)
        }
        else null
      }.groupBy { fromAsset(it.first) }.map {
        var selected: Trinity<String, out PsiElement, Boolean>? = null
        for (trinity in it.value) {
          val isVue = VueFileType.INSTANCE == trinity.second.containingFile.fileType
          if (trinity.third) {
            if (isVue) return@map trinity
            selected = trinity
          }
          else if (selected == null && isVue) selected = trinity
        }
        return@map selected ?: it.value[0]
      }
      val componentsMap = mutableMapOf<String, Pair<PsiElement, Boolean>>()
      componentsList.forEach { componentsMap.put(fromAsset(it.first), Pair(it.second, it.third)) }
      return ComponentsData(componentsMap, libCompResolveMap)
    }

    private fun doAdditionalLibResolve(element: JSImplicitElement): Pair<String, PsiElement>? {
      val descriptor = VueComponents.findComponentDescriptor(element)
      if (descriptor == null) {
        return Pair(element.name.substringBefore("*"), element)
      }
      val name = getTextIfLiteral(descriptor.findProperty("name")?.value) ?: element.name.substringBefore("*")
      return Pair(name, descriptor)
    }

    class ComponentsData(val map: Map<String, Pair<PsiElement, Boolean>>, val libCompResolveMap: Map<String, String>)
  }
}