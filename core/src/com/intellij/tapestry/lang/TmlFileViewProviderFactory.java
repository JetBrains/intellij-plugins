package com.intellij.tapestry.lang;

import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.lang.Language;

/**
 * @author Alexey Chmutov
 *         Date: Jun 18, 2009
 *         Time: 9:13:28 PM
 */
public class TmlFileViewProviderFactory implements FileViewProviderFactory {
  public FileViewProvider createFileViewProvider(final VirtualFile file, final Language language, final PsiManager manager, final boolean physical) {
    return new TmlFileViewProvider(manager, file, physical);
  }
}
