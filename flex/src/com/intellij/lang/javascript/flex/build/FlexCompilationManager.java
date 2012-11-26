package com.intellij.lang.javascript.flex.build;

import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessage;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.GuiUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlexCompilationManager {

  private final CompileContext myCompileContext;
  private final int myMaxParallelCompilations;
  private final int myTasksAmount;
  private final Collection<FlexCompilationTask> myNotStartedTasks;
  private final Collection<FlexCompilationTask> myInProgressTasks;
  private final Collection<FlexCompilationTask> myFinishedTasks;

  private boolean myCompilationFinished;
  private final FlexCompilerDependenciesCache myCompilerDependenciesCache;

  static final Pattern OUTPUT_FILE_CREATED_PATTERN = Pattern.compile("(\\[.*\\] )?(.+) \\(([0-9]+) bytes\\)");
  private static final String BYTES_WRITTEN_TO = " bytes written to ";

  public FlexCompilationManager(final CompileContext context, final Collection<FlexCompilationTask> compilationTasks) {
    myCompileContext = context;
    myMaxParallelCompilations = FlexCompilerProjectConfiguration.getInstance(context.getProject()).MAX_PARALLEL_COMPILATIONS;
    myTasksAmount = compilationTasks.size();
    myNotStartedTasks = new LinkedList<FlexCompilationTask>(compilationTasks);
    myInProgressTasks = new LinkedList<FlexCompilationTask>();
    myFinishedTasks = new LinkedList<FlexCompilationTask>();
    myCompilationFinished = false;
    myCompilerDependenciesCache = FlexCompilerHandler.getInstance(context.getProject()).getCompilerDependenciesCache();
  }

  public void compile() {
    try {
      while (!myNotStartedTasks.isEmpty() || !myInProgressTasks.isEmpty()) {

        if (myCompileContext.getProgressIndicator().isCanceled()) {
          for (FlexCompilationTask task : myInProgressTasks) {
            task.cancel();
          }
          break;
        }

        checkFinishedTasks();
        startNewTaskIfPossible();
        updateProgressIndicator();

        try {
          //noinspection BusyWait
          Thread.sleep(200);
        }
        catch (InterruptedException e) {
          assert false;
        }
      }
    }
    finally {
      //noinspection SynchronizeOnThis
      synchronized (this) {
        myCompilationFinished = true;
      }
    }
  }

  public synchronized void addMessage(final FlexCompilationTask task,
                                      CompilerMessageCategory category,
                                      final String message,
                                      final @Nullable String url,
                                      final int lineNum,
                                      final int columnNum) {
    if (!myCompilationFinished) {

      if (message.contains(FlexCommonUtils.COULD_NOT_CREATE_JVM)) {
        category = CompilerMessageCategory.ERROR;
      }

      if (category == CompilerMessageCategory.INFORMATION) {
        final int bytesWrittenIndex = message.indexOf(BYTES_WRITTEN_TO);

        if (bytesWrittenIndex > 0) {
          // ASC 2.0 sources: MXMLC.bytes_written_to_file_in_seconds_format=${byteCount} bytes written to ${path} in ${seconds} seconds
          final int inIndex = message.lastIndexOf(" in ");
          if (inIndex > bytesWrittenIndex) {
            final String outputFilePath = message.substring(bytesWrittenIndex + BYTES_WRITTEN_TO.length(), inIndex);
            // need to refresh FS in order to notify artifact compiler that Flex output has changed
            if (!ApplicationManager.getApplication().isUnitTestMode()) {
              refreshAndFindFileInWriteAction(outputFilePath);
            }
          }
        }
        else {

          final Matcher matcher = OUTPUT_FILE_CREATED_PATTERN.matcher(message);
          if (matcher.matches()) {
            final String outputFilePath = matcher.group(2);
            // need to refresh FS in order to notify artifact compiler that Flex output has changed
            if (!ApplicationManager.getApplication().isUnitTestMode()) {
              refreshAndFindFileInWriteAction(outputFilePath);
            }
          }
        }
      }

      final String prefix = getMessagePrefix(task);
      myCompileContext.addMessage(category, prefix + message, url, lineNum, columnNum);

      if (message.contains(FlexCommonUtils.OUT_OF_MEMORY) || message.contains(FlexCommonUtils.JAVA_HEAP_SPACE)) {
        myCompileContext
          .addMessage(CompilerMessageCategory.ERROR, prefix + FlexCommonBundle.message("increase.flex.compiler.heap"), null, -1, -1);
      }
    }
  }

  public boolean isRebuild() {
    return !myCompileContext.isMake();
  }

  private void checkFinishedTasks() {
    final Iterator<FlexCompilationTask> iterator = myInProgressTasks.iterator();
    while (iterator.hasNext()) {
      FlexCompilationTask task = iterator.next();
      if (task.isFinished()) {
        iterator.remove();
        myFinishedTasks.add(task);

        if (task.isCompilationFailed()) {
          final Collection<FlexCompilationTask> cancelledTasks = cancelNotStartedDependentTasks(task);
          if (cancelledTasks.isEmpty()) {
            addMessage(task, CompilerMessageCategory.INFORMATION, FlexCommonBundle.message("compilation.failed"), null, -1, -1);
          }
          else {
            addMessage(task, CompilerMessageCategory.INFORMATION, FlexCommonBundle.message("compilation.failed.dependent.will.be.skipped"),
                       null, -1, -1);
            for (final FlexCompilationTask cancelledTask : cancelledTasks) {
              addMessage(cancelledTask, CompilerMessageCategory.INFORMATION, FlexBundle.message("compilation.skipped"), null, -1, -1);
            }
          }
        }
        else {
          addMessage(task, CompilerMessageCategory.INFORMATION, FlexCommonBundle.message("compilation.successful"), null, -1, -1);

          final String prefix = getMessagePrefix(task);
          final List<String> taskMessages = new ArrayList<String>();
          for (CompilerMessage message : myCompileContext.getMessages(CompilerMessageCategory.INFORMATION)) {
            if (message.getMessage().startsWith(prefix)) {
              taskMessages.add(message.getMessage().substring(prefix.length()));
            }
          }

          try {
            FlexCompilationUtils.performPostCompileActions(task.getModule(), task.getBC(), taskMessages);
          }
          catch (FlexCompilerException e) {
            addMessage(task, CompilerMessageCategory.ERROR, e.getMessage(), e.getUrl(), e.getLine(), e.getColumn());
          }
        }

        if (task.isCompilationFailed()) {
          myCompilerDependenciesCache.markBCDirty(task.getModule(), task.getBC());
        }
        else {
          //noinspection SynchronizeOnThis
          synchronized (this) {
            myCompilerDependenciesCache.cacheBC(task.getModule(), task.getBC(), task.getConfigFiles());
          }
        }
      }
    }
  }

  private String getMessagePrefix(final FlexCompilationTask task) {
    return "[" + task.getPresentableName() + "] ";
  }

  private Collection<FlexCompilationTask> cancelNotStartedDependentTasks(final FlexCompilationTask failedTask) {
    final Collection<FlexCompilationTask> tasksToCancel = new LinkedList<FlexCompilationTask>();
    appendAndCancelNotStartedDependentTasks(tasksToCancel, failedTask);

    if (BCUtils.canHaveRLMsAndRuntimeStylesheets(failedTask.getBC())) {
      appendAndCancelNotStartedRLMTasks(tasksToCancel, failedTask.getModule(), failedTask.getBC());
    }

    return tasksToCancel;
  }

  private void appendAndCancelNotStartedDependentTasks(final Collection<FlexCompilationTask> cancelledTasks,
                                                       final FlexCompilationTask task) {
    final Collection<FlexCompilationTask> tasksToCancel = new ArrayList<FlexCompilationTask>();

    for (FlexCompilationTask notStartedTask : myNotStartedTasks) {
      //noinspection ConstantConditions
      if (notStartedTask.getDependencies().contains(task.getBC())) {
        tasksToCancel.add(notStartedTask);
      }
    }

    for (FlexCompilationTask taskToCancel : tasksToCancel) {
      taskToCancel.cancel();
      if (myNotStartedTasks.remove(taskToCancel)) {
        myFinishedTasks.add(taskToCancel);
        cancelledTasks.add(taskToCancel);
        appendAndCancelNotStartedDependentTasks(cancelledTasks, taskToCancel);
      }
    }
  }

  private void appendAndCancelNotStartedRLMTasks(final Collection<FlexCompilationTask> tasks,
                                                 final Module module,
                                                 final FlexBuildConfiguration bc) {
    final Iterator<FlexCompilationTask> iterator = myNotStartedTasks.iterator();
    while (iterator.hasNext()) {
      final FlexCompilationTask task = iterator.next();
      if (module == task.getModule() && bc.getName().equals(task.getBC().getName()) && BCUtils.isRLMTemporaryBC(task.getBC())) {
        iterator.remove();
        myFinishedTasks.add(task);
        tasks.add(task);
      }
    }
  }

  private void startNewTaskIfPossible() {
    FlexCompilationTask taskToStart = null;

    if (!myNotStartedTasks.isEmpty() && myInProgressTasks.size() < myMaxParallelCompilations) {
      boolean allTasksHaveDependenciesOnlyInNotStarted = true; // to handle cyclic dependencies

      for (FlexCompilationTask task : myNotStartedTasks) {

        if (BCUtils.isRLMTemporaryBC(task.getBC()) && !isMainAppCompiledForRLM(task.getModule(), task.getBC())) {
          allTasksHaveDependenciesOnlyInNotStarted = false;
          continue;
        }

        if (hasDependenciesIn(task, myInProgressTasks)) {
          allTasksHaveDependenciesOnlyInNotStarted = false;
          continue;
        }
        if (hasDependenciesIn(task, myNotStartedTasks)) {
          continue;
        }

        taskToStart = task;
        break;
      }

      if (taskToStart == null && allTasksHaveDependenciesOnlyInNotStarted) {
        taskToStart = myNotStartedTasks.iterator().next(); // just take any from cycle dependencies
      }

      if (taskToStart != null) {
        myNotStartedTasks.remove(taskToStart);

        if (myCompilerDependenciesCache.isNothingChangedSincePreviousCompilation(taskToStart.getModule(), taskToStart.getBC())) {
          addMessage(taskToStart, CompilerMessageCategory.INFORMATION, FlexBundle.message("compilation.skipped.because.nothing.changed"),
                     null, -1, -1);
          taskToStart.cancel();
          myFinishedTasks.add(taskToStart);

          try {
            FlexCompilationUtils.performPostCompileActions(taskToStart.getModule(), taskToStart.getBC(), Collections.<String>emptyList());
          }
          catch (FlexCompilerException e) {
            addMessage(taskToStart, CompilerMessageCategory.ERROR, e.getMessage(), e.getUrl(), e.getLine(), e.getColumn());
          }
        }
        else {
          taskToStart.start(this);
          myInProgressTasks.add(taskToStart);
        }

        startNewTaskIfPossible();
      }
    }
  }

  private boolean isMainAppCompiledForRLM(final Module module, final FlexBuildConfiguration rlmBC) {
    for (FlexCompilationTask task : myFinishedTasks) {
      final FlexBuildConfiguration bc = task.getBC();
      if (task.getModule() == module &&
          bc.getName().equals(rlmBC.getName()) &&
          !BCUtils.isRLMTemporaryBC(bc) &&
          !BCUtils.isRuntimeStyleSheetBC(bc) &&
          BCUtils.canHaveRLMsAndRuntimeStylesheets(bc) &&
          bc.getRLMs().size() > 0) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasDependenciesIn(final FlexCompilationTask task,
                                           final Collection<FlexCompilationTask> tasksToSearchDependencies) {
    for (final FlexCompilationTask otherTask : tasksToSearchDependencies) {
      //noinspection ConstantConditions
      if (task.getDependencies().contains(otherTask.getBC())) {
        return true;
      }
    }
    return false;
  }

  private void updateProgressIndicator() {
    final ProgressIndicator progressIndicator = myCompileContext.getProgressIndicator();
    progressIndicator.setFraction(1. * myFinishedTasks.size() / myTasksAmount);
    final StringBuilder builder = new StringBuilder();

    if (!myInProgressTasks.isEmpty()) {
      for (FlexCompilationTask inProgressTask : myInProgressTasks) {
        if (builder.length() > 0) builder.append(", ");
        builder.append(inProgressTask.getPresentableName());
      }
      progressIndicator.setText(FlexCommonBundle.message("compiling", builder.toString()));
    }
  }

  static VirtualFile refreshAndFindFileInWriteAction(final String outputFilePath, final String... possibleBaseDirs) {
    final LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
    final Ref<VirtualFile> outputFileRef = new Ref<VirtualFile>();

    GuiUtils.invokeAndWaitIfNeeded(new Runnable() {
      public void run() {
        outputFileRef.set(ApplicationManager.getApplication().runWriteAction(new NullableComputable<VirtualFile>() {
          public VirtualFile compute() {
            VirtualFile outputFile = localFileSystem.refreshAndFindFileByPath(outputFilePath);
            //if (outputFile == null) {
            //  outputFile =
            //    localFileSystem.refreshAndFindFileByPath(FlexUtils.getFlexCompilerWorkDirPath(project, null) + "/" + outputFilePath);
            //}
            if (outputFile == null) {
              for (final String baseDir : possibleBaseDirs) {
                outputFile = localFileSystem.refreshAndFindFileByPath(baseDir + "/" + outputFilePath);
                if (outputFile != null) {
                  break;
                }
              }
            }
            if (outputFile == null) return null;

            // it's important because this file has just been created
            outputFile.refresh(false, false);
            return outputFile;
          }
        }));
      }
    }, ModalityState.defaultModalityState());

    return outputFileRef.get();
  }
}
