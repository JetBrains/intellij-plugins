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
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class AngularCliConfig {

  private static final Logger LOG = Logger.getInstance(AngularCliConfig.class);
  private static final String DEFAULT_PROJECT = "defaultProject";

  private final VirtualFile myConfig;

  public AngularCliConfig(@NotNull VirtualFile config) {
    myConfig = config;
  }

  @Nullable
  public String getDefaultOrFirstProject() {
    try {
      String value = doGetDefaultOrFirstProject();
      if (value == null) {
        LOG.info("Neither default nor any project defined in " + myConfig.getPath());
      }
      return value;
    }
    catch (Exception e) {
      LOG.info("Cannot get default or first project from " + myConfig.getPath(), e);
      return null;
    }
  }

  @Nullable
  private String doGetDefaultOrFirstProject() throws IOException {
    if (!myConfig.isValid()) return null;
    String text = JSLinterConfigFileUtil.loadActualText(myConfig);
    JsonElement root = new JsonParser().parse(text);
    JsonObject rootObj = root.getAsJsonObject();
    String defaultProject = JsonUtil.getChildAsString(rootObj, DEFAULT_PROJECT);
    if (defaultProject != null) return defaultProject;
    JsonObject projects = JsonUtil.getChildAsObject(rootObj, "projects");
    return projects != null ? ContainerUtil.getFirstItem(projects.keySet()) : null;
  }

  /**
   * Finds angular.json config using logic similar to
   * https://github.com/angular/angular-cli/blob/v6.0.3/packages/%40angular/cli/utilities/project.ts
   */
  @Nullable
  public static AngularCliConfig findProjectConfig(@NotNull File workingDirectory) {
    VirtualFile root = LocalFileSystem.getInstance().findFileByIoFile(workingDirectory);
    if (root == null) return null;
    VirtualFile config = JSLinterConfigFileUtil.lookupFileByName(root, new String[]{
      "angular.json",
      ".angular.json",
      "angular-cli.json",
      ".angular-cli.json"
    });
    return config != null ? new AngularCliConfig(config) : null;
  }
}
