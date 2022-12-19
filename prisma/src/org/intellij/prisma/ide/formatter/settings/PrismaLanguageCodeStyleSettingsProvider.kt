package org.intellij.prisma.ide.formatter.settings

import com.intellij.application.options.*
import com.intellij.ide.highlighter.HighlighterFactory
import com.intellij.lang.Language
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.codeStyle.*
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable.CommenterOption
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider.SettingsType.COMMENTER_SETTINGS
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider.SettingsType.INDENT_SETTINGS
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.UIUtil
import org.intellij.prisma.PrismaBundle
import org.intellij.prisma.ide.highlighting.PrismaSyntaxHighlighter
import org.intellij.prisma.lang.PrismaFileType
import org.intellij.prisma.lang.PrismaLanguage
import javax.swing.BorderFactory
import javax.swing.JComponent

class PrismaLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
  override fun getLanguage(): Language = PrismaLanguage

  override fun getCodeSample(settingsType: SettingsType): String = when (settingsType) {
    INDENT_SETTINGS, COMMENTER_SETTINGS -> CODE_SAMPLE
    else -> ""
  }

  override fun getConfigurableDisplayName(): String = PrismaLanguage.displayName

  override fun createCustomSettings(settings: CodeStyleSettings): CustomCodeStyleSettings =
    PrismaCodeStyleSettings(settings)

  override fun createConfigurable(
    baseSettings: CodeStyleSettings,
    modelSettings: CodeStyleSettings
  ): CodeStyleConfigurable {
    return object : CodeStyleAbstractConfigurable(baseSettings, modelSettings, configurableDisplayName) {
      override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel =
        PrismaCodeStyleMainPanel(currentSettings, settings)
    }
  }

  override fun getIndentOptionsEditor(): IndentOptionsEditor = IndentOptionsEditor()

  override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
    if (settingsType == COMMENTER_SETTINGS) {
      consumer.showStandardOptions(
        CommenterOption.LINE_COMMENT_ADD_SPACE.name,
        CommenterOption.LINE_COMMENT_ADD_SPACE_ON_REFORMAT.name,
        CommenterOption.LINE_COMMENT_AT_FIRST_COLUMN.name,
      )
    }
  }

  override fun customizeDefaults(
    commonSettings: CommonCodeStyleSettings,
    indentOptions: CommonCodeStyleSettings.IndentOptions
  ) {
    commonSettings.apply {
      LINE_COMMENT_AT_FIRST_COLUMN = false
      LINE_COMMENT_ADD_SPACE = true
      LINE_COMMENT_ADD_SPACE_ON_REFORMAT = true

      KEEP_LINE_BREAKS = false
      KEEP_BLANK_LINES_IN_DECLARATIONS = 1
      KEEP_BLANK_LINES_IN_CODE = 1
    }

    indentOptions.apply {
      INDENT_SIZE = DEFAULT_INDENT
      CONTINUATION_INDENT_SIZE = DEFAULT_INDENT
      TAB_SIZE = DEFAULT_INDENT
    }
  }

  companion object {
    private const val DEFAULT_INDENT = 2
  }
}

class PrismaCodeStyleMainPanel(currentSettings: CodeStyleSettings, settings: CodeStyleSettings) :
  TabbedLanguageCodeStylePanel(PrismaLanguage, currentSettings, settings) {
  override fun initTabs(settings: CodeStyleSettings) {
    addIndentOptionsTab(settings)
    addTab(GenerationCodeStylePanel(settings, PrismaLanguage))
    addTab(PrismaCodeStyleOtherPanel(settings))
  }
}

class PrismaCodeStyleOtherPanel(settings: CodeStyleSettings) : CodeStyleAbstractPanel(settings) {
  private var runPrismaFmtOnReformat: Boolean = true

  private val panel = panel {
    group(PrismaBundle.message("prisma.run.formatter.group")) {
      row {
        checkBox(PrismaBundle.message("prisma.run.on.reformat")).bindSelected(this@PrismaCodeStyleOtherPanel::runPrismaFmtOnReformat)
      }
    }
  }.apply {
    border = BorderFactory.createEmptyBorder(UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP, UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP)
  }

  override fun getRightMargin(): Int = 0

  override fun createHighlighter(scheme: EditorColorsScheme): EditorHighlighter =
    HighlighterFactory.createHighlighter(PrismaSyntaxHighlighter(), scheme)

  override fun getFileType(): FileType = PrismaFileType

  override fun getPreviewText(): String? = null

  override fun apply(settings: CodeStyleSettings) {
    panel.apply()

    settings.prismaSettings.RUN_PRISMA_FMT_ON_REFORMAT = runPrismaFmtOnReformat
  }

  override fun isModified(settings: CodeStyleSettings): Boolean {
    return settings.prismaSettings.RUN_PRISMA_FMT_ON_REFORMAT != runPrismaFmtOnReformat
  }

  override fun getPanel(): JComponent = panel

  override fun resetImpl(settings: CodeStyleSettings) {
    runPrismaFmtOnReformat = settings.prismaSettings.RUN_PRISMA_FMT_ON_REFORMAT

    panel.reset()
  }

  private val CodeStyleSettings.prismaSettings
    get() = getCustomSettings(PrismaCodeStyleSettings::class.java)

}

private const val CODE_SAMPLE = """
generator client {
    provider = "prisma-client-js"
}

datasource db {
    provider = "postgresql"
    url      = env("DATABASE_URL")
}

model User {
    id          Int          @id @default(autoincrement())
    email       String       @unique
    firstName   String?
    lastName    String?
    social      Json?
    isAdmin     Boolean      @default(false)
    testResults TestResult[] @relation(name: "results")
    testsGraded TestResult[] @relation(name: "graded")
}    
"""