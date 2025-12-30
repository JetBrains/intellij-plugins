// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.tree.IElementType
import org.angular2.lang.html.Angular2TemplateSyntax
import org.jetbrains.annotations.NonNls

class Angular2PsiParser : PsiParser {
  override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
    val containingFile = builder.getUserData(FileContextUtil.CONTAINING_FILE_KEY)
    if (containingFile != null) {
      val ext = FileUtilRt.getExtension(containingFile.name)
      val parseMethod = parseMappings[ext]
      val templateSyntax = Angular2TemplateSyntax.of(containingFile) ?: Angular2TemplateSyntax.entries.last()
      if (parseMethod != null) {
        parseMethod(templateSyntax, builder, root)
      }
      else if (TEMPLATE_BINDINGS == ext) {
        val templateKey = FileUtilRt.getExtension(FileUtilRt.getNameWithoutExtension(containingFile.name))
        Angular2Parser.parseTemplateBindings(templateSyntax, builder, root, templateKey)
      }
      else if (ext == "js") {
        //special case for creation of AST from text
        Angular2Parser.parseJS(templateSyntax, builder, root)
      }
      else {
        Angular2Parser.parseInterpolation(templateSyntax, builder, root)
      }
    }
    else {
      LOG.error("No containing file while parsing Angular2 expression.")
    }
    return builder.treeBuilt
  }

  companion object {
    const val ACTION: @NonNls String = "action"

    const val BINDING: @NonNls String = "binding"

    const val TEMPLATE_BINDINGS: @NonNls String = "template_bindings"
    const val INTERPOLATION: @NonNls String = "interpolation"

    const val SIMPLE_BINDING: @NonNls String = "simple_binding"

    private val LOG: @NonNls Logger = Logger.getInstance(Angular2PsiParser::class.java)

    private val parseMappings = mapOf(
      ACTION to { templateSyntax: Angular2TemplateSyntax, builder: PsiBuilder, root: IElementType ->
        Angular2Parser.parseAction(templateSyntax, builder, root)
      },
      BINDING to { templateSyntax: Angular2TemplateSyntax, builder: PsiBuilder, root: IElementType ->
        Angular2Parser.parseBinding(templateSyntax, builder, root)
      },
      INTERPOLATION to { templateSyntax: Angular2TemplateSyntax, builder: PsiBuilder, root: IElementType ->
        Angular2Parser.parseInterpolation(templateSyntax, builder, root)
      },
      SIMPLE_BINDING to { templateSyntax: Angular2TemplateSyntax, builder: PsiBuilder, root: IElementType ->
        Angular2Parser.parseSimpleBinding(templateSyntax, builder, root)
      })
  }
}