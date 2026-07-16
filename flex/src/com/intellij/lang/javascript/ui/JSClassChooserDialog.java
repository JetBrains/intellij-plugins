package com.intellij.lang.javascript.ui;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.projectView.impl.AbstractProjectTreeStructure;
import com.intellij.ide.projectView.impl.ProjectAbstractTreeStructureBase;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.ide.util.gotoByName.ChooseByNamePanel;
import com.intellij.ide.util.gotoByName.ChooseByNamePopupComponent;
import com.intellij.ide.util.gotoByName.GotoClassModel2;
import com.intellij.ide.util.treeView.AlphaComparator;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.javascript.flex.index.ActionScriptElementFinder;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ex.IdeFocusTraversalPolicy;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchScopeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TabbedPaneWrapper;
import com.intellij.ui.TreeUIHelper;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class JSClassChooserDialog extends DialogWrapper {

  private final @NotNull Project myProject;
  private final @NotNull GlobalSearchScope mySearchScope;
  private Tree myTree;

  private @Nullable JSClass myInitialClass;
  private StructureTreeModel<? extends ProjectAbstractTreeStructureBase> myModel;
  private ChooseByNamePanel myGotoByNamePanel;
  private TabbedPaneWrapper myTabbedPane;

  private JSClass mySelectedClass;
  private boolean myResult;
  private final @Nullable Condition<? super JSClass> myClassFilter;

  public JSClassChooserDialog(@NotNull Project project,
                              @NotNull @NlsContexts.DialogTitle String title,
                              @NotNull GlobalSearchScope scope,
                              @Nullable JSClass initialClass,
                              @Nullable Condition<? super JSClass> classFilter) {
    super(project, true);

    myProject = project;
    mySearchScope = scope;
    myInitialClass = initialClass;
    myClassFilter = classFilter;
    setTitle(title);

    init();

    if (myInitialClass != null) {
      selectElementInTree(myInitialClass);
    }

    handleSelectionChanged();
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myGotoByNamePanel.getPreferredFocusedComponent();
  }

  public JSClass getSelectedClass() {
    return mySelectedClass;
  }

  public boolean showDialog() {
    myResult = false;
    show();
    return myResult;
  }

  private @Nullable JSClass calcSelectedClass() {
    if (myTabbedPane.getSelectedIndex() == 0) {
      //if goto by name is active
      return (JSClass)myGotoByNamePanel.getChosenElement();
    }
    else {
      //if project view is enabled

      final TreePath path = myTree.getSelectionPath();
      if (path == null) {
        return null;
      }
      final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
      final Object userObject = node.getUserObject();
      if (userObject instanceof PsiFileNode) {
        final PsiFile file = ((PsiFileNode)userObject).getValue();
        if (file instanceof JSFile) {
          return JSPsiImplUtils.findClass((JSFile)file);
        }
        else if (file instanceof XmlFile) {
          return XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)file);
        }
      }
    }
    return null;
  }

  private JSClass getContext() {
    return myInitialClass;
  }

  private ModalityState getModalityState() {
    return ModalityState.stateForComponent(getRootPane());
  }

  private void handleSelectionChanged() {
    JSClass selection = calcSelectedClass();
    setOKActionEnabled(selection != null && isAccepted(selection));
  }

  private boolean isAccepted(final @NotNull JSClass clazz) {
    return !ActionScriptResolveUtil.hasExcludeClassMetadata(clazz) &&
           PsiSearchScopeUtil.isInScope(mySearchScope, clazz) &&
           (myClassFilter == null || myClassFilter.value(clazz));
  }

  //select element in project view
  private void selectElementInTree(@NotNull PsiElement element) {
    ReadAction.nonBlocking(() -> element.getContainingFile())
      .coalesceBy(element, mySearchScope, this)
      .expireWith(getDisposable())
      .finishOnUiThread(getModalityState(), psiFile -> {
        if (myModel != null) {
          myModel.select(psiFile, myTree, path -> {
          });
        }
      }).submit(AppExecutorUtil.getAppExecutorService());
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    ProjectAbstractTreeStructureBase treeStructure = new AbstractProjectTreeStructure(myProject) {
      @Override
      public boolean isHideEmptyMiddlePackages() {
        return true;
      }

      @Override
      public boolean isShowModules() {
        return false;
      }
    };

    myModel = new StructureTreeModel<>(treeStructure, getDisposable());
    myModel.setComparator(AlphaComparator.getInstance());
    myTree = new Tree(new AsyncTreeModel(myModel, getDisposable()));
    myTree.setRootVisible(false);
    myTree.setShowsRootHandles(true);
    myTree.expandRow(0);
    myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    myTree.setCellRenderer(new NodeRenderer());

    final JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(myTree);
    scrollPane.setPreferredSize(JBUI.size(500, 300));

    myTree.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (KeyEvent.VK_ENTER == e.getKeyCode()) {
          doOKAction();
        }
      }
    });

    new DoubleClickListener() {
      @Override
      protected boolean onDoubleClick(@NotNull MouseEvent e) {
        TreePath path = myTree.getPathForLocation(e.getX(), e.getY());
        if (path != null && myTree.isPathSelected(path)) {
          JSClass jsClass = calcSelectedClass();
          if (jsClass != null && isAccepted(jsClass)) {
            doOKAction();
            return true;
          }
        }
        return false;
      }
    }.installOn(myTree);

    myTree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        handleSelectionChanged();
      }
    });

    TreeUIHelper.getInstance().installTreeSpeedSearch(myTree);
    final JPanel dummyPanel = new JPanel(new BorderLayout());

    String name = null;
    myGotoByNamePanel = new MyChooseByNamePanel(name, dummyPanel);

    myTabbedPane = new TabbedPaneWrapper(getDisposable());
    myTabbedPane.addTab(IdeBundle.message("tab.chooser.search.by.name"), dummyPanel);
    myTabbedPane.addTab(IdeBundle.message("tab.chooser.project"), scrollPane);

    myGotoByNamePanel.invoke(new MyCallback(), getModalityState(), false);

    myTabbedPane.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        handleSelectionChanged();
      }
    });

    return myTabbedPane.getComponent();
  }

  private ChooseByNameModel createChooseByNameModel() {
    return new MyGotoClassModel(myProject);
  }

  @Override
  protected void doOKAction() {
    myResult = true;
    mySelectedClass = calcSelectedClass();
    if (mySelectedClass == null) {
      return;
    }
    super.doOKAction();
  }

  @Override
  protected String getDimensionServiceKey() {
    return "#com.intellij.ide.util.TreeClassChooserDialog";
  }

  private class MyCallback extends ChooseByNamePopupComponent.Callback {
    @Override
    public void elementChosen(Object element) {
      mySelectedClass = (JSClass)element;
      close(OK_EXIT_CODE);
    }
  }

  private class MyChooseByNamePanel extends ChooseByNamePanel {
    private final JPanel dummyPanel;

    MyChooseByNamePanel(final String name, final JPanel dummyPanel) {
      super(JSClassChooserDialog.this.myProject, createChooseByNameModel(), name, mySearchScope.isSearchInLibraries(), getContext());
      this.dummyPanel = dummyPanel;
    }

    @Override
    protected void showTextFieldPanel() {
    }

    @Override
    protected void close(boolean isOk) {
      super.close(isOk);

      if (isOk) {
        doOKAction();
      }
      else {
        doCancelAction();
      }
    }

    @Override
    protected void initUI(ChooseByNamePopupComponent.Callback callback, ModalityState modalityState, boolean allowMultipleSelection) {
      super.initUI(callback, modalityState, allowMultipleSelection);
      dummyPanel.add(myGotoByNamePanel.getPanel(), BorderLayout.CENTER);
      IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance()
        .requestFocus(IdeFocusTraversalPolicy.getPreferredFocusedComponent(myGotoByNamePanel.getPanel()), true));
    }

    @Override
    protected void showList() {
      super.showList();
      if (myInitialClass != null && myList.getModel().getSize() > 0) {
        myList.setSelectedValue(myInitialClass, true);
        myInitialClass = null;
      }
    }

    @Override
    protected void chosenElementMightChange() {
      handleSelectionChanged();
    }
  }

  private class MyGotoClassModel extends GotoClassModel2 {
    MyGotoClassModel(@NotNull Project project) {
      super(project);
    }

    @Override
    public Object @NotNull [] getElementsByName(@NotNull String name, @NotNull FindSymbolParameters parameters, @NotNull ProgressIndicator canceled) {
      final Collection<JSQualifiedNamedElement> elements = ActionScriptElementFinder.findElementsByName(name, myProject, parameters.getSearchScope(), false);

      final List<JSClass> list = new ArrayList<>();
      for (JSQualifiedNamedElement element : elements) {
        if (element instanceof JSClass && isAccepted(((JSClass)element))) {
          list.add((JSClass)element);
        }
      }
      return list.toArray(JSClass.EMPTY_ARRAY);
    }

    @Override
    public @Nullable String getPromptText() {
      return null;
    }
  }
}
