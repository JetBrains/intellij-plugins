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
 * Inspection which will check, if a class or package is available inside the osgi context.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ClassUnavailableInspection extends LocalInspectionTool {
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
    return "Unavailable in OSGi container";
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
        if (OsmorcFacet.hasOsmorcFacet(expression) && OsmorcFacet.getInstance(expression).getConfiguration().isManifestManuallyEdited()) {
          PsiJavaCodeReferenceElement classReference = expression.getClassReference();
          if (classReference != null) {
            final AvailabilityCheckResult result = checkElement(classReference.resolve(), ModuleUtil.findModuleForPsiElement(expression));
            if (!result.isOk()) {
              holder.registerProblem(expression, result.getDescription(), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
            }
          }
        }
      }

      @Override
      public void visitVariable(PsiVariable variable) {
        // only check this if the manifest is manually edited
        if (OsmorcFacet.hasOsmorcFacet(variable) && OsmorcFacet.getInstance(variable).getConfiguration().isManifestManuallyEdited()) {
          PsiClass psiClass = null;
          PsiType psiType = variable.getType();
          if (psiType instanceof PsiClassType) {
            psiClass = ((PsiClassType)psiType).resolve();
          }
          final AvailabilityCheckResult result = checkElement(psiClass, ModuleUtil.findModuleForPsiElement(variable));
          if (!result.isOk()) {
            holder.registerProblem(variable, result.getDescription(), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
          }
        }
      }


      @Override
      public void visitImportStatement(PsiImportStatement statement) {
        // only check this if the manifest is manually edited
        if (OsmorcFacet.hasOsmorcFacet(statement) && OsmorcFacet.getInstance(statement).getConfiguration().isManifestManuallyEdited()) {
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

          final AvailabilityCheckResult result = checkElement(element, ModuleUtil.findModuleForPsiElement(statement));
          if (!result.isOk()) {
            holder.registerProblem(statement, result.getDescription(), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
          }
        }
      }
    };
  }

  @NotNull
  protected AvailabilityCheckResult checkElement(PsiElement element, Module usingModule) {
    AvailabilityCheckResult result = new AvailabilityCheckResult();
    if (element != null) {
      if (element instanceof PsiPackage) {
        PsiDirectory[] directories = ((PsiPackage)element).getDirectories();
        for (PsiDirectory directory : directories) {
          result = checkDirectory(directory, usingModule);
          if (!result.isOk()) {
            break;
          }
        }
      }
      else {
        final PsiFile containingFile = element.getContainingFile();
        if (containingFile != null) {
          final PsiDirectory folder = containingFile.getContainingDirectory();
          if (folder != null) {
            result = checkDirectory(folder, usingModule);
          }
        }
      }
    }
    return result;
  }

  /**
   * Checks if the package represented by <code>directory</code> is available to the given module.
   *
   * @param directory   the directory representing a package
   * @param usingModule the module for which the given package should be available.
   * @return
   */
  @NotNull
  private AvailabilityCheckResult checkDirectory(final @NotNull PsiDirectory directory, final @NotNull Module usingModule) {
    BundleManager bundleManager = ServiceManager.getService(directory.getProject(), BundleManager.class);
    final Module containingModule = ModuleUtil.findModuleForFile(directory.getVirtualFile(), usingModule.getProject());

    AvailabilityCheckResult result =
      new AvailabilityCheckResult(AvailabilityCheckResult.ResultType.SymbolIsNotExported, "No bundle provides this entity.");

    // first check if the dependency would be satisfied by one of the host bundles of this module.
    Collection<Object> usingModuleHostBundles = bundleManager.getHostBundles(usingModule);
    for (Object usingModuleHostBundle : usingModuleHostBundles) {
      if (usingModuleHostBundle == containingModule) {
        return result; // ok, the package is provided by the bundle itself or it's hosts.
      }
    }

    Collection<Object> containingBundles;
    if (containingModule != null) {
      containingBundles = bundleManager.getHostBundles(containingModule);
    }
    else {
      containingBundles = new ArrayList<Object>();
    }

    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(usingModule.getProject()).getFileIndex();
    final List<OrderEntry> entriesForFile = projectFileIndex.getOrderEntriesForFile(directory.getVirtualFile());
    LibraryHandler libraryHandler = ServiceManager.getService(LibraryHandler.class);

    // check all orderentries which link to the specified directory (package)
    for (OrderEntry orderEntry : entriesForFile) {
      Module orderEntryModule = orderEntry.getOwnerModule();
      // and are imported by the usingModule
      if (orderEntryModule == usingModule) {
        if (orderEntry instanceof LibraryOrderEntry) {
          final Library library = ((LibraryOrderEntry)orderEntry).getLibrary();
          if (library != null) {
            result = isLibraryExportingPackageTo(library, directory, usingModule, bundleManager);
            if (result.isOk()) {
              break;
            }
          }
        }
        else if (orderEntry instanceof ModuleOrderEntry) {
          Module module = ((ModuleOrderEntry)orderEntry).getModule();
          if (containingModule != null) {
            result = isBundleExportingContainingPackageTo(module, containingModule, usingModule, directory, bundleManager);
            if (!result.isOk()) {
              for (Object containingBundleHost : containingBundles) {
                result =
                  isBundleExportingContainingPackageTo(containingBundleHost, containingBundleHost, usingModule, directory, bundleManager);
                if (result.isOk()) {
                  break;
                }
              }
            }
          }
          else {
            ProjectSettings projectSettings = ServiceManager.getService(usingModule.getProject(), ProjectSettings.class);
            String frameworkInstanceName = projectSettings.getFrameworkInstanceName();
            if (frameworkInstanceName != null) {
              List<Library> libraries = libraryHandler.getLibraries(frameworkInstanceName);
              for (Library library : libraries) {
                VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
                for (VirtualFile file : files) {
                  if (VfsUtil.isAncestor(file, directory.getVirtualFile(), false)) {
                    result = isLibraryExportingPackageTo(library, directory, usingModule, bundleManager);
                  }
                }
              }
            }
          }
        }
      }
    }
    return result;
  }

  private AvailabilityCheckResult isLibraryExportingPackageTo(Library library,
                                                              PsiDirectory folder,
                                                              Module usingModule,
                                                              BundleManager bundleManager) {
    // check if the library exports the package directly
    AvailabilityCheckResult result = isBundleExportingContainingPackageTo(library, library, usingModule, folder, bundleManager);
    if (!result.isOk()) {
      // if not, check it's fragment hosts and see if they export the package
      Collection<Object> hostBundles = bundleManager.getHostBundles(library);
      for (Object bundleHost : hostBundles) {
        result = isBundleExportingContainingPackageTo(bundleHost, bundleHost, usingModule, folder, bundleManager);
        if (result.isOk()) {
          break;
        }
      }
    }
    return result;
  }

  private AvailabilityCheckResult isBundleExportingContainingPackageTo(final @NotNull Object exportingBundle,
                                                                       final @NotNull Object containingBundle,
                                                                       final @NotNull Object importingBundle,
                                                                       final @NotNull PsiDirectory folder,
                                                                       final @NotNull BundleManager bundleManager) {
    PsiPackage containingPackage = JavaDirectoryService.getInstance().getPackage(folder);
    BundleDescription exporterDescription = bundleManager.getBundleDescription(exportingBundle);
    BundleDescription containerDescription = bundleManager.getBundleDescription(containingBundle);
    BundleDescription importerDescription = bundleManager.getBundleDescription(importingBundle);

    // FIXES Exception ID: 16964. getBundleDescription and JavaDirectoryService.getPackage are @Nullable
//        assert importerDescription != null;
//        assert containingPackage != null;

    if (importerDescription != null && containingPackage != null) {
      if (containerDescription == null ||
          isDirectImportExport(containingPackage, exporterDescription, importerDescription, bundleManager)) {
        return new AvailabilityCheckResult();
      }

      boolean containerExports = isContainerExportingPackage(containingPackage, containerDescription);
      boolean recursiveRequired = isRecursiveRequired(importerDescription, containerDescription, bundleManager);
      if (!containerExports) {
        return new AvailabilityCheckResult(AvailabilityCheckResult.ResultType.SymbolIsNotExported, "Bundle '" +
                                                                                                   bundleManager.getDisplayName(exportingBundle) +
                                                                                                   "' contains package '" +
                                                                                                   containingPackage.getQualifiedName() +
                                                                                                   "' but does not export it.");
      }
      // TODO: actually it could also not be exported somehwere but this case is not handled.
      if (!recursiveRequired) {
        return new AvailabilityCheckResult(AvailabilityCheckResult.ResultType.SymbolIsNotImported,
                                           "The package  '" + containingPackage.getQualifiedName() + "' is not imported in the manifest.");
      }
      return new AvailabilityCheckResult();
    }
    else {
      // TOOD: dunno if this is really ok.
      return new AvailabilityCheckResult();
    }

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

  private boolean isDirectImportExport(PsiPackage containingPackage,
                                       BundleDescription exporterDescription,
                                       BundleDescription importerDescription,
                                       BundleManager bundleManager) {
    final List<ExportPackageDescription> resolvedImports = bundleManager.getResolvedImports(importerDescription.getUserObject());
    for (ExportPackageDescription resolvedImport : resolvedImports) {
      if (resolvedImport.getName().equals(containingPackage.getQualifiedName()) && resolvedImport.getExporter() == exporterDescription) {
        return true;
      }
    }
    return false;
  }

  private boolean isRecursiveRequired(final @NotNull BundleDescription requirer,
                                      final @NotNull BundleDescription required,
                                      BundleManager bundleManager) {
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
          final BundleDescription supplier = (BundleDescription)requiredBundle.getSupplier();
          if (supplier == required) {
            return true;
          }
          else if (supplier != null && isRecursiveRequired(supplier, required, bundleManager)) {
            return true;
          }
        }
      }
    }
    return false;
  }

}
