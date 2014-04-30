package com.jetbrains.lang.dart.ide.template;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.module.DartProjectTemplate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DartCommandLineApplicationGenerator extends BaseDartApplicationGenerator {

  @Override
  @NotNull
  protected DartProjectTemplate<?> getGenerator() { return this; }

  @Override
  protected void createContents(final VirtualFile baseDir, final Module module, Project project) throws IOException {
    final VirtualFile binDir = baseDir.createChildDirectory(this, "bin");
    final VirtualFile main = binDir.createChildData(DartCommandLineApplicationGenerator.this, module.getName().toLowerCase() + ".dart");
    main.setBinaryContent("void main() {\n  print(\"Hello, World!\");\n}\n".getBytes());
    openFile(main, project);
  }

  @Override
  protected void setPubspecContent(final VirtualFile pubspecYamlFile, final Module module) throws IOException {
    pubspecYamlFile.setBinaryContent(("name: " + module.getName() + "\n" +
                                      "description:  A sample command-line application\n" +
                                      "dev_dependencies:\n" +
                                      "    unittest: any").getBytes());
  }

  @Override
  @NotNull
  public String getName() {
    return DartBundle.message("dart.commandline.application.title");
  }

  @Override
  @NotNull
  public String getDescription() {
    return DartBundle.message("dart.commandline.application.description");
  }

}
