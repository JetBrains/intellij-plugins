package com.intellij.coldFusion.UI.editorActions.typedHandlers;

import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate;
import com.intellij.coldFusion.UI.editorActions.utils.CfmlEditorUtil;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

/**
 * Created by Lera Nikolaenko
 * Date: 16.10.2008
 */
public class CfmlBackspaceHandler extends BackspaceHandlerDelegate {

    public void beforeCharDeleted(char c, PsiFile file, Editor editor) {
        if (file.getLanguage() != CfmlLanguage.INSTANCE) return;
        if (c == '#') {
            if (CfmlEditorUtil.countSharpsBalance(editor) == 0) {
                final int offset = editor.getCaretModel().getOffset();
                final Document doc = editor.getDocument();
                char charAtOffset = DocumentUtils.getCharAt(doc, offset);
                if (charAtOffset == '#') {
                    doc.deleteString(offset, offset + 1);
                }
            }
        } else if (c == '{' && file.findElementAt(editor.getCaretModel().getOffset()) == CfscriptTokenTypes.L_CURLYBRACKET) {
            final int offset = editor.getCaretModel().getOffset();
            final Document doc = editor.getDocument();
            char charAtOffset = DocumentUtils.getCharAt(doc, offset);
            if (charAtOffset == '}') {
                doc.deleteString(offset, offset + 1);
            }
        }
    }

    public boolean charDeleted(char c, PsiFile file, Editor editor) {
        if (file.getLanguage() != CfmlLanguage.INSTANCE) return false;
        return false;
    }
}
