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

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.ManifestFileType;
import org.jetbrains.osgi.project.BundleManifest;
import org.jetbrains.osgi.project.BundleManifestCache;
import org.osgi.framework.Constants;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.util.OsgiPsiUtil;

/**
 * Inspection that reports classes implementing BundleActivator
 * which are not registered in either manifest or facet configuration.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public final class UnregisteredActivatorInspection extends AbstractOsgiVisitor {
  @Override
  protected @NotNull PsiElementVisitor buildVisitor(final OsmorcFacet facet, final ProblemsHolder holder, boolean isOnTheFly) {
    if (!(holder.getFile() instanceof PsiClassOwner)) return PsiElementVisitor.EMPTY_VISITOR;
    return new JavaElementVisitor() {
      @Override
      public void visitFile(@NotNull PsiFile psiFile) {
        if (psiFile instanceof PsiClassOwner) {
          for (PsiClass psiClass : ((PsiClassOwner)psiFile).getClasses()) {
            String className = psiClass.getQualifiedName();
            if (OsgiPsiUtil.isActivator(psiClass) && className != null) {
              BundleManifest manifest = BundleManifestCache.getInstance().getManifest(facet.getModule());
              if (manifest != null && !className.equals(manifest.getBundleActivator())) {
                LocalQuickFix[] fixes = LocalQuickFix.EMPTY_ARRAY;

                OsmorcFacetConfiguration configuration = facet.getConfiguration();
                if (configuration.isManifestManuallyEdited()) {
                  fixes = new LocalQuickFix[]{new RegisterInManifestQuickfix(className)};
                }
                else if (configuration.isOsmorcControlsManifest()) {
                  fixes = new LocalQuickFix[]{new RegisterInConfigurationQuickfix(className, configuration)};
                }

                PsiElement identifier = unwrap(psiClass.getNameIdentifier());
                if (isValidElement(identifier)) {
                  holder.registerProblem(identifier, OsmorcBundle.message("UnregisteredActivatorInspection.message"), fixes);
                }
              }
            }
          }
        }
      }
    };
  }

  private static final class RegisterInManifestQuickfix extends AbstractOsgiQuickFix {
    private final String myActivatorClass;

    private RegisterInManifestQuickfix(@NotNull String activatorClass) {
      myActivatorClass = activatorClass;
    }

    @Override
    public @NotNull String getName() {
      return OsmorcBundle.message("UnregisteredActivatorInspection.fix.manifest");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      var manifestFile = getVerifiedManifestFile(descriptor.getEndElement());
      if (manifestFile != null) {
        WriteCommandAction.writeCommandAction(project, manifestFile).run(() -> OsgiPsiUtil.setHeader(manifestFile, Constants.BUNDLE_ACTIVATOR, myActivatorClass));
      }
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull ProblemDescriptor previewDescriptor) {
      var manifestFile = getManifestFile(previewDescriptor.getEndElement());
      if (manifestFile != null) {
        var existing = manifestFile.getHeader(Constants.BUNDLE_ACTIVATOR);
        var oldText = existing != null ? existing.getText() : "";
        var newText = Constants.BUNDLE_ACTIVATOR + ": " + myActivatorClass;
        return new IntentionPreviewInfo.CustomDiff(ManifestFileType.INSTANCE, oldText, newText);
      }
      else {
        return IntentionPreviewInfo.EMPTY;
      }
    }
  }

  private static final class RegisterInConfigurationQuickfix extends AbstractOsgiQuickFix {
    private final String myActivatorClass;
    private final OsmorcFacetConfiguration myConfiguration;

    private RegisterInConfigurationQuickfix(@NotNull String activatorClass, @NotNull OsmorcFacetConfiguration configuration) {
      myActivatorClass = activatorClass;
      myConfiguration = configuration;
    }

    @Override
    public @NotNull String getName() {
      return OsmorcBundle.message("UnregisteredActivatorInspection.fix.config");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      myConfiguration.setBundleActivator(myActivatorClass);
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull ProblemDescriptor previewDescriptor) {
      return IntentionPreviewInfo.EMPTY;
    }
  }
}
