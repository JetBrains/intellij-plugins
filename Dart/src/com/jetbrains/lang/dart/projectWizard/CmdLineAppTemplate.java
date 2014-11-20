package com.jetbrains.lang.dart.projectWizard;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

class CmdLineAppTemplate extends DartProjectTemplate {
  public CmdLineAppTemplate() {
    super(DartBundle.message("dart.commandline.app.title"), "");
  }

  @Override
  public Collection<VirtualFile> generateProject(@NotNull final Module module, @NotNull final VirtualFile baseDir) throws IOException {
    final VirtualFile pubspecFile = baseDir.createChildData(this, PubspecYamlUtil.PUBSPEC_YAML);
    pubspecFile.setBinaryContent(("name: " + module.getName() + "\n" +
                                  "version: 0.0.1\n" +
                                  "description: A sample command-line application\n" +
                                  "dependencies:\n" +
                                  "\n" +
                                  "dev_dependencies:\n" +
                                  "#  unittest: any\n").getBytes(Charset.forName("UTF-8")));
    final VirtualFile binDir = VfsUtil.createDirectoryIfMissing(baseDir, "bin");
    final VirtualFile mainFile = binDir.createChildData(this, module.getName().toLowerCase(Locale.US) + ".dart");
    mainFile.setBinaryContent(("void main() {\n" +
                               "  print('Hello, World!');\n" +
                               "}\n").getBytes(Charset.forName("UTF-8")));

    createCmdLineRunConfiguration(module, mainFile);

    return Arrays.asList(pubspecFile, mainFile);
  }
}
