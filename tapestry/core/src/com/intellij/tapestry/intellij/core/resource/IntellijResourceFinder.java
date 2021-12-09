package com.intellij.tapestry.intellij.core.resource;

import com.intellij.javaee.web.WebRoot;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.core.resource.IResourceFinder;
import com.intellij.tapestry.core.util.LocalizationUtils;
import com.intellij.tapestry.core.util.PathUtils;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IntellijResourceFinder implements IResourceFinder {

    private final Module _module;

    public IntellijResourceFinder(Module module) {
        _module = module;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IResource> findClasspathResource(String path, boolean includeDependencies) {
        Collection<IResource> resources = new ArrayList<>();

      String filename = PathUtils.getLastPathElement(path);
        for (PsiDirectory directory : findPackageDirectories(path, includeDependencies)) {
            PsiFile resource = directory.findFile(filename);
            if (resource != null) {
                resources.add(new IntellijResource(resource));
            }
        }

        return resources;
    }

  private PsiDirectory[] findPackageDirectories(String path, boolean includeDependencies) {
    PsiPackage psiPackage = JavaPsiFacade.getInstance(_module.getProject()).findPackage(PathUtils.pathIntoPackage(path, true));
    return psiPackage == null ? PsiDirectory.EMPTY_ARRAY : psiPackage.getDirectories(getSearchScope(includeDependencies));
  }

  /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IResource> findLocalizedClasspathResource(String path, boolean includeDependencies) {
        Collection<IResource> resources = new ArrayList<>();

        String filename = PathUtils.getLastPathElement(path);
        for (PsiDirectory directory : findPackageDirectories(path, includeDependencies)) {
            for (PsiFile file : directory.getFiles()) {
                if (LocalizationUtils.unlocalizeFileName(file.getName()).equals(filename)) {
                    resources.add(new IntellijResource(file));
                }
            }
        }

        return resources;
    }

  private GlobalSearchScope getSearchScope(boolean includeDependencies) {
    if (includeDependencies) {
        return GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(_module, false);
    }
    return GlobalSearchScope.moduleScope(_module);
  }

  /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public IResource findContextResource(String path) {
        WebFacet webFacet = IdeaUtils.getWebFacet(_module);

        if (webFacet == null)
            return null;

        List<WebRoot> webRoots = webFacet.getWebRoots();

        for (WebRoot webRoot : webRoots) {
            String relativePath = path;
            if (PathUtils.toUnixPath(path).startsWith(webRoot.getRelativePath())) {
                relativePath = PathUtils.toUnixPath(path).substring(webRoot.getRelativePath().length() + (webRoot.getRelativePath().endsWith("/") ? 0 : 1));
            }

          final VirtualFile file = webRoot.getFile();
          if (file != null) {
                VirtualFile virtualFile = file.findFileByRelativePath(relativePath);
                if (virtualFile != null) {
                  final PsiFile psiFile = PsiManager.getInstance(_module.getProject()).findFile(virtualFile);
                  if (psiFile != null) {
                    return new IntellijResource(psiFile);
                  }
                }
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Collection<IResource> findLocalizedContextResource(String path) {
        Collection<IResource> resources = new ArrayList<>();

        WebFacet webFacet = IdeaUtils.getWebFacet(_module);
        if (webFacet == null)
            return resources;

        List<WebRoot> webRoots = webFacet.getWebRoots();

        String filename = PathUtils.getLastPathElement(path);

        for (WebRoot webRoot : webRoots) {
            String relativePath = path;
            if (PathUtils.toUnixPath(path).startsWith(webRoot.getRelativePath())) {
                relativePath = PathUtils.toUnixPath(path).substring(webRoot.getRelativePath().length() + (webRoot.getRelativePath().endsWith("/") ? 0 : 1));
            }

            String parentPath = PathUtils.removeLastFilePathElement(relativePath, true);


          final VirtualFile virtualFile = webRoot.getFile();
          VirtualFile parentVirtualFile = parentPath.length() > 0 && virtualFile != null
                                          ? virtualFile.findFileByRelativePath(parentPath)
                                          : virtualFile;
            if (parentVirtualFile != null) {
                for (VirtualFile file : parentVirtualFile.getChildren()) {
                    if (LocalizationUtils.unlocalizeFileName(file.getName()).equals(filename)) {
                      final PsiFile psiFile = PsiManager.getInstance(_module.getProject()).findFile(file);
                      if (psiFile != null) {
                        resources.add(new IntellijResource(psiFile));
                      }
                    }
                }
            }
        }

        return resources;
    }

    ///**
    // * //@TODO in IDEA 7.0 should be able to use PsiManager.findPackage("META-INF")
    // * {@inheritDoc}
    // */
    /*public Collection<IResource> findManifestResources() {
        Collection<IResource> manifests = new ArrayList<IResource>();

        for (LibraryLink libraryLink : JavaeeModuleProperties.getInstance(_module).getContainingLibraries()) {
            for (VirtualFile file : libraryLink.getLibrary().getFiles(OrderRootType.CLASSES))
                if (file instanceof com.intellij.openapi.vfs.impl.jar.VirtualFileImpl && (file.findChild("META-INF") != null || file.findChild("meta-inf") != null)) {
                    VirtualFile manifest = file.findChild("MANIFEST.MF");
                    if (manifest == null) {
                        manifest = file.findChild("manifest.mf");
                    }

                    if (manifest != null) {
                        manifests.add(new IntellijResource(PsiManager.getInstance(_module.getProject()).findFile(manifest)));
                    }
                }
        }

        return manifests;
    }*/
}
