package name.kropp.intellij.makefile

import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage

class MakefileColorSettingsPage : ColorSettingsPage {
  override fun getDisplayName() = MakefileLanguage.displayName
  override fun getIcon() = MakefileIcon

  private val DESCRIPTORS = arrayOf(
      AttributesDescriptor("Target", MakefileSyntaxHighlighter.TARGET),
      AttributesDescriptor("Separator", MakefileSyntaxHighlighter.SEPARATOR),
      AttributesDescriptor("Dependency", MakefileSyntaxHighlighter.DEPENDENCY)
  )

  override fun getAttributeDescriptors() = DESCRIPTORS
  override fun getHighlighter() = MakefileSyntaxHighlighter()

  override fun getDemoText() = """# Simple Makefile

  all: hello

  hello: hello.o world.o

  .o.c:
  gcc -c qwe
      echo "Hello World""""

  override fun getAdditionalHighlightingTagToDescriptorMap() = null
  override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
}