package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;

public class BrowsePreviewDialog extends DialogWrapper {
  private final Project myProject;
  private final @NlsSafe String myJobViewQueryString;
  private final List<PerforceJob> myJobs;
  private final int myMaxCount;

  private JPanel myMainPanel;

  private JobsMasterDetails myTable;

  public BrowsePreviewDialog(@NotNull final Project project, @NotNull final String jobViewQueryString, @NotNull final List<PerforceJob> jobs,
                             final int maxCount) {
    super(project, true);
    myProject = project;
    myJobViewQueryString = jobViewQueryString;
    myJobs = jobs;
    myMaxCount = maxCount;

    createUI();
    setTitle(PerforceBundle.message("dialog.title.link.job.to.changelist"));

    init();
    setOKActionEnabled(!myTable.getSelectedJobs().isEmpty());
  }

  @Override
  protected String getDimensionServiceKey() {
    return "org.jetbrains.idea.perforce.perforce.jobs.BrowsePreviewDialog";
  }

  private void createUI() {
    myMainPanel = new JPanel(new GridBagLayout());

    final GridBagConstraints gb = DefaultGb.create();

    gb.gridx = 0;
    gb.anchor = GridBagConstraints.WEST;
    final JLabel jobViewTitleLabel = new JLabel(PerforceBundle.message("job.view.query"));
    jobViewTitleLabel.setFont(jobViewTitleLabel.getFont().deriveFont(Font.BOLD));
    final Insets insets = gb.insets;
    gb.insets = JBUI.insets(2, 2, 7, 2);
    myMainPanel.add(jobViewTitleLabel, gb);

    ++ gb.gridx;
    final JLabel jobView = new JLabel(myJobViewQueryString);
    myMainPanel.add(jobView, gb);

    gb.insets = insets;

    myTable = new JobsMasterDetails(myProject);
    myTable.fillTree(myJobs, null);

    if (myJobs.size() > myMaxCount) {
      JLabel limitExceededLabel = new JLabel(PerforceBundle.message("perforce.jobs.search.limit.exceeded.warning", myMaxCount));
      limitExceededLabel.setForeground(JBColor.RED);
      ++ gb.gridy;
      gb.gridx = 0;
      gb.gridwidth = 2;
      gb.insets = JBUI.insets(2);
      myMainPanel.add(limitExceededLabel, gb);
    }

    gb.gridx = 0;
    gb.gridwidth = 2;
    ++ gb.gridy;
    gb.fill = GridBagConstraints.BOTH;
    gb.weightx = 1;
    gb.weighty = 1;
    gb.anchor = GridBagConstraints.CENTER;
    myMainPanel.add(myTable.createComponent(), gb);

    myTable.setSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        setOKActionEnabled(!myTable.getSelectedJobs().isEmpty());
      }
    });
  }

  @Override
  protected void doOKAction() {
    if (!myTable.getSelectedJobs().isEmpty()) {
      super.doOKAction();
    }
  }

  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }
}

