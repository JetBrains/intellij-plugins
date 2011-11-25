package com.intellij.flex.uiDesigner;

import com.intellij.ProjectTopics;
import com.intellij.diagnostic.LogMessageEx;
import com.intellij.diagnostic.errordialog.Attachment;
import com.intellij.execution.ExecutionException;
import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.flex.uiDesigner.io.MessageSocketManager;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.InitException;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.mxml.ProjectDocumentReferenceCounter;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.ModuleAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerAdapter;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.ui.ProjectJdksEditor;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.Consumer;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.PlatformUtils;
import com.intellij.util.concurrency.Semaphore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.intellij.flex.uiDesigner.AdlUtil.*;

public class DesignerApplicationLauncher extends Task.Backgroundable {
  private static final Logger LOG = Logger.getInstance(DesignerApplicationLauncher.class.getName());

  private static final String DESIGNER_SWF = "designer.swf";
  private static final String DESCRIPTOR_XML = "descriptor.xml";
  private static final String DESCRIPTOR_XML_DEV_PATH = "main/resources/" + DESCRIPTOR_XML;

  private ProgressIndicator indicator;
  private Module module;

  private boolean debug;
  private final PostTask postTask;
  private Future<ProjectDocumentReferenceCounter> initializeThread;

  private final Semaphore semaphore = new Semaphore();

  private final ProblemsHolder problemsHolder = new ProblemsHolder();

  public DesignerApplicationLauncher(@NotNull final Module module, final boolean debug, @NotNull final PostTask postTask) {
    super(module.getProject(), DesignerApplicationManager.getOpenActionTitle(debug));

    this.module = module;
    this.debug = debug;
    this.postTask = postTask;
  }

  public void clientOpened() {
    LOG.info("clientOpened");
    semaphore.up();
  }

  public void clientSocketNotAccepted() {
    indicator.cancel();
  }

  @Override
  public void run(@NotNull final ProgressIndicator indicator) {
    this.indicator = indicator;

    try {
      Throwable error = null;
      try {
        doRun(indicator);
      }
      catch (Throwable e) {
        error = e;
      }

      if (error != null || indicator.isCanceled()) {
        if (!DesignerApplicationManager.getInstance().isApplicationClosed()) {
          DesignerApplicationManager.getInstance().disposeApplication();
        }

        if (initializeThread != null) {
          initializeThread.cancel(true);
          initializeThread = null;
        }

        semaphore.up();
      }

      if (error != null) {
        LOG.error(error);
      }
    }
    finally {
      postTask.end();
    }
  }

  private void doRun(@NotNull final ProgressIndicator indicator)
    throws IOException, java.util.concurrent.ExecutionException, InterruptedException, TimeoutException {
    final List<AdlRunConfiguration> adlRunConfigurations;
    indicator.setText(FlashUIDesignerBundle.message("copying.app.files"));

    copyAppFiles();
    indicator.setText(FlashUIDesignerBundle.message("finding.suitable.air.runtime"));
    adlRunConfigurations = getSuitableAdlRunConfigurations();

    if (adlRunConfigurations.isEmpty()) {
      notifyNoSuitableSdkToLaunch();
      return;
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
    for (final AdlRunConfiguration adlRunConfiguration : adlRunConfigurations) {
      final Ref<Boolean> found = new Ref<Boolean>(true);
      adlRunConfiguration.arguments = arguments;
      try {
        adlProcessHandler = runAdl(adlRunConfiguration, DesignerApplicationManager.APP_DIR.getPath() + File.separatorChar + DESCRIPTOR_XML,
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
        semaphore.waitForUnsafe(15 * 1024);
      }
      catch (InterruptedException e) {
        if (indicator.isCanceled()) {
          return;
        }
        continue;
      }

      indicator.checkCanceled();

      if (found.get()) {
        break;
      }
    }
    
    final ProjectDocumentReferenceCounter projectDocumentReferenceCounter = initializeThread.get(60, TimeUnit.SECONDS);
    indicator.checkCanceled();

    final DesignerApplication application = DesignerApplicationManager.getApplication();
    assert adlProcessHandler != null && application != null;
    application.setProcessHandler(adlProcessHandler);
    attachProjectAndModuleListeners(application);

    if (!postTask.run(projectDocumentReferenceCounter, indicator, problemsHolder)) {
      indicator.cancel();
    }
  }

  private static void attachProjectAndModuleListeners(DesignerApplication designerApplication) {
    Application application = ApplicationManager.getApplication();
    ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerAdapter() {
      @Override
      public void projectClosed(Project project) {
        Client client = Client.getInstance();
        if (client.getRegisteredProjects().contains(project)) {
          client.closeProject(project);
        }
      }
    }, designerApplication);

    application.getMessageBus().connect(designerApplication).subscribe(ProjectTopics.MODULES,
      new ModuleAdapter() {
        @Override
        public void moduleRemoved(Project project, Module module) {
          Client client = Client.getInstance();
          if (client.isModuleRegistered(module)) {
            client.unregisterModule(module);
          }
        }
      });
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
          // TODO wrap this in ProjectSettingsService?
          if (PlatformUtils.isFlexIde()) {
            if (ProjectSettingsService.getInstance(myProject).canOpenModuleDependenciesSettings()) {
              ProjectSettingsService.getInstance(myProject).openModuleDependenciesSettings(module, null);
            }
          }
          else {
            new ProjectJdksEditor(null, module.getProject(), WindowManager.getInstance().suggestParentWindow(myProject)).show();
          }
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
    for (Sdk sdk : PlatformUtils.isFlexIde() ? FlexSdkUtils.getAllFlexSdks() : FlexSdkUtils.getAllFlexRelatedSdks()) {
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
        result.add(new AdlRunConfiguration(adlPath, runtime));
      }
    }

    return result;
  }

  private static AdlRunConfiguration createTestAdlRunConfiguration() {
    String adlExecutable = System.getProperty("adl.executable");
    if (adlExecutable == null) {
      if (SystemInfo.isMac) {
        adlExecutable = "/Developer/SDKs/flex_4.5.1/bin/adl";
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

    return new AdlRunConfiguration(adlExecutable, adlRuntime);
  }

  private static void copyAppFiles() throws IOException {
    @SuppressWarnings("unchecked")
    final Pair<String, String>[] files = new Pair[]{new Pair(DESIGNER_SWF, "main-loader/target/main-loader-1.0-SNAPSHOT.swf"),
      new Pair(DESCRIPTOR_XML, DESCRIPTOR_XML_DEV_PATH)};

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
    LibraryManager.getInstance().setAppDir(DesignerApplicationManager.APP_DIR);
    initializeThread = ApplicationManager.getApplication().executeOnPooledThread(new Callable<ProjectDocumentReferenceCounter>() {
      @Nullable
      @Override
      public ProjectDocumentReferenceCounter call() {
        LibraryManager.getInstance().garbageCollection(indicator);
        indicator.checkCanceled();

        try {
          if (!StringRegistry.getInstance().isEmpty()) {
            Client.getInstance().initStringRegistry();
          }
          indicator.setText(FlashUIDesignerBundle.message("collect.libraries"));
          return LibraryManager.getInstance().initLibrarySets(module, true, problemsHolder);
        }
        catch (Throwable e) {
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

  static void processInitException(InitException e, Module module, boolean debug) {
    DesignerApplicationManager.notifyUser(debug, e.getMessage(), module);
    if (e.attachments == null) {
      LOG.error(e.getCause());
    }
    else {
      final Collection<Attachment> attachments = new ArrayList<Attachment>(e.attachments.length);
      for (Attachment attachment : e.attachments) {
        if (attachment != null) {
          attachments.add(attachment);
        }
        else {
          break;
        }
      }

      LOG.error(LogMessageEx.createEvent(e.getMessage(), e.technicalMessage + "\n" + ExceptionUtil.getThrowableText(e), e.getMessage(),
        null, attachments));
    }
  }

  interface PostTask {
    boolean run(ProjectDocumentReferenceCounter projectDocumentReferenceCounter, ProgressIndicator indicator, ProblemsHolder problemsHolder);

    void end();
  }
}