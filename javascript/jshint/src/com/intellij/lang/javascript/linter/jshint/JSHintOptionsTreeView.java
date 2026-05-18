package com.intellij.lang.javascript.linter.jshint;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.linter.jshint.JSHintBundle;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.linter.option.OptionTypes;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.profile.codeInspection.ui.DescriptionEditorPane;
import com.intellij.profile.codeInspection.ui.DescriptionEditorPaneKt;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.ClickListener;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ObjectUtils;
import com.intellij.util.concurrency.ThreadingAssertions;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Sergey Simonchik
 */
public class JSHintOptionsTreeView {

  private static final @NonNls String TREE_SCROLL_BAR_HORIZONTAL = "JSHintOptionsTreeView.TREE_SCROLL_BAR_HORIZONTAL";
  private static final @NonNls String TREE_SCROLL_BAR_VERTICAL = "JSHintOptionsTreeView.TREE_SCROLL_BAR_VERTICAL";
  private static final @NonNls String TREE_SELECTED_NODE_ID = "JSHintOptionsTreeView.TREE_SELECTED_NODE_ID";

  private static final float DEFAULT_PROPORTION = 0.7f;

  private DescriptionEditorPane myBrowser;

  private final CheckedTreeNode myRoot = new CheckedTreeNode(null);

  private final JPanel myMainPanel;
  private Splitter myMainSplitter;
  private boolean myDisposed = false;
  private Map<JSHintOption, JSHintTreeNode> myNodeByOptionMap;
  private JSHintOptionsState myPrevFiredOptionsState;
  private boolean mySettingOptionsInProgress;
  private JScrollPane myTreeScrollPane;
  private Tree myTree;

  public JSHintOptionsTreeView(boolean fullModeDialog) {
    myMainPanel = createMainPanel();
    final Dimension preferredSize;
    if (fullModeDialog) {
      preferredSize = JBUI.size(800, 600);
    }
    else {
      preferredSize = JBUI.size(400, 300);
    }
    myMainPanel.setPreferredSize(preferredSize);
  }

  public Component getComponent() {
    return myMainPanel;
  }

  private JPanel createMainPanel() {
    myBrowser = new DescriptionEditorPane();
    JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(myBrowser);
    scrollPane.setBorder(JBUI.Borders.empty());
    JComponent descriptionPanel = scrollPane;

    final JPanel treePanel = new JPanel(new BorderLayout());
    final JScrollPane treeScrollPane = createTreeScrollPane();
    myTreeScrollPane = treeScrollPane;
    treePanel.add(treeScrollPane, BorderLayout.CENTER);

    myMainSplitter = new JBSplitter("JSHintOptionsTreeView.MAIN_DIVIDER_PROPORTION", DEFAULT_PROPORTION);
    myMainSplitter.setDividerWidth(20);
    myMainSplitter.setFirstComponent(treePanel);
    myMainSplitter.setSecondComponent(descriptionPanel);
    myMainSplitter.setHonorComponentsMinimumSize(false);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(myMainSplitter, BorderLayout.CENTER);
    return panel;
  }

  private JScrollPane createTreeScrollPane() {
    JSHintTreeCellRenderer treeCellRenderer = new JSHintTreeCellRenderer();
    final Tree tree = new CheckboxTree(treeCellRenderer, myRoot) {

      private boolean myGroupNodeTriggered = false;

      @Override
      protected void installSpeedSearch() {
        TreeSpeedSearch.installOn(this, false, o -> {
          final JSHintTreeNode node = (JSHintTreeNode) o.getLastPathComponent();
          JSHintOption option = node.getUserDataAsOption();
          if (option != null) {
            return option.getShortDescription() + " " + option.getKey();
          }
          return node.getTitle();
        });
      }

      @Override
      protected void onNodeStateChanged(final CheckedTreeNode ctNode) {
        if (ctNode == myRoot) {
          return;
        }
        JSHintTreeNode node = ObjectUtils.tryCast(ctNode, JSHintTreeNode.class);
        if (node == null) {
          return;
        }
        if (node.getUserDataAsOptionGroup() != null) {
          myGroupNodeTriggered = true;
          SwingUtilities.invokeLater(() -> {
            myGroupNodeTriggered = false;
            onOptionsStateChanged();
          });
        }
        else {
          if (!myGroupNodeTriggered) {
            onOptionsStateChanged();
          }
        }
      }

      @Override
      public void scrollRectToVisible(Rectangle aRect) {
        if (myTreeScrollPane != null) {
          // disallow horizontal scrolling on move up and down
          Point viewPosition = myTreeScrollPane.getViewport().getViewPosition();
          aRect.x = viewPosition.x;
        }
        super.scrollRectToVisible(aRect);
      }
    };
    myTree = tree;

    tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        JSHintTreeNode node = (JSHintTreeNode)e.getPath().getLastPathComponent();
        if (node != null) {
          DescriptionEditorPaneKt.readHTMLWithCodeHighlighting(
            myBrowser,
            node.getDescription(),
            JavaScriptFileType.INSTANCE.getName()
          );
        }
      }
    });

    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    TreeUtil.installActions(tree);
    tree.setBorder(JBUI.Borders.empty());


    tree.setSelectionModel(new DefaultTreeSelectionModel());

    final JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(tree);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    myNodeByOptionMap = addTreeNodes(tree);
    TreeUtil.expandAll(tree);

    TreeNode selectedNode = selectInitialNode(tree, myRoot.getChildAt(0));
    doInitialScrolling(tree, scrollPane, selectedNode);
    installSetLinkSupport(tree, treeCellRenderer);

    return scrollPane;
  }

  private @NotNull TreeNode selectInitialNode(@NotNull Tree tree, @NotNull TreeNode defaultNodeToSelect) {
    int selectedNodeId = StringUtil.parseInt(PropertiesComponent.getInstance().getValue(TREE_SELECTED_NODE_ID), -1);
    TreeNode nodeToSelect = null;
    if (selectedNodeId != -1) {
      nodeToSelect = findNodeByIdRec(myRoot, selectedNodeId, new AtomicInteger(0));
    }
    if (nodeToSelect == null) {
      nodeToSelect = defaultNodeToSelect;
    }
    tree.setSelectionPath(TreeUtil.getPathFromRoot(nodeToSelect));
    return nodeToSelect;
  }

  private void doInitialScrolling(final @NotNull Tree tree, final @NotNull JScrollPane scrollPane, final @NotNull TreeNode selectedNode) {
    final int verticalScrollValue = StringUtil.parseInt(PropertiesComponent.getInstance().getValue(TREE_SCROLL_BAR_VERTICAL), -1);
    final int horizontalScrollValue = StringUtil.parseInt(PropertiesComponent.getInstance().getValue(TREE_SCROLL_BAR_HORIZONTAL), -1);
    final TreePath selectedPath = TreeUtil.getPathFromRoot(selectedNode);

    if (verticalScrollValue != -1 && horizontalScrollValue != -1) {
      scrollPane.getViewport().setViewPosition(new Point(horizontalScrollValue, verticalScrollValue));
      return;
    }
    Rectangle bounds = tree.getPathBounds(selectedPath);
    Rectangle indentBounds = tree.getPathBounds(TreeUtil.getPathFromRoot(myRoot.getChildAt(0)));

    if (bounds != null) {
      if (indentBounds != null) {
        bounds.x = indentBounds.x;
      }
      tree.scrollRectToVisible(bounds);
    }
  }

  private void installSetLinkSupport(final @NotNull Tree tree, final @NotNull JSHintTreeCellRenderer cellRenderer) {
    new ClickListener() {
      @Override
      public boolean onClick(@NotNull MouseEvent e, int clickCount) {
        final JSHintTreeNode node = findNodeOfEditLink(tree, cellRenderer, e.getPoint());
        if (node != null) {
          JSHintOption option = node.getUserDataAsOption();
          if (option != null) {
            node.setMouseInside(false);
            EditValueDialog dialog = new EditValueDialog(myMainPanel, node.getTitle(), option, node.getValue());
            boolean isOK = dialog.showAndGet();
            if (isOK) {
              node.setValue(dialog.getValue());
              onOptionsStateChanged();
            }
          }
        }
        return false;
      }
    }.installOn(tree);

    tree.addMouseMotionListener(new MouseAdapter() {
      private final Cursor myOriginalCursor = tree.getCursor();
      private final Cursor myHandCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
      private JSHintTreeNode myPrevNode = null;
      @Override
      public void mouseMoved(MouseEvent e) {
        JSHintTreeNode node = findNodeOfEditLink(tree, cellRenderer, e.getPoint());
        Cursor newCursor = node == null ? myOriginalCursor : myHandCursor;
        Cursor currentCursor = tree.getCursor();
        if (!currentCursor.equals(newCursor)) {
          tree.setCursor(newCursor);
        }
        if (myPrevNode != null) {
          myPrevNode.setMouseInside(false);
          tree.setToolTipText(null);
        }
        if (node != null) {
          node.setMouseInside(true);
          tree.setToolTipText(JSHintBundle.message("jshint.options.tree.tooltip.set.a.new.value"));
        }
        myPrevNode = node;
      }
    });
  }

  private static @Nullable JSHintTreeNode findNodeOfEditLink(@NotNull Tree tree,
                                                             @NotNull JSHintTreeCellRenderer cellRenderer,
                                                             @NotNull Point mousePoint) {
    int row = tree.getRowForLocation(mousePoint.x, mousePoint.y);
    if (row < 0) {
      return null;
    }
    final Object o = tree.getPathForRow(row).getLastPathComponent();
    final JSHintTreeNode node = ObjectUtils.tryCast(o, JSHintTreeNode.class);
    if (node == null) {
      return null;
    }
    if (!node.isEditLinkNeeded() || !node.isEnabled()) {
      return null;
    }
    Rectangle rowBounds = tree.getRowBounds(row);
    Point relativeToRow = new Point(mousePoint);
    relativeToRow.translate(-rowBounds.x, -rowBounds.y);
    if (cellRenderer.isPointInsideEditLink(node, relativeToRow)) {
      return node;
    }
    return null;
  }

  private void onOptionsStateChanged() {
    if (mySettingOptionsInProgress) {
      JSHintOptionsState newOptionsState = getOptionsState();
      if (!newOptionsState.equals(myPrevFiredOptionsState)) {
        myPrevFiredOptionsState = newOptionsState;
      }
    }
  }

  private static Map<JSHintOption, JSHintTreeNode> addTreeNodes(@NotNull Tree tree) {
    CheckedTreeNode root = (CheckedTreeNode) tree.getModel().getRoot();
    root.removeAllChildren();
    Map<JSHintOption, JSHintTreeNode> map = new HashMap<>();
    Set<JSHintOption> unhandledOptions = EnumSet.allOf(JSHintOption.class);
    JSHintDocumentation documentation = JSHintDocumentation.getInstance();
    for (JSHintOptionGroup group : documentation.getGroups()) {
      JSHintTreeNode titleNode = new JSHintTreeNode(tree, group);
      titleNode.setAllowsChildren(true);
      root.add(titleNode);
      List<JSHintOption> options = new ArrayList<>(group.getOptions());
      options.sort(Comparator.naturalOrder());
      for (JSHintOption option : options) {
        String description = group.getHtmlDescriptionByOption(option);
        description = StringUtil.notNullize(description);
        unhandledOptions.remove(option);
        JSHintTreeNode optionNode = new JSHintTreeNode(tree, option, description);
        optionNode.setAllowsChildren(false);
        map.put(option, optionNode);
        titleNode.add(optionNode);
      }
    }
    for (JSHintOption option : unhandledOptions) {
      String htmlDescription = documentation.getHtmlDescriptionForNonGroupOption(option);
      htmlDescription = StringUtil.notNullize(htmlDescription);
      JSHintTreeNode titleNode = new JSHintTreeNode(tree, option, htmlDescription);
      titleNode.setAllowsChildren(true);
      root.add(titleNode);
      map.put(option, titleNode);
    }
    return map;
  }

  /**
   * Should be called on EDT after all component creations.
   */
  private void applyOptionsState(@NotNull JSHintOptionsState optionsState) {
    for (Map.Entry<JSHintOption, JSHintTreeNode> entry : myNodeByOptionMap.entrySet()) {
      JSHintOption option = entry.getKey();
      JSHintTreeNode node = entry.getValue();
      if (OptionTypes.isBooleanOption(option)) {
        boolean checked = Boolean.TRUE == optionsState.getValue(option);
        node.setChecked(checked);
      }
      else {
        Object value = optionsState.getValue(option);
        node.setValue(value);
      }
    }
  }

  private @NotNull JSHintOptionsState createState() {
    JSHintOptionsState.Builder builder = new JSHintOptionsState.Builder();
    for (Map.Entry<JSHintOption, JSHintTreeNode> entry : myNodeByOptionMap.entrySet()) {
      JSHintOption option = entry.getKey();
      JSHintTreeNode node = entry.getValue();
      if (OptionTypes.isBooleanOption(option)) {
        builder.put(option, node.isChecked());
      }
      else {
        builder.put(option, node.getValue());
      }
    }
    return builder.build();
  }

  public void disposeUI() {
    if (!myDisposed) {
      PropertiesComponent properties = PropertiesComponent.getInstance();
      Point viewPosition = myTreeScrollPane.getViewport().getViewPosition();
      properties.setValue(TREE_SCROLL_BAR_HORIZONTAL, (int)viewPosition.getX(), -1);
      properties.setValue(TREE_SCROLL_BAR_VERTICAL, (int)viewPosition.getY(), -1);
      properties.setValue(TREE_SELECTED_NODE_ID, getSelectedNodeId(), -1);
    }
    myDisposed = true;
  }

  private int getSelectedNodeId() {
    TreePath selectedPath = myTree.getSelectionPath();
    TreeNode selectedNode = ObjectUtils.tryCast(selectedPath.getLastPathComponent(), TreeNode.class);
    if (selectedNode == null) {
      return -1;
    }
    return assignIdToPath(myRoot, selectedNode, new AtomicInteger(0));
  }

  private static TreeNode findNodeByIdRec(@NotNull TreeNode node, int id, @NotNull AtomicInteger size) {
    if (id == size.get()) {
      return node;
    }
    size.incrementAndGet();
    int childCount = node.getChildCount();
    for (int i = 0; i < childCount; i++) {
      TreeNode child = node.getChildAt(i);
      TreeNode found = findNodeByIdRec(child, id, size);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private static int assignIdToPath(@NotNull TreeNode treeNode, @NotNull TreeNode selectedNode, @NotNull AtomicInteger size) {
    if (treeNode == selectedNode) {
      return size.get();
    }
    size.incrementAndGet();
    int childCount = treeNode.getChildCount();
    for (int i = 0; i < childCount; i++) {
      TreeNode child = treeNode.getChildAt(i);
      int found = assignIdToPath(child, selectedNode, size);
      if (found != -1) {
        return found;
      }
    }
    return -1;
  }

  public void setOptionsState(final @NotNull JSHintOptionsState optionsState) {
    ThreadingAssertions.assertEventDispatchThread();
    mySettingOptionsInProgress = false;
    applyOptionsState(optionsState);
    mySettingOptionsInProgress = true;
    onOptionsStateChanged();
  }

  public JSHintOptionsState getOptionsState() {
    ThreadingAssertions.assertEventDispatchThread();
    return createState();
  }

  public void setEnabled(boolean enabled) {
    setEnabledRec(myRoot, enabled);
  }

  private static void setEnabledRec(@NotNull CheckedTreeNode node, boolean enabled) {
    node.setEnabled(enabled);
    int childCount = node.getChildCount();
    for (int i = 0; i < childCount; i++) {
      TreeNode treeNode = node.getChildAt(i);
      if (treeNode instanceof CheckedTreeNode) {
        setEnabledRec((CheckedTreeNode)treeNode, enabled);
      }
    }
  }

}
