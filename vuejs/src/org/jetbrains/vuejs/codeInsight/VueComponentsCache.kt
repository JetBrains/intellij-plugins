package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.vuejs.index.GLOBAL_BINDING_MARK
import org.jetbrains.vuejs.index.getVueIndexData

class VueComponentsCache {
  companion object {
    private val PACKAGES_WITH_GLOBAL_COMPONENTS = arrayOf("vuetify")

    // returns module: (component: (navigation-element, isGlobal))
    fun getAllComponentsGroupedByModules(project: Project, filter: ((String) -> Boolean)?, onlyGlobal: Boolean):
      Map<String, Map<String, Pair<PsiElement, Boolean>>> {
      val result: MutableMap<String, Map<String, Pair<PsiElement, Boolean>>> = mutableMapOf()
      result[""] = getOnlyProjectComponents(project).map

      getLibraryPackageJsons(project).forEach {
        val moduleComponents = getModuleComponents(it, project)
        if (moduleComponents != null) {
          val name = PackageJsonUtil.getOrCreateData(it).name ?: it.parent.name
          result[name] = moduleComponents.map
        }
      }

      if (onlyGlobal || filter != null) {
        return result.map { entry ->
          Pair(entry.key, entry.value.filter { (!onlyGlobal || it.value.second) && (filter == null || filter.invoke(it.key)) })
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
        val psiDirectory = PsiManager.getInstance(project).findDirectory(packageJson.parent)
        if (psiDirectory != null) {
          return getCachedComponentsData(psiDirectory)
        }
      }
      return null
    }

    private fun getCachedComponentsData(psiDirectory: PsiDirectory): ComponentsData? {
      val provider = CachedValueProvider {
        val directoryFile = psiDirectory.virtualFile
        
        val scope = GlobalSearchScopesCore.directoryScope(psiDirectory.project, directoryFile, true)
        val globalize = PACKAGES_WITH_GLOBAL_COMPONENTS.contains(psiDirectory.name)
        CachedValueProvider.Result(VueComponentsCalculation.calculateScopeComponents(scope, globalize), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
      }
      return CachedValuesManager.getCachedValue(psiDirectory, provider)
    }

    private fun getOnlyProjectComponents(project: Project): ComponentsData {
      return CachedValuesManager.getManager(project).getCachedValue(project) {
        val componentsData = VueComponentsCalculation.calculateScopeComponents(GlobalSearchScope.projectScope(project), false)
        CachedValueProvider.Result(componentsData, PsiManager.getInstance(project).modificationTracker)
      }
    }

    private fun resolveComponentsCollection(component: JSImplicitElement): Map<String, Pair<PsiElement, Boolean>> {
      val virtualFile = component.containingFile.viewProvider.virtualFile
      val variants = mutableListOf<VirtualFile>()
      PackageJsonUtil.processUpPackageJsonFilesInAllScope(virtualFile) {
        if (packageJsonForLibraryAndHasVue(it)) variants.add(it)
        true
      }
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