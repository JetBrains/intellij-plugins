package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

public class MxmlcCompcCompilationTask extends FlexCompilationTask {

  private Process myProcess;

  public MxmlcCompcCompilationTask(final @NotNull Module module,
                                   final @NotNull FlexIdeBuildConfiguration bc,
                                   final @NotNull Collection<FlexIdeBuildConfiguration> dependencies) {
    super(module, bc, dependencies);
  }

  protected void doStart(final FlexCompilationManager compilationManager) throws IOException {
    final boolean app = myBC.getOutputType() != OutputType.Library;
    final Sdk sdk = myBC.getSdk();
    assert sdk != null;

    final List<String> compilerCommand = FlexCompilationUtils.getMxmlcCompcCommand(myModule.getProject(), sdk, app);
    final List<String> command = FlexCompilationUtils.buildCommand(compilerCommand, getConfigFiles(), myModule, myBC);
    final ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    processBuilder.directory(new File(FlexUtils.getFlexCompilerWorkDirPath(myModule.getProject(), null)));

    compilationManager.addMessage(this, CompilerMessageCategory.INFORMATION, StringUtil.join(command, " "), null, -1, -1);

    myProcess = processBuilder.start();
    readInputStream(compilationManager);
  }

  protected void doCancel() {
    if (myProcess != null) {
      myProcess.destroy();
    }
  }

  private void readInputStream(final FlexCompilationManager compilationManager) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        final InputStreamReader reader = FlexSdkUtils.createInputStreamReader(myProcess.getInputStream());

        try {
          char[] buf = new char[2048];
          int read;
          while ((read = reader.read(buf, 0, buf.length)) >= 0) {
            final String output = new String(buf, 0, read);
            final boolean ok = FlexCompilationUtils.handleCompilerOutput(compilationManager, MxmlcCompcCompilationTask.this, output);
            if (!ok) {
              myCompilationFailed = true;
            }
          }
        }
        catch (IOException e) {
          compilationManager.addMessage(MxmlcCompcCompilationTask.this, CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
          myCompilationFailed = true;
        }
        finally {
          cancel();

          try {
            reader.close();
          }
          catch (IOException e) {/*ignore*/}
        }
      }
    });
  }
}
