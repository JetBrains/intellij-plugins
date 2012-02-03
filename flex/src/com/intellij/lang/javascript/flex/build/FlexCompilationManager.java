package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.ModuleCompilerUtil;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.graph.Graph;
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

  private final Graph<Module> myModuleGraph;
  private boolean myCompilationFinished;
  private final FlexCompilerDependenciesCache myCompilerDependenciesCache;

  private static final String OUT_OF_MEMORY = "java.lang.OutOfMemoryError";
  private static final String JAVA_HEAP_SPACE = "Java heap space";
  private static final String COULD_NOT_CREATE_JVM = "Could not create the Java virtual machine";
  static final Pattern OUTPUT_FILE_CREATED_PATTERN = Pattern.compile("(\\[.*\\] )?(.+) \\(([0-9]+) bytes\\)");

  public FlexCompilationManager(final CompileContext context, final Collection<FlexCompilationTask> compilationTasks) {
    myCompileContext = context;
    myMaxParallelCompilations = FlexCompilerProjectConfiguration.getInstance(context.getProject()).MAX_PARALLEL_COMPILATIONS;
    myTasksAmount = compilationTasks.size();
    myNotStartedTasks = new LinkedList<FlexCompilationTask>(compilationTasks);
    myInProgressTasks = new LinkedList<FlexCompilationTask>();
    myFinishedTasks = new LinkedList<FlexCompilationTask>();

    myModuleGraph = compilationTasks.iterator().next().getBC() == null
                    ? ModuleCompilerUtil.createModuleGraph(ModuleManager.getInstance(context.getProject()).getModules())
                    : null;

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

      if (message.contains(COULD_NOT_CREATE_JVM)) {
        category = CompilerMessageCategory.ERROR;
      }

      if (category == CompilerMessageCategory.INFORMATION) {
        final Matcher matcher = FlexCompilationManager.OUTPUT_FILE_CREATED_PATTERN.matcher(message);
        if (matcher.matches()) {
          final String outputFilePath = matcher.group(2);
          // need to refresh FS in order to notify artifact compiler that Flex output has changed
          refreshAndFindFileInWriteAction(myCompileContext.getProject(), outputFilePath);
        }
      }

      final String prefix = "[" + task.getPresentableName() + "] ";
      myCompileContext.addMessage(category, prefix + message, url, lineNum, columnNum);

      if (message.contains(OUT_OF_MEMORY) || message.contains(JAVA_HEAP_SPACE)) {
        myCompileContext
          .addMessage(CompilerMessageCategory.ERROR, prefix + FlexBundle.message("increase.flex.compiler.heap"), null, -1, -1);
      }
    }
  }

  public boolean isMake() {
    return myCompileContext.isMake();
  }

  private void checkFinishedTasks() {
    final Iterator<FlexCompilationTask> iterator = myInProgressTasks.iterator();
    while (iterator.hasNext()) {
      FlexCompilationTask task = iterator.next();
      if (task.isFinished()) {
        iterator.remove();
        myFinishedTasks.add(task);

        if (task.isCompilationFailed()) {
          final Collection<FlexCompilationTask> cancelledTasks = getNotStartedDependentTasks(task);
          if (cancelledTasks.isEmpty()) {
            addMessage(task, CompilerMessageCategory.INFORMATION, FlexBundle.message("compilation.failed"), null, -1, -1);
          }
          else {
            addMessage(task, CompilerMessageCategory.INFORMATION, FlexBundle.message("compilation.failed.dependent.will.be.skipped"), null,
                       -1, -1);
            for (final FlexCompilationTask cancelledTask : cancelledTasks) {
              addMessage(cancelledTask, CompilerMessageCategory.INFORMATION, FlexBundle.message("compilation.skipped"), null, -1, -1);
            }
          }
        }
        else {
          addMessage(task, CompilerMessageCategory.INFORMATION, FlexBundle.message("compilation.successfull"), null, -1, -1);

          final FlexIdeBuildConfiguration config = task.getBC();
          if (config != null) {
            try {
              FlexCompilationUtils.performPostCompileActions(config);
            }
            catch (FlexCompilerException e) {
              addMessage(task, CompilerMessageCategory.ERROR, e.getMessage(), e.getUrl(), e.getLine(), e.getColumn());
            }
          }
        }

        if (task.useCache()) {
          final Module module = task.getModule();
          if (task.isCompilationFailed()) {
            myCompilerDependenciesCache.markModuleAndDependentModulesDirty(module);
          }
          else if (areAllModuleCompilationsSuccessful(module)) {
            final Collection<List<VirtualFile>> allConfigFiles = new ArrayList<List<VirtualFile>>();
            for (final FlexCompilationTask t : myFinishedTasks) {
              if (!(t instanceof CssCompilationTask) && t.getModule().equals(module)) {
                allConfigFiles.add(t.getConfigFiles());
              }
            }

            //noinspection SynchronizeOnThis
            synchronized (this) {
              myCompilerDependenciesCache.cacheModuleWithDependencies(myCompileContext, module, allConfigFiles);
            }
          }
        }
      }
    }
  }

  private boolean areAllModuleCompilationsSuccessful(final Module module) {
    for (final FlexCompilationTask task : myNotStartedTasks) {
      if (task.getModule().equals(module)) {
        return false;
      }
    }

    for (final FlexCompilationTask task : myInProgressTasks) {
      if (task.getModule().equals(module)) {
        return false;
      }
    }

    for (final FlexCompilationTask task : myFinishedTasks) {
      if (task.getModule().equals(module) && task.isCompilationFailed()) {
        return false;
      }
    }

    return true;
  }

  private Collection<FlexCompilationTask> getNotStartedDependentTasks(final FlexCompilationTask failedTask) {
    final Collection<FlexCompilationTask> cancelledTasks = new LinkedList<FlexCompilationTask>();
    appendNotStartedDependentTasks(cancelledTasks, failedTask);
    return cancelledTasks;
  }

  private void appendNotStartedDependentTasks(final Collection<FlexCompilationTask> cancelledTasks,
                                              final FlexCompilationTask task) {
    final Collection<FlexCompilationTask> tasksToCancel = new ArrayList<FlexCompilationTask>();

    if (task.getBC() == null) {
      final Iterator<Module> dependentModulesIterator = myModuleGraph.getOut(task.getModule());

      while (dependentModulesIterator.hasNext()) {
        final Module dependentModule = dependentModulesIterator.next();
        for (FlexCompilationTask notStartedTask : myNotStartedTasks) {
          if (dependentModule.equals(notStartedTask.getModule())) {
            tasksToCancel.add(notStartedTask);
          }
        }
      }
    }
    else {
      for (FlexCompilationTask notStartedTask : myNotStartedTasks) {
        //noinspection ConstantConditions
        if (notStartedTask.getDependencies().contains(task.getBC())) {
          tasksToCancel.add(notStartedTask);
        }
      }
    }

    if (!tasksToCancel.isEmpty()) {
      for (FlexCompilationTask taskToCancel : tasksToCancel) {
        taskToCancel.cancel();
        if (myNotStartedTasks.remove(taskToCancel)) {
          myFinishedTasks.add(taskToCancel);
          cancelledTasks.add(taskToCancel);
          appendNotStartedDependentTasks(cancelledTasks, taskToCancel);
        }
      }
    }
  }

  private void startNewTaskIfPossible() {
    FlexCompilationTask taskToStart = null;

    if (!myNotStartedTasks.isEmpty() && myInProgressTasks.size() < myMaxParallelCompilations) {
      boolean allTasksHaveDependenciesOnlyInNotStarted = true; // to handle cyclic dependencies

      for (FlexCompilationTask task : myNotStartedTasks) {

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

        if (taskToStart.useCache() && isMake()
            && myCompilerDependenciesCache.isNothingChangedSincePreviousCompilation(taskToStart.getModule())) {
          addMessage(taskToStart, CompilerMessageCategory.INFORMATION, FlexBundle.message("compilation.skipped.because.nothing.changed"),
                     null, -1, -1);
          taskToStart.cancel();
          myFinishedTasks.add(taskToStart);
        }
        else {
          taskToStart.start(this);
          myInProgressTasks.add(taskToStart);
        }

        startNewTaskIfPossible();
      }
    }
  }

  private boolean hasDependenciesIn(final FlexCompilationTask task, final Collection<FlexCompilationTask> tasksToSearchDependencies) {
    if (task.getBC() == null) {
      final Iterator<Module> dependencies = myModuleGraph.getIn(task.getModule());
      while (dependencies.hasNext()) {
        final Module dependency = dependencies.next();
        for (FlexCompilationTask potentialDependency : tasksToSearchDependencies) {
          if (dependency.equals(potentialDependency.getModule())) {
            return true;
          }
        }
      }
      return false;
    }
    else {
      for (final FlexCompilationTask otherTask : tasksToSearchDependencies) {
        //noinspection ConstantConditions
        if (task.getDependencies().contains(otherTask.getBC())) {
          return true;
        }
      }
      return false;
    }
  }

  private void updateProgressIndicator() {
    final ProgressIndicator progressIndicator = myCompileContext.getProgressIndicator();
    progressIndicator.setFraction(1. * myFinishedTasks.size() / myTasksAmount);
    final StringBuilder builder = new StringBuilder();
    for (FlexCompilationTask inProgressTask : myInProgressTasks) {
      if (builder.length() > 0) builder.append(", ");
      builder.append(inProgressTask.getPresentableName());
    }
    progressIndicator.setText(FlexBundle.message("compiling", builder.toString()));
  }

  static VirtualFile refreshAndFindFileInWriteAction(final Project project, final String outputFilePath, final String... possibleBaseDirs) {
    final LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
    final Application application = ApplicationManager.getApplication();
    final Ref<VirtualFile> outputFileRef = new Ref<VirtualFile>();

    application.invokeAndWait(new Runnable() {
      public void run() {
        outputFileRef.set(application.runWriteAction(new NullableComputable<VirtualFile>() {
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
