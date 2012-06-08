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
package com.google.jstestdriver.idea.server.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

/**
 * @author Konstantin Bulenkov
 */
public class JstdToolWindow implements ToolWindowFactory {
  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    toolWindow.setAvailable(true, null);
    toolWindow.setToHideOnEmptyContent(true);

    final ContentManager contentManager = toolWindow.getContentManager();
    JstdToolWindowPanel component = new JstdToolWindowPanel();
    final Content content = contentManager.getFactory().createContent(component, null, false);
    content.setDisposer(project);
    content.setCloseable(false);

    content.setPreferredFocusableComponent(component.getPreferredFocusedComponent());
    contentManager.addContent(content);

    contentManager.setSelectedContent(content, true);
  }
}
