package org.angularjs

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.codeInsight.CodeInsightSettings
import org.angularjs.settings.AngularJSConfig

/**
 * Heavily borrow from @dcheryasov's work on Django braces
 */
public open class AngularBracesInterpolationTypedHandler(): TypedHandlerDelegate() {
    public override fun beforeCharTyped(c: Char, project: Project?, editor: Editor?, file: PsiFile?, fileType: FileType?): TypedHandlerDelegate.Result? {
        if(CodeInsightSettings.getInstance()!!.AUTOINSERT_PAIR_BRACKET) return TypedHandlerDelegate.Result.DEFAULT;
        if (file?.getFileType() == HtmlFileType.INSTANCE)
        {
            if (c == '{')
            {
                val addWhiteSpaceBetweenBraces = AngularJSConfig.getInstance()!!.INSERT_WHITESPACE
                val offset = editor?.getCaretModel()?.getOffset()!!
                var chars = editor?.getDocument()?.getText()
                if (offset > 0 && (chars?.charAt(offset - 1)) == '{')
                {
                    if (offset < 2 || (chars?.charAt(offset - 2)) != '{')
                    {
                        if (alreadyHasEnding(chars, c, offset))
                        {
                            return TypedHandlerDelegate.Result.CONTINUE
                        }
                        else
                        {
                            var interpolation: String? = null
                            if (c == '{')
                            {
                                if(addWhiteSpaceBetweenBraces)
                                {
                                    interpolation = "{  }"

                                }
                                else{
                                    interpolation = "{}"
                                }
                            }

                            if (interpolation != null)
                            {
                                if (offset == (chars?.length()) || (offset < (chars?.length())!! && (chars?.charAt(offset)) != '}'))
                                {
                                    interpolation += "}"
                                }

                                var move = 2
                                if(!addWhiteSpaceBetweenBraces) move = 1

                                typeInStringAndMoveCaret(editor, offset + move, interpolation)
                                return TypedHandlerDelegate.Result.STOP
                            }

                        }
                    }

                }

            }

        }

        return TypedHandlerDelegate.Result.CONTINUE
    }

    class object {
        open fun typeInStringAndMoveCaret(editor: Editor?, offset: Int, str: String?): Unit {
            EditorModificationUtil.typeInStringAtCaretHonorBlockSelection(editor, str, true)
            editor?.getCaretModel()?.moveToOffset(offset)
        }
        private open fun alreadyHasEnding(chars: CharSequence?, c: Char, offset: Int): Boolean {
            var i: Int = offset
            var endChar: Char
            if (c == '{')
            {
                endChar = '}'
            }
            else
            {
                endChar = c
            }
            while (i < (chars?.length())!! && ((chars?.charAt(i)) != '{' && (chars?.charAt(i)) != endChar && (chars?.charAt(i)) != '\n'))
            {
                i++
            }
            if (i + 1 < (chars?.length())!! && (chars?.charAt(i)) == endChar && (chars?.charAt(i + 1)) == '}')
            {
                return true
            }

            return false
        }
    }
}
