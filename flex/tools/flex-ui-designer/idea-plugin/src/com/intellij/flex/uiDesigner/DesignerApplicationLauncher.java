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

import static com.intellij.flex.uiDesigner.AdlUtil.*;

public class DesignerApplicationLauncher extends DocumentTask {
  private boolean debug;
  private Future<ProjectComponentReferenceCounter> initializeThread;

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
    if (initializeThread != null) {
      initializeThread.cancel(true);
      initializeThread = null;
    }

    if (!DesignerApplicationManager.getInstance().isApplicationClosed()) {
      DesignerApplicationManager.getInstance().disposeApplication();
    }

    semaphore.up();
  }

  protected boolean doRun(@NotNull final ProgressIndicator indicator)
    throws IOException, java.util.concurrent.ExecutionException, InterruptedException, TimeoutException {
    final List<AdlRunConfiguration> adlRunConfigurations;
    indicator.setText(FlashUIDesignerBundle.message("copying.app.files"));

    copyAppFiles();
    indicator.setText(FlashUIDesignerBundle.message("finding.suitable.air.runtime"));
    adlRunConfigurations = getSuitableAdlRunConfigurations();

    if (adlRunConfigurations.isEmpty()) {
      notifyNoSuitableSdkToLaunch();
      return false;
    }

    indicator.checkCanceled();

    DesignerApplicationManager.getInstance().setApplication(new DesignerApplication());

    runInitializeLibrariesAndModuleThread();
    if (debug) {
      runAndWaitDebugger();
    }

    indicator.checkCanceled();

    MessageSocketManager messageSocketManager = new MessageSocketManager(this, DesignerApplicationManager.APP_DIR);
    Disposer.register(DesignerApplicationManager.getApplication(), messageSocketManager);

    final List<String> arguments = new ArrayList<String>();
    arguments.add(Integer.toString(messageSocketManager.listen()));
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      arguments.add("-p");
      arguments.add(DebugPathManager.getFudHome() + "/test-plugin/target/test-1.0-SNAPSHOT.swf");
    }

    AdlProcessHandler adlProcessHandler = null;
    final Ref<Boolean> found = new Ref<Boolean>(true);
    for (final AdlRunConfiguration adlRunConfiguration : adlRunConfigurations) {
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
          new Consumer<Integer>() {
            @Override
            public void consume(Integer exitCode) {
              found.set(false);
              if (!indicator.isCanceled()) {
                LOG.info(describeAdlExit(exitCode));
                semaphore.up();
              }
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

    final ProjectComponentReferenceCounter projectComponentReferenceCounter = initializeThread.get(DebugPathManager.IS_DEV ? 999 : 60, TimeUnit.SECONDS);
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

  private void runAndWaitDebugger() {
    final Semaphore debuggerRunSemaphor = new Semaphore();
    debuggerRunSemaphor.down();
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          runDebugger(module, new Runnable() {
            @Override
            public void run() {
              debuggerRunSemaphor.up();
            }
          });
        }
        catch (ExecutionException e) {
          LOG.error(e);
        }
      }
    });

    debuggerRunSemaphor.waitFor();
  }

  private void notifyNoSuitableSdkToLaunch() {
    String message = FlashUIDesignerBundle.message(SystemInfo.isLinux ? "no.sdk.to.launch.designer.linux" : "no.sdk.to.launch.designer");
    DesignerApplicationManager.notifyUser(debug, message, module.getProject(), new Consumer<String>() {
      @Override
      public void consume(String id) {
        if ("edit".equals(id)) {
          new ProjectJdksEditor(null, module.getProject(), WindowManager.getInstance().suggestParentWindow(myProject)).show();
        }
        else {
          LOG.error("unexpected id: " + id);
        }
      }
    });
  }

  private static List<AdlRunConfiguration> getSuitableAdlRunConfigurations() throws IOException {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return Collections.singletonList(createTestAdlRunConfiguration());
    }

    final List<Sdk> sdks = new ArrayList<Sdk>();
    for (Sdk sdk : FlexSdkUtils.getFlexAndFlexmojosSdks()) {
      if (StringUtil.compareVersionNumbers(sdk.getVersionString(), "4.5") >= 0) {
        sdks.add(sdk);
      }
    }

    Collections.sort(sdks, new Comparator<Sdk>() {
      @Override
      public int compare(Sdk o1, Sdk o2) {
        return StringUtil.compareVersionNumbers(o2.getVersionString(), o1.getVersionString());
      }
    });

    final String installedRuntime = findInstalledRuntime();
    final List<AdlRunConfiguration> result = new ArrayList<AdlRunConfiguration>(sdks.size());
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
    final Pair<String, String>[] files = new Pair[]{
      new Pair("designer-air2.6.swf", "main-loader/target/main-loader-1.0-SNAPSHOT-air2.6.swf"),
      new Pair("designer-air3.0.swf", "main-loader/target/main-loader-1.0-SNAPSHOT.swf"),
      new Pair("descriptor-air2.6.xml", "main/resources/descriptor-air2.6.xml"),
      new Pair("descriptor-air3.0.xml", "main/resources/descriptor-air3.0.xml")
    };

    if (DebugPathManager.IS_DEV) {
      final String homePath = DebugPathManager.getFudHome();
      final File home = new File(homePath);
      for (Pair<String, String> file : files) {
        IOUtil.saveStream(new URL("file://" + home + "/" + file.second), new File(DesignerApplicationManager.APP_DIR, file.first));
      }
    }
    else {
      final ClassLoader classLoader = DesignerApplicationLauncher.class.getClassLoader();
      //noinspection unchecked
      for (Pair<String, String> file : files) {
        IOUtil.saveStream(classLoader.getResource(file.first), new File(DesignerApplicationManager.APP_DIR, file.first));
      }
    }
  }

  private void runInitializeLibrariesAndModuleThread() {
    initializeThread = ApplicationManager.getApplication().executeOnPooledThread(new Callable<ProjectComponentReferenceCounter>() {
      @Nullable
      @Override
      public ProjectComponentReferenceCounter call() {
        try {
          LibraryManager.getInstance().init();
          indicator.checkCanceled();

          if (!StringRegistry.getInstance().isEmpty()) {
            Client.getInstance().initStringRegistry();
          }
          indicator.setText(FlashUIDesignerBundle.message("collect.libraries"));
          return LibraryManager.getInstance().registerModule(module, problemsHolder);
        }
        catch (Throwable e) {
          if (initializeThread == null || initializeThread.isCancelled()) {
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
      }
    });
  }
}