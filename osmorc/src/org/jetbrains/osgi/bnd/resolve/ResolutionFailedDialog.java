/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.jetbrains.osgi.bnd.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.osgi.resource.Requirement;
import org.osgi.service.resolver.ResolutionException;

import javax.swing.*;

import static org.osmorc.i18n.OsmorcBundle.message;

class ResolutionFailedDialog extends DialogWrapper {
  private final ResolutionException myResolutionException;

  ResolutionFailedDialog(Project project, ResolutionException resolutionException) {
    super(project);
    myResolutionException = resolutionException;
    init();
    setTitle(message("bnd.resolve.failed.title"));
  }

  @Override
  protected JComponent createCenterPanel() {
    StringBuilder sb = new StringBuilder();
    sb.append("<html>").append(myResolutionException.getMessage()).append("<br><br>Unresolved requirements:");
    for (Requirement requirement : myResolutionException.getUnresolvedRequirements()) sb.append("<br>").append(requirement);
    sb.append("</html>");

    JBLabel label = new JBLabel(sb.toString(), UIUtil.getErrorIcon(), SwingConstants.LEADING);
    label.setCopyable(true);
    return label;
  }

  @NotNull
  @Override
  protected Action[] createActions() {
    return new Action[]{getOKAction()};
  }
}