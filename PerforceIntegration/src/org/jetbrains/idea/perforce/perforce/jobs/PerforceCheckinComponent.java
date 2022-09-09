package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.checkin.CheckinChangeListSpecificComponent;
import com.intellij.openapi.vcs.ui.TextFieldAction;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.table.TableView;
import com.intellij.util.Consumer;
import com.intellij.util.IconUtil;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jetbrains.idea.perforce.application.PerforceCheckinEnvironment.LINKED_JOBS_KEY;

public class PerforceCheckinComponent implements CheckinChangeListSpecificComponent, JobsTablePresentation {
  @NotNull private final Project myProject;
  @NotNull private final CommitContext myCommitContext;
  private JPanel myPanel;

  private LocalChangeList myCurrent;
  private final Map<LocalChangeList, Map<ConnectionKey, List<PerforceJob>>> myCache;
  private final PerforceVcs myVcs;
  private final Map<ConnectionKey, P4JobsLogicConn> myConnMap;
  private final AdderRemover myAdderRemover;

  private final JobsMasterDetails myDetails;

  public PerforceCheckinComponent(@NotNull Project project, @NotNull CommitContext commitContext) {
    myProject = project;
    myCommitContext = commitContext;
    myVcs = PerforceVcs.getInstance(myProject);
    myConnMap = new HashMap<>();
    myCache = new HashMap<>();

    myDetails = new JobsMasterDetails(myProject) {
      @Override
      protected Dimension getPanelPreferredSize() {
        return JBUI.size(200, 70);
      }
    };
    myDetails.onlyMain();

    initUI();
    myAdderRemover = new WiseAdderRemover(myProject, this);
  }

  @Override
  public void refreshJobs(PerforceJob job) {
    assert !ApplicationManager.getApplication().isDispatchThread();
    if (! myCurrent.hasDefaultName()) {
      final Map<ConnectionKey, List<PerforceJob>> data = loadOnSelect(myCurrent);
      myCache.put(myCurrent, data);
      ApplicationManager.getApplication().invokeLater(() -> setItems(ContainerUtil.flatten(data.values())));
    }
  }

  @Override
  public void addJob(PerforceJob job) {
    // todo todo 2
    final Map<ConnectionKey, List<PerforceJob>> data = getCurrentListJobs();
    if (data != null) {
      List<PerforceJob> jobs = data.get(job.getConnectionKey());
      if (jobs == null) {
        jobs = new ArrayList<>();
        data.put(job.getConnectionKey(), jobs);
      }
      jobs.add(job);
      saveJobsInCache(data);
      setItems(ContainerUtil.flatten(data.values()));
    }
  }

  private Map<ConnectionKey, List<PerforceJob>> getCurrentListJobs() {
    final Map<ConnectionKey, List<PerforceJob>> data;
    if (myCurrent.hasDefaultName()) {
      data = myVcs.getDefaultAssociated();
    } else {
      data = myCache.get(myCurrent);
    }
    return data;
  }

  @Override
  public void removeSelectedJobs() {
    final List<PerforceJob> jobs = myDetails.getSelectedJobs();
    if (jobs.isEmpty()) return;
    myDetails.removeSelectedJobs();

    final Map<ConnectionKey, List<PerforceJob>> data = getCurrentListJobs();
    if (data != null) {
      for (PerforceJob job : jobs) {
        data.get(job.getConnectionKey()).remove(job);
      }
      setItems(ContainerUtil.flatten(data.values()));
    }
  }

  private JComponent createToolbar() {
    final DefaultActionGroup group = new DefaultActionGroup();
    group.add(new AnAction(PerforceBundle.message("job.unlink.selected.title"), PerforceBundle.message("job.unlink.selected"), PlatformIcons.DELETE_ICON) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        for (PerforceJob job : myDetails.getSelectedJobs()) {
          final VcsException vcsException = myAdderRemover.remove(job, myCurrent, myProject);
          if (vcsException != null) {
            new ErrorReporter(PerforceBundle.message("job.removing.from.changelist")).report(myProject, vcsException);
          }
        }
      }

      @Override
      public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
      }

      @Override
      public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(!myDetails.getSelectedJobs().isEmpty());
      }
    });

    group.add(new AnAction(PerforceBundle.message("job.edit.associated.title"), PerforceBundle.message("job.edit.associated"), AllIcons.Actions.EditSource) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        final Map<ConnectionKey, List<PerforceJob>> data = getCurrentListJobs();
        if (data == null) {
          return;
        }
        ensureDefaultConnections();

        final EditChangelistJobsDialog dialog =
          new EditChangelistJobsDialog(myProject, myCurrent, myCurrent.hasDefaultName(), myConnMap, data);
        dialog.show();
        final Map<ConnectionKey, List<PerforceJob>> jobs = dialog.getJobs();
        saveJobsInCache(jobs);
        setItems(ContainerUtil.flatten(jobs.values()));
      }
    });
    group.add(new MyTextFieldAction(PerforceBundle.message("job.search.in.view.title"), PerforceBundle.message("job.search.in.view"), IconUtil.getAddIcon()));

    final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("PerforceCheckIn", group, true);
    return actionToolbar.getComponent();
  }

  private final class MyTextFieldAction extends TextFieldAction {
    private MyTextFieldAction(@NlsActions.ActionText String text, @NlsActions.ActionDescription String description, Icon icon) {
      super(text, description, icon, 20);
    }

    @Override
    public void perform() {
      searchByJobviewAndFilter();
    }

    private void searchByJobviewAndFilter() {
      final String text = myField.getText().trim();
      if (text.length() > 0) {
        ensureDefaultConnections();

        if (myCurrent != null) {
          final Map<ConnectionKey, P4Connection> p4ConnectionMap = ConnectionSelector.getConnections(myProject, myCurrent);
          if (p4ConnectionMap.isEmpty()) return;
          if (p4ConnectionMap.size() == 1) {
            final ConnectionKey key = p4ConnectionMap.keySet().iterator().next();
            final Pair<ConnectionKey,P4Connection> pair = Pair.create(key, p4ConnectionMap.get(key));
            addImpl(text, pair);
          } else {
            ConnectionSelector.selectConnection(p4ConnectionMap, connectionKey -> {
              if (connectionKey != null) {
                addImpl(text, Pair.create(connectionKey, p4ConnectionMap.get(connectionKey)));
              }
            });
          }
        }
      }
    }

    private void addImpl(String text, final Pair<ConnectionKey, P4Connection> pair) {
      if (pair != null) {
        final P4JobsLogicConn connMap = myConnMap.get(pair.getFirst());
        if (connMap != null) {
          final JobsWorker worker = new JobsWorker(myProject);
          final JobViewSearchSpecificator searchSpecificator = new JobViewSearchSpecificator(connMap.getJobView(), text);
          final P4Connection connection = pair.getSecond();
          final ConnectionKey key = pair.getFirst();
          final PerforceJobSpecification spec = connMap.getSpec();
          final List<PerforceJob> jobs = getJobsUnderProgress(worker, searchSpecificator, connection, key, spec);
          if (jobs == null) {
            return;
          }
          if (jobs.isEmpty()) {
            Messages.showMessageDialog(myProject, PerforceBundle.message("job.no.matching.pattern"), PerforceBundle.message("job.add"), Messages.getInformationIcon());
            return;
          }

          final Consumer<PerforceJob> consumer = perforceJob -> {
            //noinspection ThrowableNotThrown
            myAdderRemover.add(perforceJob, myCurrent, myProject);
            myField.setText("");
          };

          if (jobs.size() > 1) {
            showListPopup(jobs, myField, consumer, searchSpecificator.getMaxCount());
          } else {
            consumer.consume(jobs.get(0));
          }
        }
      }
    }
  }

  @Nullable("if canceled")
  public static List<PerforceJob> getJobsUnderProgress(final JobsWorker worker,
                                                        final JobsSearchSpecificator searchSpecificator,
                                                        final P4Connection connection,
                                                        final ConnectionKey key, final PerforceJobSpecification spec) {
    try {
      return ProgressManager.getInstance().runProcessWithProgressSynchronously(
        () -> worker.getJobs(spec, searchSpecificator, connection, key), PerforceBundle.message("job.searching.jobs"), true, worker.getProject());
    } catch (VcsException e1) {
      new ErrorReporter(PerforceBundle.message("job.searching.jobs.to.add")).report(worker.getProject(), e1);
      return null;
    }
  }

  private void initUI() {
    myPanel = new JPanel(new GridBagLayout());

    final GridBagConstraints gb = DefaultGb.create();
    gb.insets = new Insets(0,0,0,0);
    gb.anchor = GridBagConstraints.WEST;

    // todo prompt message when jobs ought to exist for CL
    final JLabel jobsPrompt = new JLabel(PerforceBundle.message("job.jobs"));
    gb.gridwidth = 3;
    myPanel.add(jobsPrompt, gb);

    ++ gb.gridy;
    gb.fill = GridBagConstraints.HORIZONTAL;
    myPanel.add(createToolbar(), gb);

    ++ gb.gridy;
    gb.fill = GridBagConstraints.BOTH;
    final JComponent comp = myDetails.createComponent();
    myPanel.add(comp, gb);
  }

  private void saveJobsInCache(Map<ConnectionKey, List<PerforceJob>> jobs) {
    if (myCurrent.hasDefaultName()) {
      myVcs.setDefaultAssociated(jobs);
    } else {
      myCache.put(myCurrent, jobs);
    }
  }

  private final static ColumnInfo<PerforceJob, String> JOB = new ColumnInfo<>(PerforceBundle.message("job")) {
    @Override
    public String valueOf(PerforceJob perforceJob) {
      return perforceJob.getName();
    }
  };
  private final static ColumnInfo<PerforceJob, String> STATUS = new ColumnInfo<>(PerforceBundle.message("job.status")) {
    @Override
    public String valueOf(PerforceJob perforceJob) {
      PerforceJobFieldValue field = perforceJob.getValueForStandardField(StandardJobFields.status);
      return field == null ? null : field.getValue();
    }
  };
  private final static ColumnInfo<PerforceJob, String> USER = new ColumnInfo<>(PerforceBundle.message("job.user")) {
    @Override
    public String valueOf(PerforceJob perforceJob) {
      PerforceJobFieldValue field = perforceJob.getValueForStandardField(StandardJobFields.user);
      return field == null ? null : field.getValue();
    }
  };
  private final static ColumnInfo<PerforceJob, String> DATE = new ColumnInfo<>(PerforceBundle.message("job.date")) {
    @Override
    public String valueOf(PerforceJob perforceJob) {
      PerforceJobFieldValue field = perforceJob.getValueForStandardField(StandardJobFields.date);
      return field == null ? null : field.getValue();
    }
  };
  private final static ColumnInfo<PerforceJob, String> DESCRIPTION = new ColumnInfo<>(PerforceBundle.message("job.description")) {
    @Override
    public String valueOf(PerforceJob perforceJob) {
      PerforceJobFieldValue field = perforceJob.getValueForStandardField(StandardJobFields.description);
      return field == null ? null : field.getValue();
    }
  };

  private static final ColumnInfo[] columns = new ColumnInfo[] {JOB, STATUS, USER, DATE, DESCRIPTION};

  private JPanel createSouthPanel(final TableView<PerforceJob> table, final @Nls String warningText) {
    final JPanel southPanel = new JPanel(new BorderLayout());

    final JPanel stuffPanel = new JPanel(new BorderLayout());

    if (warningText != null) {
      stuffPanel.setPreferredSize(JBUI.size(300, 120));
      southPanel.add(stuffPanel, BorderLayout.NORTH);
      final JLabel warningLabel = new JLabel(warningText);
      warningLabel.setFont(warningLabel.getFont().deriveFont(Font.BOLD));
      southPanel.add(warningLabel, BorderLayout.SOUTH);
    } else {
      southPanel.add(stuffPanel);
    }
    /*
    // todo draw job view text
    if (jobViewText != null) {

      stuffPanel.setPreferredSize(new Dimension(300, 100));
      southPanel.add(stuffPanel, BorderLayout.NORTH);

      final StringCutter stringCutter = new StringCutter("Job View: " + jobViewText, 20);
      stringCutter.cutString();
      final String s = stringCutter.getResult();

      final JLabel label = new JLabel(s) {
        @Override
        public Dimension getPreferredSize() {
          final Dimension oldSize = super.getPreferredSize();
          return new Dimension(oldSize.width, stringCutter.getNumLines() * lineSize);
        }
      };
      label.setUI(new MultiLineLabelUI());
      label.setForeground(UIUtil.getInactiveTextColor());

      final JScrollPane scroll = new JScrollPane(label);
      scroll.setPreferredSize(new Dimension(300, 40));
      southPanel.add(scroll, BorderLayout.SOUTH);
    } else {
      southPanel.add(stuffPanel);
    }*/

    southPanel.setPreferredSize(JBUI.size(300, 140));
    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        final PerforceJob job = table.getSelectedObject();
        if (job == null) {
          stuffPanel.removeAll();
        } else {
          final SelfLoadingJobDetailsPanel panel = new SelfLoadingJobDetailsPanel(myProject, job);
          stuffPanel.removeAll();
          final JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(panel.getPanel());
          stuffPanel.add(scrollPane);
        }
        southPanel.revalidate();
        southPanel.repaint();
      }
    });
    return southPanel;
  }

  private void showListPopup(final List<PerforceJob> jobs, final Component component, final Consumer<PerforceJob> consumer,
                             final int maxCount) {
    final List<PerforceJob> jobsNarrowed = ContainerUtil.getFirstItems(jobs, maxCount);
    final TableView<PerforceJob> table = new TableView<>(new ListTableModel<>(columns, jobsNarrowed, 0));
    table.setShowHorizontalLines(false);
    table.setTableHeader(null);
    Runnable runnable = () -> {
      PerforceJob job = table.getSelectedObject();
      if (job != null) {
        consumer.consume(job);
      }
    };

    if (table.getModel().getRowCount() == 0) {
      table.clearSelection();
    }

    table.setMinimumSize(JBUI.size(300, 50));
    final PopupChooserBuilder builder = new PopupChooserBuilder(table);

    builder.setSouthComponent(createSouthPanel(table, (jobs.size() > maxCount) ?
                                                      PerforceBundle.message("perforce.jobs.search.limit.exceeded.warning", maxCount) : null));

    builder.setTitle(PerforceBundle.message("perforce.jobs.select.one")).
        setItemChoosenCallback(runnable).
        setResizable(true).
        setDimensionServiceKey("org.jetbrains.idea.perforce.perforce.jobs.PerforceCheckinComponent.SelectOneJob").
        setMinSize(JBUI.size(300, 300));
    final JBPopup popup = builder.createPopup();

    popup.showUnderneathOf(component);
  }

  private void ensureDefaultConnections() {
    if (myCurrent.hasDefaultName()) {
      final JobDetailsLoader loader = new JobDetailsLoader(myProject);
      loader.fillConnections(myCurrent, myConnMap);
    }
  }

  @Override
  public JComponent getComponent() {
    return myPanel;
  }

  @Override
  public void saveState() {
    if (myCurrent != null && myCurrent.hasDefaultName()) {
      keepDefaultListJobs();
    }
    saveJobsToContext(getCurrentListJobs());
  }

  @Override
  public void restoreState() {

  }

  @Override
  public void onChangeListSelected(@NotNull LocalChangeList list) {
    if (Comparing.equal(list, myCurrent)) return;

    if (!list.hasDefaultName() && myCurrent != null && myCurrent.hasDefaultName()) {
      keepDefaultListJobs();
    }
    myCurrent = list;

    Map<ConnectionKey, List<PerforceJob>> data;
    if (myCurrent.hasDefaultName()) {
      correctDefaultAssociated(list);
      data = myVcs.getDefaultAssociated();

      final List<PerforceJob> filtered = new ArrayList<>();
      for (Map.Entry<ConnectionKey, List<PerforceJob>> entry : data.entrySet()) {
        filtered.addAll(entry.getValue());
      }

      setItems(filtered);
    }
    else {
      data = myCache.get(myCurrent);
      if (data == null) {
        data = loadOnSelect(myCurrent);
        myCache.put(list, data);
      }
      setItems(ContainerUtil.flatten(data.values()));
    }

    saveJobsToContext(data);
  }

  private void saveJobsToContext(@Nullable Map<ConnectionKey, List<PerforceJob>> data) {
    myCommitContext.putUserData(LINKED_JOBS_KEY, data == null ? null : ContainerUtil.flatten(data.values()));
  }

  private void correctDefaultAssociated(final LocalChangeList defaultList) {
    final Map<ConnectionKey, List<PerforceJob>> data = myVcs.getDefaultAssociated();

    final Map<ConnectionKey, P4Connection> map = ConnectionSelector.getConnections(myProject, defaultList);

    final Map<ConnectionKey, List<PerforceJob>> filtered = new HashMap<>();
    for (Map.Entry<ConnectionKey, List<PerforceJob>> entry : data.entrySet()) {
      if (map.containsKey(entry.getKey())) {
        filtered.put(entry.getKey(), entry.getValue());
      }
    }

    myVcs.setDefaultAssociated(filtered);
  }

  private void keepDefaultListJobs() {
    final List<PerforceJob> perforceJobs = myDetails.getJobs();
    final Map<ConnectionKey, List<PerforceJob>> jobs = new HashMap<>();
    for (PerforceJob job : perforceJobs) {
      List<PerforceJob> oldList = jobs.get(job.getConnectionKey());
      if (oldList == null) {
        oldList = new ArrayList<>();
        jobs.put(job.getConnectionKey(), oldList);
      }
      oldList.add(job);
    }
    myVcs.setDefaultAssociated(jobs);
  }

  private Map<ConnectionKey, List<PerforceJob>> loadOnSelect(final LocalChangeList list) {
    final JobDetailsLoader loader = new JobDetailsLoader(myProject);
    final Map<ConnectionKey, List<PerforceJob>> perforceJobs = new HashMap<>();
    loader.loadJobsForList(list, myConnMap, perforceJobs);
    return perforceJobs;
  }

  private void setItems(final List<PerforceJob> items) {
    myDetails.fillTree(items, items.isEmpty() ? null : items.get(0));
  }

}
