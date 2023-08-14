package com.intellij.lang.javascript.frameworks.nextjs.inspections

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.JSElementBase
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilCore
import com.intellij.webSymbols.context.WebSymbolsContext


class NextJsImplicitUsageProvider : ImplicitUsageProvider {

  override fun isImplicitUsage(element: PsiElement): Boolean =
    isInNextJsContext(element) && (
      isKnownFunctionName(element)
        || isKnownObjectName(element)
        || isExportDefault(element)
        || isInAppDir(element) && isRouteSegmentOption(element)
        || isHttpMethod(element)
        || isMiddlewareFunctionOrItsConfig(element)
    )

  private fun isInNextJsContext(element: PsiElement): Boolean =
    WebSymbolsContext.get("nextjs-project", element) == "nextjs"

  private fun isKnownFunctionName(element: PsiElement): Boolean =
    (element is JSElementBase)
      && element.isExported
      && (element is JSFunction || element is JSVariable)
      && KNOWN_FUNCTIONS.contains(element.name)

  private val KNOWN_FUNCTIONS = hashSetOf(
    "getServerSideProps", "getStaticPaths", "getStaticProps",
    "generateStaticParams", "generateMetadata", "generateImageMetadata"
  )

  private fun isKnownObjectName(element: PsiElement): Boolean =
    (element is JSVariable)
      && element.isExported
      && element.name == "metadata"

  private fun isExportDefault(element: PsiElement): Boolean = element is ES6ExportDefaultAssignment

  private fun isInAppDir(element: PsiElement): Boolean =
    PsiUtilCore.getVirtualFile(element)?.let{
      JSLibraryUtil.hasDirectoryInPath(it, "app", null)
    } == true

  private fun isRouteSegmentOption(element: PsiElement): Boolean =
    (element is JSVariable)
      && element.isExported
      && ROUTE_SEGMENT_OPTIONS.contains(element.name)

  private val ROUTE_SEGMENT_OPTIONS = hashSetOf("dynamic", "dynamicParams", "revalidate", "fetchCache", "runtime", "preferredRegion")

  private fun isHttpMethod(element: PsiElement): Boolean =
    element.containingFile?.name?.startsWith("route.") == true
      && (element is JSElementBase)
      && element.isExported
      && (element is JSFunction || element is JSVariable)
      && HTTP_METHOD_FUNCTIONS.contains(element.name)

  private val HTTP_METHOD_FUNCTIONS = hashSetOf("GET", "HEAD", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")

  private fun isMiddlewareFunctionOrItsConfig(element: PsiElement): Boolean =
    element.containingFile?.name?.startsWith("middleware.") == true
      && (element is JSElementBase)
      && element.isExported
      && (
        (element is JSFunction || element is JSVariable) && element.name == "middleware"
          || (element is JSVariable) && element.name == "config"
      )

  override fun isImplicitRead(element: PsiElement) = false
  override fun isImplicitWrite(element: PsiElement) = false
}