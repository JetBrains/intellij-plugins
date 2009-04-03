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
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IntellijResourceFinder implements IResourceFinder {

    private Module _module;

    public IntellijResourceFinder(Module module) {
        _module = module;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<IResource> findClasspathResource(String path, boolean includeDependencies) {
        Collection<IResource> resources = new ArrayList<IResource>();
        GlobalSearchScope searchScope;
        if (includeDependencies) {
            searchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(_module, false);
        } else {
            searchScope = GlobalSearchScope.moduleScope(_module);
        }

        PsiPackage psiPackage = JavaPsiFacade.getInstance(_module.getProject()).findPackage(PathUtils.pathIntoPackage(path, true));
        if (psiPackage == null) {
            return resources;
        }

        PsiDirectory[] packageDirectories = psiPackage.getDirectories(searchScope);

        for (PsiDirectory directory : packageDirectories) {
            PsiFile resource = directory.findFile(PathUtils.getLastPathElement(path));
            if (resource != null) {
                resources.add(new IntellijResource(resource));
            }
        }

        return resources;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<IResource> findLocalizedClasspathResource(String path, boolean includeDependencies) {
        Collection<IResource> resources = new ArrayList<IResource>();
        GlobalSearchScope searchScope;
        if (includeDependencies) {
            searchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(_module, false);
        } else {
            searchScope = GlobalSearchScope.moduleScope(_module);
        }

        PsiPackage psiPackage = JavaPsiFacade.getInstance(_module.getProject()).findPackage(PathUtils.pathIntoPackage(path, true));
        if (psiPackage == null) {
            return resources;
        }

        PsiDirectory[] packageDirectories = psiPackage.getDirectories(searchScope);
        String filename = PathUtils.getLastPathElement(path);
        for (PsiDirectory directory : packageDirectories) {
            for (PsiFile file : directory.getFiles()) {
                if (LocalizationUtils.unlocalizeFileName(file.getName()).equals(filename)) {
                    resources.add(new IntellijResource(file));
                }
            }
        }

        return resources;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    public IResource findContextResource(String path) {
        WebFacet webFacet = IdeaUtils.getWebFacet(_module);

        if (webFacet == null)
            return null;

        List<WebRoot> webRoots = webFacet.getWebRoots(false);

        for (WebRoot webRoot : webRoots) {
            String relativePath = path;
            if (PathUtils.toUnixPath(path).startsWith(webRoot.getRelativePath())) {
                relativePath = PathUtils.toUnixPath(path).substring(webRoot.getRelativePath().length() + (webRoot.getRelativePath().endsWith("/") ? 0 : 1));
            }

            if (webRoot.getFile() != null) {
                VirtualFile virtualFile = webRoot.getFile().findFileByRelativePath(relativePath);
                if (virtualFile != null) {
                    return new IntellijResource(PsiManager.getInstance(_module.getProject()).findFile(virtualFile));
                }
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    public Collection<IResource> findLocalizedContextResource(String path) {
        Collection<IResource> resources = new ArrayList<IResource>();

        WebFacet webFacet = IdeaUtils.getWebFacet(_module);
        if (webFacet == null)
            return resources;

        List<WebRoot> webRoots = webFacet.getWebRoots(false);

        String filename = PathUtils.getLastPathElement(path);

        for (WebRoot webRoot : webRoots) {
            String relativePath = path;
            if (PathUtils.toUnixPath(path).startsWith(webRoot.getRelativePath())) {
                relativePath = PathUtils.toUnixPath(path).substring(webRoot.getRelativePath().length() + (webRoot.getRelativePath().endsWith("/") ? 0 : 1));
            }

            String parentPath = PathUtils.removeLastFilePathElement(relativePath, true);
            VirtualFile parentVirtualFile;

            if (parentPath.length() > 0) {
                parentVirtualFile = webRoot.getFile().findFileByRelativePath(parentPath);
            } else {
                parentVirtualFile = webRoot.getFile();
            }

            if (parentVirtualFile != null) {
                for (VirtualFile file : parentVirtualFile.getChildren()) {
                    if (LocalizationUtils.unlocalizeFileName(file.getName()).equals(filename)) {
                        resources.add(new IntellijResource(PsiManager.getInstance(_module.getProject()).findFile(file)));
                    }
                }
            }
        }

        return resources;
    }

    /**
     * //@TODO in IDEA 7.0 should be able to use PsiManager.findPackage("META-INF")
     * {@inheritDoc}
     */
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