package org.angular2.codeInsight.attributes

import com.intellij.javascript.findArgumentList
import com.intellij.lang.documentation.QuickDocHighlightingHelper
import com.intellij.lang.javascript.documentation.JSHtmlHighlightingUtil
import com.intellij.lang.javascript.psi.JSArgumentsHolder
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.parameterInfo.CreateParameterInfoContext
import com.intellij.lang.parameterInfo.ParameterInfoHandlerWithTabActionSupport
import com.intellij.lang.parameterInfo.ParameterInfoUIContext
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.ui.ColorHexUtil
import com.intellij.ui.ColorUtil
import com.intellij.util.containers.addIfNotNull
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind.*
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.codeInsight.attributes.Angular2TemplateBindingsParameterInfoHandler.Angular2TemplateBindingsParamInfo
import org.angular2.directiveInputToTemplateBindingVar
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings
import org.angular2.web.scopes.TemplateBindingKeyScope.Companion.getDirectiveInputsFor
import java.awt.Color

class Angular2TemplateBindingsParameterInfoHandler : ParameterInfoHandlerWithTabActionSupport<Angular2HtmlTemplateBindings, Angular2TemplateBindingsParamInfo, Angular2TemplateBinding> {

  override fun getActualParameters(o: Angular2HtmlTemplateBindings): Array<out Angular2TemplateBinding> =
    o.bindings.bindings.filter {
      it.textRange.length > 0 && it.keyKind != Angular2TemplateBinding.KeyKind.AS
    }.toTypedArray()

  override fun getActualParameterDelimiterType(): IElementType =
    TokenType.WHITE_SPACE

  override fun getActualParametersRBraceType(): IElementType =
    TokenType.WHITE_SPACE //none

  override fun getArgumentListAllowedParentClasses(): Set<Class<*>> =
    setOf(PsiElement::class.java)

  override fun getArgListStopSearchClasses(): Set<Class<*>> =
    setOf(JSFunction::class.java, JSArgumentsHolder::class.java)

  override fun getArgumentListClass(): Class<Angular2HtmlTemplateBindings> =
    Angular2HtmlTemplateBindings::class.java

  override fun findElementForParameterInfo(context: CreateParameterInfoContext): Angular2HtmlTemplateBindings? {
    val bindings = findTemplateBindings(context.file, context.offset) ?: return null
    val result = buildParametersList(bindings).takeIf { it.isNotEmpty() } ?: return null
    context.itemsToShow = arrayOf(Angular2TemplateBindingsParamInfo(result, bindings.containingFile.modificationStamp))
    return bindings
  }

  override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): Angular2HtmlTemplateBindings? =
    findTemplateBindings(context.file, context.offset)

  override fun showParameterInfo(element: Angular2HtmlTemplateBindings, context: CreateParameterInfoContext) {
    context.showHint(element, element.textOffset, this)
  }

  override fun updateParameterInfo(parameterOwner: Angular2HtmlTemplateBindings, context: UpdateParameterInfoContext) {
    (context.objectsToView.getOrNull(0) as? Angular2TemplateBindingsParamInfo)
      ?.let {
        if (it.modCount != parameterOwner.containingFile.modificationStamp) {
          it.bindings = buildParametersList(parameterOwner)
          it.modCount = parameterOwner.containingFile.modificationStamp
        }
      }
    context.setCurrentParameter(context.offset)
  }

  override fun updateUI(presentation: Angular2TemplateBindingsParamInfo, context: ParameterInfoUIContext) {
    val currentParameter = presentation.bindings
      .indexOfLast { context.currentParameterIndex >= (it.range?.startOffset ?: Int.MAX_VALUE) }

    val backgroundColor = context.defaultParameterColor
    val alpha = if (ColorUtil.isDark(backgroundColor)) 0.6 else 0.9

    @Suppress("HardCodedStringLiteral")
    val text = presentation.bindings
      .mapIndexed { index, binding ->
        if (index == currentParameter)
          "<b>${binding.text}</b>"
        else
          blendColors(binding.text, backgroundColor, alpha)
      }
      .joinToString(blendColors(wrapWithTextColor("; "), backgroundColor, alpha))
      .replace(Regex("</?a([^a-zA-Z>][^>]*>|>)"), "")
    context.setupRawUIComponentPresentation(text)
  }

  private fun blendColors(text: String, background: Color, alpha: Double): String {
    return text.replace(Regex("color:#([0-9a-f]+);")) {
      val colorText = it.groupValues.getOrNull(1) ?: return@replace it.value
      val color = ColorHexUtil.fromHex(colorText)
      "color: #${ColorUtil.toHex(ColorUtil.blendColorsInRgb(background, color, alpha))};"
    }
  }

  private fun findTemplateBindings(file: PsiFile?, offset: Int): Angular2HtmlTemplateBindings? {
    if (file == null || findArgumentList(file, offset) != null) return null
    var element = file.findElementAt(offset)
    if (element is PsiWhiteSpace) element = PsiTreeUtil.prevLeaf(element)
    return element?.parentOfType<Angular2HtmlTemplateBindings>()
  }

  private fun buildParametersList(bindings: Angular2HtmlTemplateBindings): MutableList<Angular2BindingPresentation> {
    val templateName = bindings.templateName

    val inputDefinitions = getDirectiveInputsFor(bindings.bindings)
      .associateTo(mutableMapOf()) { input ->
        val bindingName = directiveInputToTemplateBindingVar(input.name, templateName)
        val definition = input.createDocumentation(bindings.bindings)?.definition
          ?.replaceFirst("${input.name}:", bindingName.withColor(NG_INPUT, bindings) + (if (input.required) "" else "?") + ":")
        Pair(input.name, definition)
      }

    val result = mutableListOf<Angular2BindingPresentation>()
    result.addIfNotNull(
      inputDefinitions.remove(templateName)
        ?.let { Angular2BindingPresentation("&lt;expr&gt;$it", TextRange(0, 0)) }
    )

    bindings.bindings.bindings
      .mapNotNullTo(result) {
        if (it.key == templateName)
          null
        else when (it.keyKind) {
          Angular2TemplateBinding.KeyKind.LET -> {
            Angular2BindingPresentation("let".withColor(TS_KEYWORD, bindings) + " " + it.key.withColor(NG_TEMPLATE_VARIABLE, bindings) +
                                        renderJSType(it.variableDefinition), it.textRange)
          }
          Angular2TemplateBinding.KeyKind.BINDING -> {
            val inputDef = inputDefinitions.remove(it.key)
            if (inputDef != null) {
              Angular2BindingPresentation(inputDef, it.textRange)
            }
            else {
              Angular2BindingPresentation(directiveInputToTemplateBindingVar(it.key, templateName).withColor(NG_INPUT, bindings) + ": " +
                                          "&lt;unknown&gt;".withColor(UNUSED, bindings), it.textRange)
            }
          }
          Angular2TemplateBinding.KeyKind.AS -> null
        }
      }

    inputDefinitions.values.mapTo(result) {
      Angular2BindingPresentation("[$it]", null)
    }
    return result
  }

  class Angular2TemplateBindingsParamInfo(
    var bindings: List<Angular2BindingPresentation>,
    var modCount: Long,
  )

  class Angular2BindingPresentation(text: String, val range: TextRange?) {
    val text: String = wrapWithTextColor(text)
  }
}

private fun renderJSType(variable: JSVariable?): String {
  return JSHtmlHighlightingUtil.getElementHtmlHighlighting(variable ?: return "", "name", variable.jsType ?: return "")
    .toString()
    .replaceFirst("name", "")
}

private fun wrapWithTextColor(text: String): String = QuickDocHighlightingHelper.getStyledFragment(
  text, TextAttributes().apply { foregroundColor = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(HighlighterColors.TEXT).foregroundColor })
