package com.intellij.vaadin.endpoints

import com.intellij.jam.JavaLibraryUtils.hasLibraryClass
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnchor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.util.Processor
import org.jetbrains.uast.UClass
import org.jetbrains.uast.evaluateString
import org.jetbrains.uast.toUElementOfType

internal const val VAADIN_ROUTE = "com.vaadin.flow.router.Route"

internal fun hasVaadinFlow(project: Project): Boolean = hasLibraryClass(project, VAADIN_ROUTE)

internal fun hasVaadinFlow(module: Module): Boolean = hasLibraryClass(module, VAADIN_ROUTE)

internal fun findVaadinRoutes(project: Project, scope: GlobalSearchScope): Collection<VaadinRoute> {
  val vaadinRouteClass = JavaPsiFacade.getInstance(project)
      .findClass(VAADIN_ROUTE, ProjectScope.getLibrariesScope(project)) ?: return emptyList()

  val routes = ArrayList<VaadinRoute>()

  AnnotatedElementsSearch.searchPsiClasses(vaadinRouteClass, scope).forEach(Processor { psiClass ->
    val uClass = psiClass.toUElementOfType<UClass>()
    val sourcePsi = uClass?.sourcePsi
    val className = psiClass.name

    if (sourcePsi == null || className == null) return@Processor true
    val uAnnotation = uClass.findAnnotation(VAADIN_ROUTE) ?: return@Processor true

    val urlMapping = uAnnotation.findAttributeValue("value")?.evaluateString() ?: ""

    routes.add(VaadinRoute(urlMapping, className, PsiAnchor.create(sourcePsi)))

    true
  })

  return routes.toList()
}