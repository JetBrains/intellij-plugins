package org.angular2.entities

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

interface Angular2EntitiesSource {

  fun getEntity(element: PsiElement): Angular2Entity?

  fun getAllModules(project: Project): Collection<Angular2Module>

  fun getAllPipeNames(project: Project): Collection<String>

  fun findDirectivesCandidates(project: Project, indexLookupName: String): List<Angular2Directive>

  fun findPipes(project: Project, name: String): Collection<Angular2Pipe>

}