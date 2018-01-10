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
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.putValue
import org.jetbrains.vuejs.GLOBAL_BINDING_MARK
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.index.*
import java.util.*

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
        val reference = getTypeString(element) ?: return null

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

    fun isGlobal(element: JSImplicitElement) = getTypeString(element) != null

    fun isGlobalExact(element: JSImplicitElement) = getTypeString(element) != null && !getOriginalName(element).endsWith(GLOBAL_BINDING_MARK)

    fun vueMixinDescriptorFinder(implicitElement: JSImplicitElement): JSObjectLiteralExpression? {
      val typeString = getTypeString(implicitElement)
      if (!StringUtil.isEmptyOrSpaces(typeString)) {
        val expression = VueComponents.resolveReferenceToObjectLiteral(implicitElement, typeString!!)
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

    fun isGlobalLibraryComponent(component: JSImplicitElement): Boolean {
      val originalName = getOriginalName(component)
      if (originalName.endsWith(GLOBAL_BINDING_MARK)) return false
      return resolveComponentsCollection(component)[fromAsset(originalName)]?.second ?: false
    }

    fun findGlobalLibraryComponent(project: Project, name: String): PsiElement? {
      val projectComponents = getOnlyProjectComponents(project)
      var element = findComponentByAlias(projectComponents, name)
      if (element != null) return element

      val libraryPackageJsons = getLibraryPackageJsons(project)
      for (packageJson in libraryPackageJsons) {
        element = findComponentByAlias(getModuleComponents(packageJson, project), name)
        if (element != null) return element
      }
      return null
    }

    private fun findComponentByAlias(components: ComponentsData?, alias: String): PsiElement? {
      if (components == null) return null
      val localName = components.libCompResolveMap[fromAsset(alias)] ?: return null
      return components.map[localName]?.first
    }

    private fun resolveComponentsCollection(component: JSImplicitElement): Map<String, Pair<PsiElement, Boolean>> {
      val virtualFile = component.containingFile.viewProvider.virtualFile
      val variants = mutableListOf<VirtualFile>()
      PackageJsonUtil.processUpPackageJsonFilesInAllScope(virtualFile,
                                                {
                                                  if (packageJsonForLibraryAndHasVue(it)) variants.add(it)
                                                  true
                                                })
      val project = component.project
      @Suppress("LoopToCallChain") // we do not want to convert them all and then search
      for (variant in variants) {
        val moduleComponents = getModuleComponents(variant, project)
        if (moduleComponents != null) {
          return moduleComponents.map
        }
      }
      return getOnlyProjectComponents(project).map
    }

    private fun getModuleComponents(packageJson: VirtualFile, project: Project): ComponentsData? {
      if (packageJson.parent != null) {
        val folder = PsiManager.getInstance(project).findDirectory(packageJson.parent)
        if (folder != null) {
          return CachedValuesManager.getCachedValue(folder,
                                                    CachedValueProvider {
                                                      val scope = GlobalSearchScopesCore.directoryScope(project, packageJson.parent, true)
                                                      CachedValueProvider.Result(calculateScopeComponents(scope), packageJson.parent)
                                                    })
        }
      }
      return null
    }

    private fun getOnlyProjectComponents(project: Project): ComponentsData {
      return CachedValuesManager.getManager(project).getCachedValue(project, {
        val componentsData = calculateScopeComponents(GlobalSearchScope.projectScope(project))
        CachedValueProvider.Result(componentsData, PsiManager.getInstance(project).modificationTracker)
      })
    }

    fun getAllComponents(project: Project, filter: ((String) -> Boolean)?, onlyGlobal: Boolean): ComponentsData {
      val components = getOnlyProjectComponents(project)

      val libPackageJsonFiles = getLibraryPackageJsons(project)
      val libraryComponents = libPackageJsonFiles.mapNotNull { getModuleComponents(it, project) }

      var allComponentsMap: Map<String, Pair<PsiElement, Boolean>> = mutableMapOf()
      val mutableComponentsMap = allComponentsMap as MutableMap
      val libCompResolveMap: MutableMap<String, String> = mutableMapOf()
      libraryComponents.forEach {
          mutableComponentsMap.putAll(it.map)
          libCompResolveMap.putAll(it.libCompResolveMap)
        }
      mutableComponentsMap.putAll(components.map)
      libCompResolveMap.putAll(components.libCompResolveMap)

      if (onlyGlobal || filter != null) {
        allComponentsMap = allComponentsMap.filter { (!onlyGlobal || it.value.second) && (filter == null || filter.invoke(it.key)) }
      }
      return ComponentsData(allComponentsMap, libCompResolveMap)
    }

    private fun getLibraryPackageJsons(project: Project): List<VirtualFile> {
      return FilenameIndex.getVirtualFilesByName(project, PackageJsonUtil.FILE_NAME, GlobalSearchScope.allScope(project))
        .filter(this::packageJsonForLibraryAndHasVue)
   }

    private fun packageJsonForLibraryAndHasVue(it: VirtualFile) = JSLibraryUtil.isProbableLibraryFile(it) &&
                                                                  PackageJsonUtil.getOrCreateData(it).isDependencyOfAnyType("vue")

    private fun calculateScopeComponents(scope: GlobalSearchScope): ComponentsData {
      val allValues = getForAllKeys(scope, VueComponentsIndex.KEY)
      val libCompResolveMap = mutableMapOf<String, String>()

      val componentData = mutableMapOf<String, MutableList<Pair<PsiElement, Boolean>>>()
      for (value in allValues) {
        val name = getOriginalName(value)
        val isGlobal = isGlobal(value)
        if (isGlobal && GLOBAL_COMP_COLLECTION == name) {
          gatherObjectLiteralProperties(value, libCompResolveMap, componentData)
        } else if (isGlobal && name.endsWith(GLOBAL_BINDING_MARK)) {
          val pair = doAdditionalLibResolve(value) ?: continue
          val normalizedName = fromAsset(pair.first)
          libCompResolveMap.put(fromAsset(name.substringBefore(GLOBAL_BINDING_MARK)), normalizedName)
          componentData.putValue(normalizedName, Pair(pair.second, true))
        }
        else {
          componentData.putValue(fromAsset(name), Pair(value, isGlobal))
        }
      }

      val componentsMap = mutableMapOf<String, Pair<PsiElement, Boolean>>()
      for (entry in componentData) {
        componentsMap.put(entry.key, selectComponentDefinition(entry.value))
      }
      return ComponentsData(componentsMap, libCompResolveMap)
    }

    private fun gatherObjectLiteralProperties(value: JSImplicitElement,
                                              libCompResolveMap: MutableMap<String, String>,
                                              componentData: MutableMap<String, MutableList<Pair<PsiElement, Boolean>>>) {
      // object properties iteration
      val objLiteral = findComponentDescriptor(value) ?: return
      val queue = ArrayDeque<PsiElement>()
      queue.addAll(objLiteral.children)
      val visited = mutableSetOf<PsiElement>()
      while (!queue.isEmpty()) {
        val element = queue.removeFirst()
        // technically, I can write spread to itself or a ring
        if (visited.contains(element)) continue
        visited.add(element)

        val asSpread = element as? JSSpreadExpression
        if (asSpread != null) {
          val spreadExpression = asSpread.expression
          if (spreadExpression is JSReferenceExpression) {
            val variants = spreadExpression.multiResolve(false)
            val literal = getLiteralFromResolve(variants.mapNotNull { if (it.isValidResult) it.element else null }.toList())
            if (literal != null) queue.addAll(literal.children)
          } else if (spreadExpression is JSObjectLiteralExpression) {
            queue.addAll(spreadExpression.children)
          }
          continue
        }
        val asProperty = element as? JSProperty
        if (asProperty != null) {
          val propName = asProperty.name
          if (propName != null) {
            val descriptor = JSStubBasedPsiTreeUtil.calculateMeaningfulElement(asProperty) as? JSObjectLiteralExpression
            val nameFromDescriptor = getTextIfLiteral(descriptor?.findProperty("name")?.value) ?: propName
            // name used in call Vue.component() overrides what was set in descriptor itself
            val normalizedName = fromAsset(propName)
            val realName = fromAsset(nameFromDescriptor)
            libCompResolveMap.put(normalizedName, realName)
            componentData.putValue(realName, Pair(descriptor ?: asProperty, true))
          }
        }
      }
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