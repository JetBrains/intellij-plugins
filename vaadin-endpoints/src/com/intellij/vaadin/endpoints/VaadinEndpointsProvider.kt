// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.vaadin.endpoints

import com.intellij.microservices.endpoints.*
import com.intellij.microservices.endpoints.EndpointsProvider.Status
import com.intellij.microservices.endpoints.presentation.HttpUrlPresentation
import com.intellij.microservices.url.UrlPath
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.uast.UastModificationTracker

internal class VaadinEndpointsProvider : EndpointsProvider<VaadinRoute, VaadinRoute> {
  override val endpointType: EndpointType = HTTP_SERVER_TYPE

  override val presentation: FrameworkPresentation =
      FrameworkPresentation("Vaadin", "Vaadin Flow", VaadinEndpointsIcons.VaadinRoute)

  override fun getStatus(project: Project): Status {
    if (hasVaadinFlow(project)) return Status.HAS_ENDPOINTS

    return Status.UNAVAILABLE
  }

  override fun getModificationTracker(project: Project): ModificationTracker {
    return UastModificationTracker.getInstance(project)
  }

  override fun getEndpointGroups(project: Project, filter: EndpointsFilter): Iterable<VaadinRoute> {
    if (filter !is ModuleEndpointsFilter) return emptyList()
    if (!hasVaadinFlow(filter.module)) return emptyList()

    return findVaadinRoutes(project, filter.transitiveSearchScope)
  }

  override fun getEndpoints(group: VaadinRoute): Iterable<VaadinRoute> {
    return listOf(group)
  }

  override fun isValidEndpoint(group: VaadinRoute, endpoint: VaadinRoute): Boolean {
    return group.isValid()
  }

  override fun getEndpointPresentation(group: VaadinRoute, endpoint: VaadinRoute): ItemPresentation {
    return HttpUrlPresentation(normalizeUrl(group.urlMapping), group.locationString, VaadinEndpointsIcons.VaadinRoute)
  }

  private fun normalizeUrl(urlMapping: String): String {
    val urlString = run {
      if (urlMapping.isBlank()) return@run "/"
      if (!urlMapping.startsWith("/")) return@run "/$urlMapping"
      return@run urlMapping
    }

    return parseVaadinUrlMapping(urlString).getPresentation(VaadinUrlRenderer)
  }

  override fun getDocumentationElement(group: VaadinRoute, endpoint: VaadinRoute): PsiElement? {
    return endpoint.anchor.retrieve()
  }
}

private object VaadinUrlRenderer : UrlPath.PathSegmentRenderer {
  override fun visitVariable(variable: UrlPath.PathSegment.Variable): String {
    return "{${variable.variableName}}"
  }
}