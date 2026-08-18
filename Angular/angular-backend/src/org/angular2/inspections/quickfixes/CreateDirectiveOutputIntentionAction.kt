// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeArgumentList
import com.intellij.lang.javascript.validation.fixes.BaseCreateMemberModCommandFix
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModTemplateBuilder
import com.intellij.modcommand.Presentation
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil.OUTPUT_DEC
import org.angular2.codeInsight.template.Angular2StandardSymbolsScopesProvider
import org.angular2.entities.source.Angular2SourceUtil
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE
import org.angular2.lang.Angular2LangUtil.EVENT_EMITTER
import org.angular2.lang.expr.psi.Angular2Action

/**
 * Adds an `@Output()` `EventEmitter` for an event binding that no directive declares.
 *
 * Inferring the emitted type needs the TypeScript service, which may not be called on the EDT, hence
 * [BaseCreateMemberModCommandFix] rather than `BaseCreateDirectiveInputOutputAction`.
 */
class CreateDirectiveOutputIntentionAction(
  element: PsiElement,
  private val referencedName: String,
) : BaseCreateMemberModCommandFix<PsiElement>(element) {

  override fun getFamilyName(): String =
    Angular2Bundle.message("angular.quickfix.template.create-output.name", referencedName)

  override val chooseTargetTitle: String
    get() = Angular2Bundle.message("angular.quickfix.template.popup.choose-target-class")

  override fun getPresentation(context: ActionContext, element: PsiElement): Presentation? =
    if (getTargetClasses(element).isEmpty()) null else Presentation.of(familyName)

  override fun getTargetClasses(element: PsiElement): List<JSClass> =
    when (element) {
      is XmlAttribute -> BaseCreateDirectiveInputOutputAction.findTargetClasses(element)
      is JSReferenceExpression ->
        if (findEmitCallExpression(element) == null) emptyList()
        else listOfNotNull(Angular2SourceUtil.findComponentClass(element)?.takeIf { !JSProjectUtil.isInLibrary(it) })
      else -> emptyList()
    }

  override fun buildMember(builder: MemberTextBuilder, element: PsiElement, target: JSClass) {
    addDecoratedField(builder)
    builder.addTextSegment(" = new EventEmitter<")
    // runs off the EDT, so inferType() may use the TypeScript service
    builder.addTextSegment(inferType(element)?.getTypeText(JSType.TypeTextFormat.CODE) ?: "")
    builder.addTextSegment(">()")
    builder.addSemicolonSegment(target)
  }

  /** Offers completion for the emitted type when it could not be inferred, mirroring `BaseCreateFix.addCompletionVar`. */
  override fun addTemplateFields(member: PsiElement, builder: ModTemplateBuilder): Boolean {
    val typeArguments = PsiTreeUtil.findChildOfType(member, TypeScriptTypeArgumentList::class.java) ?: return false
    if (typeArguments.typeArguments.isNotEmpty()) return false
    builder.field(typeArguments, TextRange.from(typeArguments.textLength - 1, 0), "__type", ConstantNode(""))
    return true
  }

  override fun updateTargetFile(target: JSClass) {
    Angular2FixesPsiUtil.insertJSImport(target, ANGULAR_CORE_PACKAGE, OUTPUT_DEC)
    Angular2FixesPsiUtil.insertJSImport(target, ANGULAR_CORE_PACKAGE, EVENT_EMITTER)
  }

  /** Mirrors `BaseCreateDirectiveInputOutputAction.addDecoratedField`. */
  private fun addDecoratedField(builder: MemberTextBuilder) {
    if (StringUtil.isJavaIdentifier(referencedName)) {
      builder.addTextSegment("@$OUTPUT_DEC() $referencedName")
    }
    else {
      builder.addTextSegment("@$OUTPUT_DEC('$referencedName') ")
      builder.addTextSegment(JSStringUtil.toCamelCase(referencedName.replace(Regex("[^a-zA-Z_0-9]"), "_"))
                               .let { if (it[0].isDigit()) "_$it" else it })
    }
  }

  private fun inferType(context: PsiElement?): JSType? =
    when (context) {
      is JSReferenceExpression -> findEmitCallExpression(context)
        ?.arguments?.getOrNull(0)
        ?.let { JSResolveUtil.getElementJSType(it) }
      is XmlAttribute -> SmartList<JSType>().also { result ->
        Angular2Action.get(context)?.acceptChildren(object : JSRecursiveWalkingElementVisitor() {
          override fun visitJSReferenceExpression(node: JSReferenceExpression) {
            if (node.referenceName == Angular2StandardSymbolsScopesProvider.`$EVENT` && node.qualifier == null) {
              JSDialectSpecificHandlersFactory.findExpectedType(node)
                ?.takeIf { !JSTypeUtils.isAnyType(it) && !JSTypeUtils.isNullOrUndefinedType(it) }
                ?.let(result::add)
            }
            super.visitJSReferenceExpression(node)
          }
        })
      }.let { JSCompositeTypeFactory.createIntersectionType(it, JSTypeSource.EMPTY_TS_EXPLICITLY_DECLARED) }
      else -> null
    }

  private fun findEmitCallExpression(reference: JSReferenceExpression): JSCallExpression? =
    reference
      .parent.asSafely<JSReferenceExpression>()
      ?.takeIf { it.referenceName == "emit" }
      ?.parent?.asSafely<JSCallExpression>()
      ?.takeIf { it.argumentSize == 1 }
}
