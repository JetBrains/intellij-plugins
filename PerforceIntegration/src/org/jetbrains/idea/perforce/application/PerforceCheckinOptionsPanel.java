package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import javax.swing.*;
import java.awt.*;


public class PerforceCheckinOptionsPanel implements RefreshableOnComponent {
  private final JPanel myPanel;
  private final JCheckBox myChkRevertUnchanged;
  private final Project myProject;

  public PerforceCheckinOptionsPanel(Project project) {
    myProject = project;
    myPanel = new JPanel(new BorderLayout());
    myChkRevertUnchanged = new JCheckBox(PerforceBundle.message("message.revert.unchanged.files"));
    myPanel.add(myChkRevertUnchanged, BorderLayout.CENTER);
  }

  @Override
  public JComponent getComponent() {
    return myPanel;
  }

  @Override
  public void saveState() {
    PerforceSettings.getSettings(myProject).REVERT_UNCHANGED_FILES_CHECKIN = myChkRevertUnchanged.isSelected();
  }

  @Override
  public void restoreState() {
    myChkRevertUnchanged.setSelected(PerforceSettings.getSettings(myProject).REVERT_UNCHANGED_FILES_CHECKIN);
  }
}
