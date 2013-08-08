package com.intellij.javascript.karma.coverage;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoveragePluginMissingDialog extends DialogWrapper {

  protected KarmaCoveragePluginMissingDialog(@Nullable Project project) {
    super(project, false);

    setTitle("Karma Coverage Plugin Missing");
    setOKButtonText("Install");
    setCancelButtonText("Cancel");

    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    String text = "<html><body>To gather coverage info, karma-coverage node package is needed."
                + "<div style=\"padding-top:4px\">To install it, run the command:</div>"
                + "<pre><code> npm install karma-coverage</code></pre>" +
                "</body></html>";
    JLabel label = new JLabel(text, UIUtil.getWarningIcon(), SwingConstants.LEFT);
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    panel.add(label);
    return panel;
  }

}
