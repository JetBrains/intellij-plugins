/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.perforce;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.PerforceClient;
import org.jetbrains.idea.perforce.perforce.View;

import java.util.Collections;
import java.util.List;

public class TestPerforceClient implements PerforceClient {
  private final String myUserName;
  private final String myName;
  private final String myRoot;
  private final List<View> myViews;

  public TestPerforceClient(final String name, final String userName, final String root, final List<View> views) {
    myUserName = userName;
    myName = name;
    myRoot = root;
    myViews = views;
  }

  @Override
  public Project getProject() {
    return null;
  }

  @Override
  public String getName() {
    return myName;
  }

  @NotNull
  @Override
  public List<String> getRoots() {
    return ContainerUtil.createMaybeSingletonList(myRoot);
  }

  @Override
  public List<View> getViews() {
    return myViews;
  }

  @Nullable
  @Override
  public List<String> getCachedOptions() {
    return Collections.emptyList();
  }

  @Override
  public String getUserName() {
    return myUserName;
  }

  @Override
  @Nullable
  public String getServerPort() {
    return "server_port";
  }

  @Nullable
  @Override
  public String getDeclaredServerPort() {
    return getServerPort();
  }
}
