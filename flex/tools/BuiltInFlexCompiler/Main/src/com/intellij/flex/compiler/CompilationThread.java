package com.intellij.flex.compiler;

import flex2.compiler.ILocalizableMessage;
import flex2.compiler.common.Configuration;
import flex2.tools.oem.Application;
import flex2.tools.oem.Builder;
import flex2.tools.oem.Library;

import java.io.File;

public class CompilationThread extends Thread {

  private static int omitTraceCompilationsCount = 0;
  private static int traceCompilationsCount = 0;
  private static final Object lock = new Object();

  private static final String ERROR_MARKER = "Error: ";

  private final SdkSpecificHandler mySdkSpecificHandler;
  private final String[] myParams;
  private final OutputLogger myLogger;

  public CompilationThread(final SdkSpecificHandler sdkSpecificHandler,
                           final String[] params,
                           final OutputLogger logger) {
    mySdkSpecificHandler = sdkSpecificHandler;
    myParams = params;
    myLogger = logger;
  }

  static {
    // initialize static data to avoid multithreaded collisions caused by partially initialized arrays or collections
    try {
      Configuration.getAliases();
      macromedia.asc.embedding.LintEvaluator.getWarningDefaults();
    }
    catch (Throwable t) {/*API changed*/}
  }

  public void run() {
    try {
      mySdkSpecificHandler.initThreadLocals(myLogger);

      final Configuration configuration = mySdkSpecificHandler.processConfiguration(myParams);
      final Builder builder = mySdkSpecificHandler.createBuilder(configuration);
      //ConfigurationUtil.setConfiguration(builder, configuration);      now it is done in createBuilder()
      builder.setLogger(myLogger);
      builder.setPathResolver(SdkFilesResolver.INSTANCE);

      final long outputFileSize;
      final boolean omitTrace = mySdkSpecificHandler.omitTrace(configuration);

      try {
        acquire(omitTrace);

        mySdkSpecificHandler.setupOmitTraceOption(omitTrace);
        outputFileSize = builder.build(true);
      }
      finally {
        release(omitTrace);
      }

      if (outputFileSize > 0) {
        final File outputFile = (builder instanceof Application) ? ((Application)builder).getOutput() : ((Library)builder).getOutput();
        myLogger.log(outputFile.getCanonicalPath() + " (" + outputFileSize + " bytes)");
      }
      //else {
      //  myLogger.log(ERROR_MARKER + "Compilation failed");
      //}
    }
    catch (final Exception e) {
      logError(e);
    }
    catch (final Throwable t) {
      logError(t);
      System.exit(1);
    }
    finally {
      myLogger.log(FlexCompiler.COMPILATION_FINISHED);
      mySdkSpecificHandler.cleanThreadLocals();
      System.gc();
    }
  }

  private static void acquire(final boolean omitTrace) throws InterruptedException {
    synchronized (lock) {
      while ((omitTrace && traceCompilationsCount > 0) || (!omitTrace && omitTraceCompilationsCount > 0)) {
        lock.wait();
      }
      if (omitTrace) {
        omitTraceCompilationsCount++;
      }
      else {
        traceCompilationsCount++;
      }
    }
  }

  private static void release(final boolean omitTrace) {
    synchronized (lock) {
      if (omitTrace) {
        omitTraceCompilationsCount--;
      }
      else {
        traceCompilationsCount--;
      }
      lock.notifyAll();
    }
  }

  private void logError(final Throwable e) {
    if (e instanceof ILocalizableMessage) {
      myLogger.log((ILocalizableMessage)e);
    }
    else {
      myLogger.log(ERROR_MARKER + e.toString());
      for (final StackTraceElement stackTraceElement : e.getStackTrace()) {
        myLogger.log("\tat " + stackTraceElement.toString());
      }
    }
  }
}

