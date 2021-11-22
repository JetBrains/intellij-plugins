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
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.serviceContainer.NonInjectable;
import com.intellij.util.xmlb.XmlSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** A persistent service that stores protobuf settings. */
@State(name = "ProtobufLanguageSettings", storages = @Storage("protoeditor.xml"))
public class PbProjectSettings implements PersistentStateComponent<PbProjectSettings.State> {

  private State state;

  public PbProjectSettings() {
    this(new State());
  }

  @NonInjectable
  private PbProjectSettings(State state) {
    this.state = state;
  }

  public static PbProjectSettings getInstance(Project project) {
    return project.getService(PbProjectSettings.class);
  }

  public static void notifyUpdated(Project project) {
    //also consider incrementing some ModificationTracker
    DaemonCodeAnalyzer.getInstance(project).restart();
  }

  @Nullable
  @Override
  public State getState() {
    // If the settings are autoconfigured, just return the default state so that there's no
    // need to serialize/persist it. It will be modified by autoconfiguration on restart anyway.
    if (state.autoConfigEnabled) {
      return new State();
    }
    return state;
  }

  @Override
  public void loadState(@NotNull State state) {
    this.state = state;
  }

  public void copyState(PbProjectSettings other) {
    loadState(other.state);
  }

  public List<ImportPathEntry> getImportPathEntries() {
    return state.importPathEntries;
  }

  public void setImportPathEntries(List<ImportPathEntry> importPathEntries) {
    if (importPathEntries == null) {
      importPathEntries = Collections.emptyList();
    }
    state.importPathEntries = importPathEntries;
  }

  @NlsSafe
  public String getDescriptorPath() {
    return state.descriptorPath;
  }

  public void setDescriptorPath(String descriptorPath) {
    state.descriptorPath = StringUtil.defaultIfEmpty(descriptorPath, "");
  }

  public boolean isAutoConfigEnabled() {
    return state.autoConfigEnabled;
  }

  public void setAutoConfigEnabled(boolean autoConfigEnabled) {
    state.autoConfigEnabled = autoConfigEnabled;
  }

  public PbProjectSettings copy() {
    return new PbProjectSettings(
        XmlSerializer.deserialize(XmlSerializer.serialize(state), State.class));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!getClass().equals(obj.getClass())) {
      return false;
    }
    PbProjectSettings other = (PbProjectSettings) obj;
    return Objects.equals(isAutoConfigEnabled(), other.isAutoConfigEnabled())
        && Objects.equals(getDescriptorPath(), other.getDescriptorPath())
        && Objects.equals(getImportPathEntries(), other.getImportPathEntries());
  }

  @Override
  public int hashCode() {
    return Objects.hash(isAutoConfigEnabled(), getDescriptorPath(), getImportPathEntries());
  }

  /**
   * Represents an import path entry with a VFS location URL and optional import prefix.
   *
   * <p>For example, if the location is "file:///mycompany/myproject/src/protos", and the prefix is
   * "com/foo/proto", then <code>import "com/foo/proto/something.proto</code> will resolve, assuming
   * that "something.proto" exists in "/mycompany/myproject/src/protos".
   */
  public static final class ImportPathEntry {
    @NlsSafe
    private String location;
    @NlsSafe
    private String prefix;

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

      ImportPathEntry other = (ImportPathEntry) obj;

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
  static class State {
    public boolean autoConfigEnabled = true;
    public List<ImportPathEntry> importPathEntries = new ArrayList<>();
    @NlsSafe
    public String descriptorPath = "";
  }
}
