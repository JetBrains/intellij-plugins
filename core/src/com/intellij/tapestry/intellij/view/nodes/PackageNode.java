package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.tapestry.core.exceptions.NotFoundException;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.Library;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.util.Icons;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.view.TapestryProjectViewPane;

import java.util.TreeSet;

public class PackageNode extends TapestryNode {

    private Library _library;

    public PackageNode(Library library, PsiDirectory psiDirectory, Module module, AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);
        _library = library;

        init(psiDirectory, new PresentationData(psiDirectory.getName(), psiDirectory.getName(), Icons.PACKAGE_ICON, Icons.PACKAGE_ICON, null));
    }

    public PackageNode(PsiDirectory psiDirectory, Module module, AbstractTreeBuilder treeBuilder) {
        this(null, psiDirectory, module, treeBuilder);
    }

    /**
     * {@inheritDoc}
     */
    public SimpleNode[] getChildren() {
        final TreeSet<TapestryNode> children = new TreeSet<TapestryNode>(PackageNodesComparator.getInstance());

        for (PsiDirectory psiDirectory : ((PsiDirectory) getElement()).getSubdirectories()) {
            PackageNode node = createNewNode(psiDirectory);
            if (node != null) {
                children.add(createNewNode(psiDirectory));
            }
        }

        for (PsiFile psiFile : ((PsiDirectory) getElement()).getFiles()) {
            if (psiFile.getFileType().equals(StdFileTypes.JAVA) || psiFile.getFileType().equals(StdFileTypes.CLASS)) {
                try {
                    if (IdeaUtils.findPublicClass(((PsiJavaFile) psiFile).getClasses()) == null || !TapestryProjectViewPane.getInstance(myProject).isGroupElementFiles()) {
                        throw new NotTapestryElementException("");
                    }

                    PresentationLibraryElement element;
                    if (_library == null) {
                        element = PresentationLibraryElement.createProjectElementInstance((IJavaClassType) IdeaUtils.createJavaTypeFromPsiType(_module,
                                JavaPsiFacade.getInstance(_module.getProject()).getElementFactory().createType(IdeaUtils.findPublicClass(((PsiJavaFile) psiFile).getClasses()))),
                                TapestryModuleSupportLoader.getTapestryProject(_module)
                        );
                    } else {
                        element = PresentationLibraryElement.createElementInstance(
                                _library,
                                new IntellijJavaClassType(getModule(), IdeaUtils.findPublicClass(((PsiJavaFile) psiFile).getClasses()).getContainingFile()),
                                TapestryModuleSupportLoader.getTapestryProject(_module)
                        );
                    }

                    switch (element.getElementType()) {
                        case PAGE:
                            children.add(new PageNode(element, _module, _treeBuilder));
                            break;
                        case COMPONENT:
                            children.add(new ComponentNode(element, _module, _treeBuilder));
                            break;
                        case MIXIN:
                            children.add(new MixinNode(element, _module, _treeBuilder));
                            break;
                    }
                } catch (NotTapestryElementException e) {
                    children.add(new ClassNode((PsiJavaFile) psiFile, _module, _treeBuilder));
                }
            }

            if (psiFile.getFileType().equals(StdFileTypes.HTML) && !TapestryProjectViewPane.getInstance(myProject).isGroupElementFiles()) {
                children.add(new FileNode(psiFile, _module, _treeBuilder));
            }

            if (psiFile.getFileType().equals(StdFileTypes.PROPERTIES) && !TapestryProjectViewPane.getInstance(myProject).isGroupElementFiles()) {
                children.add(new FileNode(psiFile, _module, _treeBuilder));
            }
        }

        return children.toArray(new TapestryNode[children.size()]);
    }

    public Library getLibrary() {
        return _library;
    }

    public static PsiPackage getPackage(PsiDirectory psiDirectory) {
        Project project = psiDirectory.getProject();
        ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        String packageName = projectFileIndex.getPackageNameByDirectory(psiDirectory.getVirtualFile());
        if (packageName == null) return null;
        return JavaPsiFacade.getInstance(project).findPackage(packageName);
    }

    private PackageNode createNewNode(PsiDirectory psiDirectory) {
        try {
            TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(_module);
            String applicationRootPackage = tapestryProject.getApplicationRootPackage();
            if (applicationRootPackage != null) {
                String packageName = IdeaUtils.getPackage(psiDirectory).getQualifiedName();
                if (applicationRootPackage.equals(packageName)) {
                    return new LibraryNode(tapestryProject.getApplicationLibrary(), psiDirectory, _module, _treeBuilder);
                } else
                if (tapestryProject.getPagesRootPackage().equals(packageName)) {
                    return new PagesNode(psiDirectory, _module, _treeBuilder);
                } else
                if (tapestryProject.getComponentsRootPackage().equals(packageName)) {
                    return new ComponentsNode(psiDirectory, _module, _treeBuilder);
                } else
                if (tapestryProject.getMixinsRootPackage().equals(packageName)) {
                    return new MixinsNode(psiDirectory, _module, _treeBuilder);
                }
            }
        } catch (NotFoundException e) {
            return null;
        }

        return new PackageNode(psiDirectory, _module, _treeBuilder);
    }
}
