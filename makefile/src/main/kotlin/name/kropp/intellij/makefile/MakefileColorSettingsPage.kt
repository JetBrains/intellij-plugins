package name.kropp.intellij.makefile

import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage

class MakefileColorSettingsPage : ColorSettingsPage {
  override fun getDisplayName() = MakefileLanguage.displayName
  override fun getIcon() = MakefileIcon

  private val DESCRIPTORS = arrayOf(
      AttributesDescriptor("Keyword", MakefileSyntaxHighlighter.KEYWORD),
      AttributesDescriptor("Target", MakefileSyntaxHighlighter.TARGET),
      AttributesDescriptor("Separator", MakefileSyntaxHighlighter.SEPARATOR),
      AttributesDescriptor("Prerequisite", MakefileSyntaxHighlighter.PREREQUISITE),
      AttributesDescriptor("Variable Name", MakefileSyntaxHighlighter.VARIABLE_NAME),
      AttributesDescriptor("Variable Value", MakefileSyntaxHighlighter.VARIABLE_VALUE)
  )

  override fun getAttributeDescriptors() = DESCRIPTORS
  override fun getHighlighter() = MakefileSyntaxHighlighter()

  override fun getDemoText() = """# Simple Makefile
include make.mk

all: hello

hello: hello.o world.o

GCC = gcc

.o.c:
if eq($(FOO),'bar')
${'\t'}$(GCC) -c qwe
else
${'\t'}echo "Hello World"
endif"""

  override fun getAdditionalHighlightingTagToDescriptorMap() = null
  override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
}