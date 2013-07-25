package com.intellij.coldFusion.model.files;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 06.10.2008
 */
public class CfmlFileViewProviderFactory implements FileViewProviderFactory {
    public FileViewProvider createFileViewProvider(@NotNull VirtualFile file, com.intellij.lang.Language language, @NotNull PsiManager manager, boolean physical) {
        return new CfmlFileViewProvider(manager, file, physical);
    }
}
