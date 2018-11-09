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
package org.osmorc.facet;

import com.intellij.openapi.application.Application;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementAdapter;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class OsmorcFacetRefactoringListenerProvider implements RefactoringElementListenerProvider {
  private final Application myApplication;

  public OsmorcFacetRefactoringListenerProvider(final Application application) {
    myApplication = application;
  }

  @Override
  @Nullable
  public RefactoringElementListener getListener(final PsiElement element) {
    if (element instanceof PsiClass) {
      OsmorcFacet osmorcFacet = OsmorcFacet.getInstance(element);
      if (osmorcFacet != null) {
        OsmorcFacetConfiguration osmorcFacetConfiguration = osmorcFacet.getConfiguration();
        PsiClass psiClass = (PsiClass)element;
        if (osmorcFacetConfiguration.isOsmorcControlsManifest() &&
            osmorcFacetConfiguration.getBundleActivator() != null &&
            osmorcFacetConfiguration.getBundleActivator().equals(psiClass.getQualifiedName())) {
          return new ActivatorClassRefactoringListener(osmorcFacetConfiguration, myApplication);
        }
      }
    }

    return null;
  }

  private static final class ActivatorClassRefactoringListener extends RefactoringElementAdapter {
    private final OsmorcFacetConfiguration osmorcFacetConfiguration;
    private final Application application;

    private ActivatorClassRefactoringListener(final OsmorcFacetConfiguration osmorcFacetConfiguration,
                                              final Application application) {
      this.osmorcFacetConfiguration = osmorcFacetConfiguration;
      this.application = application;
    }

    @Override
    public void elementRenamedOrMoved(@NotNull final PsiElement newElement) {
      application.runWriteAction(() -> osmorcFacetConfiguration.setBundleActivator(((PsiClass)newElement).getQualifiedName()));
    }

    @Override
    public void undoElementMovedOrRenamed(@NotNull PsiElement newElement, @NotNull final String oldQualifiedName) {
      application.runWriteAction(() -> osmorcFacetConfiguration.setBundleActivator(oldQualifiedName));
    }
  }
}
