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

package org.osmorc.inspection;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.fix.ReplaceQuickFixQuickFix;
import org.osmorc.manifest.lang.psi.ManifestHeaderValueImpl;
import org.osmorc.manifest.lang.psi.ManifestFile;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class MissingFinalNewlineInspection extends LocalInspectionTool {
    @Nls
    @NotNull
    public String getGroupDisplayName() {
        return "OSGi";
    }

    @Nls
    @NotNull
    public String getDisplayName() {
        return "Missing Final New Line";
    }

    @NotNull
    public String getShortName() {
        return "osmorcMissingFinalNewline";
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    @NotNull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        if (file instanceof ManifestFile) {
            String text = file.getText();
            if (text.charAt(text.length() - 1) != '\n') {
                ManifestHeaderValueImpl headerValue = PsiTreeUtil.findElementOfClassAtOffset(file, text.length() - 1,
                        ManifestHeaderValueImpl.class, false);
                if (headerValue != null) {
                    return new ProblemDescriptor[]{manager.createProblemDescriptor(headerValue,
                            "Manifest file doen't end with a final newline",
                            new AddNewlineQuickFix(headerValue), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)};
                }

            }
        }
        return new ProblemDescriptor[0];
    }

    private static class AddNewlineQuickFix extends ReplaceQuickFixQuickFix {
        private AddNewlineQuickFix(ManifestHeaderValueImpl headerValue) {
            // TODO: This is a hack. Osmorc currently doesn't handle manifest file sections. Need to fix this once Osmorc handles sections.
            super("Add newline", headerValue, headerValue.getText() + "\n\n");
        }
    }


}
