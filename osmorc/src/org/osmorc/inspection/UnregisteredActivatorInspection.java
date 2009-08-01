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
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.BundleActivator;
import org.osmorc.BundleManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.lang.psi.Header;
import org.osmorc.manifest.lang.psi.Section;
import org.osmorc.manifest.lang.ManifestTokenType;

/**
 * Inspection that reports classes implementing BundleActivator which are not registered in the manifest / facet
 * config.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 * @version $Id$
 */
public class UnregisteredActivatorInspection extends LocalInspectionTool {

    @Nls
    @NotNull
    public String getGroupDisplayName() {
        return "OSGi";
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    @NotNull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @Nls
    @NotNull
    public String getDisplayName() {
        return "Bundle Activator not registered";
    }

    @NonNls
    @NotNull
    public String getShortName() {
        return "osmorcUnregisteredActivator";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            public void visitReferenceExpression(PsiReferenceExpression expression) {
            }

            @Override
            public void visitClass(PsiClass psiClass) {
                if (OsmorcFacet.hasOsmorcFacet(psiClass)) {
                    PsiType[] types = psiClass.getSuperTypes();
                    for (PsiType type : types) {
                        if (type.equalsToText(BundleActivator.class.getName())) {
                            // okay extends bundle activator
                            OsmorcFacetConfiguration configuration = OsmorcFacet.getInstance(psiClass).getConfiguration();

                            String activatorName = psiClass.getQualifiedName();
                            // if manifest is manually written, look it up in the manifest file
                            if (configuration.isManifestManuallyEdited()) {
                                BundleManager bundleManager = ServiceManager.getService(psiClass.getProject(), BundleManager.class);
                                Module module = ModuleUtil.findModuleForPsiElement(psiClass);
                                if (!isActivatorRegistered(bundleManager, module, activatorName)) {
                                    assert activatorName != null;
                                    holder.registerProblem(psiClass.getNameIdentifier(), "Bundle activator is not registered in manifest.",
                                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new RegisterActivatorInManifestQuickfix(
                                                    activatorName, bundleManager.getBundleManifest(module).getManifestFile()));
                                }
                            } else {
                                // automagically, so look it up in the configuration
                                String configuredActivator = configuration.getBundleActivator();
                                if (!configuredActivator.equals(activatorName)) {
                                    holder.registerProblem(psiClass.getNameIdentifier(),
                                            "Bundle activator is not set up in facet configuration.",
                                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                            new RegisterActivatorInConfigurationQuickfix(activatorName, configuration));
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private boolean isActivatorRegistered(BundleManager manager, Object bundle, String activatorName) {
        BundleManifest manifest = manager.getBundleManifest(bundle);
        if (manifest != null) {
            String manifestActivator = manifest.getBundleActivator();
            return manifestActivator != null && manifestActivator.equals(activatorName);
        }
        return true;
    }

    private class RegisterActivatorInManifestQuickfix implements LocalQuickFix {
        private static final String NAME = "Register Activator In Manifest";
        private static final String FAMILY = "Osmorc";
        private final String activatorClassName;
        private final PsiFile manifestFile;

        private RegisterActivatorInManifestQuickfix(@NotNull final String activatorClassName, @NotNull final PsiFile manifestFile) {
            this.activatorClassName = activatorClassName;
            this.manifestFile = manifestFile;
        }

        @NotNull
        public String getName() {
            return NAME;
        }

        @NotNull
        public String getFamilyName() {
            return FAMILY;
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            Section mainSection = (Section) manifestFile.getFirstChild();

            Header activatorHeader = null;
            Header currentHeader = PsiTreeUtil.getChildOfType(mainSection, Header.class);
            while (activatorHeader == null && currentHeader != null) {
                if ("Bundle-Activator".equalsIgnoreCase(currentHeader.getName())) {
                    activatorHeader = currentHeader;
                }
                currentHeader = PsiTreeUtil.getNextSiblingOfType(currentHeader, Header.class);
            }

            if (activatorHeader != null) {
                replaceExistingActivatorHeader(activatorHeader);
            } else {
                addActivatorHeader();
            }
        }

        private void addActivatorHeader() {
            PsiFile fromText = PsiFileFactory.getInstance(manifestFile.getProject()).createFileFromText("DUMMY.MF",
                    String.format("Bundle-Activator: %s\n", activatorClassName));
            Header newheader = PsiTreeUtil.getChildOfType(fromText.getFirstChild(), Header.class);
            assert newheader != null;

            Section section = (Section) manifestFile.getFirstChild();
            addMissiingNewline(section);
            section.add(newheader);
        }

        private void addMissiingNewline(Section section) {
            String sectionText = section.getText();
            if (sectionText.charAt(sectionText.length() - 1) != '\n') {
                PsiElement lastChild = section.getLastChild();
                if (lastChild instanceof Header) {
                    Header header = (Header) lastChild;
                    header.getNode().addLeaf(ManifestTokenType.NEWLINE, "\n", null);
                }
            }
        }

        private void replaceExistingActivatorHeader(Header activatorHeader) {
            String headerFormatString = "Bundle-Activator: %s\n";
            PsiFile fromText = PsiFileFactory.getInstance(manifestFile.getProject()).createFileFromText("DUMMY.MF",
                    String.format(headerFormatString, activatorClassName));
            Header newheader = PsiTreeUtil.getChildOfType(fromText.getFirstChild(), Header.class);
            assert newheader != null;
            activatorHeader.replace(newheader);
        }
    }

    private class RegisterActivatorInConfigurationQuickfix implements LocalQuickFix {
        private static final String NAME = "Register Activator In Configuration";
        private static final String FAMILY = "Osmorc";
        private final String activatorClassName;
        private final OsmorcFacetConfiguration configuration;

        private RegisterActivatorInConfigurationQuickfix(@NotNull final String activatorClassName, @NotNull final OsmorcFacetConfiguration configuration) {
            this.activatorClassName = activatorClassName;
            this.configuration = configuration;
        }

        @NotNull
        public String getName() {
            return NAME;
        }

        @NotNull
        public String getFamilyName() {
            return FAMILY;
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            configuration.setBundleActivator(activatorClassName);
        }
    }


}
