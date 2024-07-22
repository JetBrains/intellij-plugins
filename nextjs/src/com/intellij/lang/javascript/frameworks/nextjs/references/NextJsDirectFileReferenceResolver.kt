package com.intellij.lang.javascript.frameworks.nextjs.references

import com.intellij.lang.javascript.config.JSDirectFileReferenceResolver
import com.intellij.lang.javascript.config.JSDirectFileReferenceResolverProvider
import com.intellij.lang.javascript.config.JSImportResolveContext
import com.intellij.lang.javascript.frameworks.modules.JSPathResolution
import com.intellij.lang.javascript.frameworks.modules.resolver.JSParsedPathElement
import com.intellij.lang.javascript.frameworks.nextjs.isNextJsContext
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.lang.javascript.patterns.JSPatterns
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import com.intellij.openapi.vfs.newvfs.impl.FsRoot
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue

class NextJsPathReferenceResolverProvider : JSDirectFileReferenceResolverProvider {
  override fun accept(element: PsiElement): Boolean {
    return (element is XmlAttributeValue || JSPatterns.jsLiteral().inJSXEmbeddedContent().accepts(element)) && isNextJsContext(element)
  }

  override fun provideResolver(element: PsiElement, resolveContext: JSImportResolveContext): JSDirectFileReferenceResolver {
    return NextJsDirectFileReferenceResolver(element, resolveContext)
  }
}

class NextJsDirectFileReferenceResolver(
  private val element: PsiElement,
  private val resolveContext: JSImportResolveContext,
) : JSDirectFileReferenceResolver {
  private val appRouterLeafFile = "page"

  override fun resolveDirectFile(moduleName: String, contextFile: VirtualFile): JSPathResolution {
    var result = JSPathResolution.EMPTY
    val pathElements = JSParsedPathElement.parseReferenceText(moduleName, false)
    val roots = resolveContext.rootsProvider.getDefaultRoots(element.project, moduleName, contextFile)
    roots.distinct()
      .filter { it !is FsRoot && it.name != NodeModuleUtil.NODE_MODULES && !it.name.startsWith("@") }
      .forEach {
        ProgressManager.checkCanceled()
        val resolved = resolveRoot(it, pathElements, moduleName.endsWith("/"))
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
    containingDirectory: VirtualFile,
    pathElements: Array<JSParsedPathElement>,
    isLastPathDirectory: Boolean,
  ): JSPathResolution {
    val routingType = when (containingDirectory.name) {
      RoutingType.APP.directoryName -> RoutingType.APP
      RoutingType.PAGES.directoryName -> RoutingType.PAGES
      else -> RoutingType.NO_ROUTING
    }

    if (routingType == RoutingType.NO_ROUTING) {
      return JSPathResolution.EMPTY
    }

    var potentialResults = mutableListOf<ResolutionResult>()
    pathElements.forEachIndexed { i, pathElement ->
      val name = pathElement.text
      if (name.startsWith("$"))
        return@forEachIndexed

      val isLast = pathElements.lastIndex == i

      if (i == 0) {
        processElement(name, containingDirectory, containingDirectory.children, potentialResults)
      }
      else if (!isLast || !isLastPathDirectory) {
        potentialResults.toMutableList().apply {
          potentialResults.forEach {
            val lastMatched = it.lastMatched
            processElement(name, lastMatched, lastMatched.children, this)
          }
          potentialResults = this
        }

      }

      if (isLast && !isLastPathDirectory) {
        potentialResults.forEach { result ->
          val resultLast = result.lastMatched
          if (resultLast.isDirectory) {
            val children = resultLast.children
            val leafPath = children.find { child -> child.isPathLeaf(routingType) }
            if (leafPath != null) {
              result.replaceLast(leafPath)
              return@forEach
            }
          }
        }
      }

      if (potentialResults.isEmpty()) return JSPathResolution.EMPTY
    }
    val mostSizedResults = potentialResults
      .groupBy { it.resultSize }
      .maxByOrNull { it.key }?.value
    val mostExactlySizedResult = mostSizedResults
                                   ?.maxByOrNull { it.exactMatchedSize + if (it.lastMatched.isFile) 1 else 0 }
                                   ?.result ?: emptyList()
    return JSPathResolution(mostExactlySizedResult, mostExactlySizedResult.size == pathElements.size, false)
  }

  private fun processElement(
    name: String,
    directory: VirtualFile,
    children: Array<VirtualFile>,
    results: MutableList<ResolutionResult>,
  ) {
    val groupOrSlotChildren = mutableListOf<VirtualFile>()
    val slugChildren = mutableListOf<VirtualFile>()
    var childByName: VirtualFile? = null
    children.forEach { child ->
      val childName = child.name
      val childNameWithoutExt = child.nameWithoutExtension
      if (childName.startsWith("_")/* || !child.isDirectory*/) return@forEach
      when {
        childName == name || childNameWithoutExt == name || childName.isInterceptingName(name) -> childByName = child
        childName.startsWith("[") -> slugChildren.add(child)
        groupOrSlotNamePattern.matches(childName) -> groupOrSlotChildren.add(child)
      }
    }

    if (groupOrSlotChildren.isNotEmpty()) {
      groupOrSlotChildren.forEach {
        processElement(name, directory, it.children, results)
      }
    }

    if (slugChildren.isNotEmpty()) {
      slugChildren.forEach {
        updateResult(directory, it, results, false)
      }
    }

    childByName?.apply {
      updateResult(directory, this, results, true)
    }
  }

  private fun updateResult(prevChild: VirtualFile, child: VirtualFile, results: MutableList<ResolutionResult>, exactMatch: Boolean) {
    val mappedResult = results.find { it.containsAsLast(prevChild) }
    if (mappedResult != null) {
      mappedResult.add(child, exactMatch)
      return
    }

    val containedResult = results.find { it.contains(prevChild) }
    if (containedResult != null) {
      results.add(containedResult.copyWithReplaceLast(child, exactMatch))
    }
    else {
      ResolutionResult().apply {
        add(child, exactMatch)
        results.add(this)
      }
    }
  }

  private fun VirtualFile.isPathLeaf(routingType: RoutingType): Boolean {
    if (isDirectory) return false
    val myName = nameWithoutExtension
    return routingType == RoutingType.APP && appRouterLeafFile == myName
  }

  private fun String.isInterceptingName(name: String): Boolean {
    val beforeName = substringBefore(name)
    return beforeName.contains(".") && beforeName.startsWith("(") && beforeName.endsWith(")")
  }

  enum class RoutingType(val directoryName: String) {
    PAGES("pages"), APP("app"), NO_ROUTING("");
  }

  private class ResolutionResult(
    private val files: MutableList<Pair<VirtualFile, Boolean>> = mutableListOf(),
  ) {
    val lastMatched: VirtualFile
      get() = files.map { it.first }.last()
    val exactMatchedSize: Int
      get() = files.filter { it.second }.size
    val resultSize: Int
      get() = files.size
    val result: List<VirtualFile>
      get() = files.map { it.first }

    fun add(file: VirtualFile, isExactMatch: Boolean) {
      files.add(Pair(file, isExactMatch))
    }

    fun contains(file: VirtualFile): Boolean {
      return files.any { it.first == file }
    }

    fun containsAsLast(file: VirtualFile): Boolean {
      return files.indexOfLast { it.first == file } == files.lastIndex
    }

    fun replaceLast(file: VirtualFile) {
      files[files.lastIndex] = Pair(file, true)
    }

    fun copyWithReplaceLast(file: VirtualFile, isExactMatch: Boolean): ResolutionResult {
      val newFiles = files.toMutableList()
      newFiles[newFiles.lastIndex] = Pair(file, isExactMatch)
      return ResolutionResult(newFiles)
    }
  }
}

private val groupOrSlotNamePattern = Regex("(\\(.*?\\))|(@\\w+)")