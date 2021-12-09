package com.jetbrains.lang.makefile

import com.intellij.openapi.options.colors.*
import icons.MakefileIcons

class MakefileColorSettingsPage : ColorSettingsPage {
  override fun getDisplayName() = MakefileLanguage.displayName
  override fun getIcon() = MakefileIcons.Makefile

  private val tags = mapOf(
    "target" to MakefileSyntaxHighlighter.TARGET,
    "specialTarget" to MakefileSyntaxHighlighter.SPECIAL_TARGET,
    "variableName" to MakefileSyntaxHighlighter.VARIABLE,
    "prerequisite" to MakefileSyntaxHighlighter.PREREQUISITE,
    "function" to MakefileSyntaxHighlighter.FUNCTION,
    "functionParam" to MakefileSyntaxHighlighter.FUNCTION_PARAM
  )

  private val DESCRIPTORS = arrayOf(
    AttributesDescriptor(MakefileLangBundle.message("attribute.descriptor.comment"), MakefileSyntaxHighlighter.COMMENT),
    AttributesDescriptor(MakefileLangBundle.message("attribute.descriptor.documentation.comment"), MakefileSyntaxHighlighter.DOCCOMMENT),
    AttributesDescriptor(MakefileLangBundle.message("attribute.descriptor.keyword"), MakefileSyntaxHighlighter.KEYWORD),
    AttributesDescriptor(MakefileLangBundle.message("attribute.descriptor.target"), MakefileSyntaxHighlighter.TARGET),
    AttributesDescriptor(MakefileLangBundle.message("attribute.descriptor.special.target"), MakefileSyntaxHighlighter.SPECIAL_TARGET),
    AttributesDescriptor(MakefileLangBundle.message("attribute.descriptor.separator"), MakefileSyntaxHighlighter.SEPARATOR),
    AttributesDescriptor(MakefileLangBundle.message("attribute.descriptor.prerequisite"), MakefileSyntaxHighlighter.PREREQUISITE),
    AttributesDescriptor(MakefileLangBundle.message("attribute.descriptor.variable.name"), MakefileSyntaxHighlighter.VARIABLE),
    AttributesDescriptor(MakefileLangBundle.message("attribute.descriptor.variable.value"), MakefileSyntaxHighlighter.VARIABLE_VALUE),
    AttributesDescriptor(MakefileLangBundle.message("attribute.descriptor.line.split"), MakefileSyntaxHighlighter.LINE_SPLIT),
    AttributesDescriptor(MakefileLangBundle.message("attribute.descriptor.tab"), MakefileSyntaxHighlighter.TAB),
    AttributesDescriptor(MakefileLangBundle.message("attribute.descriptor.function"), MakefileSyntaxHighlighter.FUNCTION),
    AttributesDescriptor(MakefileLangBundle.message("attribute.descriptor.function.param"), MakefileSyntaxHighlighter.FUNCTION_PARAM),
    AttributesDescriptor(MakefileLangBundle.message("attribute.descriptor.braces"), MakefileSyntaxHighlighter.BRACES)
  )

  override fun getAttributeDescriptors() = DESCRIPTORS
  override fun getHighlighter() = MakefileSyntaxHighlighter()

  override fun getDemoText() = """# Simple Makefile
include make.mk

<target>all</target>: <prerequisite>hello</prerequisite> ## Doc comment

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
${'\t'}$(<function>error</function><functionParam> Architecture $(ARCH) is not supported</functionParam>)
endif"""

  override fun getAdditionalHighlightingTagToDescriptorMap() = tags
  override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
}