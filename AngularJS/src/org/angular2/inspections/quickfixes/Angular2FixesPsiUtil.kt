// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.lang.javascript.JSTokenTypes.COMMA
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.refactoring.FormatFixer
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2InjectionUtils
import org.angular2.entities.Angular2ImportsOwner

object Angular2FixesPsiUtil {

  fun insertEntityDecoratorMember(module: Angular2ImportsOwner, propertyName: String, name: String): Boolean {
    val decorator = module.decorator ?: return false
    val initializer = Angular2DecoratorUtil.getObjectLiteralInitializer(decorator) ?: return false
    var targetListProp = initializer.findProperty(propertyName)
    if (targetListProp == null) {
      reformatJSObjectLiteralProperty(
        insertJSObjectLiteralProperty(initializer, propertyName, "[\n$name\n]")
      )
    }
    else {
      var propValue = targetListProp.value ?: return false
      if (propValue !is JSArrayLiteralExpression) {
        val newProperty = JSChangeUtil.createObjectLiteralPropertyFromText(
          propertyName + ": [\n" + propValue.text + "\n]", initializer) as JSProperty
        targetListProp = targetListProp.replace(newProperty) as JSProperty
        propValue = targetListProp.value!!
        assert(propValue is JSArrayLiteralExpression)
      }

      val newModuleIdent = JSChangeUtil.createExpressionPsiWithContext(name, propValue, JSReferenceExpression::class.java)!!
      insertNewLinesAroundArrayItemIfNeeded(
        propValue.addAfter(newModuleIdent,
                           ((propValue as JSArrayLiteralExpression).expressions.lastOrNull() ?: propValue.firstChild)) as JSExpression)
      FormatFixer.create(targetListProp, FormatFixer.Mode.Reformat).fixFormat()
    }
    return true
  }

  fun insertJSObjectLiteralProperty(objectLiteral: JSObjectLiteralExpression,
                                    propertyName: String,
                                    propertyValue: String): JSProperty {
    val property = JSChangeUtil.createObjectLiteralPropertyFromText("$propertyName: $propertyValue", objectLiteral)
    val added = JSRefactoringUtil.addMemberToMemberHolder(objectLiteral, property, objectLiteral) as JSProperty
    insertNewLinesAroundPropertyIfNeeded(added)
    return added
  }

  fun reformatJSObjectLiteralProperty(property: JSProperty): JSProperty {
    val propertyPointer = SmartPointerManager.createPointer(property)
    FormatFixer.create(property.parent, FormatFixer.Mode.Reformat).fixFormat()
    val formattedProperty = propertyPointer.element!!

    val documentManager = PsiDocumentManager.getInstance(formattedProperty.project)
    val document = documentManager.getDocument(formattedProperty.containingFile)!!
    documentManager.commitDocument(document)
    val htmlContent = Angular2InjectionUtils.getFirstInjectedFile(formattedProperty.value)
    if (htmlContent != null) {
      FormatFixer.create(htmlContent, FormatFixer.Mode.Reformat).fixFormat()
    }
    return propertyPointer.element!!
  }

  private fun insertNewLinesAroundArrayItemIfNeeded(expression: JSExpression) {
    insertNewLinesAroundItemHolderIfNeeded(expression) { (it as JSArrayLiteralExpression).expressions }
  }

  private fun insertNewLinesAroundPropertyIfNeeded(property: JSProperty) {
    insertNewLinesAroundItemHolderIfNeeded(property) { (it as JSObjectLiteralExpression).properties }
  }

  private fun insertNewLinesAroundItemHolderIfNeeded(item: JSElement,
                                                     getChildren: (PsiElement) -> Array<out JSElement>) {
    val parent = item.parent
    val wrapWithNewLines = item.text.contains("\n")
                           || getChildren(parent).find { e -> e !== item && !isPrefixedWithNewLine(e) } == null
    if (wrapWithNewLines) {
      if (!isPrefixedWithNewLine(item)) {
        JSChangeUtil.addWs(parent.node, item.node, "\n")
      }
      val comma = findCommaOrBracket(item)
      if (comma != null) {
        val next = comma.nextSibling
        if (next !is PsiWhiteSpace || !next.getText().contains("\n")) {
          JSChangeUtil.addWsAfter(parent, comma, "\n")
        }
      }
      else {
        var e: PsiElement? = item.nextSibling
        while (e != null) {
          if (e is PsiWhiteSpace && e.text.contains("\n")) {
            return
          }
          e = e.nextSibling
        }
        JSChangeUtil.addWsAfter(parent, item, "\n")
      }
    }
  }

  private fun isPrefixedWithNewLine(property: JSElement): Boolean {
    val whiteSpace = property.prevSibling as? PsiWhiteSpace
    return whiteSpace != null && whiteSpace.text.contains("\n")
  }

  private fun findCommaOrBracket(property: JSElement): LeafPsiElement? {
    val el = PsiTreeUtil.skipWhitespacesForward(property)
    return el?.asSafely<LeafPsiElement>()?.takeIf { it.node.elementType == COMMA }
  }
}
