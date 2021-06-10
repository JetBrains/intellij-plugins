package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.CommonBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.ui.TextFieldAction;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.IconUtil;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditChangelistJobsDialog extends DialogWrapper {
  private final Project myProject;
  private final LocalChangeList myList;
  private final JobsWorker myWorker;
  private final Map<ConnectionKey, P4JobsLogicConn> myConnMap;

  private JComponent myInitFocus;

  //--------------
  private final Map<ConnectionKey, MyTab> myTabsMap;
  private JPanel myMainPanel;

  public EditChangelistJobsDialog(final Project project, final LocalChangeList list, boolean inMemory, final Map<ConnectionKey, P4JobsLogicConn> connMap,
                                  final Map<ConnectionKey, List<PerforceJob>> perforceJobs) {
    super(project, true);

    myTabsMap = new HashMap<>();

    myProject = project;
    myList = list;
    myWorker = new JobsWorker(myProject);
    myConnMap = connMap;

    createTabs(inMemory, perforceJobs);
    setCancelButtonText(CommonBundle.message("action.text.close"));
    setTitle(PerforceBundle.message("job.edit.linked", myList.getName()));

    init();
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myInitFocus == null ? myMainPanel : myInitFocus;
  }

  @Override
  protected String getDimensionServiceKey() {
    return "org.jetbrains.idea.perforce.perforce.jobs.EditChangelistJobsDialog";
  }

  private void createTabs(final boolean inMemory, final Map<ConnectionKey, List<PerforceJob>> perforceJobs) {
    myMainPanel = new JPanel(new GridBagLayout());
    final GridBagConstraints gb = DefaultGb.create();

    gb.weightx = 1;
    gb.weighty = 1;
    gb.fill = GridBagConstraints.BOTH;
    final JTabbedPane pane = new JBTabbedPane();
    if (myConnMap.size() > 1) {
      myMainPanel.add(pane, gb);
    }

    for (Map.Entry<ConnectionKey, P4JobsLogicConn> entry : myConnMap.entrySet()) {
      final P4JobsLogicConn value = entry.getValue();
      final MyTab tab = new MyTab(myProject, myList, entry.getValue().getSpec(), myWorker, entry.getKey(),
                                  value.getConnection(), inMemory, perforceJobs.get(entry.getKey()));
      if (myInitFocus == null) {
        myInitFocus = tab.getMainTable().getPreferredFocusTarget();
      }

      myTabsMap.put(entry.getKey(), tab);
      if (myConnMap.size() > 1) {
        pane.add(entry.getKey().getServer(), tab.createCenterPanel());
      } else {
        myMainPanel.add(tab.createCenterPanel(), gb);
      }
    }
  }

  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public Map<ConnectionKey, List<PerforceJob>> getJobs() {
    final Map<ConnectionKey, List<PerforceJob>> result = new HashMap<>();
    for (MyTab tab : myTabsMap.values()) {
      result.put(tab.myKey, tab.getJobs());
    }
    return result;
  }

  @Override
  protected Action @NotNull [] createActions() {
    return new Action[]{getCancelAction()};
  }

  private static final class MyTab implements JobsTablePresentation {
    private final Project myProject;
    private final AdderRemover myAdderRemover;
    private final LocalChangeList myList;
    private final PerforceJobSpecification mySpecification;

    private final JobsWorker myWorker;

    private final ConnectionKey myKey;
    private final P4Connection myConnection;

    private final JobsMasterDetails myMainTable;
    private JTextField myAddText;
    private JButton myAddBtn;
    private JButton mySearchBtn;
    private JButton myRemoveBtn;

    private MyTab(Project project,
                  LocalChangeList list,
                  PerforceJobSpecification specification,
                  JobsWorker worker,
                  ConnectionKey key,
                  P4Connection connection, final boolean inMemory, final List<PerforceJob> jobs) {
      myProject = project;
      myList = list;
      mySpecification = specification;
      myWorker = worker;
      myKey = key;
      myConnection = connection;
      myAdderRemover = inMemory ? new MemoryAdderRemover(this) : new WritingAdderRemover(myWorker, this);

      myMainTable = new JobsMasterDetails(myProject);
      if (jobs != null) {
        myMainTable.fillTree(jobs, null);
      }
    }

    JobsMasterDetails getMainTable() {
      return myMainTable;
    }

    private JComponent createToolbar() {
      final DefaultActionGroup group = new DefaultActionGroup();
      group.add(new AnAction(PerforceBundle.message("job.unlink.selected.title"), PerforceBundle.message("job.unlink.selected"), PlatformIcons.DELETE_ICON) {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
          for (PerforceJob job : myMainTable.getSelectedJobs()) {
            reportException(myAdderRemover.remove(job, myList, myProject), PerforceBundle.message("job.removing.from.changelist"));
          }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
          e.getPresentation().setEnabled(!myMainTable.getSelectedJobs().isEmpty());
        }
      });

      group.add(new AnAction(PerforceBundle.message("search"), PerforceBundle.message("search"), AllIcons.Actions.Find) {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
          final AddJobToChangeListDialog dialog = new AddJobToChangeListDialog(myProject, true, myWorker, mySpecification,
                                                                               myConnection, myKey);
          dialog.show();
          if (dialog.isOK()) {
            final List<PerforceJob> jobs = dialog.getSelectedJobs();
            for (PerforceJob job : jobs) {
              final VcsException vcsException = myAdderRemover.add(job, myList, myProject);
              reportException(vcsException, PerforceBundle.message("job.adding.to.changelist"));
            }
          }
        }
      });

      group.add(new TextFieldAction(PerforceBundle.message("job.find.matching.pattern.title"), PerforceBundle.message("job.find.matching.pattern"), IconUtil.getAddIcon(), 20) {
        @Override
        public void perform() {
          final String text = myField.getText().trim();
          if (text.length() > 0) {
            final List<PerforceJob> jobs = PerforceCheckinComponent.getJobsUnderProgress(myWorker, new ByNamesConstraint(Collections.singletonList(text)), myConnection, myKey, mySpecification);
            if (jobs == null) {
              return;
            }
            if (jobs.size() > 1) {
              Messages.showMessageDialog(myProject, PerforceBundle.message("job.several.matching.pattern"), PerforceBundle.message("job.add"), Messages.getInformationIcon());
              return;
            }
            if (jobs.isEmpty()) {
              Messages.showMessageDialog(myProject, PerforceBundle.message("job.no.matching.pattern"), PerforceBundle.message("job.add"), Messages.getInformationIcon());
              return;
            }
            if (!reportException(myAdderRemover.add(jobs.get(0), myList, myProject), PerforceBundle.message("job.adding"))) {
              myAddText.setText("");
            }
          }
        }
      });

      final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("PerforceEditChangeListJobs", group, true);
      return actionToolbar.getComponent();
    }

    private boolean reportException(VcsException vcsException, String operation) {
      if (vcsException != null) {
        new ErrorReporter(operation).report(myProject, vcsException);
        return true;
      }
      return false;
    }

    private JComponent createCenterPanel() {
      final JPanel main = new JPanel(new GridBagLayout());

      final GridBagConstraints gb = new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,
                                                           new Insets(0,0,1,0), 0,0);

      gb.fill = GridBagConstraints.HORIZONTAL;
      gb.weightx = 1;
      main.add(createToolbar(), gb);

      ++ gb.gridy;
      gb.weightx = 1;
      gb.weighty = 1;
      gb.fill = GridBagConstraints.BOTH;
      gb.gridwidth = 2;

      main.add(myMainTable.createComponent(), gb);

      final JPanel btnPanel = new JPanel(new GridBagLayout());
      final GridBagConstraints panelGb = DefaultGb.create();

      myAddText = new JTextField(30);
      myAddBtn = new JButton(CommonBundle.message("button.add"));
      mySearchBtn = new JButton(PerforceBundle.message("search"));
      myRemoveBtn = new JButton(CommonBundle.message("button.remove"));

      gb.gridwidth = 1;
      panelGb.anchor = GridBagConstraints.NORTHWEST;
      btnPanel.add(myAddText, panelGb);

      ++ panelGb.gridx;
      panelGb.anchor = GridBagConstraints.CENTER;
      panelGb.fill = GridBagConstraints.NONE;
      panelGb.weightx = 0;
      btnPanel.add(myAddBtn, panelGb);

      ++ panelGb.gridx;
      btnPanel.add(mySearchBtn, panelGb);

      ++ panelGb.gridx;
      btnPanel.add(myRemoveBtn, panelGb);

      ++ gb.gridy;
      gb.weightx = 1;
      gb.weighty = 0;
      gb.anchor = GridBagConstraints.EAST;
      gb.fill = GridBagConstraints.HORIZONTAL;

      return main;
    }

    @Override
    public void refreshJobs(final PerforceJob job) throws VcsException {
      assert !ApplicationManager.getApplication().isDispatchThread();
      final List<PerforceJob> jobs = myWorker.getJobsForList(mySpecification, myList, myConnection, myKey);
      ApplicationManager.getApplication().invokeLater(() -> {
        myMainTable.fillTree(jobs, job);
        if (job == null) {
          selectDefault();
        }
      });
    }

    @Override
    public void addJob(PerforceJob job) {
      myMainTable.addJob(job);
    }

    @Override
    public void removeSelectedJobs() {
      myMainTable.removeSelectedJobs();
    }

    private void selectDefault() {
      final List<PerforceJob> perforceJobs = myMainTable.getJobs();
      if (! perforceJobs.isEmpty()) {
        myMainTable.selectNodeInTree(perforceJobs.get(0).getName());
      }
    }

    public List<PerforceJob> getJobs() {
      return myMainTable.getJobs();
    }
  }
}
