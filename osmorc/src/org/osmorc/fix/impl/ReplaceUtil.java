/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.fix.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.osmorc.manifest.lang.psi.Clause;
import org.osmorc.manifest.lang.psi.Header;
import org.osmorc.manifest.lang.psi.HeaderValuePart;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ReplaceUtil {
    public static void replace(PsiElement element, String with) {
        replace(element, element.getTextRange(), with);
    }

    public static void replace(PsiElement element, TextRange range, String with) {
        TextRange relativeTextRange =
                TextRange.from(range.getStartOffset() - element.getTextRange().getStartOffset(), range.getLength());
        String newText = relativeTextRange.replace(element.getText(), with);

        if (element instanceof HeaderValuePart) {
            replace((HeaderValuePart) element, newText);
        }

    }

    private static void replace(HeaderValuePart headerValue, String with) {
        ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(headerValue.getProject()).ensureFilesWritable(PsiTreeUtil.getParentOfType(headerValue, PsiFile.class, false).getVirtualFile());

        if (!status.hasReadonlyFiles()) {
            PsiFile fromText = PsiFileFactory.getInstance(headerValue.getProject()).createFileFromText("DUMMY.MF", "dummy: " + with);
            Header header = PsiTreeUtil.getChildOfType(fromText.getFirstChild(), Header.class);
            assert header != null;
            Clause clause = PsiTreeUtil.getChildOfType(header, Clause.class);
            assert clause != null;
            HeaderValuePart value = PsiTreeUtil.getChildOfAnyType(clause, HeaderValuePart.class);

            try {
                assert value != null;
                headerValue.replace(value);
            }
            catch (IncorrectOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
