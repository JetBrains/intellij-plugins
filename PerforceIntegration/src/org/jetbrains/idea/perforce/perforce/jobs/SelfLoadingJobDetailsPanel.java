package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class SelfLoadingJobDetailsPanel {
  private JPanel myMain;
  private final Project myProject;
  private final PerforceJob myJob;
  private final List<Pair<String, String>> myDetails;
  private final JPanel myPanel;

  SelfLoadingJobDetailsPanel(final Project project, final PerforceJob job) {
    myProject = project;
    myJob = job;
    myDetails = new ArrayList<>();
    myPanel = new JPanel(new BorderLayout());
    createDetailsPanel();
  }

  private void initData() {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      if (load()) {
        ApplicationManager.getApplication().invokeLater(this::displayJobDetails, ModalityState.any());
      }
    });
  }

  private void displayJobDetails() {
    myMain.removeAll();

    final GridBag gb = new GridBag();
    for (Pair<@NlsSafe String, @NlsSafe String> detail : myDetails) {
      final JTextArea text = new JTextArea(detail.getSecond().trim());
      text.setEditable(false);
      text.setBackground(UIUtil.getBgFillColor(myMain));

      gb.nextLine();
      myMain.add(new JLabel(detail.getFirst() + ":"), gb.next().fillCellNone().weightx(0));
      myMain.add(text, gb.next().fillCellHorizontally().weightx(1));
    }

    myMain.add(new JPanel(), gb.nextLine().next().coverLine(2).weighty(1));

    myPanel.revalidate();
    myPanel.repaint();
  }

  private void createDetailsPanel() {
    myMain = new JPanel(new GridBagLayout());
    final GridBagConstraints gb = DefaultGb.create();
    gb.anchor = GridBagConstraints.CENTER;
    gb.fill = GridBagConstraints.BOTH;
    gb.weightx = gb.weighty = 1;
    myMain.add(new JLabel(CommonBundle.getLoadingTreeNodeText()), gb);

    myPanel.add(myMain, BorderLayout.NORTH);

    initData();
  }

  private boolean load() {
    try {
      final List<Pair<String, String>> items = new JobDetailsLoader(myProject).load(myJob);
      if (items != null) {
        // remove name
        final PerforceJobFieldValue nameValue = myJob.getNameValue();
        if (nameValue != null) {
          final String fieldName = nameValue.getField().getName();
          final String name = nameValue.getValue();
          items.remove(Pair.create(fieldName, name));
        }

        myDetails.addAll(items);
      }
    }
    catch (VcsException e) {
      new ErrorReporter("loading job details").report(myProject, e);
      return false;
    }
    return true;
  }

  public JPanel getPanel() {
    return myPanel;
  }
}
