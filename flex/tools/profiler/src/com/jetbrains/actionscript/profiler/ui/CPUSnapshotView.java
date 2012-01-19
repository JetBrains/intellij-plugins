package com.jetbrains.actionscript.profiler.ui;

import com.intellij.ide.util.scopeChooser.ScopeChooserCombo;
import com.intellij.ide.util.scopeChooser.ScopeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.scope.ProjectFilesScope;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.SpeedSearchComparator;
import com.intellij.ui.TreeTableSpeedSearch;
import com.intellij.util.Alarm;
import com.intellij.util.Function;
import com.intellij.util.ui.tree.TreeUtil;
import com.jetbrains.actionscript.profiler.ProfilerIcons;
import com.jetbrains.actionscript.profiler.calltree.CallTree;
import com.jetbrains.actionscript.profiler.calltree.CallTreeUtil;
import com.jetbrains.actionscript.profiler.calltreetable.CallTreeTable;
import com.jetbrains.actionscript.profiler.calltreetable.MergedCallNode;
import com.jetbrains.actionscript.profiler.model.ProfileData;
import com.jetbrains.actionscript.profiler.render.FrameInfoCellRenderer;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import com.jetbrains.actionscript.profiler.sampler.Sample;
import com.jetbrains.actionscript.profiler.util.AllSearchScope;
import com.jetbrains.actionscript.profiler.util.JTreeUtil;
import com.jetbrains.actionscript.profiler.util.ResolveUtil;
import com.jetbrains.actionscript.profiler.vo.CallInfo;
import com.jetbrains.profiler.ProfileView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: Fedor.Korotkov
 */
public class CPUSnapshotView extends ProfileView implements Disposable {
  private static final int MS_COLUMN_WIDTH = 140;

  private JCheckBox myFilterSystemStuff;
  private ScopeChooserCombo filterScope;
  private JLabel scopeLabel;
  private CallTreeTable myHotSpotsTreeTable;
  private CallTreeTable myTracesTreeTable;
  private JPanel mainPanel;
  private JLabel invokedFunctionsLabel;
  private JLabel mergedCalleesLabel;
  private JPanel topPanel;
  private JPanel bottomPanel;

  private final CallTree rawCallTree;
  private Alarm myAlarm;

  private final GlobalSearchScope projectScope;

  private final Function<List<FrameInfo>, List<FrameInfo>> scopeMatcher = new Function<List<FrameInfo>, List<FrameInfo>>() {
    @Override
    public List<FrameInfo> fun(List<FrameInfo> traces) {
      final GlobalSearchScope scope = getCurrentScope();

      return ResolveUtil.filterByScope(traces, scope);
    }
  };

  public CPUSnapshotView(VirtualFile file, Project project) {
    super(file, project);
    projectScope = GlobalSearchScope.projectScope(project);
    rawCallTree = file.getUserData(ProfileData.CALL_TREE_KEY);
    setupUI();
    buildPerformanceSamples(myHotSpotsTreeTable.getSortableTreeTableModel());
  }

  @Nullable
  private GlobalSearchScope getCurrentScope() {
    final SearchScope _selectedScope = filterScope.getSelectedScope();
    if (_selectedScope instanceof AllSearchScope) {
      return null;
    }
    return _selectedScope instanceof GlobalSearchScope ?
           (GlobalSearchScope)_selectedScope : GlobalSearchScope.allScope(getProject());
  }

  @Override
  protected void uiSettingsChange() {
    super.uiSettingsChange();
    myHotSpotsTreeTable.clearColorCaches();
    myTracesTreeTable.clearColorCaches();
  }

  private void setupUI() {
    myHotSpotsTreeTable.setRootVisible(false);
    myTracesTreeTable.setRootVisible(false);

    setColumnWidth(myHotSpotsTreeTable.getColumnModel().getColumn(1), MS_COLUMN_WIDTH);
    setColumnWidth(myHotSpotsTreeTable.getColumnModel().getColumn(2), MS_COLUMN_WIDTH);
    setColumnWidth(myTracesTreeTable.getColumnModel().getColumn(1), MS_COLUMN_WIDTH);
    setColumnWidth(myTracesTreeTable.getColumnModel().getColumn(2), MS_COLUMN_WIDTH);

    scopeLabel.setLabelFor(filterScope.getComboBox());
    invokedFunctionsLabel.setLabelFor(myHotSpotsTreeTable);
    mergedCalleesLabel.setLabelFor(myTracesTreeTable);

    new TreeTableSpeedSearch(myHotSpotsTreeTable).setComparator(new SpeedSearchComparator(false));
    new TreeTableSpeedSearch(myTracesTreeTable).setComparator(new SpeedSearchComparator(false));

    PopupHandler.installPopupHandler(myHotSpotsTreeTable, PROFILER_VIEW_GROUP_ID, ActionPlaces.UNKNOWN);
    PopupHandler.installPopupHandler(myTracesTreeTable, PROFILER_VIEW_GROUP_ID, ActionPlaces.UNKNOWN);

    final ComboBoxModel model = filterScope.getComboBox().getModel();
    if (model instanceof DefaultComboBoxModel) {
      ((DefaultComboBoxModel)model).insertElementAt(new ScopeDescriptor(new EverythingGlobalScope(getProject())), 0);
    }

    myHotSpotsTreeTable.getTree().setCellRenderer(new FrameInfoCellRenderer(projectScope) {
      @Override
      public void customizeCellRenderer(JTree tree,
                                        Object value,
                                        boolean selected,
                                        boolean expanded,
                                        boolean leaf,
                                        int row,
                                        boolean hasFocus) {
        setPaintFocusBorder(false);
        setScopeIcon(ProfilerIcons.CALLER_SOLID_ARROW);
        setNonScopeIcon(ProfilerIcons.CALLER_DOTTED_ARROW);
        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, false);
      }
    });
    myTracesTreeTable.getTree().setCellRenderer(new FrameInfoCellRenderer(projectScope) {
      @Override
      public void customizeCellRenderer(JTree tree,
                                        Object value,
                                        boolean selected,
                                        boolean expanded,
                                        boolean leaf,
                                        int row,
                                        boolean hasFocus) {
        setPaintFocusBorder(false);
        setScopeIcon(ProfilerIcons.CALLEE_SOLID_ARROW);
        setNonScopeIcon(ProfilerIcons.CALLEE_DOTTED_ARROW);
        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, false);
      }
    });

    myFilterSystemStuff.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        buildPerformanceSamples(myHotSpotsTreeTable.getSortableTreeTableModel());
        TreeUtil.expand(myHotSpotsTreeTable.getTree(), 1);
      }
    });


    filterScope.getComboBox().addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        myAlarm.cancelAllRequests();
        myAlarm.addRequest(new Runnable() {
          public void run() {
            buildPerformanceSamples(myHotSpotsTreeTable.getSortableTreeTableModel());
            TreeUtil.expand(myHotSpotsTreeTable.getTree(), 1);
          }
        }, 100);
      }
    });
  }

  private void createUIComponents() {
    myTracesTreeTable = new CallTreeTable(getProject());
    createHotSpotsTreeTable();

    myAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);

    filterScope = new ScopeChooserCombo(getProject(), true, false, ProjectFilesScope.NAME);
  }

  private void createHotSpotsTreeTable() {
    myHotSpotsTreeTable = new CallTreeTable(getProject());

    myHotSpotsTreeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        final Object node = myHotSpotsTreeTable.getSelectedValue();
        if (!(node instanceof MergedCallNode)) return;
        myAlarm.cancelAllRequests();
        final MergedCallNode mergedCallNode = (MergedCallNode)node;

        myAlarm.addRequest(new Runnable() {
          public void run() {
            FrameInfo[] frames = new FrameInfo[]{mergedCallNode.getFrameInfo()};
            final Pair<Map<FrameInfo, Long>, Map<FrameInfo, Long>> countMaps = mergedCallNode.getCallTree().getCalleesTimeMaps(frames);
            final Map<FrameInfo, Long> countMap = countMaps.getFirst();
            final Map<FrameInfo, Long> selfCountMap = countMaps.getSecond();

            DefaultMutableTreeNode tracesRoot = (DefaultMutableTreeNode)myTracesTreeTable.getSortableTreeTableModel().getRoot();
            JTreeUtil.removeChildren(tracesRoot, myTracesTreeTable.getSortableTreeTableModel());
            fillTreeModelRoot(tracesRoot, mergedCallNode.getCallTree(), countMap, selfCountMap, false, frames);
            myTracesTreeTable.reload();

            TreeUtil.expand(myTracesTreeTable.getTree(), 1);
          }
        }, 500);
      }
    });
  }

  private void buildPerformanceSamples(final DefaultTreeModel treeModel) {
    final boolean skipSystemStuff = myFilterSystemStuff.isSelected();
    CallTree filteredCallTree = rawCallTree;
    if (skipSystemStuff) {
      filteredCallTree = CallTreeUtil.filterSystemStuff(filteredCallTree);
    }

    final Pair<Map<FrameInfo, Long>, Map<FrameInfo, Long>> countMaps = filteredCallTree.getTimeMaps();
    final Map<FrameInfo, Long> countMap = countMaps.getFirst();
    final Map<FrameInfo, Long> selfCountMap = countMaps.getSecond();

    DefaultMutableTreeNode tracesRoot = (DefaultMutableTreeNode)treeModel.getRoot();
    JTreeUtil.removeChildren(tracesRoot, treeModel);
    fillTreeModelRoot(tracesRoot, filteredCallTree, countMap, selfCountMap, true, new FrameInfo[0]);
    treeModel.reload();
  }

  private <T extends Sample> void fillTreeModelRoot(TreeNode node,
                                                    CallTree callTree,
                                                    final Map<FrameInfo, Long> countMap,
                                                    final Map<FrameInfo, Long> selfCountMap,
                                                    boolean backTrace,
                                                    FrameInfo[] frames) {
    final MutableTreeNode root = (MutableTreeNode)node;
    List<FrameInfo> traces = scopeMatcher.fun(new ArrayList<FrameInfo>(countMap.keySet()));

    GlobalSearchScope scope = getCurrentScope();
    int index = 0;
    for (final FrameInfo s : traces) {
      root
        .insert(new MergedCallNode<T>(new CallInfo(s, countMap.get(s), selfCountMap.get(s)), callTree, frames, backTrace, scope), index++);
    }
  }

  private static void setColumnWidth(TableColumn column, int newSize) {
    column.setMinWidth(newSize);
    column.setWidth(newSize);
    column.setMaxWidth(newSize);
  }

  @Override
  public void dispose() {

  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return mainPanel;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return null;
  }
}
