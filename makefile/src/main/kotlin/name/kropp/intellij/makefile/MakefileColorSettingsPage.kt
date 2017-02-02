package name.kropp.intellij.makefile

import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage

class MakefileColorSettingsPage : ColorSettingsPage {
  override fun getDisplayName() = MakefileLanguage.displayName
  override fun getIcon() = MakefileIcon

  private val tags = mapOf(
      "target" to MakefileSyntaxHighlighter.TARGET,
      "specialTarget" to MakefileSyntaxHighlighter.SPECIAL_TARGET,
      "variableName" to MakefileSyntaxHighlighter.VARIABLE,
      "prerequisite" to MakefileSyntaxHighlighter.PREREQUISITE
  )

  private val DESCRIPTORS = arrayOf(
      AttributesDescriptor("Keyword", MakefileSyntaxHighlighter.KEYWORD),
      AttributesDescriptor("Target", MakefileSyntaxHighlighter.TARGET),
      AttributesDescriptor("Special Target", MakefileSyntaxHighlighter.SPECIAL_TARGET),
      AttributesDescriptor("Separator", MakefileSyntaxHighlighter.SEPARATOR),
      AttributesDescriptor("Prerequisite", MakefileSyntaxHighlighter.PREREQUISITE),
      AttributesDescriptor("Variable Name", MakefileSyntaxHighlighter.VARIABLE),
      AttributesDescriptor("Variable Value", MakefileSyntaxHighlighter.VARIABLE_VALUE),
      AttributesDescriptor("Line Split", MakefileSyntaxHighlighter.LINE_SPLIT),
      AttributesDescriptor("Tab", MakefileSyntaxHighlighter.TAB)
  )

  override fun getAttributeDescriptors() = DESCRIPTORS
  override fun getHighlighter() = MakefileSyntaxHighlighter()

  override fun getDemoText() = """# Simple Makefile
include make.mk

<target>all</target>: <prerequisite>hello</prerequisite>

<target>hello</target>: <prerequisite>hello.o</prerequisite> <prerequisite>world.o</prerequisite>
<specialTarget>.PHONY: hello</specialTarget>

<variableName>GCC</variableName> = gcc \
           -O2

<target>.o.c</target>:
ifeq ($(FOO),'bar')
${'\t'}$(GCC) -c qwe \
              -Wall
else
${'\t'}echo "Hello World"
endif"""

  override fun getAdditionalHighlightingTagToDescriptorMap() = tags
  override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
}