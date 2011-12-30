package com.jetbrains.actionscript.profiler;

import com.intellij.ide.util.scopeChooser.ScopeChooserCombo;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.scope.ProjectFilesScope;
import com.intellij.ui.*;
import com.intellij.util.Alarm;
import com.intellij.util.Function;
import com.intellij.util.ui.tree.TreeUtil;
import com.jetbrains.actionscript.profiler.base.LazyNode;
import com.jetbrains.actionscript.profiler.base.NavigatableDataProducer;
import com.jetbrains.actionscript.profiler.base.NavigatableTree;
import com.jetbrains.actionscript.profiler.calltree.CallTree;
import com.jetbrains.actionscript.profiler.calltreetable.CallTreeTable;
import com.jetbrains.actionscript.profiler.calltreetable.MergedCallNode;
import com.jetbrains.actionscript.profiler.model.AgentVersionMismatchProblem;
import com.jetbrains.actionscript.profiler.model.ProfileData;
import com.jetbrains.actionscript.profiler.model.ProfilerDataConsumer;
import com.jetbrains.actionscript.profiler.model.ProfilingManager;
import com.jetbrains.actionscript.profiler.render.FrameInfoCelleRenderer;
import com.jetbrains.actionscript.profiler.sampler.*;
import com.jetbrains.actionscript.profiler.util.JTreeUtil;
import com.jetbrains.actionscript.profiler.util.LocationResolverUtil;
import com.jetbrains.actionscript.profiler.vo.CallInfo;
import com.jetbrains.profiler.ProfileView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim
 * Date: 02.09.2010
 * Time: 13:52:59
 */
public class ActionScriptProfileView extends ProfileView {
  public static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.balloonGroup("Action Script Profiler");
  private static final Logger LOG = Logger.getInstance(ActionScriptProfileView.class.getName());
  private static final int MS_COLUMN_WIDTH = 140;
  public static final String PROFILER_ACTION_GROUP_ID = "ProfilerViewMenu";
  private JPanel myPanel;
  private NavigatableTree mySamplesTree;
  private JButton myStartCpuButton;
  private JButton myStopCpuButton;

  private NavigatableTree myMemoryTree;
  private NavigatableTree myReachableFromTree;
  private JButton myDoGcButton;
  private JLabel myStatus;
  private JButton myCaptureSnapshot;
  private JTabbedPane myTabbedPane;
  private CallTreeTable myHotSpotsTreeTable;
  private JCheckBox myFilterSystemStuff;
  private CallTreeTable myTracesTreeTable;
  private ScopeChooserCombo filterScope;
  private JLabel scopeLabel;
  private final ProfilingManager myProfilingManager;
  public static final Key<ProfilingManager> ourProfilingManagerKey = Key.create("profiler.manager.key");
  private boolean myEOFReached;
  private static final int MEMORY_TAB_INDEX = 2;
  private static final int CPU_HOTSPOTS_TAB_INDEX = 1;
  private Alarm myAlarm;

  private final Function<List<FrameInfo>, List<FrameInfo>> scopeMatcher = new Function<List<FrameInfo>, List<FrameInfo>>() {
    @Override
    public List<FrameInfo> fun(List<FrameInfo> traces) {
      final GlobalSearchScope scope = getCurrentScope();

      return LocationResolverUtil.filterByScope(traces, scope);
    }
  };

  private GlobalSearchScope getCurrentScope() {
    final SearchScope _selectedScope = filterScope.getSelectedScope();
    return _selectedScope instanceof GlobalSearchScope ?
      (GlobalSearchScope) _selectedScope : GlobalSearchScope.allScope(getProject());
  }

  private void createUIComponents() {
    createHotSpotsTreeTable();

    myAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);

    filterScope = new ScopeChooserCombo(getProject(), true, false, ProjectFilesScope.NAME);
  }

  private void createHotSpotsTreeTable() {
    myHotSpotsTreeTable = new CallTreeTable();

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

  enum State {
    NONE, CPU_PROFILING, SIMPLE_WORKING
  }

  void switchToState(State state) {
    if (state == State.NONE) {
      myStartCpuButton.setEnabled(true);
      myStopCpuButton.setEnabled(false);
      myCaptureSnapshot.setEnabled(true);
      myDoGcButton.setEnabled(true);
      myFilterSystemStuff.setEnabled(true);
    } else if (state == State.CPU_PROFILING) {
      myStartCpuButton.setEnabled(false);
      myStopCpuButton.setEnabled(true);
      myCaptureSnapshot.setEnabled(false);
      myDoGcButton.setEnabled(false);
      myFilterSystemStuff.setEnabled(false);
    } else if (state == State.SIMPLE_WORKING) {
      myStartCpuButton.setEnabled(false);
      myStopCpuButton.setEnabled(false);
      myCaptureSnapshot.setEnabled(false);
      myDoGcButton.setEnabled(false);
      myFilterSystemStuff.setEnabled(false);
    }
  }

  private final ProfileData data = new ProfileData();

  public ActionScriptProfileView(VirtualFile file, Project project) {
    super(file, project);

    myProfilingManager = file.getUserData(ourProfilingManagerKey);

    myHotSpotsTreeTable.setRootVisible(false);
    myTracesTreeTable.setRootVisible(false);

    myMemoryTree.setRootVisible(false);
    mySamplesTree.setRootVisible(false);
    myReachableFromTree.setRootVisible(false);

    setColumnWidth(myHotSpotsTreeTable.getColumnModel().getColumn(1), MS_COLUMN_WIDTH);
    setColumnWidth(myHotSpotsTreeTable.getColumnModel().getColumn(2), MS_COLUMN_WIDTH);
    setColumnWidth(myTracesTreeTable.getColumnModel().getColumn(1), MS_COLUMN_WIDTH);
    setColumnWidth(myTracesTreeTable.getColumnModel().getColumn(2), MS_COLUMN_WIDTH);

    new TreeTableSpeedSearch(myHotSpotsTreeTable).setComparator(new SpeedSearchComparator(false));
    new TreeTableSpeedSearch(myTracesTreeTable).setComparator(new SpeedSearchComparator(false));

    new TreeSpeedSearch(myMemoryTree).setComparator(new SpeedSearchComparator(false));
    new TreeSpeedSearch(mySamplesTree).setComparator(new SpeedSearchComparator(false));
    new TreeSpeedSearch(myReachableFromTree).setComparator(new SpeedSearchComparator(false));

    PopupHandler.installPopupHandler(myHotSpotsTreeTable, PROFILER_ACTION_GROUP_ID, ActionPlaces.UNKNOWN);
    PopupHandler.installPopupHandler(myTracesTreeTable, PROFILER_ACTION_GROUP_ID, ActionPlaces.UNKNOWN);
    PopupHandler.installPopupHandler(myMemoryTree, PROFILER_ACTION_GROUP_ID, ActionPlaces.UNKNOWN);
    PopupHandler.installPopupHandler(myReachableFromTree, PROFILER_ACTION_GROUP_ID, ActionPlaces.UNKNOWN);
    PopupHandler.installPopupHandler(mySamplesTree, PROFILER_ACTION_GROUP_ID, ActionPlaces.UNKNOWN);

    myHotSpotsTreeTable.getTree().setCellRenderer(new FrameInfoCelleRenderer() {

      @Override
      public void customizeCellRenderer(JTree tree,
                                        Object value,
                                        boolean selected,
                                        boolean expanded,
                                        boolean leaf,
                                        int row,
                                        boolean hasFocus) {
        setPaintFocusBorder(false);
        setOpenIcon(ProfilerIcons.CALLER_ARROW);
        setClosedIcon(ProfilerIcons.CALLER_ARROW);
        setLeafIcon(ProfilerIcons.CALLER_LEAF_ARROW);
        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, false);
      }
    });

    myTracesTreeTable.getTree().setCellRenderer(new FrameInfoCelleRenderer() {
      @Override
      public void customizeCellRenderer(JTree tree,
                                        Object value,
                                        boolean selected,
                                        boolean expanded,
                                        boolean leaf,
                                        int row,
                                        boolean hasFocus) {
        setPaintFocusBorder(false);
        setOpenIcon(ProfilerIcons.CALLEE_ARROW);
        setClosedIcon(ProfilerIcons.CALLEE_ARROW);
        setLeafIcon(ProfilerIcons.CALLEE_LEAF_ARROW);
        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, false);
      }
    });

    TreeCellRenderer classCellRenderer = new FrameInfoCelleRenderer() {
      @Override
      public void customizeCellRenderer(JTree tree,
                                        Object value,
                                        boolean selected,
                                        boolean expanded,
                                        boolean leaf,
                                        int row,
                                        boolean hasFocus) {
        setPaintFocusBorder(false);
        Icon icon = ProfilerIcons.CLASS;
        if (value instanceof MergedPathNode) {
          icon = ProfilerIcons.METHOD;
        }
        setOpenIcon(icon);
        setClosedIcon(icon);
        setLeafIcon(icon);
        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, false);
      }
    };

    myMemoryTree.setCellRenderer(classCellRenderer);
    myReachableFromTree.setCellRenderer(classCellRenderer);

    mySamplesTree.setCellRenderer(new FrameInfoCelleRenderer() {
      @Override
      public void customizeCellRenderer(JTree tree,
                                        Object value,
                                        boolean selected,
                                        boolean expanded,
                                        boolean leaf,
                                        int row,
                                        boolean hasFocus) {
        setPaintFocusBorder(false);
        setOpenIcon(ProfilerIcons.METHOD);
        setClosedIcon(ProfilerIcons.METHOD);
        setLeafIcon(ProfilerIcons.METHOD);
        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, false);
        if(value instanceof MergedPathNode){
          append(" " + ((MergedPathNode)value).getPercents() + "%");
        }
      }
    });

    scopeLabel.setLabelFor(filterScope.getComboBox());

    resetMemoryData();

    myFilterSystemStuff.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        buildPerformanceSamples(myHotSpotsTreeTable.getSortableTreeTableModel(), data.getProfile());
        TreeUtil.expand(myHotSpotsTreeTable.getTree(), 1);
      }
    });


    filterScope.getComboBox().addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        myAlarm.cancelAllRequests();
        myAlarm.addRequest(new Runnable() {
          public void run() {
            buildPerformanceSamples(myHotSpotsTreeTable.getSortableTreeTableModel(), data.getProfile());
            TreeUtil.expand(myHotSpotsTreeTable.getTree(), 1);
          }
        }, 100);
      }
    });

    myMemoryTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        final TreePath path = e.getPath();
        if (path.getPathCount() < 2) {
          return;
        }
        Object pathComponent = path.getPathComponent(1);
        String qName = (String)((DefaultMutableTreeNode)pathComponent).getUserObject();
        qName = qName.substring(0, qName.indexOf(' '));
        myAlarm.cancelAllRequests();
        final String finalQName = qName;
        myAlarm.addRequest(new Runnable() {
          public void run() {
            final DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
            myReachableFromTree.setModel(treeModel);
            treeModel.setRoot(new LazyNode() {
              @Override
              protected void doLoadChildren() {
                int index = 0;
                List<CreateObjectSample> samples = new ArrayList<CreateObjectSample>();
                for(final CreateObjectSample s:data.getCreateObjectSamples()) {
                  if (finalQName.equals(s.className)) {
                    samples.add(s);
                  }
                }

                Collections.sort(samples, new Comparator<CreateObjectSample>() {
                  public int compare(CreateObjectSample s, CreateObjectSample s2) {
                    int sizeDiff = s2.size - s.size;
                    return sizeDiff != 0 ? sizeDiff:s2.id - s.id;
                  }
                });

                MutableTreeNode intermediateNode = this;
                int subindex = 0;
                String title = "Group of 500 objects";

                if (samples.size() > 500) {
                  intermediateNode = new DefaultMutableTreeNode(title);
                  treeModel.insertNodeInto(intermediateNode, this, subindex++);
                }

                for(CreateObjectSample s:samples) {
                  treeModel.insertNodeInto(new BackRefNode(s, treeModel), intermediateNode, index++);
                  if (index % 500 == 0 && index > 0 && intermediateNode != this) {
                    intermediateNode = new DefaultMutableTreeNode(title);
                    treeModel.insertNodeInto(intermediateNode, this, subindex++);
                    index = 0;
                  }
                }

                if (intermediateNode != this) {
                  Runnable runnable = new Runnable() {
                    public void run() {
                      myReachableFromTree.expandPath(new TreePath(new Object[] {treeModel.getRoot(), getChildAt(0)}));
                    }
                  };
                  SwingUtilities.invokeLater(runnable);
                }
              }
            }
);
          }
        }, 500);
      }
    });

    resetCpuUsageData();

    if (myProfilingManager != null) {
      myProfilingManager.initializeProfiling(new ProfilerDataConsumer() {
        public void process(Sample sample) {
          if (sample instanceof CreateObjectSample) {
            final CreateObjectSample createObjectSample = (CreateObjectSample) sample;
            data.incAllocated(createObjectSample.size);
            data.putNewObject(createObjectSample.id, createObjectSample);
            return;
          } else if (sample instanceof DeleteObjectSample) {
            DeleteObjectSample deleteObjectSample = (DeleteObjectSample) sample;

            final CreateObjectSample objectSample = data.removeObject(deleteObjectSample.id);
            int size = deleteObjectSample.size;
            if (objectSample != null) { // already collected items
              if (size != objectSample.size) {
                size = objectSample.size;
              }
            }

            data.decAllocated(size);

            return;
          }

          data.addPerformanceSample(sample);
        }

        public void referenced(int pid, int id) {
          Set<Integer> integers = data.getReferences().get(pid);
          if (integers == null) {
            integers = new LinkedHashSet<Integer>(3);
            data.getReferences().put(pid, integers);
          }
          integers.add(id);
        }
      }, new ProfilingManager.Callback() {
        public void finished(String data, IOException ex) {
          if (data != null) {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                myStatus.setText("Connected to profiling agent");
                switchToState(State.NONE);
                resetDependents(true);
              }
            });
          }
          else {
            reportProblem(ex);
          }
        }
      });
    }

    myStartCpuButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        adjustControls(false);
      }
    });

    myStopCpuButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        adjustControls(true);
        myTabbedPane.setSelectedIndex(CPU_HOTSPOTS_TAB_INDEX);
      }
    });

    myCaptureSnapshot.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        switchToState(State.SIMPLE_WORKING);
        resetMemoryData();

        myStatus.setText("Capturing memory snapshot...");
        myProfilingManager.captureMemorySnapshot(new ProfilingManager.Callback() {
          public void finished(String str, IOException ex) {
            if (ex != null) {
              reportProblem(ex);
              invokeOnEdt(new Runnable() {
                public void run() {
                  switchToState(State.NONE);
                }
              });
              return;
            }

            invokeOnEdt(new Runnable() {
              public void run() {
                dumpMemory(data);
                myTabbedPane.setSelectedIndex(MEMORY_TAB_INDEX);
                myStatus.setText("Memory snapshot captured");
                switchToState(State.NONE);
              }
            });
          }
        });
      }
    });

    myDoGcButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        myProfilingManager.doGc(new ProfilingManager.Callback() {
          public void finished(final String str, IOException ex) {
            if (str != null) {
              invokeOnEdt(new Runnable() {
                public void run() {
                  myStatus.setText(str.substring(str.lastIndexOf(' ') + 1));
                }
              });
              return;
            }
            reportProblem(ex);
          }
        });
      }
    });
  }

  private static void setColumnWidth(TableColumn column, int newSize) {
    column.setMinWidth(newSize);
    column.setWidth(newSize);
    column.setMaxWidth(newSize);
  }

  private static String fixUserObjectStringForNode(Object lastPathComponent) {
    String path = lastPathComponent.toString();
    if (path.endsWith("%") || lastPathComponent instanceof MergedCallNode) {
      path = path.substring(0, path.lastIndexOf(' ')); // skip % // TODO
    }
    return path;
  }

  private void reportProblem(final IOException ex) {
    if (ex != null) {
      Runnable runnable = new Runnable() {
        public void run() {
          if (myEOFReached) return;
          boolean agentVersionMismatch = ex instanceof AgentVersionMismatchProblem;

          if (myStopCpuButton.isEnabled() && !agentVersionMismatch) {
            resetDependents(true);
            dumpCurrentData(data);
          }

          switchToState(State.SIMPLE_WORKING);

          if (ex instanceof EOFException) {
            myEOFReached = true;
            myStatus.setText("Profiler agent disconnected" + (agentVersionMismatch ? " , agent version is different":""));
            return;
          }

          if (ex instanceof SocketException) {
            myEOFReached = true;
            myStatus.setText("Unexpected loss of connection");
            final String message = "Unexpected loss of connection with Profiler Agent:<br/>" + ex.getLocalizedMessage();
            NOTIFICATION_GROUP.createNotification(message, NotificationType.WARNING).notify(getProject());
            return;
          }

          NOTIFICATION_GROUP.createNotification("Connection error:<br/>" + ex.getLocalizedMessage(), NotificationType.ERROR).notify(getProject());
        }
      };

      invokeOnEdt(runnable);
    }
  }

  private void adjustControls(final boolean b) {
    switchToState(State.SIMPLE_WORKING);

    if (myProfilingManager != null) {
      ProfilingManager.Callback callback = new ProfilingManager.Callback() {
        public void finished(String str, IOException ex) {
          if (str != null) {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                if (b) {
                  myStatus.setText("Sampling stopped");
                  switchToState(State.NONE);
                  resetDependents(true);
                } else {
                  myStatus.setText("Sampling started");
                  switchToState(State.CPU_PROFILING);
                  resetDependents(false);
                }

                if (!b) {
                  resetCpuUsageData();
                  return;
                }

                dumpCurrentData(data);
              }
            });
          }
          reportProblem(ex);
        }
      };

      if (b) {
        myStatus.setText("Stopping profiling...");
        myProfilingManager.stopCpuProfiling(callback);
      } else {
        myStatus.setText("Starting profiling...");
        myProfilingManager.startCpuProfiling(callback);
      }
    }
  }

  private void resetDependents(boolean b) {
    mySamplesTree.setEnabled(b);
    myMemoryTree.setEnabled(b);
    myHotSpotsTreeTable.setEnabled(b);
  }

  private void dumpCurrentData(final ProfileData data) {
    TreeModel treeModel = mySamplesTree.getModel();
    final Function<Set<Sample>, Integer> sizeFunction = new Function<Set<Sample>, Integer>() {
      public Integer fun(Set<Sample> samples) {
        return (samples.size() * 100) / data.getPerformanceInfoSize();
      }
    };

    myHotSpotsTreeTable.clearSelection();

    buildSamples((DefaultTreeModel) treeModel, (DefaultMutableTreeNode) treeModel.getRoot(), false, data.getProfile(), 0, sizeFunction, getCurrentScope());
    buildPerformanceSamples(myHotSpotsTreeTable.getSortableTreeTableModel(), data.getProfile());
    TreeUtil.expand(myHotSpotsTreeTable.getTree(), 1);

    DefaultMutableTreeNode tracesRoot = (DefaultMutableTreeNode)myTracesTreeTable.getSortableTreeTableModel().getRoot();
    tracesRoot.removeAllChildren();
    myTracesTreeTable.reload();

    List sortKeys = myHotSpotsTreeTable.getRowSorter().getSortKeys();
    if(sortKeys == null || sortKeys.size() == 0){
      myHotSpotsTreeTable.getRowSorter().toggleSortOrder(0);
    }

  }

  private void dumpMemory(final ProfileData data) {
    final Map<String, Set<CreateObjectSample>> objectsByClasses = new HashMap<String, Set<CreateObjectSample>>();

    final ArrayList<Set<CreateObjectSample>> list = new ArrayList<Set<CreateObjectSample>>();
    for(CreateObjectSample x : data.getCreateObjectSamples()) {
      Set<CreateObjectSample> createObjectSamples = objectsByClasses.get(x.className);
      if (createObjectSamples == null) {
        createObjectSamples = new HashSet<CreateObjectSample>();
        objectsByClasses.put(x.className, createObjectSamples);
        list.add(createObjectSamples);
      }
      createObjectSamples.add(x);
    }
    final Map<Set<CreateObjectSample>, Integer> instances2Size = new HashMap<Set<CreateObjectSample>, Integer>();
    Collections.sort(list, new Comparator<Set<CreateObjectSample>>() {
      public int compare(Set<CreateObjectSample> o1, Set<CreateObjectSample> o2) {
        return getKey(o2) - getKey(o1);
      }

      private Integer getKey(Set<CreateObjectSample> o1) {
        Integer integer = instances2Size.get(o1);
        if (integer == null) {
          integer = calcSize(o1);
          instances2Size.put(o1, integer);
        }
        return integer;
      }

    });

    final DefaultTreeModel memoryModel = (DefaultTreeModel) myMemoryTree.getModel();
    final DefaultMutableTreeNode memoryModelRoot = (DefaultMutableTreeNode) memoryModel.getRoot();
    int index2 = 0;

    final Function<Set<CreateObjectSample>, Integer> function = new Function<Set<CreateObjectSample>, Integer>() {
      public Integer fun(Set<CreateObjectSample> samples) {
        return percent(calcSize(samples), data);
      }
    };

    for(final Set<CreateObjectSample> s:list) {
      Integer total = instances2Size.get(s);
      if (total == null) total = calcSize(s);
      final String userObject = s.iterator().next().className + " " + percent(total, data) + "% {" +  s.size() + " objects of " + total + " bytes}";
      DefaultMutableTreeNode classNode = new LazyNode() {
        @Override
        protected void doLoadChildren() {
          buildSamples(memoryModel, this, true, s, 0, function, getCurrentScope());
        }
      };
      classNode.setUserObject(userObject);
      memoryModel.insertNodeInto(classNode, memoryModelRoot, index2++);
    }
  }

  private static int calcSize(Set<CreateObjectSample> samples) {
    int total = 0;
    for (CreateObjectSample c : samples) total += c.size;
    return total;
  }

  private static int percent(int total, ProfileData data) {
    if (data.getAllocated() == 0) return 0;
    return (int)(((long)total * 100) / data.getAllocated());
  }

  interface GroupHandler<T extends Sample, K> {
    void process(Map<K,  Set<T>> data);
    @Nullable K getCategory(T sample);
  }

  private static <T extends Sample> void buildSamples(final DefaultTreeModel model, final DefaultMutableTreeNode root, final boolean innermostFirst,
                            Set<T> sampleSet, final int level, final Function<Set<T>, Integer> classifier, final GlobalSearchScope scope) {
    processSamples(sampleSet, new GroupHandler<T, String>() {
      public void process(final Map<String, Set<T>> data) {
        List<String> traces = new ArrayList<String>(data.keySet());
        Collections.sort(traces,
          new Comparator<String>() {
            public int compare(String o1, String o2) {
              return classifier.fun(data.get(o2)) - classifier.fun(data.get(o1));
            }
          });

        int index = 0;
        for (final String s : traces) {
          final Set<T> stackFrameSampleSet = data.get(s);
          model.insertNodeInto(new MergedPathNode<T>(stackFrameSampleSet, model, innermostFirst, level, classifier, scope), root, index++);
        }
      }

      public String getCategory(T sample) {
        int i = innermostFirst ? level : sample.frames.length - 1 - level;
        if (i < 0 || i >= sample.frames.length) return null;
        return sample.frames[i].toString();
      }
    });
  }

  private static <T extends Sample, K> void processSamples(Set<T> sampleSet, GroupHandler<T,  K> dataHandler) {
    final Map<K, Set<T>> stackFrame = new HashMap<K, Set<T>>();

    for(T s:sampleSet) {
      final K frame = dataHandler.getCategory(s);
      if (frame == null) continue;
      Set<T> sets = stackFrame.get(frame);

      if (sets == null) {
        sets = new LinkedHashSet<T>();
        stackFrame.put(frame, sets);
      }
      sets.add(s);
    }

    dataHandler.process(stackFrame);
  }

  private <T extends Sample> void buildPerformanceSamples(final DefaultTreeModel treeModel, final Set<T> profile) {
    final boolean skipSystemStuff = myFilterSystemStuff.isSelected();
    CallTree callTree = new CallTree();
    for(T sample : profile) {
      callTree.addFrames(sample.frames, sample.duration, skipSystemStuff);
    }

    final Pair<Map<FrameInfo, Long>, Map<FrameInfo, Long>> countMaps = callTree.getTimeMaps();
    final Map<FrameInfo, Long> countMap = countMaps.getFirst();
    final Map<FrameInfo, Long> selfCountMap = countMaps.getSecond();

    DefaultMutableTreeNode tracesRoot = (DefaultMutableTreeNode)treeModel.getRoot();
    JTreeUtil.removeChildren(tracesRoot, treeModel);
    fillTreeModelRoot(tracesRoot, callTree, countMap, selfCountMap, true, new FrameInfo[0]);
    treeModel.reload();
  }

  private <T extends Sample> void fillTreeModelRoot(TreeNode node,
                                                    CallTree callTree,
                                                    final Map<FrameInfo, Long> countMap,
                                                    final Map<FrameInfo, Long> selfCountMap,
                                                    boolean backTrace,
                                                    FrameInfo[] frames) {
    final MutableTreeNode root = (MutableTreeNode) node;
    List<FrameInfo> traces = scopeMatcher.fun(new ArrayList<FrameInfo>(countMap.keySet()));

    GlobalSearchScope scope = getCurrentScope();
    int index = 0;
    for (final FrameInfo s : traces) {
      root.insert(new MergedCallNode<T>(new CallInfo(s, countMap.get(s), selfCountMap.get(s)), callTree, frames, backTrace, scope), index++);
    }
  }

  private void resetCpuUsageData() {
    myHotSpotsTreeTable.removeAll();
    myTracesTreeTable.removeAll();
    mySamplesTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(), true));

    data.clearPerformance();
  }

  private void resetMemoryData() {
    myMemoryTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(), true));
    myReachableFromTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(), true));
    data.clearMemory();
  }

  @NotNull
  public JComponent getComponent() {
    return myPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return null;
  }

  private static class MergedPathNode<T extends Sample> extends LazyNode implements NavigatableDataProducer{
    private final Set<T> stackFrameSampleSet;
    private final DefaultTreeModel model;
    private final boolean innermostFirst;
    private final int level;
    private final int percents;
    private final FrameInfo frameInfo;
    private final GlobalSearchScope scope;
    private final Function<Set<T>, Integer> classifier;
    private SampleLocationResolver samplLocationResolver;

    public MergedPathNode(Set<T> stackFrameSampleSet, DefaultTreeModel model, boolean innermostFirst,
                          int level, Function<Set<T>, Integer> classifier, GlobalSearchScope scope) {
      Iterator<T> iterator = stackFrameSampleSet.iterator();
      if(iterator.hasNext()){
        frameInfo = iterator.next().frames[level];
        setUserObject(frameInfo);
      } else {
        frameInfo = null;
      }

      this.scope = scope;
      this.stackFrameSampleSet = stackFrameSampleSet;
      this.model = model;
      this.innermostFirst = innermostFirst;
      this.level = level;
      this.classifier = classifier;

      percents = classifier.fun(stackFrameSampleSet);
    }
    
    public int getPercents(){
      return percents;
    }

    @Override
    protected void doLoadChildren() {
      buildSamples(model, this, innermostFirst, stackFrameSampleSet, level + 1, classifier, scope);
    }

    @Override
    public Navigatable getNavigatable() {
      if (samplLocationResolver == null) {
        samplLocationResolver = new SampleLocationResolver(frameInfo, scope);
      }
      return samplLocationResolver;
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    disposeNonguiResources();
  }

  private boolean disposed;

  @Override
  public void disposeNonguiResources() {
    if (!disposed) {
      if (myProfilingManager != null) myProfilingManager.dispose();
      ActionScriptProfileRunner.removePreloadingOfProfilerSwf();
      disposed = true;
    }
  }

  private static String someShortLocationHint(CreateObjectSample s) {
    if (s.frames == null || s.frames.length == 0) {
      return "";
    }
    String frame = s.frames[0].toString();
    String locationHint = " (" + (frame.length() < 50 ? frame : frame.substring(0, 50) + "...") + ")";
    if (LOG.isDebugEnabled()) {
      locationHint += " #" + s.id;
    }
    return locationHint;
  }

  private class BackRefNode extends LazyNode implements NavigatableDataProducer {
    private final CreateObjectSample s;
    private final DefaultTreeModel treeModel;
    private SampleLocationResolver sampleLocationResolver;

    public BackRefNode(CreateObjectSample s, DefaultTreeModel treeModel) {
      this(s, treeModel, s.className + " " + s.size +" bytes "+ someShortLocationHint(s));
    }


    public BackRefNode(CreateObjectSample s, DefaultTreeModel treeModel, Object userObject) {
      this.s = s;
      this.treeModel = treeModel;
      setUserObject(userObject);
    }

    @Override
    protected void doLoadChildren() {
      int index = 0;
      for(Map.Entry<Integer, Set<Integer>> e : data.getReferenceIds()) {
        if (e.getValue().contains(s.id)) {
          CreateObjectSample s2 = data.getObjects().get(e.getKey());
          treeModel.insertNodeInto(new BackRefNode(s2, treeModel, s2.className + someShortLocationHint(s2)), this, index++);
        }
      }
    }

    public CreateObjectSample getObjectSample() {
      return s;
    }

    @Override
    public Navigatable getNavigatable() {
      if (sampleLocationResolver == null) {
        FrameInfo frameInfo = getObjectSample().frames[0];
        sampleLocationResolver = new SampleLocationResolver(frameInfo, ActionScriptProfileView.this.getCurrentScope());
      }
      return sampleLocationResolver;
    }
  }
}
