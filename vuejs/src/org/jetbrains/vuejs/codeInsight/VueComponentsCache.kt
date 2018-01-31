package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.vuejs.index.GLOBAL_BINDING_MARK
import org.jetbrains.vuejs.index.getVueIndexData

/**
 * @author Irina.Chernushina on 1/12/2018.
 */
class VueComponentsCache {
  companion object {
    // returns module: (component: (navigation-element, isGlobal))
    fun getAllComponentsGroupedByModules(project: Project, filter: ((String) -> Boolean)?, onlyGlobal: Boolean):
      Map<String, Map<String, Pair<PsiElement, Boolean>>> {
      val result: MutableMap<String, Map<String, Pair<PsiElement, Boolean>>> = mutableMapOf()
      result.put("", getOnlyProjectComponents(project).map)

      getLibraryPackageJsons(project).forEach {
        val moduleComponents = getModuleComponents(it, project)
        if (moduleComponents != null) {
          val name = PackageJsonUtil.getOrCreateData(it).name ?: it.parent.name
          result.put(name, moduleComponents.map)
        }
      }

      if (onlyGlobal || filter != null) {
        return result.map {
          Pair(it.key, it.value.filter { (!onlyGlobal || it.value.second) && (filter == null || filter.invoke(it.key)) })
        }.toMap()
      }
      return result
    }

    fun isGlobalLibraryComponent(component: JSImplicitElement): Boolean {
      val nameText = getVueIndexData(component).originalName
      val originalName = nameText.substringBefore(GLOBAL_BINDING_MARK)
      if (nameText.endsWith(GLOBAL_BINDING_MARK)) return false
      return resolveComponentsCollection(component)[fromAsset(originalName)]?.second ?: false
    }

    fun findGlobalLibraryComponent(project: Project, name: String): Pair<String, PsiElement>? {
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

    private fun findComponentByAlias(components: ComponentsData?, alias: String): Pair<String, PsiElement>? {
      if (components == null) return null
      val localName = components.libCompResolveMap[fromAsset(alias)] ?: return null
      val localComp = components.map[localName]?.first ?: return null
      return Pair(localName, localComp)
    }

    private fun getModuleComponents(packageJson: VirtualFile, project: Project): ComponentsData? {
      if (packageJson.parent != null) {
        val folder = PsiManager.getInstance(project).findDirectory(packageJson.parent)
        if (folder != null) {
          val provider = CachedValueProvider {
            val scope = GlobalSearchScopesCore.directoryScope(project, packageJson.parent, true)
            CachedValueProvider.Result(VueComponentsCalculation.calculateScopeComponents(scope), packageJson.parent)
          }
          return CachedValuesManager.getCachedValue(folder, provider)
        }
      }
      return null
    }

    private fun getOnlyProjectComponents(project: Project): ComponentsData {
      return CachedValuesManager.getManager(project).getCachedValue(project, {
        val componentsData = VueComponentsCalculation.calculateScopeComponents(GlobalSearchScope.projectScope(project))
        CachedValueProvider.Result(componentsData, PsiManager.getInstance(project).modificationTracker)
      })
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

    private fun getLibraryPackageJsons(project: Project): List<VirtualFile> {
      return FilenameIndex.getVirtualFilesByName(project, PackageJsonUtil.FILE_NAME, GlobalSearchScope.allScope(project))
        .filter(this::packageJsonForLibraryAndHasVue)
    }

    private fun packageJsonForLibraryAndHasVue(it: VirtualFile) = JSLibraryUtil.isProbableLibraryFile(it) &&
                                                                  PackageJsonUtil.getOrCreateData(it).isDependencyOfAnyType("vue")

  }

  class ComponentsData(val map: Map<String, Pair<PsiElement, Boolean>>,
                       val libCompResolveMap: Map<String, String>)
}