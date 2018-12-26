// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.javascript.karma.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AngularCliConfig {

  private static final Logger LOG = Logger.getInstance(AngularCliConfig.class);
  private static final String DEFAULT_PROJECT = "defaultProject";

  private final VirtualFile myConfig;

  public AngularCliConfig(@NotNull VirtualFile config) {
    myConfig = config;
  }

  @Nullable
  public String getProjectContainingFileOrDefault(@Nullable VirtualFile file) {
    try {
      String value = doGetProjectContainingFileOrDefault(file);
      if (value == null) {
        LOG.info("No project found in " + myConfig.getPath());
      }
      return value;
    }
    catch (Exception e) {
      LOG.info("Failed to find project in " + myConfig.getPath(), e);
      return null;
    }
  }

  @Nullable
  private String doGetProjectContainingFileOrDefault(@Nullable VirtualFile file) throws IOException {
    if (!myConfig.isValid()) return null;
    String text = JSLinterConfigFileUtil.loadActualText(myConfig);
    JsonObject rootObj = new JsonParser().parse(text).getAsJsonObject();
    String defaultProject = JsonUtil.getChildAsString(rootObj, DEFAULT_PROJECT);
    JsonObject projectsObj = JsonUtil.getChildAsObject(rootObj, "projects");
    if (projectsObj != null && file != null) {
      MultiMap<VirtualFile, String> projectRootToNameMap = getProjectRootToNameMap(projectsObj);
      VirtualFile nearestRoot = findNearestRoot(file, projectRootToNameMap.keySet());
      if (nearestRoot != null) {
        Collection<String> projects = projectRootToNameMap.get(nearestRoot);
        if (projects.contains(defaultProject) && isSuitableProject(defaultProject)) {
          return defaultProject;
        }
        String suitable = projects.stream().filter(AngularCliConfig::isSuitableProject).findFirst().orElse(null);
        return ObjectUtils.notNull(suitable, Objects.requireNonNull(ContainerUtil.getFirstItem(projects)));
      }
    }
    LOG.info("Cannot find project containing file, fallback to default project");
    if (defaultProject != null) return defaultProject;
    return projectsObj != null ? ContainerUtil.getFirstItem(projectsObj.keySet()) : null;
  }

  @NotNull
  private MultiMap<VirtualFile, String> getProjectRootToNameMap(@NotNull JsonObject projectsObj) {
    MultiMap<VirtualFile, String> projectRootToNameMap = MultiMap.create();
    for (Map.Entry<String, JsonElement> entry : projectsObj.entrySet()) {
      JsonObject projectObj = JsonUtil.getAsObject(entry.getValue());
      if (projectObj == null) {
        LOG.info("Unexpected " + entry);
      }
      else {
        VirtualFile root = getProjectRoot(projectObj);
        if (root != null) {
          projectRootToNameMap.putValue(root, entry.getKey());
        }
      }
    }
    return projectRootToNameMap;
  }

  @Nullable
  private VirtualFile getProjectRoot(@NotNull JsonObject projectObj) {
    String rootStr = StringUtil.notNullize(JsonUtil.getChildAsString(projectObj, "root"));
    return myConfig.getParent().findFileByRelativePath(rootStr);
  }

  @Nullable
  private static VirtualFile findNearestRoot(@NotNull VirtualFile file, @NotNull Set<VirtualFile> roots) {
    VirtualFile parent = file;
    while (parent != null) {
      if (roots.contains(parent)) {
        return parent;
      }
      parent = parent.getParent();
    }
    return null;
  }

  @Contract("null -> false")
  private static boolean isSuitableProject(@Nullable String projectName) {
    return projectName != null && !projectName.endsWith("-e2e");
  }

  /**
   * Finds angular.json config using logic similar to
   * https://github.com/angular/angular-cli/blob/v6.0.3/packages/%40angular/cli/utilities/project.ts
   */
  @Nullable
  public static AngularCliConfig findProjectConfig(@NotNull File workingDirectory) {
    VirtualFile root = LocalFileSystem.getInstance().findFileByIoFile(workingDirectory);
    if (root == null) return null;
    VirtualFile config = JSLinterConfigFileUtil.findFileUpToFileSystemRoot(root, new String[]{
      "angular.json",
      ".angular.json",
      "angular-cli.json",
      ".angular-cli.json"
    });
    return config != null ? new AngularCliConfig(config) : null;
  }
}