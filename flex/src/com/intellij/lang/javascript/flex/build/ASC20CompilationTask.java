package com.intellij.lang.javascript.flex.build;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.text.StringTokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

public class ASC20CompilationTask extends FlexCompilationTask {

  private Process myProcess;
  private @Nullable String myPreviousUnreportedMessage;

  public ASC20CompilationTask(final @NotNull Module module,
                              final @NotNull FlexBuildConfiguration bc,
                              final @NotNull Collection<FlexBuildConfiguration> dependencies) {
    super(module, bc, dependencies);
  }

  protected void doStart(final FlexCompilationManager compilationManager) throws IOException {
    final boolean app = myBC.getOutputType() != OutputType.Library;
    final Sdk sdk = myBC.getSdk();
    assert sdk != null;

    final List<String> compilerCommand = FlexCompilationUtils.getASC20Command(myModule.getProject(), sdk, app);
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
        final InputStreamReader reader = FlexCommonUtils.createInputStreamReader(myProcess.getInputStream());

        try {
          char[] buf = new char[2048];
          int read;

          while ((read = reader.read(buf, 0, buf.length)) >= 0) {
            final String output = new String(buf, 0, read);

            final StringTokenizer tokenizer = new StringTokenizer(output, "\r\n");

            while (tokenizer.hasMoreElements()) {
              final String message = tokenizer.nextElement();
              if (StringUtil.isEmptyOrSpaces(message)) continue;

              final boolean ok = handleCompilerMessage(compilationManager, message);

              if (!ok) {
                myCompilationFailed = true;
              }
            }
          }

          printPreviousLine(compilationManager);
        }
        catch (IOException e) {
          compilationManager.addMessage(ASC20CompilationTask.this, CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
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

  /**
   * @return <code>false</code> if error reported
   */
  private boolean handleCompilerMessage(final FlexCompilationManager compilationManager, final String message) {
    if ("^".equals(message.trim())) {
      // ignore this and previous line - no need to print source code in Messages tool window
      myPreviousUnreportedMessage = null;
      return true;
    }

    if ("command line".equals(message)) {
      // ignore this line and print previous if any
      printPreviousLine(compilationManager);
      return true;
    }

    if (message.startsWith("Exception in thread \"")) {
      printPreviousLine(compilationManager);
      compilationManager.addMessage(this, CompilerMessageCategory.ERROR, message, null, -1, -1);
      return false;
    }

    // see messages_en.properties from Falcon sources
    if (message.startsWith("Warning: ") ||
        message.startsWith("Error: ") ||
        message.startsWith("Syntax error: ") ||
        message.startsWith("Internal error: ")) {
      final CompilerMessageCategory category = message.startsWith("Warning: ") ? CompilerMessageCategory.WARNING
                                                                               : CompilerMessageCategory.ERROR;
      final int index = message.indexOf(": ");
      final String usefulMessage = message.substring(index);

      final Pair<String, Integer> sourcePathAndLine = FlexCommonUtils.getSourcePathAndLineFromASC20Message(myPreviousUnreportedMessage);
      if (sourcePathAndLine == null) {
        printPreviousLine(compilationManager);
        compilationManager.addMessage(this, category, usefulMessage, null, -1, -1);
      }
      else {
        compilationManager
          .addMessage(this, category, usefulMessage, VfsUtilCore.pathToUrl(sourcePathAndLine.first), sourcePathAndLine.second, -1);
      }

      myPreviousUnreportedMessage = null;
      return category == CompilerMessageCategory.WARNING;
    }

    printPreviousLine(compilationManager);
    myPreviousUnreportedMessage = message;

    return true;
  }

  private void printPreviousLine(final FlexCompilationManager compilationManager) {
    if (myPreviousUnreportedMessage != null) {
      compilationManager.addMessage(this, CompilerMessageCategory.INFORMATION, myPreviousUnreportedMessage, null, -1, -1);
      myPreviousUnreportedMessage = null;
    }
  }
}
