package name.kropp.intellij.makefile

import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage

private val tags = mapOf(
    "target" to MakefileSyntaxHighlighter.TARGET,
    "variableName" to MakefileSyntaxHighlighter.VARIABLE,
    "prerequisite" to MakefileSyntaxHighlighter.PREREQUISITE
)

class MakefileColorSettingsPage : ColorSettingsPage {
  override fun getDisplayName() = MakefileLanguage.displayName
  override fun getIcon() = MakefileIcon

  private val DESCRIPTORS = arrayOf(
      AttributesDescriptor("Keyword", MakefileSyntaxHighlighter.KEYWORD),
      AttributesDescriptor("Target", MakefileSyntaxHighlighter.TARGET),
      AttributesDescriptor("Separator", MakefileSyntaxHighlighter.SEPARATOR),
      AttributesDescriptor("Prerequisite", MakefileSyntaxHighlighter.PREREQUISITE),
      AttributesDescriptor("Variable Name", MakefileSyntaxHighlighter.VARIABLE),
      AttributesDescriptor("Variable Value", MakefileSyntaxHighlighter.VARIABLE_VALUE)
  )

  override fun getAttributeDescriptors() = DESCRIPTORS
  override fun getHighlighter() = MakefileSyntaxHighlighter()

  override fun getDemoText() = """# Simple Makefile
include make.mk

<target>all</target>: <prerequisite>hello</prerequisite>

<target>hello</target>: <prerequisite>hello.o</prerequisite> <prerequisite>world.o</prerequisite>

<variableName>GCC</variableName> = gcc

<target>.o.c</target>:
ifeq ($(FOO),'bar')
${'\t'}$(GCC) -c qwe
else
${'\t'}echo "Hello World"
endif"""

  override fun getAdditionalHighlightingTagToDescriptorMap() = tags
  override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
}