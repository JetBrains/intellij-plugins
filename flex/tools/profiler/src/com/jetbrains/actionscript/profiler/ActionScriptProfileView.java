package com.jetbrains.actionscript.profiler;

import com.intellij.ide.util.scopeChooser.ScopeChooserCombo;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.Alarm;
import com.intellij.util.Function;
import com.jetbrains.profiler.ProfileView;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.EOFException;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim
 * Date: 02.09.2010
 * Time: 13:52:59
 */
public class ActionScriptProfileView extends ProfileView {
  private JPanel myPanel;
  private JTree mySamplesTree;
  private JButton myStartCpuButton;
  private JButton myStopCpuButton;

  private JTree myMemoryTree;
  private JTree myReachableFromTree;
  private JButton myDoGcButton;
  private JLabel myStatus;
  private JButton myCaptureSnapshot;
  private JTabbedPane myTabbedPane;
  private JTree myHotSpotsTree;
  private JCheckBox myFilterSystemStuff;
  private JTree myTraces;
  private ScopeChooserCombo filterScope;
  private JLabel scopeLabel;
  private final ProfilingManager myProfilingManager;
  public static Key<ProfilingManager> ourProfilingManagerKey = Key.create("profiler.manager.key");
  private boolean myEOFReached;
  static final int MEMORY_TAB_INDEX = 2;
  static final int CPU_HOTSPOTS_TAB_INDEX = 1;
  private Alarm myAlarm;

  private void createUIComponents() {
    myHotSpotsTree = new JTree() { // TODO: real speed search
      @Override
      public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof MergedCallNode) {
          final SampleLocationResolver.LocationInfo locationInfo = SampleLocationResolver.buildMethodInfo(fixUserObjectStringForNode(value));
          return locationInfo.name != null? locationInfo.name:locationInfo.clazz;
        }
        return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
      }
    };
    myHotSpotsTree.setCellRenderer(new DefaultTreeCellRenderer() {
      @Override
      public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof MergedCallNode) {
          value = ((MergedCallNode) value).getUserObject();
        }
        return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      }
    });

    myAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);

    myHotSpotsTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        final TreePath path = e.getPath();
        final Object lastPathComponent = path.getLastPathComponent();
        myAlarm.cancelAllRequests();
        if (!(lastPathComponent instanceof MergedCallNode)) return;
        final MergedCallNode mergedCallNode = (MergedCallNode) lastPathComponent;

        myAlarm.addRequest(new Runnable() {
          public void run() {
            final DefaultTreeModel treeModel = new DefaultTreeModel(
              new DefaultMutableTreeNode()
            );
            myTraces.setModel(treeModel);

            treeModel.setRoot(new MergedCallNode(
              fixUserObjectStringForNode(lastPathComponent),
              mergedCallNode.countMap,
              mergedCallNode.profile,
              treeModel,
              false
            ));
          }
        }, 500);
      }
    });

    filterScope = new ScopeChooserCombo(getProject(), true, false, "Project Files");
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

  static class ProfileData {
    private final Set<Sample> profile = new LinkedHashSet<Sample>();
    private final Map<Integer, CreateObjectSample> objects = new HashMap<Integer, CreateObjectSample>();
    private int allocated;
    private Map<Integer, Set<Integer>> references = new LinkedHashMap<Integer, Set<Integer>>(50);
  }

  private ProfileData data = new ProfileData();

  public ActionScriptProfileView(VirtualFile file, Project project) {
    super(file, project);

    myProfilingManager = file.getUserData(ourProfilingManagerKey);

    myMemoryTree.setRootVisible(false);
    mySamplesTree.setRootVisible(false);
    myHotSpotsTree.setRootVisible(false);
    myReachableFromTree.setRootVisible(false);
    myTraces.setRootVisible(false);

    scopeLabel.setLabelFor(filterScope.getComboBox());

    resetMemoryData();

    myFilterSystemStuff.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        buildSamples2((DefaultTreeModel) myHotSpotsTree.getModel(), data.profile);
      }
    });


    filterScope.getComboBox().addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        myAlarm.cancelAllRequests();
        myAlarm.addRequest(new Runnable() {
          public void run() {
            buildSamples2((DefaultTreeModel) myHotSpotsTree.getModel(), data.profile);
          }
        }, 100);
      }
    });

    final KeyListener k = new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        final int code = e.getKeyCode();
        if (code != KeyEvent.VK_ENTER && code != KeyEvent.VK_F4) return;
        Object source = e.getSource();
        if (source instanceof JTree) {
          JTree j = ((JTree) source);
          final TreePath pathForLocation = j.getSelectionPath();

          navigateToPath(pathForLocation);
        }
      }
    };

    final MouseListener m = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (!e.isControlDown()) return;
        Object source = e.getSource();
        if (source instanceof JTree) {
          JTree j = ((JTree) source);
          final TreePath pathForLocation = j.getPathForLocation(e.getX(), e.getY());

          navigateToPath(pathForLocation);
        }
      }
    };

    JTree [] treesToInstallNavigation = {myMemoryTree, mySamplesTree, myHotSpotsTree, myTraces, myReachableFromTree};
    for(JTree tree:treesToInstallNavigation) {
      tree.addKeyListener(k);
      tree.addMouseListener(m);
    }

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
                for(final CreateObjectSample s:data.objects.values()) {
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
            data.allocated += createObjectSample.size;
            data.objects.put(createObjectSample.id, createObjectSample);
            return;
          } else if (sample instanceof DeleteObjectSample) {
            DeleteObjectSample deleteObjectSample = (DeleteObjectSample) sample;

            final CreateObjectSample objectSample = data.objects.remove(deleteObjectSample.id);
            int size = deleteObjectSample.size;
            if (objectSample != null) { // already collected items
              if (size != objectSample.size) {
                size = objectSample.size;
              }
            }

            data.allocated -= size;

            return;
          }

          data.profile.add(sample);
        }

        public void referenced(int pid, int id) {
          Set<Integer> integers = data.references.get(pid);
          if (integers == null) {
            integers = new LinkedHashSet<Integer>(3);
            data.references.put(pid, integers);
          }
          integers.add(id);
        }
      }, new ProfilingManager.Callback() {
        public void finished(String data, IOException ex) {
          if (data != null) {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                myStatus.setText("Connected with profiling agent");
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

        myStatus.setText("About to capture memory snapshot");
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
                myStatus.setText("Captured memory snapshot");
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

  private void navigateToPath(TreePath pathForLocation) {
    if (pathForLocation != null) {
      final Object lastPathComponent = pathForLocation.getLastPathComponent();

      if (lastPathComponent instanceof MergedPathNode ||
          lastPathComponent instanceof MergedCallNode
         ) {
        new SampleLocationResolver(fixUserObjectStringForNode(lastPathComponent), getProject()).navigate();
      } else if (lastPathComponent instanceof BackRefNode) {
        CreateObjectSample objectSample = ((BackRefNode) lastPathComponent).getObjectSample();
        new SampleLocationResolver(objectSample.frames[0],getProject()).navigate();
      }
    }
  }

  private String fixUserObjectStringForNode(Object lastPathComponent) {
    String path = lastPathComponent.toString();
    if (path.endsWith("%") || lastPathComponent instanceof MergedCallNode) {
      path = path.substring(0, path.lastIndexOf(' ')); // skip % // TODO
    }
    return path;
  }

  private void reportProblem(final IOException ex) {
    if (ex != null) {
      if (!(ex instanceof EOFException) && !myEOFReached) Logging.log(ex);

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

          Notifications.Bus.notify(new Notification("ASProfiler", "IOProblem", ex.getLocalizedMessage(), NotificationType.ERROR));
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
                  myStatus.setText("Stopped sampling");
                  switchToState(State.NONE);
                  resetDependents(true);
                } else {
                  myStatus.setText("Started sampling");
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
        myStatus.setText("About to stop profiling");
        myProfilingManager.stopCpuProfiling(callback);
      } else {
        myStatus.setText("About to start profiling");
        myProfilingManager.startCpuProfiling(callback);
      }
    }
  }

  private void resetDependents(boolean b) {
    mySamplesTree.setEnabled(b);
    myMemoryTree.setEnabled(b);
    myHotSpotsTree.setEnabled(b);
  }

  private void dumpCurrentData(final ProfileData data) {
    TreeModel treeModel = mySamplesTree.getModel();
    final Function<Set<Sample>, Integer> sizeFunction = new Function<Set<Sample>, Integer>() {
      public Integer fun(Set<Sample> samples) {
        return (samples.size() * 100) / data.profile.size();
      }
    };
    buildSamples((DefaultTreeModel) treeModel, (DefaultMutableTreeNode) treeModel.getRoot(), false, data.profile, 0, sizeFunction);
    buildSamples2((DefaultTreeModel) myHotSpotsTree.getModel(), data.profile);
  }

  private void dumpMemory(final ProfileData data) {
    final Map<String, Set<CreateObjectSample>> objectsByClasses = new HashMap<String, Set<CreateObjectSample>>();

    final ArrayList<Set<CreateObjectSample>> list = new ArrayList<Set<CreateObjectSample>>();
    for(CreateObjectSample x:data.objects.values()) {
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
          buildSamples(memoryModel, this, true, s, 0, function);
        }
      };
      classNode.setUserObject(userObject);
      memoryModel.insertNodeInto(classNode, memoryModelRoot, index2++);
    }
  }

  private int calcSize(Set<CreateObjectSample> samples) {
    int total = 0;
    for (CreateObjectSample c : samples) total += c.size;
    return total;
  }

  private int percent(int total, ProfileData data) {
    if (data.allocated == 0) return 0;
    return (int)(((long)total * 100) / data.allocated);
  }

  interface GroupHandler<T extends Sample, K> {
    void process(Map<K,  Set<T>> data);
    @Nullable K getCategory(T sample);
  }

  private static <T extends Sample> void buildSamples(final DefaultTreeModel model, final DefaultMutableTreeNode root, final boolean innermostFirst,
                            Set<T> sampleSet, final int level, final Function<Set<T>, Integer> classifier) {
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
          model.insertNodeInto(new MergedPathNode(s, stackFrameSampleSet, model, innermostFirst, level, classifier), root, index++);
        }
      }

      public String getCategory(T sample) {
        int i = innermostFirst ? level : sample.frames.length - 1 - level;
        if (i < 0 || i >= sample.frames.length) return null;
        return sample.frames[i];
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

  private <T extends Sample> void buildSamples2(final DefaultTreeModel model, final Set<T> profile) {
    final Map<String, Integer> countMap = new THashMap<String, Integer>();
    final boolean skipSystemStuff = myFilterSystemStuff.isSelected();

    processSamples(profile, new GroupHandler<T, String>() {
      int level;

      public void process(Map<String, Set<T>> data) {
        if (data.size() == 0) return;
        for (String s : data.keySet()) {
          if (skipSystemStuff && s.startsWith("[")) continue;
          String path = stripCallDelims(s);
          final Integer integer = countMap.get(path);
          int d = data.get(s).size();
          countMap.put(path, integer != null ? integer + d : d);
        }
        ++level;
        processSamples(profile, this);
        --level;
      }

      public String getCategory(T sample) {
        if (level >= sample.frames.length) return null;
        return sample.frames[level];
      }
    });

    List<String> traces = new ArrayList<String>(countMap.keySet());
    Collections.sort(traces, new Comparator<String>() {
      public int compare(String o1, String o2) {
        return countMap.get(o2) - countMap.get(o1);
      }
    });

    final MutableTreeNode root = (MutableTreeNode) model.getRoot();
    int index = 0;

    final SearchScope _selectedScope = filterScope.getSelectedScope();
    GlobalSearchScope scope = _selectedScope instanceof GlobalSearchScope ?
      (GlobalSearchScope) _selectedScope:GlobalSearchScope.allScope(getProject());

    for (final String s : traces) {
      SampleLocationResolver.LocationInfo l = SampleLocationResolver.buildMethodInfo(s);
      final PsiElement classByQName = JSResolveUtil.findClassByQName(l.clazz, scope);
      if (classByQName == null) continue;
      model.insertNodeInto(new MergedCallNode<T>(s, countMap, profile, model, true), root, index++);
    }
  }

  private static String stripCallDelims(String s) {
    final int endIndex = s.indexOf('(');
    return endIndex != -1 ? s.substring(0, endIndex) : s;
  }

  private static boolean referencesFrame(String _s, String s) {
    return _s.startsWith(s) && s.length() < _s.length() && _s.charAt(s.length()) == '(';
  }

  private void resetCpuUsageData() {
    mySamplesTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(), true));
    myHotSpotsTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(), true));
    myTraces.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(), true));

    Iterator<Sample> i = data.profile.iterator();
    while(i.hasNext()) {
      Sample next = i.next();
      if (!(next instanceof CreateObjectSample) && !(next instanceof DeleteObjectSample)) {
        i.remove();
      }
    }
  }

  private void resetMemoryData() {
    myMemoryTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(), true));
    myReachableFromTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(), true));
    data.allocated = 0;
    data.objects.clear();
    data.references.clear();

    Iterator<Sample> i = data.profile.iterator();
    while(i.hasNext()) {
      Sample next = i.next();
      if (next instanceof CreateObjectSample || next instanceof DeleteObjectSample) {
        i.remove();
      }
    }
  }

  @NotNull
  public JComponent getComponent() {
    return myPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return null;
  }

  private static class MergedPathNode<T extends Sample> extends LazyNode {
    private final Set<T> stackFrameSampleSet;
    private final DefaultTreeModel model;
    private final boolean innermostFirst;
    private final int level;
    private Function<Set<T>, Integer> classifier;

    public MergedPathNode(String s, Set<T> stackFrameSampleSet, DefaultTreeModel model, boolean innermostFirst,
                          int level, Function<Set<T>, Integer> classifier) {
      setUserObject(s + " " + classifier.fun(stackFrameSampleSet) + "%");
      this.stackFrameSampleSet = stackFrameSampleSet;
      this.model = model;
      this.innermostFirst = innermostFirst;
      this.level = level;
      this.classifier = classifier;
    }

    @Override
    protected void doLoadChildren() {
      buildSamples(model, this, innermostFirst, stackFrameSampleSet, level + 1, classifier);
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

  private class MergedCallNode<T extends Sample> extends LazyNode {
    private final String frame;
    private final Set<T> profile;
    private final Map<String, Integer> countMap;
    private boolean callers;
    private final DefaultTreeModel model;

    public MergedCallNode(String frame, Map<String, Integer> _countMap, Set<T> profile,
                          DefaultTreeModel model, boolean callers) {
      countMap = _countMap;
      this.callers = callers;
      setUserObject(frame + " " + countMap.get(frame));
      this.frame = frame;
      this.profile = profile;
      this.model = model;
    }

    @Override
    protected void doLoadChildren() {
      processSamples(profile, new GroupHandler<T, String>() {
        public void process(Map<String, Set<T>> data) {
          processSamples(data.get(data.keySet().iterator().next()), new GroupHandler<T, String>() {
            public void process(final Map<String, Set<T>> data) {
              List<String> traces = new ArrayList<String>(data.keySet());
              Collections.sort(traces, new Comparator<String>() {
                public int compare(String o1, String o2) {
                  return data.get(o2).size() - data.get(o1).size();
                }
              });

              int index = 0;
              for (String i : traces) {
                model.insertNodeInto(
                  new MergedCallNode<T>(
                    stripCallDelims(i),
                    countMap,
                    data.get(i),
                    model,
                    callers
                  ),
                  MergedCallNode.this,
                  index++
                );
              }
            }

            final boolean skipSystemStuff = myFilterSystemStuff.isSelected();

            public String getCategory(T sample) {
              final String[] frames = sample.frames;
              for (int i = frames.length - 1; i >= 0; --i) {
                if (referencesFrame(frames[i], frame)) {
                  String nextFrame;
                  if (callers) {
                    nextFrame = i + 1 < frames.length ? frames[i + 1]:null;
                  } else {
                    nextFrame = i > 0 ? frames[i - 1]:null;
                  }
                  if (nextFrame != null) {
                    if (!skipSystemStuff || !nextFrame.startsWith("[")) return nextFrame;
                  }
                  break;
                }
              }
              return null;
            }
          });
        }

        public String getCategory(T sample) {
          for (String _s : sample.frames) {
            if (referencesFrame(_s, frame)) {
              return _s;
            }
          }
          return null;
        }
      });
    }
  }

  private static String someShortLocationHint(CreateObjectSample s) {
    String frame = s.frames[0];
    String locationHint = " (" + (frame.length() < 50 ? frame : frame.substring(0, 50) + "...") + ")";
    if (Logging.is_debug) {
      locationHint += " #" + s.id;
    }
    return locationHint;
  }

  private class BackRefNode extends LazyNode {
    private final CreateObjectSample s;
    private final DefaultTreeModel treeModel;

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
      for(Map.Entry<Integer, Set<Integer>> e : data.references.entrySet()) {
        if (e.getValue().contains(s.id)) {
          CreateObjectSample s2 = data.objects.get(e.getKey());
          treeModel.insertNodeInto(new BackRefNode(s2, treeModel, s2.className + someShortLocationHint(s2)), this, index++);
        }
      }
    }

    public CreateObjectSample getObjectSample() {
      return s;
    }
  }
}
