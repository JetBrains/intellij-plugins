package com.intellij.tapestry.intellij.view;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TapestryIdeView implements IdeView {
    private final TapestryProjectViewPane _viewPane;

    protected TapestryIdeView(TapestryProjectViewPane viewPane) {
        _viewPane = viewPane;
    }

    @Override
    public PsiDirectory @NotNull [] getDirectories() {
        final List<PsiDirectory> directories = new ArrayList<>();
        final ModuleFileIndex moduleFileIndex = ModuleRootManager.getInstance((Module) _viewPane.getData(PlatformCoreDataKeys.MODULE.getName())).getFileIndex();
        moduleFileIndex.iterateContent(
          virtualfile -> {
              if (virtualfile.isDirectory() && moduleFileIndex.isInSourceContent(virtualfile)) {
                  directories.add(PsiManager.getInstance(_viewPane.getProject()).findDirectory(virtualfile));
              }
              return true;
          }
        );
        return directories.toArray(PsiDirectory.EMPTY_ARRAY);
    }

    @Override
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