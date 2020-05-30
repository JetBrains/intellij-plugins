package org.angularjs.index;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.XHtmlFileType;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis Ushakov
 */
public class AngularTemplateIndexInputFilter extends DefaultFileTypeSpecificInputFilter {
  public static final AngularTemplateIndexInputFilter INSTANCE = new AngularTemplateIndexInputFilter();

  public AngularTemplateIndexInputFilter() {
    super(HtmlFileType.INSTANCE, XHtmlFileType.INSTANCE);
  }

  @Override
  public boolean acceptInput(@NotNull VirtualFile file) {
    return super.acceptInput(file) && !(file.getFileSystem() instanceof JarFileSystem);
  }
}
