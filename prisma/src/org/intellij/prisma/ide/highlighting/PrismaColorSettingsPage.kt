package org.intellij.prisma.ide.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.OptionsBundle
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import org.intellij.prisma.PrismaBundle
import org.intellij.prisma.PrismaIcons
import org.intellij.prisma.lang.PrismaLanguage
import javax.swing.Icon

class PrismaColorSettingsPage : ColorSettingsPage {
  override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

  override fun getDisplayName(): String = PrismaLanguage.displayName

  override fun getIcon(): Icon = PrismaIcons.PRISMA

  override fun getHighlighter(): SyntaxHighlighter =
    SyntaxHighlighterFactory.getSyntaxHighlighter(PrismaLanguage, null, null)

  override fun getDemoText(): String = DEMO_TEXT

  override fun getAttributeDescriptors(): Array<AttributesDescriptor> = descriptors

  override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = additionalDescriptors

  companion object {
    private val descriptors = arrayOf(
      AttributesDescriptor(
        OptionsBundle.messagePointer("options.language.defaults.doc.comment"),
        PrismaColors.DOC_COMMENT
      ),
      AttributesDescriptor(
        OptionsBundle.messagePointer("options.language.defaults.line.comment"),
        PrismaColors.LINE_COMMENT
      ),
      AttributesDescriptor(
        OptionsBundle.messagePointer("options.language.defaults.string"),
        PrismaColors.STRING_LITERAL
      ),
      AttributesDescriptor(
        OptionsBundle.messagePointer("options.language.defaults.keyword"),
        PrismaColors.KEYWORD
      ),
      AttributesDescriptor(
        OptionsBundle.messagePointer("options.language.defaults.identifier"),
        PrismaColors.IDENTIFIER
      ),
      AttributesDescriptor(OptionsBundle.messagePointer("options.language.defaults.number"), PrismaColors.NUMBER),
      AttributesDescriptor(
        OptionsBundle.messagePointer("options.language.defaults.brackets"),
        PrismaColors.BRACKETS
      ),
      AttributesDescriptor(
        OptionsBundle.messagePointer("options.language.defaults.parentheses"),
        PrismaColors.PARENTHESES
      ),
      AttributesDescriptor(OptionsBundle.messagePointer("options.language.defaults.braces"), PrismaColors.BRACES),
      AttributesDescriptor(OptionsBundle.messagePointer("options.language.defaults.dot"), PrismaColors.DOT),
      AttributesDescriptor(OptionsBundle.messagePointer("options.language.defaults.comma"), PrismaColors.COMMA),
      AttributesDescriptor(
        OptionsBundle.messagePointer("options.language.defaults.operation"),
        PrismaColors.OPERATION_SIGN
      ),
      AttributesDescriptor(
        PrismaBundle.messagePointer("prisma.color.settings.type.name"),
        PrismaColors.TYPE_NAME
      ),
      AttributesDescriptor(
        PrismaBundle.messagePointer("prisma.color.settings.type.reference"),
        PrismaColors.TYPE_REFERENCE
      ),
      AttributesDescriptor(
        PrismaBundle.messagePointer("prisma.color.settings.attribute"),
        PrismaColors.ATTRIBUTE
      ),
      AttributesDescriptor(
        PrismaBundle.messagePointer("prisma.color.settings.parameter"),
        PrismaColors.PARAMETER
      ),
      AttributesDescriptor(
        PrismaBundle.messagePointer("prisma.color.settings.field.name"),
        PrismaColors.FIELD_NAME
      ),
      AttributesDescriptor(
        PrismaBundle.messagePointer("prisma.color.settings.field.reference"),
        PrismaColors.FIELD_REFERENCE
      ),
      AttributesDescriptor(PrismaBundle.messagePointer("prisma.color.settings.function"), PrismaColors.FUNCTION),
    )

    private val additionalDescriptors = mapOf(
      "tn" to PrismaColors.TYPE_NAME,
      "tr" to PrismaColors.TYPE_REFERENCE,
      "attr" to PrismaColors.ATTRIBUTE,
      "param" to PrismaColors.PARAMETER,
      "fn" to PrismaColors.FIELD_NAME,
      "fr" to PrismaColors.FIELD_REFERENCE,
      "func" to PrismaColors.FUNCTION,
    )

    private val DEMO_TEXT = """
        /// Doc
        /// Comment
        generator <tn>client</tn> {
          <fn>provider</fn> = "prisma-client-js"
        }
        
        // Line comment
        datasource <tn>db</tn> {
          <fn>provider</fn> = "sqlite" // property comment
          <fn>url</fn>      = "file:./dev.db"
        }
        
        model <tn>User</tn> {
          <fn>id</fn>    <tr>Int</tr>     <attr>@id</attr> <attr>@default</attr>(<func>autoincrement</func>())
          <fn>email</fn> <tr>String</tr>  <attr>@unique</attr>
          <fn>name</fn>  <tr>String</tr>?
          <fn>posts</fn> <tr>Post</tr>[]
        }
        
        model <tn>Post</tn> {
          <fn>id</fn>        <tr>Int</tr>      <attr>@id</attr> <attr>@default</attr>(<func>autoincrement</func>())
          <fn>createdAt</fn> <tr>DateTime</tr> <attr>@default</attr>(<func>now</func>())
          <fn>updatedAt</fn> <tr>DateTime</tr> <attr>@updatedAt</attr>
          <fn>title</fn>     <tr>String</tr>
          <fn>content</fn>   <tr>String</tr>?
          <fn>published</fn> <tr>Boolean</tr>  <attr>@default</attr>(<fr>false</fr>)
          <fn>viewCount</fn> <tr>Int</tr>      <attr>@default</attr>(0)
          <fn>author</fn>    <tr>User</tr>?    <attr>@relation</attr>(<param>fields</param>: [<fr>authorId</fr>], <param>references</param>: [<fr>id</fr>])
          <fn>authorId</fn>  <tr>Int</tr>?
          
          <attr>@@id</attr>([<fr>id</fr>])
        }
        """.trimIndent()
  }
}