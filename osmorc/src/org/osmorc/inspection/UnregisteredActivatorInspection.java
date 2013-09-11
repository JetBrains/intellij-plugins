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

import com.intellij.codeInspection.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.ProjectScope;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.jetbrains.lang.manifest.psi.Section;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.Constants;
import org.osmorc.BundleManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.util.OsgiPsiUtil;

import java.util.List;

/**
 * Inspection that reports classes implementing BundleActivator
 * which are not registered in either manifest or facet configuration.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class UnregisteredActivatorInspection extends LocalInspectionTool {
  private static final String ACTIVATOR_CLASS = BundleActivator.class.getName();

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new JavaElementVisitor() {
      @Override
      public void visitClass(PsiClass psiClass) {
        if (!psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
          OsmorcFacet facet = OsmorcFacet.getInstance(psiClass);
          if (facet != null) {
            Project project = psiClass.getProject();
            PsiClass activator = JavaPsiFacade.getInstance(project).findClass(ACTIVATOR_CLASS, ProjectScope.getLibrariesScope(project));
            if (activator != null && psiClass.isInheritor(activator, true)) {
              String className = psiClass.getQualifiedName();
              if (className != null) {
                LocalQuickFix fix = null;

                OsmorcFacetConfiguration configuration = facet.getConfiguration();
                if (configuration.isManifestManuallyEdited()) {
                  BundleManager bundleManager = ServiceManager.getService(project, BundleManager.class);
                  BundleManifest manifest = bundleManager.getManifestByObject(facet.getModule());
                  if (manifest != null && !className.equals(manifest.getBundleActivator())) {
                    fix = new RegisterInManifestQuickfix(className, manifest.getManifestFile());
                  }
                }
                else {
                  if (!className.equals(configuration.getBundleActivator())) {
                    fix = new RegisterInConfigurationQuickfix(className, configuration);
                  }
                }

                if (fix != null) {
                  PsiIdentifier identifier = psiClass.getNameIdentifier();
                  if (identifier != null) {
                    holder.registerProblem(identifier, OsmorcBundle.message("UnregisteredActivatorInspection.message"), fix);
                  }
                }
              }
            }
          }
        }
      }
    };
  }

  private static class RegisterInManifestQuickfix implements LocalQuickFix {
    private final String myActivatorClass;
    private final ManifestFile myManifestFile;

    private RegisterInManifestQuickfix(@NotNull String activatorClass, @NotNull ManifestFile manifestFile) {
      myActivatorClass = activatorClass;
      myManifestFile = manifestFile;
    }

    @NotNull
    @Override
    public String getName() {
      return OsmorcBundle.message("UnregisteredActivatorInspection.fix.manifest");
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return OsmorcBundle.message("inspection.group");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      if (CommonRefactoringUtil.checkReadOnlyStatus(myManifestFile)) {
        Header activatorHeader = myManifestFile.getHeader(Constants.BUNDLE_ACTIVATOR);
        Header newHeader = OsgiPsiUtil.createHeader(project, Constants.BUNDLE_ACTIVATOR, myActivatorClass);
        if (activatorHeader != null) {
          activatorHeader.replace(newHeader);
        }
        else {
          addHeader(newHeader);
        }
      }
    }

    private void addHeader(Header newHeader) {
      Section section = myManifestFile.getMainSection();
      List<Header> headers = myManifestFile.getHeaders();
      if (section == null) {
        myManifestFile.add(newHeader.getParent());
      }
      else if (headers.isEmpty()) {
        section.addBefore(newHeader, section.getFirstChild());
      }
      else {
        section.addAfter(newHeader, headers.get(headers.size() - 1));
      }
    }
  }

  private static class RegisterInConfigurationQuickfix implements LocalQuickFix {
    private final String myActivatorClass;
    private final OsmorcFacetConfiguration myConfiguration;

    private RegisterInConfigurationQuickfix(@NotNull String activatorClass, @NotNull OsmorcFacetConfiguration configuration) {
      myActivatorClass = activatorClass;
      myConfiguration = configuration;
    }

    @NotNull
    @Override
    public String getName() {
      return OsmorcBundle.message("UnregisteredActivatorInspection.fix.config");
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return OsmorcBundle.message("inspection.group");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      myConfiguration.setBundleActivator(myActivatorClass);
    }
  }
}
