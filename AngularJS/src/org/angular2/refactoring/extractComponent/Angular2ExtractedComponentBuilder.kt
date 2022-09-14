// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring.extractComponent

import com.intellij.lang.ecmascript6.refactoring.ES6ReferenceExpressionsInfo
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parents
import com.intellij.psi.xml.XmlTag
import org.angular2.codeInsight.template.Angular2StandardSymbolsScopesProvider.`$EVENT`
import org.angular2.codeInsight.template.Angular2TemplateScopesResolver
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil.OUTPUT_CHANGE_SUFFIX
import org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression
import org.angular2.lang.expr.psi.Angular2Interpolation
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression
import org.angular2.lang.expr.psi.Angular2RecursiveVisitor
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.*
import org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.Companion.ELEMENT_NG_CONTENT


class Angular2ExtractedComponentBuilder(private val sourceFile: PsiFile, private val selectionStart: Int, private val selectionEnd: Int) {
  private var extractedRange: TextRange = TextRange.EMPTY_RANGE
  private var enclosingTag: XmlTag? = null

  private val nameClashes = mutableMapOf<String, Int>()

  private fun deduplicate(name: String): String {
    var counter = nameClashes[name]
    if (counter != null) {
      counter++
      nameClashes[name] = counter
      return "${name}_$counter"
    }
    else {
      nameClashes[name] = 0
      return name
    }
  }

  private fun isInsideInterpolation(element: PsiElement): Boolean {
    val elementType = element.node.elementType
    if (elementType == Angular2HtmlTokenTypes.INTERPOLATION_START || elementType == Angular2HtmlTokenTypes.INTERPOLATION_END) return true
    for (parent in element.parents(false)) {
      if (parent.node.elementType == Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR) return true
    }
    return false
  }

  fun build(): Angular2ExtractedComponent {
    try {
      val startElement = sourceFile.findElementAt(selectionStart)!!
      val endElement = sourceFile.findElementAt(selectionEnd) ?: sourceFile.findElementAt(selectionEnd - 1)!!

      if (isInsideInterpolation(startElement) || isInsideInterpolation(endElement)) {
        throw IllegalStateException()
      }

      val tag = PsiTreeUtil.findCommonParent(startElement, endElement)?.parentOfType<XmlTag>(true)

      extractedRange = when {
        selectionStart != selectionEnd -> TextRange(selectionStart, selectionEnd)
        tag != null -> tag.textRange
        else -> throw IllegalStateException()
      }

      enclosingTag = when {
        tag == null || tag.value.textRange.containsRange(selectionStart, selectionEnd) -> tag
        else -> {
          // Allows rest of the code to always treat enclosingTag as one outside selection
          tag.parentTag
        }
      }
    }
    catch (e: Exception) {
      throw Angular2ExtractComponentUnsupportedException(
        Angular2Bundle.message("angular.refactor.extractComponent.unsupported-selection"))
    }

    return doBuild()
  }

  @Throws(Angular2ExtractComponentUnsupportedException::class)
  private fun doBuild(): Angular2ExtractedComponent {
    val templateStartOffset = extractedRange.startOffset

    val propertyBindings = mutableMapOf<String, Attr>()

    val attributes = mutableListOf<Attr>()
    val infos = mutableListOf<ES6ReferenceExpressionsInfo>()
    val replacements = mutableListOf<Replacement>()

    (enclosingTag ?: sourceFile).acceptChildren(object : Angular2HtmlRecursiveElementVisitor() {
      override fun visitElement(element: PsiElement) {
        if (element.node.elementType == Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR) {
          var grandChild = element.firstChild
          while (grandChild != null) {
            if (grandChild is Angular2Interpolation) {
              pseudoVisitTextInterpolation(grandChild)
              return
            }
            grandChild = grandChild.nextSibling
          }
        }

        super.visitElement(element)
      }

      override fun visitXmlTag(tag: XmlTag) {
        if (!extractedRange.intersects(tag.textRange)) return

        if (!extractedRange.contains(tag.textRange)) {
          throw Angular2ExtractComponentUnsupportedException(
            Angular2Bundle.message("angular.refactor.extractComponent.unsupported-selection"))
        }


        if (tag.name == ELEMENT_NG_CONTENT) {
          throw Angular2ExtractComponentUnsupportedException(
            Angular2Bundle.message("angular.refactor.extractComponent.unsupported-ng-content"))
        }

        super.visitXmlTag(tag)
      }

      override fun visitExpansionForm(expansion: Angular2HtmlExpansionForm) {
        if (!extractedRange.contains(expansion.textRange)) return
        throw Angular2ExtractComponentUnsupportedException(Angular2Bundle.message("angular.refactor.extractComponent.unsupported-i18n"))
      }

      override fun visitTemplateBindings(bindings: Angular2HtmlTemplateBindings) {
        if (!extractedRange.contains(bindings.textRange)) return

        handleEmbeddedExpression(bindings.bindings)
      }

      override fun visitPropertyBinding(propertyBinding: Angular2HtmlPropertyBinding) {
        if (!extractedRange.contains(propertyBinding.textRange)) return

        if (propertyBinding.bindingType == PropertyBindingType.ANIMATION) {
          throw Angular2ExtractComponentUnsupportedException(Angular2Bundle.message("angular.refactor.extractComponent.animations"))
        }

        val binding = propertyBinding.binding
        if (binding != null) {
          handleEmbeddedExpression(binding)
        }
        else {
          for (interpolation in propertyBinding.interpolations) {
            handleEmbeddedExpression(interpolation)
          }
        }
      }

      override fun visitEvent(event: Angular2HtmlEvent) {
        if (!extractedRange.contains(event.textRange)) return

        if (event.eventType == Angular2HtmlEvent.EventType.ANIMATION) {
          throw Angular2ExtractComponentUnsupportedException(Angular2Bundle.message("angular.refactor.extractComponent.animations"))
        }

        val embeddedExpression = event.action
        if (embeddedExpression == null) return

        handleEmbeddedExpression(embeddedExpression)
      }

      override fun visitBananaBoxBinding(bananaBoxBinding: Angular2HtmlBananaBoxBinding) {
        if (!extractedRange.contains(bananaBoxBinding.textRange)) return

        val expression = bananaBoxBinding.binding?.expression
        if (expression !is JSReferenceExpression) return // empty expression or syntax error
        if (!isReferenceReferencingOuterScope(findUnqualifiedReference(expression))) return

        val name = deduplicate(bananaBoxBinding.propertyName)
        val replacement = let {
          val inputName = bananaBoxBinding.propertyName
          val outputName = bananaBoxBinding.propertyName + OUTPUT_CHANGE_SUFFIX
          val replacedText = """
            ${Angular2AttributeType.PROPERTY_BINDING.buildName(inputName)}="$name" 
            ${Angular2AttributeType.EVENT.buildName(outputName)}="$name$OUTPUT_CHANGE_SUFFIX.emit($`$EVENT`)"
          """.trimIndent()
          Replacement(bananaBoxBinding.textRange.shiftLeft(templateStartOffset), replacedText)
        }
        val jsType = processElementJSType(bananaBoxBinding.binding?.expression)

        attributes.add(Attr(name = name, jsType = jsType, assignedValue = bananaBoxBinding.value!!,
                            attributeType = Angular2AttributeType.BANANA_BOX_BINDING))
        replacements.add(replacement)
      }

      fun pseudoVisitTextInterpolation(interpolation: Angular2Interpolation) {
        if (!extractedRange.contains(interpolation.textRange)) return

        handleEmbeddedExpression(interpolation)
      }

      fun handleEmbeddedExpression(embeddedExpression: Angular2EmbeddedExpression) {
        embeddedExpression.acceptChildren(object : Angular2RecursiveVisitor() {
          override fun visitJSReferenceExpression(reference: JSReferenceExpression) {
            val unqualifiedReference = findUnqualifiedReference(reference)

            if (reference is Angular2PipeReferenceExpression) return
            if (!isReferenceReferencingOuterScope(unqualifiedReference)) return

            val value = unqualifiedReference.text
            val name = deduplicate(value)

            var attr = propertyBindings[value]
            if (attr == null) {
              val jsType = processElementJSType(unqualifiedReference)
              attr = Attr(name = name, jsType = jsType, assignedValue = value, attributeType = Angular2AttributeType.PROPERTY_BINDING)

              attributes.add(attr)
              propertyBindings[value] = attr
            }

            val replacement = Replacement(unqualifiedReference.textRange.shiftLeft(templateStartOffset), attr.name)
            replacements.add(replacement)
          }
        })
      }

      fun processElementJSType(element: PsiElement?): JSType {
        val jsType = JSResolveUtil.getElementJSType(element, true)

        val source = jsType?.sourceElement
        if (source != null) {
          infos.add(ES6ReferenceExpressionsInfo.getInfo(source))
        }

        if (jsType != null && !jsType.isTypeScript) {
          return jsType.substitute().withNewSource(JSTypeSource.EMPTY_TS)
        }

        return jsType?.substitute() ?: JSAnyType.get(JSTypeSource.EMPTY_TS)
      }
    })

    return Angular2ExtractedComponent(extractedRange.substring(sourceFile.text), templateStartOffset, attributes, replacements, infos)
  }

  private fun isReferenceReferencingOuterScope(reference: JSReferenceExpression): Boolean {
    if (Angular2TemplateScopesResolver.isImplicitReferenceExpression(reference)) return false

    val source = reference.resolve()
    if (source == null) {
      // unknown, so it's better to assume it's from outer scope
      return true
    }
    else if (source.containingFile is JSFile) {
      return true
    }
    else if ((enclosingTag != null && !PsiTreeUtil.isContextAncestor(enclosingTag, source, true))
             || source.textRange?.let { extractedRange.contains(it) } == false) {
      return true
    }

    return false
  }

  private fun findUnqualifiedReference(reference: JSReferenceExpression): JSReferenceExpression {
    var unqualifiedReference = reference
    while (unqualifiedReference.qualifier is JSReferenceExpression) {
      unqualifiedReference = unqualifiedReference.qualifier as JSReferenceExpression
    }

    return unqualifiedReference
  }
}