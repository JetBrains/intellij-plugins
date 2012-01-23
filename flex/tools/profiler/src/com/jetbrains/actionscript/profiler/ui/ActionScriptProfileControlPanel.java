package com.jetbrains.actionscript.profiler.ui;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.PopupHandler;
import com.intellij.util.Alarm;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.jetbrains.actionscript.profiler.ProfilerBundle;
import com.jetbrains.actionscript.profiler.ProfilerIcons;
import com.jetbrains.actionscript.profiler.base.NavigatableTree;
import com.jetbrains.actionscript.profiler.base.ProfilerActionGroup;
import com.jetbrains.actionscript.profiler.livetable.LiveModelController;
import com.jetbrains.actionscript.profiler.model.ProfilerDataConsumer;
import com.jetbrains.actionscript.profiler.model.ProfilingManager;
import com.jetbrains.actionscript.profiler.ui.node.CPUSnapshotNode;
import com.jetbrains.actionscript.profiler.ui.node.LiveObjectsNode;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.io.IOException;
import java.util.Date;

/**
 * @author: Fedor.Korotkov
 */
public class ActionScriptProfileControlPanel implements ProfilerActionGroup, Disposable {
  public static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.balloonGroup("Action Script Profiler");
  private JPanel mainPanel;
  private NavigatableTree snapshotTree;
  private JLabel myStatusLabel;
  private final DefaultTreeModel treeModel;

  private ProfilingManager profilingManager;
  private ProfilerDataConsumer profilerDataConsumer;

  private Runnable connectionCallback;
  private final Module module;
  private final String host;
  private final int port;
  private final String runConfigurationName;

  private final Alarm myAlarm = new Alarm();
  private static final int MINUTE = 60 * 1000;

  public ActionScriptProfileControlPanel(String runConfigurationName, final Module module, String host, int port) {
    this.runConfigurationName = runConfigurationName;
    this.module = module;
    this.host = host;
    this.port = port;

    treeModel = (new DefaultTreeModel(new DefaultMutableTreeNode(), true));
    snapshotTree.setModel(treeModel);
    snapshotTree.setRootVisible(false);

    setupComponents();
  }

  public JPanel getMainPanel() {
    return mainPanel;
  }

  enum State {
    NONE, CPU_PROFILING, NORMAL
  }

  private volatile State currentState = State.NONE;

  private void setCurrentState(final State state) {
    currentState = state;
  }

  private void setStatus(final String status) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        myStatusLabel.setText(status);
      }
    });
  }

  private void setupComponents() {
    EditSourceOnDoubleClickHandler.install(snapshotTree);
    PopupHandler.installPopupHandler(snapshotTree, PROFILER_SNAPSHOT_GROUP_ID, ActionPlaces.UNKNOWN);

    snapshotTree.setCellRenderer(new ColoredTreeCellRenderer() {
      @Override
      public void customizeCellRenderer(JTree tree,
                                        Object value,
                                        boolean selected,
                                        boolean expanded,
                                        boolean leaf,
                                        int row,
                                        boolean hasFocus) {
        setIcon(value instanceof CPUSnapshotNode ? ProfilerIcons.SNAPSHOT_CPU : ProfilerIcons.LIVE_OBJECTS);
        if (value.toString() != null) {
          append(value.toString());
        }
      }
    });
  }

  private void doCPUSnapshot() {
    final CPUSnapshotNode newNode =
      new CPUSnapshotNode(runConfigurationName, module, new Date(), profilerDataConsumer.getProfileData().getCallTree());
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        final MutableTreeNode root = (MutableTreeNode)treeModel.getRoot();
        treeModel.insertNodeInto(newNode, root, root.getChildCount());
      }
    });
  }

  public void startProfiling() {
    if (profilingManager != null) {
      profilingManager.dispose();
    }
    profilingManager = new ProfilingManager(port);
    final LiveModelController liveModelController = new LiveModelController();

    final LiveObjectsNode liveObjectsNode = new LiveObjectsNode(runConfigurationName, module, profilingManager, liveModelController);
    profilerDataConsumer = new ProfilerDataConsumer(liveModelController);

    setStatus(ProfilerBundle.message("agent.connection.waiting"));
    myAlarm.cancelAllRequests();
    myAlarm.addRequest(new Runnable() {
      @Override
      public void run() {
        NOTIFICATION_GROUP.createNotification(ProfilerBundle.message("profiler.connection.timeout"), NotificationType.ERROR).notify(module.getProject());
      }
    }, MINUTE);
    profilingManager.initializeProfiling(profilerDataConsumer, new ProfilingManager.Callback() {
      public void finished(@Nullable String data, @Nullable IOException ex) {
        if (data != null && connectionCallback != null) {
          myAlarm.cancelAllRequests();
          setStatus(ProfilerBundle.message("agent.connection.open"));
          ApplicationManager.getApplication().invokeLater(connectionCallback);
          setCurrentState(State.NORMAL);
        }
        else if (ex != null) {
          setStatus(ProfilerBundle.message("agent.connection.close"));
          setCurrentState(State.NONE);
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
              treeModel.removeNodeFromParent(liveObjectsNode);
            }
          });
        }
      }
    });

    treeModel.insertNodeInto(liveObjectsNode, (MutableTreeNode)treeModel.getRoot(), 0);
  }

  public void setConnectionCallback(Runnable runnable) {
    this.connectionCallback = runnable;
  }

  @Override
  public void dispose() {
    profilingManager.dispose();
    Disposer.dispose(myAlarm);
  }

  public DefaultActionGroup createProfilerActionGroup() {
    return new DefaultActionGroup(
      new ToggleAction(ProfilerBundle.message("start.cpu.profiling"),
                       ProfilerBundle.message("start.cpu.profiling.description"),
                       ProfilerIcons.START_CPU) {
        @Override
        public boolean isSelected(AnActionEvent e) {
          return currentState == State.CPU_PROFILING;
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
          if (state) {
            profilingManager.startCpuProfiling(new ProfilingManager.Callback() {
              @Override
              public void finished(@Nullable String data, @Nullable IOException ex) {
                setCurrentState(State.CPU_PROFILING);
                profilerDataConsumer.resetCpuUsageData();
              }
            });
            e.getPresentation().setText(ProfilerBundle.message("stop.cpu.profiling"));
            e.getPresentation().setDescription(ProfilerBundle.message("stop.cpu.profiling.description"));
            e.getPresentation().setIcon(ProfilerIcons.STOP_CPU);
          }
          else {
            profilingManager.stopCpuProfiling(new ProfilingManager.Callback() {
              @Override
              public void finished(@Nullable String data, @Nullable IOException ex) {
                setCurrentState(State.NORMAL);
                doCPUSnapshot();
              }
            });
            e.getPresentation().setText(ProfilerBundle.message("start.cpu.profiling"));
            e.getPresentation().setDescription(ProfilerBundle.message("start.cpu.profiling.description"));
            e.getPresentation().setIcon(ProfilerIcons.START_CPU);
          }
        }

        @Override
        public void update(AnActionEvent e) {
          super.update(e);
          e.getPresentation().setEnabled(currentState != State.NONE);
        }
      },
      new AnAction(ProfilerBundle.message("do.gc"),
                   ProfilerBundle.message("do.gc.description"),
                   ProfilerIcons.DO_GC) {
        @Override
        public void actionPerformed(AnActionEvent e) {
          profilingManager.doGc(new ProfilingManager.Callback() {
            public void finished(@Nullable String data, @Nullable IOException ex) {
              //TODO
            }
          });
        }

        @Override
        public void update(AnActionEvent e) {
          super.update(e);
          e.getPresentation().setEnabled(currentState != State.NONE);
        }
      }
    );
  }
}
