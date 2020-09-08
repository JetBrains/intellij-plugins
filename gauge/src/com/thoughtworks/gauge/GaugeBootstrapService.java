package com.thoughtworks.gauge;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import com.thoughtworks.gauge.connection.GaugeConnection;
import com.thoughtworks.gauge.core.GaugeCli;
import com.thoughtworks.gauge.core.GaugeExceptionHandler;
import com.thoughtworks.gauge.exception.GaugeNotFoundException;
import com.thoughtworks.gauge.module.lib.GaugeLibHelper;
import com.thoughtworks.gauge.module.lib.LibHelper;
import com.thoughtworks.gauge.module.lib.LibHelperFactory;
import com.thoughtworks.gauge.reference.ReferenceCache;
import com.thoughtworks.gauge.settings.GaugeSettingsModel;
import com.thoughtworks.gauge.util.GaugeUtil;
import com.thoughtworks.gauge.util.SocketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.intellij.openapi.progress.PerformInBackgroundOption.ALWAYS_BACKGROUND;

@Service
public final class GaugeBootstrapService implements Disposable {
  private static final Logger LOG = Logger.getInstance(GaugeBootstrapService.class);

  private final Map<Module, GaugeCli> gaugeProjectHandle = new ConcurrentHashMap<>();
  private final Map<String, HashSet<Module>> linkedModulesMap = new ConcurrentHashMap<>();
  private final Map<Module, ReferenceCache> moduleReferenceCaches = new ConcurrentHashMap<>();

  private final Project myProject;
  private final MergingUpdateQueue myUpdateQueue = new MergingUpdateQueue("GAUGE_BOOTSTRAP", 5000, true, null, this);

  private final Queue<WeakReference<Module>> modulesQueue = new LinkedBlockingQueue<>();

  private GaugeBootstrapService(Project project) {
    myProject = project;
  }

  public static GaugeBootstrapService getInstance(Project project) {
    return project.getService(GaugeBootstrapService.class);
  }

  public void moduleAdded(@NotNull Module module) {
    modulesQueue.add(new WeakReference<>(module));

    myUpdateQueue.queue(Update.create("INIT_GAUGE", () -> {
      Task.Backgroundable task = new Task.Backgroundable(myProject,
                                                         GaugeBundle.message("gauge.connecting"),
                                                         false, ALWAYS_BACKGROUND) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          bootstrapServiceForModules(indicator);
        }
      };

      ProgressManager.getInstance()
        .runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
    }));
  }

  public void moduleRemoved(@NotNull Module module) {
    disposeComponent(module);
  }

  private void bootstrapServiceForModules(@NotNull ProgressIndicator indicator) {
    WeakReference<Module> mRef;
    while ((mRef = modulesQueue.poll()) != null) {
      Module module = mRef.get();
      if (module != null && !module.isDisposed()) {
        LibHelper helper = ReadAction.compute(() -> new LibHelperFactory().helperFor(module));
        if (helper == LibHelperFactory.NO_GAUGE_MODULE) continue;

        indicator.setText(GaugeBundle.message("gauge.init.connection.for", module.getName()));
        helper.initConnection();

        if (helper instanceof GaugeLibHelper) {
          WriteCommandAction.runWriteCommandAction(myProject,
                                                   GaugeBundle.message("gauge.check.dependencies"),
                                                   GaugeBundle.GAUGE, () -> {
              if (module.isDisposed()) return;

              ((GaugeLibHelper)helper).checkModuleDependencies();
            });
        }
      }
    }

    WriteCommandAction.runWriteCommandAction(myProject, GaugeBundle.message("gauge.restart.inspections"), GaugeBundle.GAUGE, () -> {
      DaemonCodeAnalyzer.getInstance(myProject).restart();
    });
  }

  private void addModule(Module module, GaugeCli gaugeCli) {
    Set<Module> modules = getSubModules(module);
    if (modules.isEmpty()) modules.add(module);

    for (Module m : modules) {
      gaugeProjectHandle.put(m, gaugeCli);
    }
  }

  /**
   * Creates a gauge service for the particular module. GaugeCli is used to make api calls to the gauge daemon process.
   */
  public GaugeCli startGaugeCli(Module module) {
    int freePortForApi = SocketUtils.findFreePortForApi();
    Process gaugeProcess = initializeGaugeProcess(freePortForApi, module);
    GaugeConnection gaugeConnection = initializeGaugeConnection(freePortForApi);
    GaugeCli gaugeCli = new GaugeCli(gaugeProcess, gaugeConnection);
    addModule(module, gaugeCli);
    return gaugeCli;
  }

  private static GaugeConnection initializeGaugeConnection(int apiPort) {
    if (apiPort != -1) {
      LOG.info("Initializing Gauge connection at " + apiPort);
      GaugeConnection gaugeConn = null;

      for (int i = 1; i <= 10; i++) {
        try {
          gaugeConn = new GaugeConnection(apiPort);
          break;
        }
        catch (RuntimeException ex) {
          LOG.warn("Unable to open connection on try " + i + ". Waiting and trying again");
          try {
            Thread.sleep(5000);
          }
          catch (InterruptedException e) {
            LOG.debug(e);
          }
        }
      }

      return gaugeConn;
    }
    else {
      return null;
    }
  }

  private static Process initializeGaugeProcess(int apiPort, Module module) {
    try {
      GaugeSettingsModel settings = GaugeUtil.getGaugeSettings();
      String port = String.valueOf(apiPort);
      ProcessBuilder gauge = new ProcessBuilder(settings.getGaugePath(), Constants.DAEMON, port);
      GaugeUtil.setGaugeEnvironmentsTo(gauge, settings);
      String cp = GaugeUtil.classpathForModule(module);
      LOG.info(String.format("Setting `%s` to `%s`", Constants.GAUGE_CUSTOM_CLASSPATH, cp));
      gauge.environment().put(Constants.GAUGE_CUSTOM_CLASSPATH, cp);
      File dir = GaugeUtil.moduleDir(module);
      LOG.info(String.format("Using `%s` as api port to connect to gauge API for project %s", port, dir));
      gauge.directory(dir);
      Process process = gauge.start();
      new GaugeExceptionHandler(process, module.getProject()).start();
      return process;
    }
    catch (IOException | GaugeNotFoundException e) {
      LOG.error("An error occurred while starting gauge api. \n" + e);
    }
    return null;
  }

  public GaugeCli getGaugeCli(@Nullable Module module, boolean moduleDependent) {
    if (module == null) {
      return null;
    }
    GaugeCli service = gaugeProjectHandle.get(module);
    if (service != null) return service;
    Set<Module> modules = getSubModules(module);
    for (Module m : modules) {
      service = gaugeProjectHandle.get(m);
      if (service != null) {
        addModule(module, service);
        return service;
      }
    }
    return moduleDependent ? null : getGaugeCli();
  }

  public ReferenceCache getReferenceCache(Module module) {
    ReferenceCache referenceCache = moduleReferenceCaches.get(module);
    if (referenceCache == null) {
      referenceCache = new ReferenceCache();
      moduleReferenceCaches.put(module, referenceCache);
    }
    return referenceCache;
  }

  public Set<Module> getSubModules(Module module) {
    String value = getProjectGroupValue(module);

    Set<Module> modules = linkedModulesMap.get(value);
    if (modules != null) return modules;
    modules = new HashSet<>();
    for (Module m : ModuleManager.getInstance(module.getProject()).getModules()) {
      if (getProjectGroupValue(m).contains(value)) {
        modules.add(m);
        addToModulesMap(m, value);
      }
    }
    return modules;
  }

  private void addToModulesMap(Module module, String name) {
    if (!linkedModulesMap.containsKey(name)) {
      linkedModulesMap.put(name, new HashSet<>());
    }
    linkedModulesMap.get(name).add(module);
  }

  @NotNull
  private static String getProjectGroupValue(Module module) {
    String[] values = ModuleManager.getInstance(module.getProject()).getModuleGroupPath(module);
    return values == null || values.length < 1 ? module.getName() : values[0];
  }

  private GaugeCli getGaugeCli() {
    Iterator<GaugeCli> iterator = gaugeProjectHandle.values().iterator();
    return iterator.hasNext() ? iterator.next() : null;
  }

  public void disposeComponent(@Nullable Module module) {
    if (module == null) return;

    modulesQueue.remove(module);
    String value = getProjectGroupValue(module);
    linkedModulesMap.remove(value);
    moduleReferenceCaches.remove(module);
    GaugeCli service = gaugeProjectHandle.get(module);

    if (service != null && service.getGaugeProcess().isAlive()) {
      LOG.info("Stopping Gauge PID: " + service.getGaugeProcess().pid());

      service.getGaugeProcess().destroy();
    }
  }

  @Override
  public void dispose() {
    modulesQueue.clear();
  }
}
