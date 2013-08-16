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
import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.header.HeaderNameMatch;
import org.jetbrains.lang.manifest.header.HeaderParserRepository;
import org.jetbrains.lang.manifest.psi.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class MisspelledHeaderNameInspection extends LocalInspectionTool {
  private HeaderParserRepository myRepository;

  public MisspelledHeaderNameInspection(HeaderParserRepository repository) {
    myRepository = repository;
  }

  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return "OSGi";
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Unknown or Misspelled Header Name";
  }

  @NonNls
  @NotNull
  @Override
  public String getShortName() {
    return "osmorcMisspelledHeaderName";
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        if (element instanceof Header) {
          final Header header = (Header)element;
          String name = header.getName();
          if (name != null && name.length() > 0) {
            final List<HeaderNameSpellingQuickFix> quickFixes = new ArrayList<HeaderNameSpellingQuickFix>();
            for (HeaderNameMatch match : myRepository.getMatches(name)) {
              quickFixes.add(new HeaderNameSpellingQuickFix(header, match));
              if (quickFixes.size() > 20) {
                break;
              }
            }

            if (quickFixes.size() > 0) {
              holder.registerProblem(header.getNameToken(), "Header name is unknown or spelled incorrectly",
                                     ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                     quickFixes.toArray(new HeaderNameSpellingQuickFix[quickFixes.size()]));
            }
          }
        }
      }
    };
  }

  private static class HeaderNameSpellingQuickFix implements LocalQuickFix {
    private final Header myHeader;
    private final String myNewName;

    private HeaderNameSpellingQuickFix(Header header, HeaderNameMatch match) {
      myHeader = header;
      myNewName = match.getHeaderName();
    }

    @NotNull
    @Override
    public String getName() {
      return String.format("Change to '%s'", myNewName);
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return "Osmorc";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      myHeader.setName(myNewName);
    }
  }
}
