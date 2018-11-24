package com.intellij.flex.uiDesigner;

import com.intellij.execution.ExecutionException;
import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.flex.uiDesigner.io.MessageSocketManager;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.InitException;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.ui.ProjectJdksEditor;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.Consumer;
import com.intellij.util.concurrency.Semaphore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.flex.uiDesigner.AdlUtil.*;

public class DesignerApplicationLauncher extends DocumentTask {
  private boolean debug;
  private Future<ProjectComponentReferenceCounter> initializeTask;

  private final Semaphore semaphore = new Semaphore();

  DesignerApplicationLauncher(final @NotNull Module module, final @NotNull PostTask postTask, final boolean debug) {
    super(module, debug, postTask);

    this.debug = debug;
  }

  DesignerApplicationLauncher(final @NotNull Module module, final @NotNull PostTask postTask) {
    this(module, postTask, false);
  }

  public void clientOpened(@NotNull OutputStream outputStream) {
    Client.getInstance().setOut(outputStream);
    LOG.info("Client opened");
    semaphore.up();
  }

  public void clientSocketNotAccepted() {
    indicator.cancel();
  }

  @Override
  protected void beforeRun() {
    File startupErrorFile = new File(DesignerApplicationManager.APP_DIR, "startup-error.txt");
    if (startupErrorFile.exists()) {
      //noinspection ResultOfMethodCallIgnored
      startupErrorFile.delete();
    }
  }

  @Override
  protected void processErrorOrCancel() {
    if (initializeTask != null) {
      initializeTask.cancel(true);
      initializeTask = null;
    }

    if (!DesignerApplicationManager.getInstance().isApplicationClosed()) {
      DesignerApplicationManager.getInstance().disposeApplication();
    }

    semaphore.up();
  }

  @Override
  protected boolean doRun(@NotNull final ProgressIndicator indicator)
    throws IOException, java.util.concurrent.ExecutionException, InterruptedException, TimeoutException {
    indicator.setText(FlashUIDesignerBundle.message("copying.app.files"));

    copyAppFiles();
    indicator.setText(FlashUIDesignerBundle.message("finding.suitable.air.runtime"));
    List<AdlRunConfiguration> adlRunConfigurations = getSuitableAdlRunConfigurations();
    if (adlRunConfigurations.isEmpty()) {
      notifyNoSuitableSdkToLaunch();
      return false;
    }

    indicator.checkCanceled();

    DesignerApplicationManager.getInstance().setApplication(new DesignerApplication());

    runInitializeLibrariesAndModuleThread();
    if (debug && !runAndWaitDebugger()) {
      return false;
    }

    indicator.checkCanceled();

    MessageSocketManager messageSocketManager = new MessageSocketManager(this, DesignerApplicationManager.APP_DIR);
    Disposer.register(DesignerApplicationManager.getApplication(), messageSocketManager);

    final List<String> arguments = new ArrayList<>();
    arguments.add(Integer.toString(messageSocketManager.listen()));
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      arguments.add("-p");
      arguments.add(DebugPathManager.resolveTestArtifactPath("test-1.0-SNAPSHOT.swf"));
    }

    AdlProcessHandler adlProcessHandler = null;
    final Ref<Boolean> found = new Ref<>(true);
    for (AdlRunConfiguration adlRunConfiguration : adlRunConfigurations) {
      found.set(true);
      adlRunConfiguration.arguments = arguments;
      try {
        final String appClassifierVersion;
        if (StringUtil.compareVersionNumbers(adlRunConfiguration.getRuntimeVersion(), "3.0") < 0 || !(SystemInfo.isMac || SystemInfo.isWindows)) {
          appClassifierVersion = "2.6";
        }
        else {
          appClassifierVersion = "3.0";
        }

        adlProcessHandler = runAdl(adlRunConfiguration, DesignerApplicationManager.APP_DIR.getPath() + File.separatorChar + "descriptor-air" + appClassifierVersion + ".xml",
                                   exitCode -> {
                                     found.set(false);
                                     if (!indicator.isCanceled()) {
                                       LOG.info(describeAdlExit(exitCode));
                                       semaphore.up();
                                     }
                                   });
      }
      catch (ExecutionException e) {
        adlProcessHandler = null;
        LOG.error(e);
        continue;
      }

      semaphore.down();
      try {
        if (!semaphore.waitForUnsafe(60 * 1000)) {
          found.set(false);
          LOG.warn("Client not opened in 60 seconds");
          if (checkStartupError()) {
            return false;
          }
        }
      }
      catch (InterruptedException e) {
        if (indicator.isCanceled()) {
          return false;
        }

        LOG.warn(e);
        continue;
      }

      indicator.checkCanceled();

      if (found.get()) {
        break;
      }
    }

    if (!found.get()) {
      if (!checkStartupError()) {
        notifyNoSuitableSdkToLaunch();
      }
      return false;
    }

    ProjectComponentReferenceCounter projectComponentReferenceCounter = initializeTask.get(DebugPathManager.IS_DEV ? 999 : 60, TimeUnit.SECONDS);
    indicator.checkCanceled();

    final DesignerApplication application = DesignerApplicationManager.getApplication();
    LOG.assertTrue(adlProcessHandler != null && application != null);
    application.setProcessHandler(adlProcessHandler);
    DesignerApplicationManager.getInstance().attachProjectAndModuleListeners(application);

    return postTask.run(module, projectComponentReferenceCounter, indicator, problemsHolder);
  }

  private static boolean checkStartupError() throws IOException {
    final File startupErrorFile = new File(DesignerApplicationManager.APP_DIR, "startup-error.txt");
    if (!startupErrorFile.exists()) {
      return false;
    }

    LOG.error(FileUtil.loadFile(startupErrorFile));
    //noinspection ResultOfMethodCallIgnored
    startupErrorFile.delete();

    return true;
  }

  private boolean runAndWaitDebugger() {
    final AtomicBoolean result = new AtomicBoolean();
    final Semaphore debuggerRunSemaphore = new Semaphore();
    debuggerRunSemaphore.down();
    ApplicationManager.getApplication().invokeLater(() -> {
      try {
        runDebugger(module, () -> {
          result.set(true);
          debuggerRunSemaphore.up();
        });
      }
      catch (ExecutionException e) {
        LOG.error(e);
        debuggerRunSemaphore.up();
      }
    });

    debuggerRunSemaphore.waitFor();
    return result.get();
  }

  private void notifyNoSuitableSdkToLaunch() {
    String message = FlashUIDesignerBundle.message(SystemInfo.isLinux ? "no.sdk.to.launch.designer.linux" : "no.sdk.to.launch.designer");
    DesignerApplicationManager.notifyUser(debug, message, module.getProject(), id -> {
      if ("edit".equals(id)) {
        new ProjectJdksEditor(null, module.getProject(), WindowManager.getInstance().suggestParentWindow(myProject)).show();
      }
      else {
        LOG.error("unexpected id: " + id);
      }
    });
  }

  private static List<AdlRunConfiguration> getSuitableAdlRunConfigurations() throws IOException {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return Collections.singletonList(createTestAdlRunConfiguration());
    }

    final List<Sdk> sdks = new ArrayList<>();
    for (Sdk sdk : FlexSdkUtils.getFlexAndFlexmojosSdks()) {
      if (StringUtil.compareVersionNumbers(sdk.getVersionString(), "4.5") >= 0) {
        sdks.add(sdk);
      }
    }

    Collections.sort(sdks, (o1, o2) -> StringUtil.compareVersionNumbers(o2.getVersionString(), o1.getVersionString()));

    final String installedRuntime = findInstalledRuntime();
    final List<AdlRunConfiguration> result = new ArrayList<>(sdks.size());
    for (Sdk sdk : sdks) {
      final String adlPath = FlexSdkUtils.getAdlPath(sdk);
      if (!checkAdl(adlPath)) {
        continue;
      }

      String runtime = FlexSdkUtils.getAirRuntimePath(sdk);
      if (checkRuntime(runtime) || checkRuntime((runtime = installedRuntime))) {
        result.add(new AdlRunConfiguration(adlPath, runtime, StringUtil.compareVersionNumbers(sdk.getVersionString(), "4.6") < 0 ? "2.6" : "3.0"));
      }
    }

    return result;
  }

  private static AdlRunConfiguration createTestAdlRunConfiguration() {
    String adlExecutable = System.getProperty("adl.executable");
    if (adlExecutable == null) {
      if (SystemInfo.isMac) {
        adlExecutable = System.getProperty("user.home") + "/sdks/flex4.6.0/bin/adl";
      }
      else {
        throw new IllegalStateException("Please define 'adl.executable' to point to ADL executable");
      }
    }

    String adlRuntime = System.getProperty("adl.runtime");
    if (adlRuntime == null) {
      if (SystemInfo.isMac) {
        adlRuntime = "/Library/Frameworks";
      }
      else {
        throw new IllegalStateException("Please define 'adl.runtime' to point to ADL runtime");
      }
    }

    return new AdlRunConfiguration(adlExecutable, adlRuntime, "3.0");
  }

  private static void copyAppFiles() throws IOException {
    @SuppressWarnings("unchecked")
    Pair<String, String>[] files = new Pair[]{
      new Pair("designer-air2.6.swf", "main-loader/target/main-loader-1.0-SNAPSHOT-air2.6.swf"),
      new Pair("designer-air3.0.swf", "main-loader/target/main-loader-1.0-SNAPSHOT.swf"),
      new Pair("descriptor-air2.6.xml", "main/resources/descriptor-air2.6.xml"),
      new Pair("descriptor-air3.0.xml", "main/resources/descriptor-air3.0.xml")
    };

    if (DebugPathManager.IS_DEV) {
      for (Pair<String, String> file : files) {
        IOUtil.saveStream(new URL("file://" + DebugPathManager.resolveTestArtifactPath(file.first, file.second)), new File(DesignerApplicationManager.APP_DIR, file.first));
      }
    }
    else {
      ClassLoader classLoader = DesignerApplicationLauncher.class.getClassLoader();
      //noinspection unchecked
      for (Pair<String, String> file : files) {
        IOUtil.saveStream(classLoader.getResource(file.first), new File(DesignerApplicationManager.APP_DIR, file.first));
      }
    }
  }

  private void runInitializeLibrariesAndModuleThread() {
    initializeTask = ApplicationManager.getApplication().executeOnPooledThread(() -> {
      try {
        LibraryManager.getInstance().init();
        indicator.checkCanceled();

        if (!StringRegistry.getInstance().isEmpty()) {
          Client.getInstance().initStringRegistry();
        }
        indicator.setText(FlashUIDesignerBundle.message("collect.libraries"));

        assert myProject != null;
        DumbService dumbService = DumbService.getInstance(myProject);
        if (dumbService.isDumb()) {
          dumbService.waitForSmartMode();
        }

        return LibraryManager.getInstance().registerModule(module, problemsHolder);
      }
      catch (Throwable e) {
        if (initializeTask == null || initializeTask.isCancelled()) {
          return null;
        }

        //noinspection InstanceofCatchParameter
        if (e instanceof InitException) {
          processInitException((InitException)e, module, debug);
        }
        else {
          LOG.error(e);
        }

        indicator.cancel();
        return null;
      }
    });
  }
}