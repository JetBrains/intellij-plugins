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
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.BundleManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.frameworkintegration.LibraryHandler;
import org.osmorc.settings.ProjectSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class InvalidImportInspection extends LocalInspectionTool {
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
        return "Invalid Import";
    }

    @NonNls
    @NotNull
    public String getShortName() {
        return "osmorcInvalidImport";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitReferenceExpression(PsiReferenceExpression expression) {
            }

            @Override
            public void visitNewExpression(PsiNewExpression expression) {
                // only check this if the manifest is manually edited
                if (OsmorcFacet.hasOsmorcFacet(expression) &&
                        OsmorcFacet.getInstance(expression).getConfiguration().isManifestManuallyEdited()) {
                    PsiJavaCodeReferenceElement classReference = expression.getClassReference();
                    if (classReference != null &&
                            isInvalid(classReference.resolve(), ModuleUtil.findModuleForPsiElement(expression))) {
                        holder.registerProblem(expression, "Invalid import", ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                    }
                }
            }

            @Override
            public void visitVariable(PsiVariable variable) {
                // only check this if the manifest is manually edited
                if (OsmorcFacet.hasOsmorcFacet(variable) &&
                        OsmorcFacet.getInstance(variable).getConfiguration().isManifestManuallyEdited()) {
                    PsiClass psiClass = null;
                    PsiType psiType = variable.getType();
                    if (psiType instanceof PsiClassType) {
                        psiClass = ((PsiClassType) psiType).resolve();
                    }
                    if (isInvalid(psiClass, ModuleUtil.findModuleForPsiElement(variable))) {
                        holder.registerProblem(variable, "Invalid import", ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                    }
                }
            }

            @Override
            public void visitImportStatement(PsiImportStatement statement) {
                // only check this if the manifest is manually edited
                if (OsmorcFacet.hasOsmorcFacet(statement) &&
                        OsmorcFacet.getInstance(statement).getConfiguration().isManifestManuallyEdited()) {
                    PsiElement element = statement.resolve();

                    if (element == null) {
                        PsiJavaCodeReferenceElement importReference = statement.getImportReference();
                        if (importReference != null) {
                            String importText = importReference.getText();
                            if (importText.endsWith(";")) {
                                importText = importText.substring(0, importText.length() - 1);
                            }
                            if (importText.endsWith(".*")) {
                                importText = importText.substring(0, importText.length() - 2);
                            }

                            PsiReference reference = importReference.findReferenceAt(importText.length() - 1);
                            if (reference != null) {
                                element = reference.resolve();
                            }
                        }
                    }

                    if (isInvalid(element, ModuleUtil.findModuleForPsiElement(statement))) {
                        holder.registerProblem(statement, "Invalid import", ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                    }
                }
            }
        };
    }

    protected boolean isInvalid(PsiElement element, Module usingModule) {
        boolean result = false;
        if (element != null) {
            if (element instanceof PsiPackage) {
                PsiDirectory[] directories = ((PsiPackage) element).getDirectories();
                for (PsiDirectory directory : directories) {
                    result = checkInvalidity(directory, usingModule);
                    if (!result) {
                        break;
                    }
                }
            } else {
                final PsiFile containingFile = element.getContainingFile();
                if (containingFile != null) {
                    final PsiDirectory folder = containingFile.getContainingDirectory();
                    if (folder != null) {
                        result = checkInvalidity(folder, usingModule);
                    }
                }
            }
        }
        return result;
    }

    private boolean checkInvalidity(final @NotNull PsiDirectory folder, final @NotNull Module usingModule) {
        BundleManager bundleManager = ServiceManager.getService(folder.getProject(), BundleManager.class);
        final Module containingModule = ModuleUtil.findModuleForFile(folder.getVirtualFile(), usingModule.getProject());

        boolean result = false;
        boolean containingModuleIsHost = false;

        Collection<Object> usingModuleHostBundles = bundleManager.getHostBundles(usingModule);
        for (Object usingModuleHostBundle : usingModuleHostBundles) {
            if (usingModuleHostBundle == containingModule) {
                containingModuleIsHost = true;
                break;
            }
        }

        if (!containingModuleIsHost) {
            final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(usingModule.getProject()).getFileIndex();
            final List<OrderEntry> entriesForFile = projectFileIndex.getOrderEntriesForFile(folder.getVirtualFile());
            Collection<Object> containiingModuleBundleHosts;
            if (containingModule != null) {
                containiingModuleBundleHosts = bundleManager.getHostBundles(containingModule);
            } else {
                containiingModuleBundleHosts = new ArrayList<Object>();
            }
            LibraryHandler libraryHandler = ServiceManager.getService(LibraryHandler.class);

            for (OrderEntry orderEntry : entriesForFile) {
                Module orderEntryModule = orderEntry.getOwnerModule();
                if (orderEntryModule == usingModule) {
                    if (orderEntry instanceof LibraryOrderEntry &&
                            libraryHandler.isFrameworkInstanceLibrary(((LibraryOrderEntry) orderEntry))) {
                        final Library library = ((LibraryOrderEntry) orderEntry).getLibrary();
                        if (library != null) {
                            result = !isLibraryExportingPackageTo(library, folder, usingModule, bundleManager);
                        }
                    } else if (orderEntry instanceof ModuleOrderEntry) {
                        Module module = ((ModuleOrderEntry) orderEntry).getModule();
                        if (containingModule != null) {
                            result = !isBundleExportingContainingPackageTo(module, containingModule, usingModule, folder, bundleManager);
                            if (result) {
                                for (Object containiingBundleHost : containiingModuleBundleHosts) {
                                    result = !isBundleExportingContainingPackageTo(containiingBundleHost, containiingBundleHost, usingModule, folder, bundleManager);
                                    if (!result) {
                                        break;
                                    }
                                }
                            }
                        } else {
                            ProjectSettings projectSettings = ServiceManager.getService(usingModule.getProject(), ProjectSettings.class);
                            String frameworkInstanceName = projectSettings.getFrameworkInstanceName();
                            if (frameworkInstanceName != null) {
                                List<Library> libraries = libraryHandler.getLibraries(frameworkInstanceName);
                                for (Library library : libraries) {
                                    VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
                                    for (VirtualFile file : files) {
                                        if (VfsUtil.isAncestor(file, folder.getVirtualFile(), false)) {
                                            result = !isLibraryExportingPackageTo(library, folder, usingModule, bundleManager);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }

    private boolean isLibraryExportingPackageTo(Library library, PsiDirectory folder, Module usingModule,
                                                BundleManager bundleManager) {
        boolean result = isBundleExportingContainingPackageTo(library, library, usingModule, folder, bundleManager);
        if (!result) {
            Collection<Object> hostBundles = bundleManager.getHostBundles(library);
            for (Object bundleHost : hostBundles) {
                result = isBundleExportingContainingPackageTo(bundleHost, bundleHost, usingModule, folder, bundleManager);
                if (result) {
                    break;
                }
            }
        }
        return result;
    }

    private boolean isBundleExportingContainingPackageTo(final @NotNull Object exportingBundle,
                                                         final @NotNull Object containingBundle,
                                                         final @NotNull Object importingBundle,
                                                         final @NotNull PsiDirectory folder,
                                                         final @NotNull BundleManager bundleManager) {
        PsiPackage containingPackage = JavaDirectoryService.getInstance().getPackage(folder);
        BundleDescription exporterDescription = bundleManager.getBundleDescription(exportingBundle);
        BundleDescription containerDescription = bundleManager.getBundleDescription(containingBundle);
        BundleDescription importerDescription = bundleManager.getBundleDescription(importingBundle);

        assert importerDescription != null;
        assert containingPackage != null;

        return containerDescription == null ||
                isDirectImportExport(containingPackage, exporterDescription, importerDescription, bundleManager) ||
                isContainerExportingPackage(containingPackage, containerDescription) &&
                        isRecursiveRequired(importerDescription, containerDescription, bundleManager);

    }

    private boolean isContainerExportingPackage(PsiPackage containingPackage, BundleDescription containerDescription) {
        boolean containerExportsPackage = false;
        final ExportPackageDescription[] exportPackageDescriptions = containerDescription.getExportPackages();
        for (ExportPackageDescription exportPackageDescription : exportPackageDescriptions) {
            if (exportPackageDescription.getName().equals(containingPackage.getQualifiedName())) {
                containerExportsPackage = true;
                break;
            }
        }
        return containerExportsPackage;
    }

    private boolean isDirectImportExport(PsiPackage containingPackage, BundleDescription exporterDescription,
                                         BundleDescription importerDescription, BundleManager bundleManager) {
        final List<ExportPackageDescription> resolvedImports = bundleManager.getResolvedImports(importerDescription.getUserObject());
        for (ExportPackageDescription resolvedImport : resolvedImports) {
            if (resolvedImport.getName().equals(containingPackage.getQualifiedName()) &&
                    resolvedImport.getExporter() == exporterDescription) {
                return true;
            }
        }
        return false;
    }

    private boolean isRecursiveRequired(final @NotNull BundleDescription requirer,
                                        final @NotNull BundleDescription required, BundleManager bundleManager) {
        final List<BundleDescription> resolvedRequires = bundleManager.getResolvedRequires(requirer.getUserObject());
        for (BundleDescription resolvedRequire : resolvedRequires) {
            if (required == resolvedRequire) {
                return true;
            }
        }
        for (BundleDescription resolvedRequire : resolvedRequires) {
            final BundleSpecification[] requiredBundles = resolvedRequire.getRequiredBundles();
            for (BundleSpecification requiredBundle : requiredBundles) {
                if (requiredBundle.isExported()) {
                    final BundleDescription supplier = (BundleDescription) requiredBundle.getSupplier();
                    if (supplier == required) {
                        return true;
                    } else if (supplier != null && isRecursiveRequired(supplier, required, bundleManager)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
