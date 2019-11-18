// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageDescriptor;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Ref;
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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public final class PrettierConfiguration implements JSNpmLinterState {
  @NotNull
  private final Project myProject;
  private static final String NODE_INTERPRETER_PROPERTY = "prettierjs.PrettierConfiguration.NodeInterpreter";
  private static final String PACKAGE_PROPERTY = "prettierjs.PrettierConfiguration.Package";
  private static final String OLD_PACKAGE_PROPERTY = "node.js.selected.package.prettier";
  private static final String OLD_INTERPRETER_PROPERTY = "node.js.path.for.package.prettier";
  private static final NodePackageDescriptor PKG_DESC = new NodePackageDescriptor(PrettierUtil.PACKAGE_NAME);

  public PrettierConfiguration(@NotNull Project project) {
    myProject = project;
    ExecutorService threadPoolExecutor = SequentialTaskExecutor.createSequentialApplicationPoolExecutor("Prettier.PackageJsonUpdater");
    project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
      @Override
      public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent event : events) {
          if (PackageJsonUtil.isPackageJsonFile(event.getFile())) {
            threadPoolExecutor.execute(() -> ReadAction.run(PrettierConfiguration.this::detectLocalPackage));
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
    return NodeJsInterpreterRef.create(ObjectUtils.coalesce(PropertiesComponent.getInstance(myProject).getValue(NODE_INTERPRETER_PROPERTY),
                                                            PropertiesComponent.getInstance(myProject).getValue(OLD_INTERPRETER_PROPERTY)));
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
    String value = ObjectUtils.coalesce(PropertiesComponent.getInstance(myProject).getValue(PACKAGE_PROPERTY),
                                        PropertiesComponent.getInstance(myProject).getValue(OLD_PACKAGE_PROPERTY),
                                        "");
    return PKG_DESC.createPackage(value);
  }

  public void update(@NotNull NodeJsInterpreterRef interpreterRef, @Nullable NodePackage nodePackage) {
    PropertiesComponent.getInstance(myProject).setValue(NODE_INTERPRETER_PROPERTY, interpreterRef.getReferenceName());
    PropertiesComponent.getInstance(myProject).setValue(PACKAGE_PROPERTY, nodePackage != null ? nodePackage.getSystemDependentPath() : null);
  }

  private void detectLocalOrGlobalPackage() {
    detectPackage(() -> ObjectUtils.coalesce(PKG_DESC.findUnambiguousDependencyPackage(myProject), findDefaultPackage()));
  }

  private void detectLocalPackage() {
    detectPackage(() -> PKG_DESC.findUnambiguousDependencyPackage(myProject));
  }

  private void detectPackage(@NotNull Supplier<NodePackage> packageProducer) {
    if (myProject.isDisposed() || myProject.isDefault()) {
      return;
    }
    String stored = PropertiesComponent.getInstance(myProject).getValue(PACKAGE_PROPERTY, "");
    if (!StringUtil.isEmpty(stored)) {
      return;
    }

    NodePackage detected = packageProducer.get();
    if (detected != null) {
      PropertiesComponent.getInstance(myProject).setValue(PACKAGE_PROPERTY, detected.getSystemDependentPath());
    }
  }

  @Nullable
  private NodePackage findDefaultPackage() {
    return NodePackage.findDefaultPackage(myProject, PrettierUtil.PACKAGE_NAME, getInterpreterRef().resolve(myProject));
  }

  public static class ProjectConfigurator implements DirectoryProjectConfigurator {
    @Override
    public void configureProject(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull Ref<Module> moduleRef, boolean newProject) {
      StartupManager.getInstance(project).runWhenProjectIsInitialized(
        (DumbAwareRunnable)() -> getInstance(project).detectLocalOrGlobalPackage());
    }
  }
}
