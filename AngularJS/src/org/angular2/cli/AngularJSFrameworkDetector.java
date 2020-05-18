// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.intellij.framework.FrameworkType;
import com.intellij.framework.detection.DetectedFrameworkDescription;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.framework.detection.FrameworkDetectionContext;
import com.intellij.framework.detection.FrameworkDetector;
import com.intellij.ide.projectView.actions.MarkRootActionBase;
import com.intellij.json.JsonFileType;
import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.StandardPatterns;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileContent;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSFrameworkDetector extends FrameworkDetector {
  protected AngularJSFrameworkDetector() {
    super(AngularJSFramework.ID);
  }

  @Override
  public @NotNull FileType getFileType() {
    return JsonFileType.INSTANCE;
  }

  @Override
  public @NotNull ElementPattern<FileContent> createSuitableFilePattern() {
    return FileContentPattern.fileContent().withName(
      StandardPatterns.string().with(new PatternCondition<String>("cli-json-name") {
        @Override
        public boolean accepts(@NotNull String s, ProcessingContext context) {
          return AngularCliUtil.isAngularJsonFile(s);
        }
      })
    ).with(new PatternCondition<FileContent>("notLibrary") {
      @Override
      public boolean accepts(@NotNull FileContent content, ProcessingContext context) {
        return !JSLibraryUtil.isProbableLibraryFile(content.getFile());
      }
    });
  }

  @Override
  public List<? extends DetectedFrameworkDescription> detect(@NotNull Collection<VirtualFile> newFiles,
                                                             @NotNull FrameworkDetectionContext context) {
    if (newFiles.size() > 0 && !isConfigured(newFiles, context.getProject())) {
      return Collections.singletonList(new AngularCLIFrameworkDescription(newFiles));
    }
    return Collections.emptyList();
  }

  private static boolean isConfigured(Collection<? extends VirtualFile> files, Project project) {
    if (project == null) return false;

    for (VirtualFile file : files) {
      Module module = ModuleUtilCore.findModuleForFile(file, project);
      if (module != null) {
        for (String root : ModuleRootManager.getInstance(module).getExcludeRootUrls()) {
          //noinspection HardCodedStringLiteral
          if (root.equals(file.getParent().getUrl() + "/tmp")) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public @NotNull FrameworkType getFrameworkType() {
    return AngularJSFramework.INSTANCE;
  }

  private class AngularCLIFrameworkDescription extends DetectedFrameworkDescription {
    private final Collection<? extends VirtualFile> myNewFiles;

    AngularCLIFrameworkDescription(Collection<? extends VirtualFile> newFiles) {
      myNewFiles = newFiles;
    }

    @Override
    public @NotNull Collection<? extends VirtualFile> getRelatedFiles() {
      return myNewFiles;
    }

    @Override
    public @NotNull String getSetupText() {
      return Angular2Bundle.message("angular.description.angular-cli");
    }

    @Override
    public @NotNull FrameworkDetector getDetector() {
      return AngularJSFrameworkDetector.this;
    }

    @Override
    public void setupFramework(@NotNull ModifiableModelsProvider modifiableModelsProvider, @NotNull ModulesProvider modulesProvider) {
      for (Module module : modulesProvider.getModules()) {
        ModifiableRootModel model = modifiableModelsProvider.getModuleModifiableModel(module);
        VirtualFile item = ContainerUtil.getFirstItem(myNewFiles);
        ContentEntry entry = item != null ? MarkRootActionBase.findContentEntry(model, item) : null;
        if (entry == null) {
          modifiableModelsProvider.disposeModuleModifiableModel(model);
          continue;
        }
        AngularJSProjectConfigurator.excludeDefault(item.getParent(), entry);
        modifiableModelsProvider.commitModuleModifiableModel(model);
        for (VirtualFile vf : myNewFiles) {
          AngularCliUtil.createRunConfigurations(module.getProject(), vf.getParent());
        }
      }
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof AngularCLIFrameworkDescription && myNewFiles.equals(((AngularCLIFrameworkDescription)obj).myNewFiles);
    }

    @Override
    public int hashCode() {
      return myNewFiles.hashCode();
    }
  }
}
