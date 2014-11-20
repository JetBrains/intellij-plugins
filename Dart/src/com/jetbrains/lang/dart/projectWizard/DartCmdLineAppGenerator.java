package com.jetbrains.lang.dart.projectWizard;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunConfigurationType;
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
  protected VirtualFile[] doGenerateProject(@NotNull final Module module, final VirtualFile baseDir) throws IOException {
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

    createRunConfiguration(module, mainFile);

    return new VirtualFile[]{pubspecFile, mainFile};
  }

  private static void createRunConfiguration(final @NotNull Module module, final @NotNull VirtualFile mainDartFile) {
    final RunManager runManager = RunManager.getInstance(module.getProject());
    final RunnerAndConfigurationSettings settings =
      runManager.createRunConfiguration("", DartCommandLineRunConfigurationType.getInstance().getConfigurationFactories()[0]);

    final DartCommandLineRunConfiguration runConfiguration = (DartCommandLineRunConfiguration)settings.getConfiguration();
    runConfiguration.getRunnerParameters().setFilePath(mainDartFile.getPath());
    runConfiguration.getRunnerParameters().setWorkingDirectory(mainDartFile.getParent().getPath());
    settings.setName(runConfiguration.suggestedName());

    runManager.addConfiguration(settings, false);
    runManager.setSelectedConfiguration(settings);
  }
}
