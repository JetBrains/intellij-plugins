package org.angular2.entities

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

interface Angular2EntitiesSource {

  fun getSupportedEntityPsiElements(): Collection<Class<out PsiElement>>

  fun getEntity(element: PsiElement): Angular2Entity?

  fun getAllModules(project: Project): Collection<Angular2Module>

  fun getAllPipeNames(project: Project): Collection<String>

  fun findDirectiveCandidates(project: Project, indexLookupName: String): Collection<Angular2Directive>

  fun findPipes(project: Project, name: String): Collection<Angular2Pipe>

  fun findTemplateComponent(templateContext: PsiElement): Angular2Component?

}