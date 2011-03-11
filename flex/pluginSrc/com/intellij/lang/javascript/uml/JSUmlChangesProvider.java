package com.intellij.lang.javascript.uml;

import com.intellij.diagram.ChangeTracker;
import com.intellij.diagram.DiagramChangesProvider;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public class JSUmlChangesProvider extends DiagramChangesProvider {

  @Override
  public boolean accept(FileType type, String filename) {
    if (type instanceof JavaScriptFileType) {
      return JavaScriptSupportLoader.getLanguageDialect(FileUtil.getExtension(filename)) == JavaScriptSupportLoader.ECMA_SCRIPT_L4;
    }
    else if (type instanceof XmlFileType) {
      return JavaScriptSupportLoader.isFlexMxmFile(filename);
    }
    return false;
  }

  @Override
  public ChangeTracker createTracker(Project project, @Nullable PsiFile before, @Nullable PsiFile after) {
    return new JSChangeTracker(project, before, after);
  }
}
