package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.lang.properties.PropertiesFileType;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.view.TapestryProjectViewPane;
import com.intellij.tapestry.lang.TmlFileType;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.TreeSet;

public class PackageNode extends TapestryNode {

    private final TapestryLibrary _library;

    public PackageNode(TapestryLibrary library, PsiDirectory psiDirectory, Module module, AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);
        _library = library;

        init(psiDirectory, new PresentationData(psiDirectory.getName(), psiDirectory.getName(),
                                                PlatformIcons.PACKAGE_ICON, null));
    }

    public PackageNode(PsiDirectory psiDirectory, Module module, AbstractTreeBuilder treeBuilder) {
        this(null, psiDirectory, module, treeBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleNode @NotNull [] getChildren() {
        final TreeSet<TapestryNode> children = new TreeSet<>(PackageNodesComparator.getInstance());

        for (PsiDirectory psiDirectory : ((PsiDirectory) getElement()).getSubdirectories()) {
            PackageNode node = createNewNode(psiDirectory);
            if (node != null) {
                children.add(createNewNode(psiDirectory));
            }
        }

        for (PsiFile psiFile : ((PsiDirectory) getElement()).getFiles()) {
            if (psiFile instanceof PsiClassOwner) {
                try {
                    if (IdeaUtils.findPublicClass(psiFile) == null || !TapestryProjectViewPane.getInstance(myProject).isGroupElementFiles()) {
                        throw new NotTapestryElementException("");
                    }

                    PresentationLibraryElement element;
                    if (_library == null) {
                        element = PresentationLibraryElement.createProjectElementInstance((IJavaClassType) IdeaUtils.createJavaTypeFromPsiType(_module,
                                JavaPsiFacade.getInstance(_module.getProject()).getElementFactory().createType(IdeaUtils.findPublicClass(psiFile))),
                                TapestryModuleSupportLoader.getTapestryProject(_module)
                        );
                    } else {
                        element = PresentationLibraryElement.createElementInstance(
                                _library,
                                new IntellijJavaClassType(getModule(), IdeaUtils.findPublicClass(psiFile).getContainingFile()),
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
                    children.add(new ClassNode((PsiClassOwner)psiFile, _module, _treeBuilder));
                }
            }

            if (psiFile.getFileType().equals(TmlFileType.INSTANCE) && !TapestryProjectViewPane.getInstance(myProject).isGroupElementFiles()) {
                children.add(new FileNode(psiFile, _module, _treeBuilder));
            }

            if (psiFile.getFileType().equals(PropertiesFileType.INSTANCE) && !TapestryProjectViewPane.getInstance(myProject).isGroupElementFiles()) {
                children.add(new FileNode(psiFile, _module, _treeBuilder));
            }
        }

        return children.toArray(new TapestryNode[0]);
    }

  @Nullable
  private PackageNode createNewNode(PsiDirectory psiDirectory) {
    final PsiPackage aPackage = IdeaUtils.getPackage(psiDirectory);
    if (aPackage == null) return null;
    TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(_module);
    if (tapestryProject == null) return null;
    String applicationRootPackage = tapestryProject.getApplicationRootPackage();
    String packageName = aPackage.getQualifiedName();
    if (packageName.equals(applicationRootPackage)) {
      return new LibraryNode(tapestryProject.getApplicationLibrary(), psiDirectory, _module, _treeBuilder);
    }
    if (packageName.equals(tapestryProject.getPagesRootPackage())) {
      return new PagesNode(psiDirectory, _module, _treeBuilder);
    }
    if (packageName.equals(tapestryProject.getComponentsRootPackage())) {
      return new ComponentsNode(psiDirectory, _module, _treeBuilder);
    }
    if (packageName.equals(tapestryProject.getMixinsRootPackage())) {
      return new MixinsNode(psiDirectory, _module, _treeBuilder);
    }

    return new PackageNode(psiDirectory, _module, _treeBuilder);
  }
}
