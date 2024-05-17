package com.intellij.lang.javascript.frameworks.nextjs.references

import com.intellij.javascript.nodejs.NodeModuleDirectorySearchProcessor.INDEX_NAME
import com.intellij.lang.javascript.config.JSDirectFileReferenceResolver
import com.intellij.lang.javascript.config.JSDirectFileReferenceResolverProvider
import com.intellij.lang.javascript.config.JSImportResolveContext
import com.intellij.lang.javascript.frameworks.JSXmlAttributePathUtil
import com.intellij.lang.javascript.frameworks.modules.JSPathResolution
import com.intellij.lang.javascript.frameworks.modules.resolver.JSParsedPathElement
import com.intellij.lang.javascript.frameworks.nextjs.isNextJsContext
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.PathUtilRt

class NextJsPathReferenceResolverProvider : JSDirectFileReferenceResolverProvider {
  override fun accept(element: PsiElement): Boolean {
    return element is XmlAttributeValue && isNextJsContext(element)
  }

  override fun provideResolver(element: PsiElement, resolveContext: JSImportResolveContext): JSDirectFileReferenceResolver {
    return NextJsDirectFileReferenceResolver(element, resolveContext)
  }
}

class NextJsDirectFileReferenceResolver(
  private val element: PsiElement,
  private val resolveContext: JSImportResolveContext
) : JSDirectFileReferenceResolver {
  override fun resolveDirectFile(moduleName: String, contextFile: VirtualFile): JSPathResolution {
    val pathElements = JSParsedPathElement.parseReferenceText(moduleName, false)
    val roots = resolveContext.rootsProvider.getDefaultRoots(element.project, moduleName, contextFile)
    var result = JSPathResolution.EMPTY
    roots.forEach {
      ProgressManager.checkCanceled()
      val resolved = resolveRoot(moduleName, it, pathElements)
      if (!resolved.isFullChainResolved) {
        val currentSize = result.size()
        result = if (resolved.size() > currentSize) resolved else result
      }
      else {
        result = resolved
      }
    }

    return result
  }

  private fun resolveRoot(
    relativePath: String,
    containingDirectory: VirtualFile,
    pathElements: Array<JSParsedPathElement>,
  ): JSPathResolution {
    val isLastDirectory = relativePath.endsWith("/")
    val result: MutableList<VirtualFile> = mutableListOf()

    var parent = containingDirectory
    pathElements.forEachIndexed { index, pathElement ->
      ProgressManager.checkCanceled()

      val name = pathElement.text
      val nextNameAsList = pathElements.getOrNull(index + 1)?.text?.takeIf { it.isNotEmpty() }?.let { listOf(it) } ?: emptyList()
      val isLast = index == pathElements.size - 1
      if (!isLast || isLastDirectory) {
        val resolved = resolvePathElement(parent, name, nextNameAsList)
        if (resolved == null) {
          return JSPathResolution(result, false, false)
        }

        var next = resolved
        if (isLast && next.isDirectory) {
          next = resolveContext.nodeModuleSearchProcessor.loadDirectory(next)
        }

        if (next == null) {
          return JSPathResolution(result, true, false)
        }

        parent = next

        result.add(next)
        return@forEachIndexed
      }

      //process named part
      val candidate = loadModuleForLastPartDirectly(name, parent, result)
      if (candidate != null) return candidate
    }

    return JSPathResolution(result, result.size == pathElements.size, false)
  }

  private fun resolvePathElement(parent: VirtualFile, path: String, nextPaths: List<String> = emptyList()): VirtualFile? {
    if (path == "." || path.isEmpty()) return parent
    if (path == "..") return parent.parent
    if (!PathUtilRt.isValidFileName(path, false)) return null
    val result = parent.children.find { it.nameWithoutExtension == path || it.name == path }
    if (result != null) return result

    return parent.children.firstNotNullOfOrNull { child ->
      child.takeIf { it.name.startsWith("(") || it.name.startsWith("@") }
        ?.children
        ?.find { subChild ->
          subChild.nameWithoutExtension == path && (nextPaths.isEmpty() || subChild.children.any { it.nameWithoutExtension in nextPaths || it.name in nextPaths })
        }
    }
  }

  private fun loadModuleForLastPartDirectly(
    name: String,
    parent: VirtualFile,
    result: MutableList<VirtualFile>,
  ): JSPathResolution? {
    val nextPaths = listOf(INDEX_NAME) + JSXmlAttributePathUtil.additionalLeafFiles(element)
    val resolved = resolvePathElement(parent, name, nextPaths)
    return if (resolved != null) {
      handleResolvedFile(resolved, result)
    }
    else {
      handleAsDynamicRouteFile(parent, result)
    }
  }

  private fun handleResolvedFile(
    resolved: VirtualFile,
    result: MutableList<VirtualFile>
  ): JSPathResolution? {
    if (resolved.isDirectory) {
      val loadedDirectory = resolveContext.nodeModuleSearchProcessor.loadDirectory(resolved)
      if (loadedDirectory != null) {
        result.add(loadedDirectory)
        return JSPathResolution(result, true, false)
      }
    }

    if (!resolved.isDirectory || resolveContext.isAllowFolders) {
      result.add(resolved)
      return JSPathResolution(result, true, false)
    }

    return null
  }

  private fun handleAsDynamicRouteFile(
    parent: VirtualFile,
    result: MutableList<VirtualFile>
  ): JSPathResolution? {
    return parent.children.find { it.name.startsWith("[") }?.let {
      result.add(it)
      JSPathResolution(result, true, false)
    }
  }
}
