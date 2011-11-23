/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea;

import org.jetbrains.annotations.NotNull;

import com.google.jstestdriver.idea.ui.ToolPanel;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentFactory.SERVICE;

/**
 * Top-level plugin component, registered in the plugin.xml. This provides a popout tool window
 * to control the JSTestDriver server and see results from JSTestDriver test executions.
 * @author alexeagle@google.com (Alex Eagle)
 */
public class JSTestDriverToolWindow implements ProjectComponent {
  private Project project;
  private ToolPanel myToolPanel;

  public static final String TOOL_WINDOW_ID = "JsTestDriver Server";

  public JSTestDriverToolWindow(Project project) {
    this.project = project;
  }

  public void projectOpened() {
    registerToolWindow();
  }

  public void projectClosed() {
    unregisterToolWindow();
  }

  public void initComponent() {
    // empty
  }

  public void disposeComponent() {
    // empty
  }

  @NotNull
  public String getComponentName() {
    return "SimpleToolWindow.SimpleToolWindowPlugin";
  }

  private void registerToolWindow() {
    ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);

    ToolWindow myToolWindow =
        toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.BOTTOM);

    ContentFactory contentFactory = SERVICE.getInstance();
    myToolPanel = new ToolPanel();
    Content content = contentFactory.createContent(myToolPanel, "", false);
    myToolWindow.getContentManager().addContent(content);
    myToolWindow.setIcon(PluginResources.getJstdSmallIcon());
  }

  private void unregisterToolWindow() {
    myToolPanel.onDispose();
    ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
    toolWindowManager.unregisterToolWindow(TOOL_WINDOW_ID);
  }

}
