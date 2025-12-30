// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.refactoring.FormatFixer
import com.intellij.openapi.project.Project
import com.intellij.psi.util.parentOfTypes
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.STANDALONE_PROP
import org.angular2.entities.Angular2ClassBasedEntity
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2EntityUtils
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

class ConvertToStandaloneNonStandaloneQuickFix(private val className: String, private val toStandalone: Boolean) : LocalQuickFix {

  @Nls
  override fun getName(): String {
    return Angular2Bundle.message(
      if (toStandalone) "angular.quickfix.standalone.convert-to-standalone.name"
      else "angular.quickfix.standalone.convert-to-non-standalone.name",
      className)
  }

  @Nls
  override fun getFamilyName(): String {
    return Angular2Bundle.message(
      if (toStandalone) "angular.quickfix.standalone.convert-to-standalone.family"
      else "angular.quickfix.standalone.convert-to-non-standalone.family",
    )
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val decorator = when (val element = descriptor.psiElement) {
                      is JSReferenceExpression -> Angular2EntitiesProvider.getEntity(element.resolve())
                      else -> Angular2EntitiesProvider.getEntity(element.parentOfTypes(ES6Decorator::class, TypeScriptClass::class))
                    }?.asSafely<Angular2ClassBasedEntity>()?.decorator ?: return

    val objectLiteral =
      Angular2DecoratorUtil.getObjectLiteralInitializer(decorator)
        ?.let { IntentionPreviewUtils.toPreviewElementIfPreviewFile(descriptor.psiElement.containingFile, it) }
      ?: return
    val standaloneByDefault = Angular2EntityUtils.isStandaloneDefault(decorator)
    val existing = objectLiteral.findProperty(STANDALONE_PROP)
      ?.let { IntentionPreviewUtils.toPreviewElementIfPreviewFile(descriptor.psiElement.containingFile, it) }
    if (standaloneByDefault == toStandalone) {
      if (existing != null) {
        val parent = existing.parent
        existing.delete()
        FormatFixer.create(parent, FormatFixer.Mode.Reformat).fixFormat()
      }
    }
    else {
      if (existing != null) {
        existing.replace(JSChangeUtil.createObjectLiteralPropertyFromText("$STANDALONE_PROP: $toStandalone", decorator))
          .let { FormatFixer.create(it, FormatFixer.Mode.Reformat).fixFormat() }
      }
      else {
        Angular2FixesPsiUtil.insertJSObjectLiteralProperty(objectLiteral, STANDALONE_PROP, "$toStandalone")
      }
    }
  }
}
