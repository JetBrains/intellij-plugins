package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import javax.swing.*;
import java.awt.*;

/**
 * @author irengrig
 */
public class PerforceIsOfflinePanel implements PerforcePanel {
  @Override
  public void updateFrom(PerforceSettings settings) {
  }

  @Override
  public void applyTo(PerforceSettings settings) throws ConfigurationException {
  }

  @Override
  public boolean isModified(PerforceSettings settings) {
    return false;
  }

  @Override
  public JPanel getPanel() {
    final JPanel panel = new JPanel(new BorderLayout());
    final JLabel label = new JLabel(PerforceBundle.message("connection.offline"));
    label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    panel.add(label, BorderLayout.CENTER);
    return panel;
  }
}
