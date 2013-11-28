package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.StringBuilderSpinAllocator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Andrey.Vokin
 * Date: 11/28/13
 */
public abstract class AbstractStepDefinitionCreator implements StepDefinitionCreator {
  @NotNull
  public String getStepDefinitionFilePath(@NotNull final PsiFile psiFile) {
    final VirtualFile file = psiFile.getVirtualFile();
    assert file != null;
    VirtualFile parent = file.getParent();
    // if file is direct child of step definitions dir
    if (parent != null && CucumberUtil.STEP_DEFINITIONS_DIR_NAME.equals(parent.getName())) {
      return file.getName();
    }

    // in subfolder
    final List<String> dirsReversed = new ArrayList<String>();
    while (parent != null) {
      final String name = parent.getName();
      if (CucumberUtil.STEP_DEFINITIONS_DIR_NAME.equals(name)) {
        break;
      }
      dirsReversed.add(name);
      parent = parent.getParent();
    }
    final StringBuilder buf = StringBuilderSpinAllocator.alloc();
    try {
      for (int i = dirsReversed.size() - 1; i >= 0; i--) {
        buf.append(dirsReversed.get(i)).append(File.separatorChar);
      }
      buf.append(file.getName());
      return buf.toString();
    }
    finally {
      StringBuilderSpinAllocator.dispose(buf);
    }
  }

}
