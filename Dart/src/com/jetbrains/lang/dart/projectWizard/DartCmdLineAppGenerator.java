package com.jetbrains.lang.dart.projectWizard;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.projectWizard.DartEmptyProjectGenerator;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Locale;

public class DartCmdLineAppGenerator extends DartEmptyProjectGenerator {

  @SuppressWarnings("UnusedDeclaration") // invoked by com.intellij.platform.DirectoryProjectGenerator.EP_NAME
  public DartCmdLineAppGenerator() {
    this(DartBundle.message("dart.commandline.app.description.webstorm"));
  }

  public DartCmdLineAppGenerator(@NotNull final String description) {
    super(DartBundle.message("dart.commandline.app.title"), description);
  }

  @NotNull
  protected VirtualFile[] doGenerateProject(final Module module, final VirtualFile baseDir) throws IOException {
    final VirtualFile pubspecFile = baseDir.createChildData(this, PubspecYamlUtil.PUBSPEC_YAML);
    pubspecFile.setBinaryContent(("name: " + module.getName() + "\n" +
                                  "version: 0.0.1\n" +
                                  "description: A sample command-line application\n" +
                                  "dependencies:\n" +
                                  "\n" +
                                  "dev_dependencies:\n" +
                                  "#  unittest: any\n").getBytes());
    final VirtualFile binDir = VfsUtil.createDirectoryIfMissing(baseDir, "bin");
    final VirtualFile mainFile = binDir.createChildData(this, module.getName().toLowerCase(Locale.US) + ".dart");
    mainFile.setBinaryContent(("void main() {\n" +
                               "  print('Hello, World!');\n" +
                               "}\n").getBytes());
    return new VirtualFile[]{pubspecFile, mainFile};
  }
}
