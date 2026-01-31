// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.bnd.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.osgi.resource.Requirement;
import org.osgi.service.resolver.ResolutionException;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

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
    HtmlBuilder html = new HtmlBuilder()
      .append(myResolutionException.getMessage()).br().br()
      .append(message("bnd.resolve.failed.list"));
    for (Requirement requirement : myResolutionException.getUnresolvedRequirements()) {
      html.br().append(String.valueOf(requirement));
    }
    JBLabel label = new JBLabel(html.toString(), UIUtil.getErrorIcon(), SwingConstants.LEADING);
    label.setCopyable(true);
    return label;
  }

  @Override
  protected Action @NotNull [] createActions() {
    return new Action[]{getOKAction()};
  }
}
