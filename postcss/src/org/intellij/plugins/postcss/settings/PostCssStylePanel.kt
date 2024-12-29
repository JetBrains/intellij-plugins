package org.intellij.plugins.postcss.settings

import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.UIUtil
import org.intellij.plugins.postcss.PostCssBundle
import org.intellij.plugins.postcss.PostCssFileType
import javax.swing.BorderFactory
import javax.swing.JComponent

internal class PostCssStylePanel(settings: CodeStyleSettings) : CodeStyleAbstractPanel(settings) {
  private var isInlineStyle = true

  private val panel = panel {
    group(PostCssBundle.message("postcss.settings.comment.message")) {
      buttonsGroup {
        row {
          radioButton(PostCssBundle.message("postcss.settings.comment.inline"), true)
        }
        row {
          radioButton(PostCssBundle.message("postcss.settings.comment.block"), false)
        }
      }.bind(::isInlineStyle)
    }
  }.apply {
    border = BorderFactory.createEmptyBorder(UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP, UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP)
  }

  override fun getRightMargin(): Int = 0

  override fun createHighlighter(scheme: EditorColorsScheme): EditorHighlighter? = null

  override fun getFileType(): FileType = PostCssFileType.POST_CSS

  override fun getPreviewText(): String? = null

  override fun apply(settings: CodeStyleSettings) {
    panel.apply()
    settings.getCustomSettings(PostCssCodeStyleSettings::class.java).COMMENTS_INLINE_STYLE = isInlineStyle
  }

  override fun isModified(settings: CodeStyleSettings): Boolean {
    return settings.getCustomSettings(PostCssCodeStyleSettings::class.java).COMMENTS_INLINE_STYLE != isInlineStyle
  }

  override fun getPanel(): JComponent = panel

  override fun resetImpl(settings: CodeStyleSettings) {
    val postCssSettings = settings.getCustomSettings(PostCssCodeStyleSettings::class.java)
    isInlineStyle = postCssSettings.COMMENTS_INLINE_STYLE
    panel.reset()
  }
}
