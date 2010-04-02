package com.intellij.tapestry.intellij.view;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TapestryIdeView implements IdeView {

    private final TapestryProjectViewPane _viewPane;

    protected TapestryIdeView(TapestryProjectViewPane viewPane) {
        _viewPane = viewPane;
    }

    public void selectElement(PsiElement element) {
    }

    public PsiDirectory[] getDirectories() {
        final List<PsiDirectory> directories = new ArrayList<PsiDirectory>();
        final ModuleFileIndex moduleFileIndex = ModuleRootManager.getInstance((Module) _viewPane.getData(DataKeys.MODULE.getName())).getFileIndex();
        moduleFileIndex.iterateContent(
                new ContentIterator() {
                    public boolean processFile(VirtualFile virtualfile) {
                        if (virtualfile.isDirectory() && moduleFileIndex.isInSourceContent(virtualfile)) {
                            directories.add(PsiManager.getInstance(_viewPane.getProject()).findDirectory(virtualfile));
                        }
                        return true;
                    }
                }
        );
        return directories.toArray(new PsiDirectory[0]);
    }

    @Nullable
    public PsiDirectory getOrChooseDirectory() {
        Object element = _viewPane.getSelectedDescriptor().getElement();

        if (element instanceof PsiDirectory) {
            return (PsiDirectory) element;
        }

        if (element instanceof PsiFile) {
            return ((PsiFile) element).getContainingDirectory();
        }

        return null;
    }
}
