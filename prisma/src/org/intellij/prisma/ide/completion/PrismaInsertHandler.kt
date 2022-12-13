package org.intellij.prisma.ide.completion

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.AddSpaceInsertHandler
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.EditorModificationUtil
import org.intellij.prisma.ide.schema.PrismaSchemaDeclaration
import org.intellij.prisma.ide.schema.PrismaSchemaFakeElement
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.PrismaSchemaParameter
import org.intellij.prisma.lang.PrismaConstants.PrimitiveTypes
import org.intellij.prisma.lang.types.isListType
import org.intellij.prisma.lang.types.isNamedType

object PrismaInsertHandler {
  val DEFAULT_INSERT_HANDLER = InsertHandler<LookupElement> { context, item ->
    val element = item.psiElement as? PrismaSchemaFakeElement
    val schemaElement = element?.schemaElement

    if (schemaElement is PrismaSchemaParameter) {
      when {
        isNamedType(schemaElement.type, PrimitiveTypes.STRING) -> COLON_QUOTED_ARGUMENT.handleInsert(
          context,
          item
        )

        isListType(schemaElement.type) -> COLON_LIST_ARGUMENT.handleInsert(context, item)
        else -> COLON_ARGUMENT.handleInsert(context, item)
      }
      showPopup(context)
      return@InsertHandler
    }

    when ((schemaElement as? PrismaSchemaDeclaration)?.kind) {
      PrismaSchemaKind.KEYWORD ->
        AddSpaceInsertHandler.INSTANCE.handleInsert(context, item)

      PrismaSchemaKind.DATASOURCE_FIELD, PrismaSchemaKind.GENERATOR_FIELD -> {
        if (isListType(schemaElement.type)) {
          EQUALS_LIST.handleInsert(context, item)
        }
        else if (isNamedType(schemaElement.type, PrimitiveTypes.STRING)) {
          EQUALS_QUOTED.handleInsert(context, item)
        }
        else {
          EQUALS.handleInsert(context, item)
        }
        showPopup(context)
      }

      PrismaSchemaKind.FUNCTION -> {
        if (schemaElement.params.isEmpty()) {
          ParenthesesInsertHandler.NO_PARAMETERS.handleInsert(context, item)
        }
        else {
          ParenthesesInsertHandler.WITH_PARAMETERS.handleInsert(context, item)
          showPopup(context)
        }
      }

      else -> {}
    }
  }

  val PARENS_QUOTED_ARGUMENT = InsertHandler<LookupElement> { context, item ->
    ParenthesesInsertHandler.getInstance(true).handleInsert(context, item)
    EditorModificationUtil.insertStringAtCaret(context.editor, "\"\"", false, true, 1)
    showPopup(context)
  }

  val PARENS_LIST_ARGUMENT = InsertHandler<LookupElement> { context, item ->
    ParenthesesInsertHandler.getInstance(true).handleInsert(context, item)
    EditorModificationUtil.insertStringAtCaret(context.editor, "[]", false, true, 1)
    showPopup(context)
  }

  val COLON_QUOTED_ARGUMENT = InsertHandler<LookupElement> { context, item ->
    EditorModificationUtil.insertStringAtCaret(context.editor, ": \"\"", false, true, 3)
    showPopup(context)
  }

  val COLON_ARGUMENT = InsertHandler<LookupElement> { context, item ->
    EditorModificationUtil.insertStringAtCaret(context.editor, ": ", false, true)
    showPopup(context)
  }

  val COLON_LIST_ARGUMENT = InsertHandler<LookupElement> { context, item ->
    EditorModificationUtil.insertStringAtCaret(context.editor, ": []", false, true, 3)
    showPopup(context)
  }

  val EQUALS = InsertHandler<LookupElement> { context, _ ->
    EditorModificationUtil.insertStringAtCaret(context.editor, " = ", false, true)
    showPopup(context)
  }

  val EQUALS_QUOTED = InsertHandler<LookupElement> { context, _ ->
    EditorModificationUtil.insertStringAtCaret(context.editor, " = \"\"", false, true, 4)
    showPopup(context)
  }

  val EQUALS_LIST = InsertHandler<LookupElement> { context, _ ->
    EditorModificationUtil.insertStringAtCaret(context.editor, " = []", false, true, 4)
    showPopup(context)
  }

  val QUALIFIED_NAME = InsertHandler<LookupElement> { context, _ ->
    EditorModificationUtil.insertStringAtCaret(context.editor, ".")
    showPopup(context)
  }
}

private fun showPopup(context: InsertionContext) {
  AutoPopupController.getInstance(context.project).scheduleAutoPopup(context.editor)
}

fun LookupElementBuilder.withPrismaInsertHandler(
  customInsertHandler: InsertHandler<LookupElement>? = null
): LookupElementBuilder = if (customInsertHandler != null) {
  withInsertHandler(customInsertHandler)
}
else {
  withInsertHandler(PrismaInsertHandler.DEFAULT_INSERT_HANDLER)
}