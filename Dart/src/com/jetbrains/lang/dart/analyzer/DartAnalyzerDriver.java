package com.jetbrains.lang.dart.analyzer;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartAnalyzerDriver {
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.analyzer.AnalyzerDriver");
  @NotNull
  private final VirtualFile analyzerExecutable;
  @NotNull
  private final VirtualFile libraryRoot;
  private final String sdkPath;
  private final Project myProject;

  public DartAnalyzerDriver(Project project, @NotNull VirtualFile executable, String sdkPath, @NotNull VirtualFile libraryRoot) {
    myProject = project;
    analyzerExecutable = executable;
    this.libraryRoot = libraryRoot;
    this.sdkPath = sdkPath;
  }

  @Nullable
  public List<AnalyzerMessage> analyze() {
    final GeneralCommandLine command = new GeneralCommandLine();
    command.setExePath(analyzerExecutable.getPath());
    command.setWorkDirectory(libraryRoot.getParent().getPath());

    command.getEnvironment().put("com.google.dart.sdk", sdkPath);

    try {
      command.addParameter("--machine");

      final VirtualFile packages = DartResolveUtil.findPackagesFolder(libraryRoot, myProject);
      if (packages != null && packages.isDirectory()) {
        command.addParameter("--package-root");
        command.addParameter(packages.getPath() + "/");
      }

      command.addParameter(libraryRoot.getName());

      LOG.debug("executing:\n" + command.getCommandLineString());
      final ProcessOutput output = new CapturingProcessHandler(command).runProcess();

      LOG.debug("analyzer exited with exit code: " + output.getExitCode());
      LOG.debug(output.getStdout());
      LOG.debug(output.getStderr());

      final List<AnalyzerMessage> messages = AnalyzerMessage.parseMessages(output.getStderrLines(), libraryRoot.getPath());
      LOG.debug("messages");
      for (AnalyzerMessage message : messages) {
        LOG.debug(message.toString());
      }
      return messages;
    }
    catch (ExecutionException e) {
      LOG.debug("Exception while executing the analyzer process:", e);
      return null;
    }
  }
}
