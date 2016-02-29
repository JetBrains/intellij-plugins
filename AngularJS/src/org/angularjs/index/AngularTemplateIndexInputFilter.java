package org.angularjs.index;

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis Ushakov
 */
public class AngularTemplateIndexInputFilter extends DefaultFileTypeSpecificInputFilter {
  public final static AngularTemplateIndexInputFilter INSTANCE = new AngularTemplateIndexInputFilter();

  public AngularTemplateIndexInputFilter() {
    super(StdFileTypes.HTML, StdFileTypes.XHTML);
  }

  @Override
  public boolean acceptInput(@NotNull VirtualFile file) {
    return super.acceptInput(file) && !(file.getFileSystem() instanceof JarFileSystem);
  }
}
