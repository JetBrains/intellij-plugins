package org.angular2.codeInsight

import com.intellij.lang.documentation.QuickDocHighlightingHelper
import com.intellij.lang.parameterInfo.CreateParameterInfoContext
import com.intellij.lang.parameterInfo.ParameterInfoHandler
import com.intellij.lang.parameterInfo.ParameterInfoUIContext
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.ColorHexUtil
import com.intellij.ui.ColorUtil
import org.angular2.codeInsight.ParameterInfoHandlerWithColoredSyntax.ParameterInfoHandlerWithColoredSyntaxData
import java.awt.Color

abstract class ParameterInfoHandlerWithColoredSyntax<ParameterOwner : PsiElement> : ParameterInfoHandler<ParameterOwner, ParameterInfoHandlerWithColoredSyntaxData> {

  final override fun findElementForParameterInfo(context: CreateParameterInfoContext): ParameterOwner? {
    val bindings = findElementForBuildingParametersListInfo(context.file ?: return null, context.offset) ?: return null
    val result = buildParametersListInfo(bindings).takeIf { it.isNotEmpty() } ?: return null
    context.itemsToShow = arrayOf(ParameterInfoHandlerWithColoredSyntaxData(result, bindings.containingFile.modificationStamp))
    return bindings
  }

  final override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): ParameterOwner? {
    return findElementForBuildingParametersListInfo(context.file ?: return null, context.offset)
  }

  final override fun showParameterInfo(element: ParameterOwner, context: CreateParameterInfoContext) {
    context.showHint(element, element.textOffset, this)
  }

  final override fun updateParameterInfo(parameterOwner: ParameterOwner, context: UpdateParameterInfoContext) {
    (context.objectsToView.getOrNull(0) as? ParameterInfoHandlerWithColoredSyntaxData)
      ?.let {
        if (it.modCount != parameterOwner.containingFile.modificationStamp) {
          it.parameters = buildParametersListInfo(parameterOwner)
          it.modCount = parameterOwner.containingFile.modificationStamp
        }
      }
    context.setCurrentParameter(StringUtil.skipWhitespaceForward(context.editor.document.text, context.offset))
  }

  protected abstract fun findElementForBuildingParametersListInfo(file: PsiFile, offset: Int): ParameterOwner?

  protected abstract fun buildParametersListInfo(parameterOwner: ParameterOwner): List<ParameterHtmlPresentation>

  final override fun updateUI(presentation: ParameterInfoHandlerWithColoredSyntaxData, context: ParameterInfoUIContext) {
    val currentParameter = presentation.parameters
      .indexOfLast { context.currentParameterIndex >= (it.range?.startOffset ?: Int.MAX_VALUE) }

    val backgroundColor = context.defaultParameterColor
    val alpha = if (ColorUtil.isDark(backgroundColor)) 0.6 else 0.9

    @Suppress("HardCodedStringLiteral")
    val text = presentation.parameters
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

  private fun blendColors(text: String, background: Color, alpha: Double): String =
    text.replace(Regex("color:#([0-9a-f]+);")) {
      val colorText = it.groupValues.getOrNull(1) ?: return@replace it.value
      val color = ColorHexUtil.fromHex(colorText)
      "color: #${ColorUtil.toHex(ColorUtil.blendColorsInRgb(background, color, alpha))};"
    }

  class ParameterInfoHandlerWithColoredSyntaxData internal constructor(
    internal var parameters: List<ParameterHtmlPresentation>,
    internal var modCount: Long,
  )

  class ParameterHtmlPresentation(html: String, val range: TextRange?) {
    val text: String = wrapWithTextColor(html)
  }
}

private fun wrapWithTextColor(text: String): String =
  QuickDocHighlightingHelper.getStyledFragment(text, TextAttributes().apply {
    foregroundColor = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(HighlighterColors.TEXT).foregroundColor
  })
