package com.intellij.jps.flex.build;

import com.intellij.execution.process.BaseOSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import java.io.File;
import java.nio.charset.Charset;
import java.util.regex.Matcher;

public class FlexCompilerProcessHandler extends BaseOSProcessHandler {
  private boolean myCompilationFailed;
  private boolean myCancelled;

  public FlexCompilerProcessHandler(final CompileContext context,
                                    final Process process,
                                    final boolean asc20,
                                    final String compilerName,
                                    final String commandLine) {
    super(process, commandLine, Charset.forName(FlexCommonUtils.SDK_TOOLS_ENCODING));

    myCompilationFailed = false;
    myCancelled = false;

    addProcessListener(new MyProcessListener(context, asc20, compilerName));
  }

  public boolean isCompilationFailed() {
    return myCompilationFailed;
  }

  public boolean isCancelled() {
    return myCancelled;
  }

  private class MyProcessListener extends ProcessAdapter {
    private static final String ERROR_PREFIX = "Error: ";

    private final CompileContext myContext;
    private final boolean myAsc20;
    private final String myCompilerName;

    private @Nullable String myPreviousUnreportedInfoMessage;

    public MyProcessListener(final CompileContext context, final boolean asc20, final String compilerName) {
      myContext = context;
      myAsc20 = asc20;
      myCompilerName = compilerName;
    }

    public void processTerminated(final ProcessEvent event) {
      reportPreviousInfoMessage();
    }

    public void onTextAvailable(final ProcessEvent event, final Key outputType) {
      handleMessage(event.getText().trim());
      checkCancelled();
    }

    private void handleMessage(final String text) {
      if (StringUtil.isEmptyOrSpaces(text)) {
        reportPreviousInfoMessage();
        return;
      }

      if (text.equals("^")) {
        myPreviousUnreportedInfoMessage = null; // don't report previous line, it contains a line of source code with warning/error
        return;
      }

      if ("command line".equals(text)) {
        // ignore this line and print previous if any
        reportPreviousInfoMessage();
        return;
      }

      if (text.startsWith("Exception in thread \"") || text.contains(FlexCommonUtils.COULD_NOT_CREATE_JVM)) {
        reportPreviousInfoMessage();
        myContext.processMessage(new CompilerMessage(myCompilerName, BuildMessage.Kind.ERROR, text));
        myCompilationFailed = true;
        return;
      }

      if (myAsc20) {
        // see messages_en.properties from Falcon sources
        if (text.startsWith("Warning: ") || text.startsWith("Error: ") ||
            text.startsWith("Syntax error: ") || text.startsWith("Internal error: ")) {

          final BuildMessage.Kind kind = text.startsWith("Warning: ") ? BuildMessage.Kind.WARNING : BuildMessage.Kind.ERROR;
          final int index = text.indexOf(": ");
          final String usefulMessage = text.substring(index);

          final Pair<String, Integer> sourcePathAndLine =
            FlexCommonUtils.getSourcePathAndLineFromASC20Message(myPreviousUnreportedInfoMessage);
          if (sourcePathAndLine == null) {
            reportPreviousInfoMessage();
            myContext.processMessage(new CompilerMessage(myCompilerName, kind, usefulMessage));
          }
          else {
            myPreviousUnreportedInfoMessage = null;
            myContext.processMessage(
              new CompilerMessage(myCompilerName, kind, usefulMessage, sourcePathAndLine.first, -1, -1, -1, sourcePathAndLine.second, 0));
          }

          myCompilationFailed |= kind == BuildMessage.Kind.ERROR;
          return;
        }
      }
      else {
        final Matcher matcher = FlexCommonUtils.ERROR_PATTERN.matcher(text);

        if (matcher.matches()) {
          final String sourceFilePath = matcher.group(1);
          final String additionalInfo = matcher.group(2);
          final String line = matcher.group(3);
          final String column = matcher.group(4);
          final String type = matcher.group(5);
          final String message = matcher.group(6);

          final BuildMessage.Kind kind = "Warning".equals(type) ? BuildMessage.Kind.WARNING : BuildMessage.Kind.ERROR;
          final File file = new File(sourceFilePath);
          final boolean sourceFileExists = file.exists();

          final StringBuilder fullMessage = new StringBuilder();
          if (!sourceFileExists) fullMessage.append(sourceFilePath).append(": ");
          if (additionalInfo != null) fullMessage.append(additionalInfo).append(' ');
          fullMessage.append(message);

          reportPreviousInfoMessage();
          myContext.processMessage(new CompilerMessage(myCompilerName,
                                                       kind,
                                                       fullMessage.toString(),
                                                       sourceFileExists ? sourceFilePath : null, -1, -1, -1,
                                                       line != null ? Integer.parseInt(line) : 0,
                                                       column != null ? Integer.parseInt(column) : 0));
          myCompilationFailed |= kind == BuildMessage.Kind.ERROR;
          return;
        }
      }

      if (text.startsWith(ERROR_PREFIX)) {
        reportPreviousInfoMessage();
        myContext.processMessage(new CompilerMessage(myCompilerName, BuildMessage.Kind.ERROR, text.substring(ERROR_PREFIX.length())));
        myCompilationFailed = true;
        return;
      }

      reportPreviousInfoMessage();
      myPreviousUnreportedInfoMessage = text;

      if (text.contains(FlexCommonUtils.OUT_OF_MEMORY) || text.contains(FlexCommonUtils.JAVA_HEAP_SPACE)) {
        myContext.processMessage(
          new CompilerMessage(myCompilerName, BuildMessage.Kind.ERROR, FlexCommonBundle.message("increase.flex.compiler.heap")));
        myCompilationFailed = true;
      }
    }

    private void reportPreviousInfoMessage() {
      if (myPreviousUnreportedInfoMessage != null) {
        myContext.processMessage(new CompilerMessage(myCompilerName, BuildMessage.Kind.INFO, myPreviousUnreportedInfoMessage));
        myPreviousUnreportedInfoMessage = null;
      }
    }

    private void checkCancelled() {
      if (myContext.getCancelStatus().isCanceled()) {
        myCancelled = true;
        destroyProcess();
      }
    }
  }
}
