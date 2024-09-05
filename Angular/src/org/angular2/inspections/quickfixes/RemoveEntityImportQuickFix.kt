package org.angular2.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.openapi.project.Project
import com.intellij.psi.util.parentOfType
import com.intellij.util.asSafely
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil.removeReferenceFromImportsList
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

class RemoveEntityImportQuickFix(private val name: String?) : LocalQuickFix {

  @Nls
  override fun getName(): String {
    return if (name != null)
      Angular2Bundle.message("angular.quickfix.remove-import.name", name)
    else
      Angular2Bundle.message("angular.quickfix.remove-import.family")
  }

  @Nls
  override fun getFamilyName(): String {
    return Angular2Bundle.message("angular.quickfix.remove-import.family")
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val reference = descriptor.psiElement
                      .asSafely<JSReferenceExpression>()
                      ?.takeIf { it.qualifier == null }
                    ?: return
    if (reference.referenceName == null
        || Angular2EntitiesProvider.getEntity(reference.parentOfType<ES6Decorator>()) == null) {
      return
    }

    removeReferenceFromImportsList(reference)
  }

}