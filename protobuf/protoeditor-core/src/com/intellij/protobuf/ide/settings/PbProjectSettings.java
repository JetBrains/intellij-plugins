/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.settings;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.util.BackgroundTaskUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.serviceContainer.NonInjectable;
import com.intellij.util.SmartList;
import com.intellij.util.xmlb.XmlSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** A persistent service that stores protobuf settings. */
@Service(Service.Level.PROJECT)
@State(name = "ProtobufLanguageSettings", storages = @Storage("protoeditor.xml"))
public final class PbProjectSettings implements PersistentStateComponent<PbProjectSettings.State>, Disposable {
  private State state;
  private final Project project;

  public PbProjectSettings(@NotNull Project project) {
    this(project, new State());
  }

  @NonInjectable
  private PbProjectSettings(@NotNull Project project, State state) {
    this.state = state;
    this.project = project;
  }

  @Override
  public void dispose() {

  }

  public static PbProjectSettings getInstance(Project project) {
    return project.getService(PbProjectSettings.class);
  }

  public static void notifyUpdated(Project project) {
    PbProjectSettings serviceInstance = getInstance(project);
    serviceInstance.state.incModificationCount();

    BackgroundTaskUtil.executeOnPooledThread(serviceInstance, () -> {
      ReadAction.run(() -> {
        for (VirtualFile file : FileEditorManager.getInstance(project).getOpenFiles()) {
          if (file.getFileType() == PbFileType.INSTANCE) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile != null) {
              DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
            }
          }
        }
      });
    });
  }

  public static ModificationTracker getModificationTracker(Project project) {
    return getInstance(project).state;
  }

  @Override
  public @NotNull State getState() {
    return state;
  }

  @Override
  public void loadState(@NotNull State state) {
    this.state = state;
  }

  public List<ImportPathEntry> getImportPathEntries() {
    return state.importPathEntries;
  }

  public void setImportPathEntries(List<ImportPathEntry> importPathEntries) {
    state.importPathEntries = importPathEntries == null ? Collections.emptyList() : importPathEntries;
  }

  public @NlsSafe String getDescriptorPath() {
    return state.descriptorPath;
  }

  public void setDescriptorPath(String descriptorPath) {
    state.descriptorPath = StringUtil.defaultIfEmpty(descriptorPath, "");
  }

  public boolean isThirdPartyConfigurationEnabled() {
    return state.thirdPartyConfiguration;
  }

  public void setThirdPartyConfigurationEnabled(boolean autoConfigEnabled) {
    state.thirdPartyConfiguration = autoConfigEnabled;
  }

  public boolean isIncludeProtoDirectories() {
    return state.includeProtoDirectories;
  }

  public void setIncludeProtoDirectories(boolean includeProtoDirectories) {
    state.includeProtoDirectories = includeProtoDirectories;
  }

  public boolean isIndexBasedResolveEnabled() {
    return state.indexBasedResolveEnabled;
  }

  public void setIndexBasedResolveEnabled(boolean indexBasedResolveEnabled) {
    state.indexBasedResolveEnabled = indexBasedResolveEnabled;
  }

  public boolean isIncludeContentRoots() {
    return state.includeContentRoots;
  }

  public void setIncludeContentRoots(boolean includeSourceRoots) {
    state.includeContentRoots = includeSourceRoots;
  }

  public boolean isIncludeWellKnownProtos() {
    return state.includeWellKnownProtos;
  }

  public void setIncludeWellKnownProtos(boolean includeWellKnownProtos) {
    state.includeWellKnownProtos = includeWellKnownProtos;
  }

  public PbProjectSettings copy() {
    return new PbProjectSettings(project, XmlSerializer.deserialize(XmlSerializer.serialize(state), State.class));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!getClass().equals(obj.getClass())) {
      return false;
    }
    PbProjectSettings other = (PbProjectSettings)obj;
    return Objects.equals(getDescriptorPath(), other.getDescriptorPath())
           && Objects.equals(getImportPathEntries(), other.getImportPathEntries());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getDescriptorPath(), getImportPathEntries());
  }

  /**
   * Represents an import path entry with a VFS location URL and optional import prefix.
   *
   * <p>For example, if the location is "file:///mycompany/myproject/src/protos", and the prefix is
   * "com/foo/proto", then <code>import "com/foo/proto/something.proto</code> will resolve, assuming
   * that "something.proto" exists in "/mycompany/myproject/src/protos".
   */
  public static final class ImportPathEntry {
    private @NlsSafe String location;
    private @NlsSafe String prefix;

    public ImportPathEntry(String location, String prefix) {
      this.location = StringUtil.defaultIfEmpty(location, "");
      this.prefix = StringUtil.defaultIfEmpty(prefix, "");
    }

    public ImportPathEntry() {
      this(null, null);
    }

    public String getLocation() {
      return location;
    }

    public void setLocation(String location) {
      this.location = location;
    }

    public String getPrefix() {
      return prefix;
    }

    public void setPrefix(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }

      ImportPathEntry other = (ImportPathEntry)obj;

      return Objects.equals(this.location, other.location)
             && Objects.equals(this.prefix, other.prefix);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.location, this.prefix);
    }
  }

  /*
   * Persistent state holder.
   * <p>Values must be public to be serialized. The initial values below represent defaults.</p>
   */
  public static class State extends SimpleModificationTracker {
    public boolean thirdPartyConfiguration = true;
    public boolean includeContentRoots = true;
    public boolean includeProtoDirectories = true;
    public boolean includeWellKnownProtos = true;
    public boolean indexBasedResolveEnabled = false;
    public List<ImportPathEntry> importPathEntries = new SmartList<>();
    public @NlsSafe String descriptorPath = "";
  }
}
