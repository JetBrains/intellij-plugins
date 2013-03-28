package org.angularjs;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlElementType;
import com.jetbrains.django.lang.template.DjangoTemplateFileViewProvider;
import com.jetbrains.python.codeInsight.PyCodeInsightSettings;

import java.util.Arrays;

/**
 * Handles interpolation braces in Django templates:
 * Single "{" is ignored.
 * Double opening brace "{{" is auto-closed and cursor is put inside.
 * Opening brace with a percent "{%" is auto-closed and cursor is put inside.
 * User: dcheryasov
 * Date: Jan 29, 2010 6:19:44 PM
 */
public class AngularBracesInterpolationTypedHandler extends TypedHandlerDelegate {
    private static char[] ourInterestingChars;

    static {
        ourInterestingChars = new char[]{'{', '%', '#'};
        Arrays.sort(ourInterestingChars);
    }

    private static boolean isInteresting(char c) {
        return Arrays.binarySearch(ourInterestingChars, c) >= 0;
    }

    // snatched from VelocityTypedHandler
    static void typeInStringAndMoveCaret(Editor editor, int offset, String str) {
        EditorModificationUtil.typeInStringAtCaretHonorBlockSelection(editor, str, true);
        editor.getCaretModel().moveToOffset(offset);
    }

    @Override
    public Result beforeCharTyped(char c, Project project, Editor editor, PsiFile file, FileType fileType) {
        // filter out Django templates. They come as HTML files with injected Django Template files;
        // fileType depends on where the cursor is, we cannot rely on it.
        if (file.getFileType() == HtmlFileType.INSTANCE) {
            if (isInteresting(c)) {
                // where we are?
                final Document document = editor.getDocument();
                final int offset = editor.getCaretModel().getOffset();
                // we only care about a character that follows a single '{' in the source.
                CharSequence chars = document.getCharsSequence();
                if (offset > 0 && chars.charAt(offset - 1) == '{') {
                    if (interpolateCommentBetweenBraces(editor, chars, c, offset)) {
                        return Result.STOP;
                    }
                    else if (offset < 2 || chars.charAt(offset - 2) != '{') {
                        if (alreadyHasEnding(chars, c, offset)) {
                            return Result.CONTINUE;
                        }
                        else {
                            // TODO: honor style settings wrt 'spaces inside template interpolators'; now spaces are always added
                            String interpolation = null;

                            if (c == '{') {      // {{
                                interpolation = "{  }";
                            }

                            else if (c == '%') { // {%
                                interpolation = "%  %";
                            }

                            else if (c == '#') { // {#

                                interpolation = "#  #";
                            }

                            if (interpolation != null) {
                                if (offset == chars.length() || (offset < chars.length() && chars.charAt(offset) != '}')) {
                                    interpolation += "}";
                                }

                                typeInStringAndMoveCaret(editor, offset + 2, interpolation);
                                return Result.STOP;
                            }
                        }
                    }
                }
            }
        }

        return Result.CONTINUE;
    }

    private static boolean alreadyHasEnding(final CharSequence chars, final char c, final int offset) {
        int i = offset;

        char endChar;
        if (c == '{') {
            endChar = '}';
        }
        else {
            endChar = c;
        }
        while (i < chars.length() && (chars.charAt(i) != '{' && chars.charAt(i) != endChar && chars.charAt(i) != '\n')) {
            i++;
        }
        if (i + 1 < chars.length() && chars.charAt(i) == endChar && chars.charAt(i + 1) == '}') {
            return true;
        }
        return false;
    }

    private static boolean interpolateCommentBetweenBraces(Editor editor, CharSequence chars, char c, int offset) {
        if (chars.length() <= offset) {
            return false;
        }
        char cc = chars.charAt(offset);
        if (c != '#' || cc != '%') {
            return false;
        }

        for (int i = offset; i < chars.length(); i++) {
            if (chars.charAt(i) == '\n') {
                break;
            }
            if (chars.charAt(i) == '}' && i - 1 > offset) {
                if (chars.charAt(i - 1) == chars.charAt(offset)) {
                    typeInStringAndMoveCaret(editor, i + 1, "#");
                    typeInStringAndMoveCaret(editor, offset, "#");
                    return true;
                }
            }
        }
        return false;
    }
}
