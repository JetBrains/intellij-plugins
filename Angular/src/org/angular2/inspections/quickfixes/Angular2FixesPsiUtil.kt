// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.javascript.web.js.WebJSResolveUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.JSTokenTypes.COMMA
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.refactoring.FormatFixer
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.applyIf
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2InjectionUtils
import org.angular2.entities.Angular2ClassBasedEntity
import org.angular2.entities.Angular2ImportsOwner

object Angular2FixesPsiUtil {

  fun <T : PsiElement> T.remapToCopyIfNeeded(targetFile: PsiFile): T =
    if (targetFile.originalFile == this.containingFile)
      PsiTreeUtil.findSameElementInCopy(this, targetFile)
    else
      this

  fun getOrCreateInputObjectLiteral(inputDeclarationSource: PsiElement?): JSObjectLiteralExpression? =
    when (inputDeclarationSource) {
      is JSLiteralExpression -> {
        when (val parent = inputDeclarationSource.parent) {
          is JSArrayLiteralExpression, is JSArgumentList -> replaceInputMappingWithObjectLiteral(inputDeclarationSource)
          is JSProperty -> parent.parent as? JSObjectLiteralExpression
          else -> null
        }
      }
      is ES6Decorator -> getOrCreateDecoratorInitializer(inputDeclarationSource)
      is JSObjectLiteralExpression -> inputDeclarationSource
      else -> null
    }

  fun insertEntityDecoratorMember(module: Angular2ImportsOwner, propertyName: String, name: String): Boolean {
    val decorator = module.asSafely<Angular2ClassBasedEntity>()?.decorator ?: return false
    val initializer = getOrCreateDecoratorInitializer(decorator) ?: return false
    var targetListProp = initializer.findProperty(propertyName)
    if (targetListProp == null) {
      insertJSObjectLiteralProperty(initializer, propertyName, "[\n$name\n]")
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
                           ((propValue as JSArrayLiteralExpression).expressions.lastOrNull() ?: propValue.firstChild)) as JSExpression,
        preferNewLines = true)
      FormatFixer.create(targetListProp, FormatFixer.Mode.Reformat).fixFormat()
    }
    return true
  }

  fun insertJSObjectLiteralProperty(objectLiteral: JSObjectLiteralExpression,
                                    propertyName: String,
                                    propertyValue: String,
                                    reformat: Boolean = true,
                                    preferNewLines: Boolean = true): JSProperty {
    val property = JSChangeUtil.createObjectLiteralPropertyFromText("$propertyName: $propertyValue", objectLiteral)
    val added = JSRefactoringUtil.addMemberToMemberHolder(objectLiteral, property, objectLiteral) as JSProperty
    insertNewLinesAroundPropertyIfNeeded(added, preferNewLines)
    return added.applyIf(reformat) { reformatJSObjectLiteralProperty(this) }
  }

  fun insertJSImport(scope: PsiElement, module: String, name: String) {
    WebJSResolveUtil.resolveSymbolFromNodeModule(scope, module, name, PsiElement::class.java)
      ?.let { ES6ImportPsiUtil.insertJSImport(scope, name, it, null) }
  }

  fun removeReferenceFromImportsList(reference: JSReferenceExpression) {
    val toRemove = reference.parent.asSafely<JSSpreadExpression>() ?: reference
    val array = toRemove.parent.asSafely<JSArrayLiteralExpression>()
    if (array != null) {
      JSChangeUtil.removeRangeWithRemovalOfCommas(toRemove, array.expressions)
    }
    else {
      toRemove.delete()
    }
  }

  private fun reformatJSObjectLiteralProperty(property: JSProperty): JSProperty {
    val propertyPointer = SmartPointerManager.createPointer(property)
    FormatFixer.create(property.parent, FormatFixer.Mode.Reformat).fixFormat()
    val formattedProperty = propertyPointer.element!!

    val documentManager = PsiDocumentManager.getInstance(formattedProperty.project)
    val document = documentManager.getDocument(formattedProperty.containingFile)
                   ?: return propertyPointer.element!!
    documentManager.commitDocument(document)
    val htmlContent = Angular2InjectionUtils.getFirstInjectedFile(formattedProperty.value)
    if (htmlContent != null) {
      FormatFixer.create(htmlContent, FormatFixer.Mode.Reformat).fixFormat()
    }
    return propertyPointer.element!!
  }

  private fun getOrCreateDecoratorInitializer(decorator: ES6Decorator): JSObjectLiteralExpression? {
    Angular2DecoratorUtil.getObjectLiteralInitializer(decorator)?.let { return it }
    val objectLiteral = JSChangeUtil.createExpressionWithContext("{}", decorator)?.psi as? JSObjectLiteralExpression
                        ?: return null
    return decorator.expression.asSafely<JSCallExpression>()
      ?.takeIf { it.argumentSize == 0 }
      ?.argumentList
      ?.let { it.addAfter(objectLiteral, it.firstChild) }
      ?.asSafely<JSObjectLiteralExpression>()
  }

  private fun replaceInputMappingWithObjectLiteral(inputMapping: JSLiteralExpression): JSObjectLiteralExpression? {
    val mappingText = inputMapping.value as? String ?: return null
    val quote = JSCodeStyleSettings.getQuote(inputMapping)
    val isDecoratorArgument = inputMapping.parent is JSArgumentList
    val expression = if (!isDecoratorArgument && mappingText.contains(':')) {
      val colon = mappingText.indexOf(':')
      "{name: $quote${mappingText.substring(0, colon).trim()}$quote, alias: $quote${mappingText.substring(colon + 1).trim()}$quote}"
    }
    else {
      "{${if (isDecoratorArgument) "alias" else "name"}: $quote${mappingText.trim()}$quote}"
    }
    val objectLiteral = JSChangeUtil.createExpressionWithContext(expression, inputMapping)
                          ?.psi as? JSObjectLiteralExpression
                        ?: return null
    return inputMapping.replace(objectLiteral) as? JSObjectLiteralExpression
  }

  private fun insertNewLinesAroundArrayItemIfNeeded(expression: JSExpression, preferNewLines: Boolean) {
    insertNewLinesAroundItemHolderIfNeeded(expression, preferNewLines) { (it as JSArrayLiteralExpression).expressions }
  }

  private fun insertNewLinesAroundPropertyIfNeeded(property: JSProperty, preferNewLines: Boolean) {
    insertNewLinesAroundItemHolderIfNeeded(property, preferNewLines) { (it as JSObjectLiteralExpression).properties }
  }

  private fun insertNewLinesAroundItemHolderIfNeeded(item: JSElement,
                                                     preferNewLines: Boolean,
                                                     getChildren: (PsiElement) -> Array<out JSElement>) {
    val parent = item.parent
    val wrapWithNewLines = item.text.contains("\n")
                           || (preferNewLines && getChildren(parent).none { e -> e !== item && !isPrefixedWithNewLine(e) })
                           || (!preferNewLines && getChildren(parent).any { e -> e !== item && isPrefixedWithNewLine(e) })
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
