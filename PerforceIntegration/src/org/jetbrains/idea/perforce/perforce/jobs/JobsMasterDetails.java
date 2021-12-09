package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class JobsMasterDetails extends MasterDetailsComponent {
  private final Project myProject;
  private ListSelectionListener myListSelectionListener;

  public JobsMasterDetails(Project project) {
    myProject = project;
    initTree();
    init();

    getSplitter().setProportion(0.3f);
  }

  public void onlyMain() {
    getSplitter().setProportion(0f);
    getSplitter().setSecondComponent(null);
  }

  public JComponent getPreferredFocusTarget() {
    return myTree;
  }

  public void setSelectionListener(ListSelectionListener selectionListener) {
    myListSelectionListener = selectionListener;
  }

  @Override
  @Nls
  public String getDisplayName() {
    return PerforceBundle.message("configurable.JobsMasterDetails.display.name");
  }

  @NotNull
  public List<PerforceJob> getSelectedJobs() {
    final MyNode[] nodes = myTree.getSelectedNodes(MyNode.class, null);
    List<PerforceJob> result = new ArrayList<>();
    for (MyNode node : nodes) {
      result.add((PerforceJob)node.getConfigurable().getEditableObject());
    }
    return result;
  }

  public void removeSelectedJobs() {
    final TreePath[] paths = myTree.getSelectionPaths();
    if (paths != null) {
      removePaths(paths);
    }
    if (myTree.getRowCount() > 0) {
      myTree.setSelectionRow(0);
    }
  }

  public void addJob(final PerforceJob job) {
    final MyConfigurable configurable = new MyConfigurable(myProject, job);
    myRoot.add(new MyNode(configurable));

    ((DefaultTreeModel) myTree.getModel()).reload(myRoot);

    myTree.setSelectionRow(myTree.getRowCount() - 1);
  }

  public List<PerforceJob> getJobs() {
    final List<PerforceJob> result = new ArrayList<>();

    final int childCount = myRoot.getChildCount();
    for (int i = 0; i < childCount; i++) {
      final MyNode node = (MyNode) myRoot.getChildAt(i);
      result.add((PerforceJob) node.getConfigurable().getEditableObject());
    }
    return result;
  }

  @Override
  protected Dimension getPanelPreferredSize() {
    return JBUI.size(400, 200);
  }

  private void init() {
    myTree.setCellRenderer(new PerforceJobCellRenderer());
    myTree.setShowsRootHandles(false);
    myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    myTree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        if (myListSelectionListener != null) {
          myListSelectionListener.valueChanged(null);
        }
      }
    });
  }

  public void fillTree(final List<PerforceJob> jobs, @Nullable final PerforceJob selectJob) {
    myRoot.removeAllChildren();

    int idx = -1;
    for (int i = 0; i < jobs.size(); i++) {
      final PerforceJob job = jobs.get(i);
      final MyConfigurable configurable = new MyConfigurable(myProject, job);
      myRoot.add(new MyNode(configurable));
      if (selectJob != null && job.getName().equals(selectJob.getName())) {
        idx = i;
      }
    }

    ((DefaultTreeModel) myTree.getModel()).reload(myRoot);
    if (idx >= 0) {
      myTree.setSelectionRow(idx);
    }
  }

  private static final class PerforceJobCellRenderer implements TreeCellRenderer {
    private final ColoredTreeCellRenderer myTop;
    private final JPanel myPanel;

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
      if (! (value instanceof MyNode)) return myPanel;

      return myTop.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }

    private PerforceJobCellRenderer() {
      myPanel = new JPanel(new GridBagLayout());
      myPanel.setBackground(JBColor.WHITE);

      myTop = new ColoredTreeCellRenderer() {
        @Override
        public void customizeCellRenderer(@NotNull JTree tree,
                                          Object value,
                                          boolean selected,
                                          boolean expanded,
                                          boolean leaf,
                                          int row,
                                          boolean hasFocus) {
          if (! (value instanceof MyRootNode)) {
            final MyNode node = (MyNode)value;
            final MyConfigurable configurable = (MyConfigurable) node.getConfigurable();
            final PerforceJob job = configurable.getEditableObject();

            append(job.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);

            PerforceJobFieldValue status = job.getValueForStandardField(StandardJobFields.status);
            append(" <" + (status == null ? PerforceBundle.message("unknown.status") : status.getValue()) + ">", 
                   SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);

            PerforceJobFieldValue desc = job.getValueForStandardField(StandardJobFields.description);
            if (desc != null) {
              String text = desc.getValue();
              text = text.length() > 30 ? text.substring(0, 30) + "..." : text;
              append(" " + text, SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES);
            }
          }
        }
      };
    }

  }

  private static final class MyConfigurable extends NamedConfigurable<PerforceJob> {
    private final SelfLoadingJobDetailsPanel mySelfLoadingJobDetailsPanel;
    private final PerforceJob myJob;

    private MyConfigurable(final Project project, @NotNull final PerforceJob job) {
      mySelfLoadingJobDetailsPanel = new SelfLoadingJobDetailsPanel(project, job);
      myJob = job;
    }

    @Override
    public void setDisplayName(String name) {
    }

    @Override
    public PerforceJob getEditableObject() {
      return myJob;
    }

    @Override
    public String getBannerSlogan() {
      return myJob.getName();
    }

    @Override
    public JComponent createOptionsPanel() {
      return mySelfLoadingJobDetailsPanel.getPanel();
    }

    @Override
    @Nls
    public String getDisplayName() {
      return myJob.getName();
    }

    @Override
    public String getHelpTopic() {
      return null;
    }

    @Override
    public boolean isModified() {
      return false;
    }

    @Override
    public void apply() throws ConfigurationException {
    }
  }
}
