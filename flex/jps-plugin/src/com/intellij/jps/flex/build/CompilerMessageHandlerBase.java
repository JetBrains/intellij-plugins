// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.jps.flex.build;

import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtilRt;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import java.io.File;
import java.util.regex.Matcher;

public abstract class CompilerMessageHandlerBase {
  private static final String ERROR_PREFIX = "Error: ";
  private static final Logger LOG = Logger.getInstance(CompilerMessageHandlerBase.class.getName());

  private final CompileContext myContext;
  private final boolean myAsc20;
  private final String myCompilerName;

  private boolean myCompilationFinished;
  private boolean myCompilationFailed;
  private boolean myCompilationCancelled;

  private @Nullable String myPreviousUnreportedInfoMessage;

  /**
   * Implementations must call {@link #registerCompilationFinished()} at the end
   */
  public CompilerMessageHandlerBase(final CompileContext context, final boolean asc20, final String compilerName) {
    myContext = context;
    myAsc20 = asc20;
    myCompilerName = compilerName;
  }

  public final void registerCompilationFinished() {
    myCompilationFinished = true;
    reportPreviousInfoMessage();
  }

  public boolean isCompilationFailed() {
    LOG.assertTrue(myCompilationFinished, "compilationFinished() method not called yet");
    return myCompilationFailed;
  }

  public boolean isCompilationCancelled() {
    LOG.assertTrue(myCompilationFinished, "compilationFinished() method not called yet");
    return myCompilationCancelled;
  }

  protected abstract void onCancelled();

  public void handleText(final String text) {
    for (String line : StringUtil.splitByLines(text)) {
      handleLine(line.trim());
    }

    checkCancelled();
  }

  private void handleLine(final String text) {
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
        final String usefulMessage = text.substring(index + ": ".length());

        final Pair<String, Integer> sourcePathAndLine =
          FlexCommonUtils.getSourcePathAndLineFromASC20Message(myPreviousUnreportedInfoMessage);
        if (sourcePathAndLine == null) {
          reportPreviousInfoMessage();
          myContext.processMessage(new CompilerMessage(myCompilerName, kind, usefulMessage));
        }
        else {
          myPreviousUnreportedInfoMessage = null;
          if (!isNotSupportedOptionFromGeneratedConfig(usefulMessage, sourcePathAndLine.first)) {
            myContext.processMessage(
              new CompilerMessage(myCompilerName, kind, usefulMessage, sourcePathAndLine.first, -1, -1, -1, sourcePathAndLine.second, 0));
          }
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
      String settingsPath = SystemInfo.isMac ? FlexCommonBundle.message("action.settings.path.mac")
                                             : FlexCommonBundle.message("action.settings.path");
      myContext.processMessage(
        new CompilerMessage(myCompilerName, BuildMessage.Kind.ERROR, FlexCommonBundle.message("increase.flex.compiler.heap", settingsPath)));
      myCompilationFailed = true;
    }
  }

  private static boolean isNotSupportedOptionFromGeneratedConfig(final String message, final String filePath) {
    final String fileName = PathUtilRt.getFileName(filePath);
    return fileName.startsWith("idea-") && fileName.endsWith(".xml")
           && ("'compiler.locale' is not fully supported.".equals(message) ||
               "'compiler.theme' is not fully supported.".equals(message) ||
               "'compiler.preloader' is not fully supported.".equals(message) ||
               "'compiler.accessible' is not fully supported.".equals(message) ||
               "'compiler.fonts.managers' is not fully supported.".equals(message) ||
               "'static-link-runtime-shared-libraries' is not fully supported.".equals(message));
  }

  private void reportPreviousInfoMessage() {
    if (myPreviousUnreportedInfoMessage != null) {
      if (!myPreviousUnreportedInfoMessage.equals("<theme />") &&
          !myPreviousUnreportedInfoMessage.equals("</locale>") &&
          !myPreviousUnreportedInfoMessage.equals("<preloader>spark.preloaders.SplashScreen</preloader>") &&
          !myPreviousUnreportedInfoMessage.equals("<accessible>true</accessible>") &&
          !myPreviousUnreportedInfoMessage.equals("<accessible>false</accessible>") &&
          !myPreviousUnreportedInfoMessage.equals("</managers>") &&
          !myPreviousUnreportedInfoMessage.equals("<static-link-runtime-shared-libraries>false</static-link-runtime-shared-libraries>")) {
        myContext.processMessage(new CompilerMessage(myCompilerName, BuildMessage.Kind.INFO, myPreviousUnreportedInfoMessage));
      }
      myPreviousUnreportedInfoMessage = null;
    }
  }

  private void checkCancelled() {
    if (!myCompilationCancelled && myContext.isCanceled()) {
      myCompilationCancelled = true;
      onCancelled();
    }
  }
}
