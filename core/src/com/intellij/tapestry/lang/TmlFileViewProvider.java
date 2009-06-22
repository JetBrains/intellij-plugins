package com.intellij.tapestry.lang;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 *         Date: Jun 18, 2009
 *         Time: 9:11:31 PM
 */
public class TmlFileViewProvider extends SingleRootFileViewProvider {

  public TmlFileViewProvider(@NotNull PsiManager manager, @NotNull VirtualFile file, boolean physical) {
    super(manager, file, physical);
  }
}
