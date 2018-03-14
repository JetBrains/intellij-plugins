package com.intellij.prettierjs;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.modules.NodeModuleUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.platform.DirectoryProjectConfigurator;
import com.intellij.util.ObjectUtils;
import com.intellij.util.Producer;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class PrettierConfiguration {
  @NotNull
  private final Project myProject;
  private final PropertiesComponent myPropertiesComponent;
  private static final String NODE_INTERPRETER_PROPERTY_KEY = "prettierjs.PrettierConfiguration.NodeInterpreter";
  private static final String PACKAGE_PROPERTY_KEY = "prettierjs.PrettierConfiguration.Package";

  public PrettierConfiguration(@NotNull Project project, @NotNull PropertiesComponent component) {
    myProject = project;
    myPropertiesComponent = component;
    ExecutorService threadPoolExecutor = AppExecutorUtil.createBoundedApplicationPoolExecutor("prettier.packageJsonUpdater", 1);
    project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
      @Override
      public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent event : events) {
          if (PackageJsonUtil.isPackageJsonFile(event.getFile())) {
            threadPoolExecutor.submit(() -> ReadAction.run(PrettierConfiguration.this::detectLocalPackage));
          }
        }
      }
    });
  }

  @NotNull
  public static PrettierConfiguration getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, PrettierConfiguration.class);
  }

  @NotNull
  public NodeJsInterpreterRef getInterpreterRef() {
    return NodeJsInterpreterRef.create(myPropertiesComponent.getValue(NODE_INTERPRETER_PROPERTY_KEY));
  }
  
  @Nullable
  public NodePackage getPackage() {
    return new NodePackage(myPropertiesComponent.getValue(PACKAGE_PROPERTY_KEY, ""));
  }

  public void update(@Nullable NodeJsInterpreter nodeInterpreter, @Nullable NodePackage nodePackage) {
    myPropertiesComponent.setValue(NODE_INTERPRETER_PROPERTY_KEY, nodeInterpreter != null ? nodeInterpreter.toRef().getReferenceName() : null);
    myPropertiesComponent.setValue(PACKAGE_PROPERTY_KEY, nodePackage != null ? nodePackage.getSystemDependentPath() : null);
  }

  private void detectLocalOrGlobalPackage() {
    detectPackage(() -> ObjectUtils.coalesce(localPackageIfInDependencies(), findDefaultPackage()));
  }

  private void detectLocalPackage() {
    detectPackage(() -> localPackageIfInDependencies());
  }

  private void detectPackage(@NotNull Producer<NodePackage> packageProducer) {
    if (myProject.isDisposed() || myProject.isDefault()) {
      return;
    }
    String stored = myPropertiesComponent.getValue(PACKAGE_PROPERTY_KEY, "");
    if (!StringUtil.isEmpty(stored)) {
      return;
    }

    NodePackage detected = packageProducer.produce();
    if (detected != null) {
      myPropertiesComponent.setValue(PACKAGE_PROPERTY_KEY, detected.getSystemDependentPath());
    }
  }

  @Nullable
  private NodePackage localPackageIfInDependencies() {
    if (myProject.isDefault() || myProject.getBasePath() == null) {
      return null;
    }
    final PackageJsonData data = PackageJsonUtil.getTopLevelPackageJsonData(myProject);
    if (data != null && data.isDependencyOfAnyType(PrettierUtil.PACKAGE_NAME)) {
      final String basePath = FileUtil.toSystemDependentName(myProject.getBasePath());
      return new NodePackage((basePath.endsWith(File.separator) ? basePath : (basePath + File.separator))
                             + NodeModuleUtil.NODE_MODULES + File.separator
                             + PrettierUtil.PACKAGE_NAME);
    }
    return null;
  }

  @Nullable
  private NodePackage findDefaultPackage() {
    return NodePackage.findDefaultPackage(myProject, PrettierUtil.PACKAGE_NAME, getInterpreterRef().resolve(myProject));
  }

  public static class ProjectConfigurator implements DirectoryProjectConfigurator {
    @Override
    public void configureProject(Project project, @NotNull VirtualFile baseDir, Ref<Module> moduleRef) {
      StartupManager.getInstance(project).runWhenProjectIsInitialized(
        (DumbAwareRunnable)() -> getInstance(project).detectLocalOrGlobalPackage());
    }
  }
}
