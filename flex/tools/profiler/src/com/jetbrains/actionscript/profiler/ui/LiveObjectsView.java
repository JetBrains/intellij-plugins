package com.jetbrains.actionscript.profiler.ui;

import com.intellij.ide.util.scopeChooser.ScopeChooserCombo;
import com.intellij.ide.util.scopeChooser.ScopeDescriptor;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.scope.ProjectFilesScope;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.SpeedSearchComparator;
import com.intellij.ui.TreeTableSpeedSearch;
import com.intellij.util.Function;
import com.intellij.util.ui.tree.TreeUtil;
import com.jetbrains.actionscript.profiler.ProfilerBundle;
import com.jetbrains.actionscript.profiler.ProfilerIcons;
import com.jetbrains.actionscript.profiler.livetable.LiveModelController;
import com.jetbrains.actionscript.profiler.livetable.LiveObjectsTreeTable;
import com.jetbrains.actionscript.profiler.livetable.SizeInfoNode;
import com.jetbrains.actionscript.profiler.model.ProfileData;
import com.jetbrains.actionscript.profiler.model.ProfilingManager;
import com.jetbrains.actionscript.profiler.render.SizeInfoCellRenderer;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import com.jetbrains.actionscript.profiler.sampler.SampleLocationResolver;
import com.jetbrains.actionscript.profiler.util.AllSearchScope;
import com.jetbrains.actionscript.profiler.util.ResolveUtil;
import com.jetbrains.profiler.ProfileView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class LiveObjectsView extends ProfileView implements Disposable {
  private static final Logger LOG = Logger.getInstance(LiveObjectsView.class.getName());
  private static final int MS_COLUMN_WIDTH = 140;
  private JCheckBox liveUpdatesCheckBox;
  private LiveObjectsTreeTable liveObjectsTreeTable;
  private JPanel mainPanel;
  private ScopeChooserCombo filterScope;
  private JLabel scopeLabel;
  private JLabel myAllocatedMemory;
  private final LiveModelController controller;
  private final ProfilingManager profilingManager;

  private Timer updateTimer = null;

  public LiveObjectsView(VirtualFile file, Project project) {
    super(file, project);
    setupUI();
    controller = file.getUserData(ProfileData.CONTROLLER);
    profilingManager = file.getUserData(ProfileData.PROFILING_MANAGER);

    if (controller == null || profilingManager == null) {
      return;
    }
    updateTimer = new Timer(2000, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (!liveUpdatesCheckBox.isSelected()) {
          return;
        }
        final List<TreePath> paths = TreeUtil.collectExpandedPaths(liveObjectsTreeTable.getTree());
        final TreePath selectionPath = liveObjectsTreeTable.getTree().getSelectionPath();
        controller.updateScope(getCurrentScope());
        controller.apply(liveObjectsTreeTable.getSortableTreeTableModel());
        liveObjectsTreeTable.reload();
        TreeUtil.restoreExpandedPaths(liveObjectsTreeTable.getTree(), paths);
        liveObjectsTreeTable.getTree().setSelectionPath(selectionPath);

        myAllocatedMemory.setText(ProfilerBundle.message("allocated.memory.size", controller.getAllocatedMemorySize()));
      }
    });
    updateTimer.setInitialDelay(1000);
    updateTimer.setRepeats(true);
    updateTimer.start();

    liveUpdatesCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (liveUpdatesCheckBox.isSelected()) {
          updateTimer.stop();
          updateTimer.setInitialDelay(0);
          updateTimer.start();
        }
      }
    });

    liveObjectsTreeTable.setFrameLocationResolveFunction(new Function<FrameInfo, Navigatable>() {
      @Override
      public Navigatable fun(FrameInfo frameInfo) {
        return new SampleLocationResolver(frameInfo, new ProjectAndLibrariesScope(getProject()));
      }
    });

    liveObjectsTreeTable.setClassNameLocationResolveFunction(new Function<String, Navigatable>() {
      @Nullable
      @Override
      public Navigatable fun(String s) {
        PsiElement element = ResolveUtil.findClassByQName(s, getCurrentScope());
        if (element instanceof JSClass) {
          return element.getNavigationElement().getContainingFile();
        }
        return null;
      }
    });

    profilingManager.startCollectingLiveObjects(new ProfilingManager.Callback() {
      @Override
      public void finished(@Nullable String data, @Nullable IOException ex) {
        if (ex != null) {
          LOG.warn(ex);
        }
      }
    });
  }

  @Override
  protected void uiSettingsChange() {
    super.uiSettingsChange();
    liveObjectsTreeTable.clearColorCaches();
  }

  private void createUIComponents() {
    liveObjectsTreeTable = new LiveObjectsTreeTable(getProject());
    filterScope = new ScopeChooserCombo(getProject(), true, false, ProjectFilesScope.NAME);
  }

  private void setupUI() {
    liveObjectsTreeTable.getTree().setRootVisible(false);
    setColumnWidth(liveObjectsTreeTable.getColumnModel().getColumn(1), MS_COLUMN_WIDTH);
    setColumnWidth(liveObjectsTreeTable.getColumnModel().getColumn(2), MS_COLUMN_WIDTH);

    new TreeTableSpeedSearch(liveObjectsTreeTable).setComparator(new SpeedSearchComparator(false));
    PopupHandler.installPopupHandler(liveObjectsTreeTable, PROFILER_VIEW_GROUP_ID, ActionPlaces.UNKNOWN);

    liveObjectsTreeTable.getEmptyText().setText(ProfilerBundle.message("live.objects.loading"));

    liveObjectsTreeTable.getTree().setCellRenderer(new SizeInfoCellRenderer() {
      @Override
      public void customizeCellRenderer(Object value) {
        Icon icon = ProfilerIcons.INFORMATION;
        if (value instanceof SizeInfoNode) {
          icon = ((SizeInfoNode)value).isMethod() ? ProfilerIcons.METHOD : ProfilerIcons.CLASS;
        }
        setPaintFocusBorder(false);
        setIcon(icon);
        super.customizeCellRenderer(value);
      }
    });

    final ComboBoxModel model = filterScope.getComboBox().getModel();
    if (model instanceof DefaultComboBoxModel) {
      ((DefaultComboBoxModel)model).insertElementAt(new ScopeDescriptor(new AllSearchScope(getProject())), 0);
    }
  }


  @Nullable
  private GlobalSearchScope getCurrentScope() {
    final SearchScope _selectedScope = filterScope.getSelectedScope();
    return _selectedScope instanceof GlobalSearchScope ?
           (GlobalSearchScope)_selectedScope : GlobalSearchScope.allScope(getProject());
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

  @Override
  public void dispose() {
    super.dispose();
    if (updateTimer != null) {
      updateTimer.stop();
    }
    if (profilingManager != null) {
      profilingManager.stopCollectingLiveObjects(new ProfilingManager.Callback() {
        @Override
        public void finished(@Nullable String data, @Nullable IOException ex) {
          if (ex != null) {
            LOG.warn(ex);
          }
        }
      });
    }
  }

  private static void setColumnWidth(TableColumn column, int newSize) {
    column.setMinWidth(newSize);
    column.setWidth(newSize);
    column.setMaxWidth(newSize);
  }
}
