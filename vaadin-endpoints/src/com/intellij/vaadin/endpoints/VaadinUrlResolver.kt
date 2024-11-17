// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.vaadin.endpoints

import com.intellij.microservices.jvm.cache.ModuleCacheValueHolder
import com.intellij.microservices.jvm.cache.SourceLibSearchProvider
import com.intellij.microservices.jvm.cache.UastCachedSearchUtils.sequenceWithCache
import com.intellij.microservices.url.*
import com.intellij.microservices.url.UrlPath.PathSegment
import com.intellij.microservices.url.references.UrlPksParser
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnchor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PartiallyKnownString
import com.intellij.psi.util.SplitEscaper
import javax.swing.Icon

internal class VaadinUrlResolverFactory : UrlResolverFactory {
  override fun forProject(project: Project): UrlResolver? {
    return if (hasVaadinFlow(project)) VaadinUrlResolver(project) else null
  }
}

internal class VaadinUrlResolver(private val project: Project) : UrlResolver {
  override val supportedSchemes: List<String>
    get() = HTTP_SCHEMES

  override fun getVariants(): Iterable<UrlTargetInfo> {
    return getAllModuleVariants(project)
        .asIterable()
  }

  override fun resolve(request: UrlResolveRequest): Iterable<UrlTargetInfo> {
    if (request.method != HttpMethods.GET) return emptyList()

    val allModuleVariants = getAllModuleVariants(project)
      .toList()

    return UrlPath.combinations(request.path)
      .flatMap { path ->
        allModuleVariants.asSequence()
          .filter { it.path.isCompatibleWith(path) }
      }
      .asIterable()
  }
}

internal val VAADIN_ROUTES_SEARCH: SourceLibSearchProvider<List<VaadinRoute>, Module> =
    SourceLibSearchProvider("VAADIN_ROUTES") { p, _, scope ->
      findVaadinRoutes(p, scope).toList()
    }

private fun getAllModuleVariants(project: Project): Sequence<VaadinUrlTargetInfo> {
  val modules = ModuleManager.getInstance(project).modules

  return modules.asSequence()
    .flatMap(::getVariants)
    .map(::VaadinUrlTargetInfo)
}

private fun getVariants(module: Module): Sequence<VaadinRoute> {
  if (!hasVaadinFlow(module)) return emptySequence()

  return sequenceWithCache(ModuleCacheValueHolder(module), VAADIN_ROUTES_SEARCH)
}

private class VaadinUrlTargetInfo(route: VaadinRoute) : UrlTargetInfo {
  private val anchor: PsiAnchor = route.anchor

  override val authorities: List<Authority>
    get() = emptyList()

  override val path: UrlPath = parseVaadinUrlMapping(route.urlMapping)

  override val icon: Icon
    get() = VaadinEndpointsIcons.VaadinRoute

  override val schemes: List<String>
    get() = HTTP_SCHEMES

  override fun resolveToPsiElement(): PsiElement? = anchor.retrieve()
}

internal val vaadinUrlPksParser: UrlPksParser = UrlPksParser(
    splitEscaper = { _, _ -> SplitEscaper.AcceptAll },
    customPathSegmentExtractor = { part ->
      if (part.startsWith(":")) {
        val varName = part.removePrefix(":")
            .substringBefore("?")
            .substringBefore("(")
        PathSegment.Variable(varName)
      } else {
        PathSegment.Exact(part)
      }
    }
)

internal fun parseVaadinUrlMapping(urlMapping: String): UrlPath {
  return vaadinUrlPksParser
      .parseUrlPath(PartiallyKnownString(urlMapping))
      .urlPath
}