// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
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
import com.intellij.util.concurrency.SequentialTaskExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public class PrettierConfiguration implements JSNpmLinterState {
  @NotNull
  private final Project myProject;
  private final PropertiesComponent myPropertiesComponent;
  private static final String NODE_INTERPRETER_PROPERTY = "prettierjs.PrettierConfiguration.NodeInterpreter";
  private static final String PACKAGE_PROPERTY = "prettierjs.PrettierConfiguration.Package";
  private static final String OLD_PACKAGE_PROPERTY = "node.js.selected.package.prettier";
  private static final String OLD_INTERPRETER_PROPERTY = "node.js.path.for.package.prettier";

  public PrettierConfiguration(@NotNull Project project, @NotNull PropertiesComponent component) {
    myProject = project;
    myPropertiesComponent = component;
    ExecutorService threadPoolExecutor = SequentialTaskExecutor.createSequentialApplicationPoolExecutor("Prettier.PackageJsonUpdater");
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

  @Override
  @NotNull
  public NodeJsInterpreterRef getInterpreterRef() {
    return NodeJsInterpreterRef.create(ObjectUtils.coalesce(myPropertiesComponent.getValue(NODE_INTERPRETER_PROPERTY),
                                                            myPropertiesComponent.getValue(OLD_INTERPRETER_PROPERTY)));
  }

  @NotNull
  @Override
  public NodePackageRef getNodePackageRef() {
    return NodePackageRef.create(getPackage());
  }

  @Override
  public JSNpmLinterState withLinterPackage(@NotNull NodePackageRef nodePackage) {
    NodePackage newPackage = nodePackage.getConstantPackage();
    assert newPackage != null : getClass().getSimpleName() + "does not support non-constant package";
    update(this.getInterpreterRef(), newPackage);
    return null;
  }

  @Override
  public JSNpmLinterState withInterpreterRef(NodeJsInterpreterRef ref) {
    update(ref, this.getPackage());
    return this;
  }

  @NotNull
  public NodePackage getPackage() {
    String value = ObjectUtils.coalesce(myPropertiesComponent.getValue(PACKAGE_PROPERTY),
                                        myPropertiesComponent.getValue(OLD_PACKAGE_PROPERTY),
                                        "");
    return new NodePackage(value);
  }

  public void update(@NotNull NodeJsInterpreterRef interpreterRef, @Nullable NodePackage nodePackage) {
    myPropertiesComponent.setValue(NODE_INTERPRETER_PROPERTY, interpreterRef.getReferenceName());
    myPropertiesComponent.setValue(PACKAGE_PROPERTY, nodePackage != null ? nodePackage.getSystemDependentPath() : null);
  }

  private void detectLocalOrGlobalPackage() {
    detectPackage(() -> ObjectUtils.coalesce(localPackageIfInDependencies(), findDefaultPackage()));
  }

  private void detectLocalPackage() {
    detectPackage(() -> localPackageIfInDependencies());
  }

  private void detectPackage(@NotNull Supplier<NodePackage> packageProducer) {
    if (myProject.isDisposed() || myProject.isDefault()) {
      return;
    }
    String stored = myPropertiesComponent.getValue(PACKAGE_PROPERTY, "");
    if (!StringUtil.isEmpty(stored)) {
      return;
    }

    NodePackage detected = packageProducer.get();
    if (detected != null) {
      myPropertiesComponent.setValue(PACKAGE_PROPERTY, detected.getSystemDependentPath());
    }
  }

  @Nullable
  private NodePackage localPackageIfInDependencies() {
    if (myProject.isDefault() || myProject.getBasePath() == null) {
      return null;
    }
    final PackageJsonData data = JSLinterUtil.getTopLevelPackageJsonData(myProject);
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
    public void configureProject(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull Ref<Module> moduleRef) {
      StartupManager.getInstance(project).runWhenProjectIsInitialized(
        (DumbAwareRunnable)() -> getInstance(project).detectLocalOrGlobalPackage());
    }
  }
}
