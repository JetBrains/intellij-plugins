package org.angularjs.cli;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Dennis.Ushakov
 */
public class AngularCLIFilter implements Filter, DumbAware {
  private static final String CREATE = "create ";
  private Project myProject;
  private String myBaseDir;

  public AngularCLIFilter(Project project, String baseDir) {
    myProject = project;
    myBaseDir = baseDir;
  }

  @Nullable
  @Override
  public Result applyFilter(String line, int entireLength) {
    final int index = line.indexOf(CREATE);
    if (index >= 0) {
      final int start = index + CREATE.length();
      final String fileName = line.substring(start).trim();
      final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(myBaseDir + "/" + fileName));
      if (file != null) return new Result(entireLength - line.length() + start, entireLength,
                                          new OpenFileHyperlinkInfo(myProject, file, 0));
    }
    return null;
  }
}
