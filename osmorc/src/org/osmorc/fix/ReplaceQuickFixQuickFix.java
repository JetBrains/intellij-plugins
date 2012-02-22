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
package org.osmorc.fix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.osmorc.fix.impl.ReplaceUtil;
import org.osmorc.manifest.lang.psi.impl.ManifestElementBase;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ReplaceQuickFixQuickFix implements LocalQuickFix {
  public ReplaceQuickFixQuickFix(String name, ManifestElementBase element, String newText) {
    this(name, element, element.getTextRange(), newText);
  }

  public ReplaceQuickFixQuickFix(String name, ManifestElementBase element, TextRange range, String newText) {
    _name = name;
    _element = element;
    _range = range;
    _newText = newText;
  }

  @NotNull
  public String getName() {
    return _name;
  }

  @NotNull
  public String getFamilyName() {
    return "Osmorc";
  }

  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    ReplaceUtil.replace(_element, _range, _newText);
  }

  private final String _name;
  private final ManifestElementBase _element;
  private final TextRange _range;
  private final String _newText;
}
