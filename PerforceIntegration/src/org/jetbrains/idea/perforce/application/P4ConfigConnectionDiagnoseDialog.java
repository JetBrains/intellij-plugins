package org.jetbrains.idea.perforce.application;

import com.intellij.CommonBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.*;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.connections.P4ConfigHelper;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.P4ConnectionParameters;
import org.jetbrains.idea.perforce.perforce.connections.PerforceMultipleConnections;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.*;

public class P4ConfigConnectionDiagnoseDialog extends DialogWrapper {
  private PerforceMultipleConnections myMultipleConnections;
  private P4RootsInformation myChecker;
  private Tree myTree;
  private BaseNode myRoot;
  private final Project myProject;
  private final ConnectionDiagnoseRefresher myRefresher;
  private DialogWrapper.DialogWrapperAction myRefreshAction;

  public P4ConfigConnectionDiagnoseDialog(Project project, ConnectionDiagnoseRefresher refresher) {
    super(project, true);
    myProject = project;
    myRefresher = refresher;
    setTitle(PerforceBundle.message("config.dialog.title"));
    setCancelButtonText(CommonBundle.message("close.action.name"));

    init();
  }

  @Override
  protected Action @NotNull [] createActions() {
    myRefreshAction = new DialogWrapperAction(CommonBundle.message("action.refresh")) {
      @Override
      protected void doAction(ActionEvent e) {
        myTree.setPaintBusy(true);
        myTree.setEnabled(false);

        final boolean complete = ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> myRefresher.refresh(),
                                                                                                   PerforceBundle.message("config.refresh"), true,
                                                                                                   myProject);
        if (complete) {
          fillTree();
        }
        myTree.setPaintBusy(false);
        myTree.setEnabled(true);
      }
    };
    return new Action[]{myRefreshAction, getCancelAction()};
  }

  // will be called on refresh
  private void fillTree() {
    myRoot.removeAllChildren();
    myMultipleConnections = myRefresher.getMultipleConnections();
    myChecker = myRefresher.getP4RootsInformation();

    final DefaultTreeModel model = (DefaultTreeModel)myTree.getModel();
    final TreeMap<VirtualFile,P4ConnectionParameters> map = myMultipleConnections.getParametersMap();
    final P4ConnectionParameters defaultParameters = myMultipleConnections.getDefaultParameters();
    final Map<VirtualFile, File> configsMap = myMultipleConnections.getConfigsMap();

    Set<String> p4ConfigNames = new LinkedHashSet<>();

    int i = 0;
    boolean containNoConfigs = false;
    for (Map.Entry<VirtualFile, P4ConnectionParameters> entry : map.entrySet()) {
      final VirtualFile root = entry.getKey();
      final BaseNode fileNode = new BaseNode(root, NodeType.root);
      model.insertNodeInto(fileNode, myRoot, i);
      /*myRoot.add(fileNode);
      fileNode.setParent(myRoot);*/

      final File configDir = configsMap.get(root);
      putConfigDir(fileNode, configDir, entry.getValue().isNoConfigFound());

      final P4ConnectionParameters parameters = entry.getValue();
      containNoConfigs |= parameters.isNoConfigFound();
      ContainerUtil.addIfNotNull(p4ConfigNames, parameters.getConfigFileName());
      if (! putConfigLines(defaultParameters, fileNode, configDir, parameters)) {
        addGenericErrors(fileNode, parameters);
        continue;
      }
      if (! checkConnection(root, fileNode)) {
        addGenericErrors(fileNode, parameters);
        continue;
      }
      boolean someErrors = addGenericErrors(fileNode, parameters);
      someErrors |= addRootError(Objects.requireNonNull(myMultipleConnections.getConnection(root)), fileNode);
      if (!someErrors) {
        addNode(fileNode, new BaseNode(CommonBundle.message("button.ok"), NodeType.ok));
      }
      ++i;
    }

    String envP4Config = P4ConfigHelper.getP4ConfigFileName();
    if (envP4Config != null) {
      addNode(myRoot,
              new BaseNode(PerforceBundle.message("connection.env", envP4Config), NodeType.info));
    }

    // common errors
    addGenericErrors(myRoot, defaultParameters);
    if (containNoConfigs) {
      if (p4ConfigNames.isEmpty()) {
        ContainerUtil.addIfNotNull(p4ConfigNames, envP4Config);
      }
      if (!p4ConfigNames.isEmpty()) {
        addNode(myRoot,
                new BaseNode(PerforceBundle.message("connection.config.file", StringUtil.join(p4ConfigNames, ", ")), NodeType.info));
      }
    }

    model.nodeStructureChanged(myRoot);
    TreeUtil.expand(myTree, 3);
  }

  private static boolean addGenericErrors(final BaseNode fileNode, final P4ConnectionParameters parameters) {
    boolean somethingAdded = false;
    final Throwable exception = parameters.getException();
    if (exception != null) {
      addNode(fileNode, new ErrorNode(exception.getMessage()));
      somethingAdded = true;
    }
    final List<String> warnings = parameters.getWarnings();
    for (String warning : warnings) {
      addNode(fileNode, new ErrorNode(warning));
      somethingAdded = true;
    }
    return somethingAdded;
  }

  private static void addNode(BaseNode parent, BaseNode child) {
    parent.add(child);
    child.setParent(parent);
  }

  private boolean addRootError(@NotNull final P4Connection connection, final BaseNode fileNode) {
    boolean somethingAdded = false;
    if (myChecker.getNotAuthorized().contains(connection)) {
      addNode(fileNode, new ErrorNode(PerforceBundle.message("login.not.logged.in")));
      somethingAdded = true;
    }
    final Collection<VcsException> exceptions = myChecker.getErrors().get(connection);
    for (VcsException exception : exceptions) {
      final ErrorNode node = new ErrorNode(exception.getMessage());
      addNode(fileNode, node);
      somethingAdded = true;
    }
    final PerforceClientRootsChecker.WrongRoots wrongRoots = myChecker.getMap().get(connection);
    if (wrongRoots != null) {
      final ErrorNode node = new ErrorNode(PerforceBundle.message("config.wrong.client.spec"));
      addNode(fileNode, node);
      somethingAdded = true;
      final List<ErrorNode> children = new ArrayList<>();
      children.add(new ErrorNode(PerforceBundle.message("config.client.roots") + ' '));
      for (String clientRoot : wrongRoots.getActualInClientSpec()) {
        children.add(new ErrorNode(clientRoot));
      }
      children.add(new ErrorNode(PerforceBundle.message("config.actual.root") + ' '));
      for (VirtualFile vf : wrongRoots.getWrong()) {
        children.add(new ErrorNode(vf.getPath()));
      }

      for (ErrorNode child : children) {
        addNode(node, child);
      }
    }
    return somethingAdded;
  }

  private boolean checkConnection(VirtualFile root, BaseNode fileNode) {
    final P4Connection connection = myMultipleConnections.getConnection(root);
    if (connection == null) {
      final ErrorNode node = new ErrorNode(PerforceBundle.message("connection.cannot.create"));
      addNode(fileNode, node);
      return false;
    } else {
      if (! connection.isConnected()) {
        final ErrorNode node = new ErrorNode(PerforceBundle.message("connection.not.connected"));
        addNode(fileNode, node);
        return false;
      }
    }
    return true;
  }

  private void putConfigDir(BaseNode fileNode, File configDir, boolean noConfigFound) {
    String p4ConfigValue = myMultipleConnections.getP4ConfigValue();
    if (configDir != null && !noConfigFound && p4ConfigValue != null) {
      final File file = new File(FileUtil.toSystemDependentName(configDir.getPath()), p4ConfigValue);
      addNode(fileNode, new BaseNode(file, NodeType.p4configFile));
    } else {
      addNode(fileNode, new BaseNode(PerforceBundle.message("connection.no.config.file"), NodeType.noConfig));
    }
  }

  private static boolean putConfigLines(P4ConnectionParameters defaultParameters,
                                        BaseNode fileNode,
                                        File configDir, P4ConnectionParameters parameters) {
    final Map<String, ConfigLine> dataPresentation = getDataPresentation(parameters, defaultParameters, configDir == null);
    if (dataPresentation.isEmpty()) {
      addNode(fileNode, new ErrorNode(PerforceBundle.message("connection.no.params")));
      return false;
    } else {
      for (ConfigLine line : dataPresentation.values()) {
        addNode(fileNode, new BaseNode(line, NodeType.configLine));
      }

      boolean requiredParametersDefined = true;

      if (parameters.getServer() == null && defaultParameters.getServer() == null) {
        addNode(fileNode, new ErrorNode(PerforceBundle.message("error.server.unknown.env")));
        requiredParametersDefined = false;
      }

      if (parameters.getUser() == null && defaultParameters.getUser() == null) {
        addNode(fileNode, new ErrorNode(PerforceBundle.message("error.user.unknown.env")));
        requiredParametersDefined = false;
      }

      if (parameters.getClient() == null && defaultParameters.getClient() == null) {
        addNode(fileNode, new ErrorNode(PerforceBundle.message("error.client.unknown.env")));
        requiredParametersDefined = false;
      }
      return requiredParametersDefined;
    }
  }

  public static Map<String, ConfigLine> getDataPresentation(final P4ConnectionParameters p,
                                                            final P4ConnectionParameters d,
                                                            final boolean noConfig) {
    final Map<String, ConfigLine> result = new HashMap<>();
    if (noConfig) {
      putParametersIntoTheMap(d, result, "");
    } else {
      putParametersIntoTheMap(d, result, "environment");
      putParametersIntoTheMap(p, result, "config");
    }
    return result;
  }

  private static void putParametersIntoTheMap(final P4ConnectionParameters p, final Map<String, ConfigLine> map, final String source) {
    addIfNotNull(p.getServer(), "P4PORT = ", map, source, 0);
    addIfNotNull(p.getClient(), "P4CLIENT = ", map, source, 1);
    addIfNotNull(p.getUser(), "P4USER = ", map, source, 2);
    if (p.getPassword() != null) {
      map.put("3Password:", new ConfigLine(source, "P4PASSWD = ", "********"));
    }
    addIfNotNull(p.getCharset(), "P4CHARSET = ", map, source, 4);
    addIfNotNull(p.getIgnoreFileName(), "P4IGNORE = ", map, source, 5);
  }

  private static void addIfNotNull(final String value, final String text, final Map<String, ConfigLine> map, final String source, int i) {
    if (value != null) {
      map.put(i + text, new ConfigLine(source, text, value));
    }
  }

  private static final class ConfigLine {
    private final String mySource;
    private final String myParam;
    private final String myValue;

    private ConfigLine(String source, String param, String value) {
      mySource = source;
      myParam = param;
      myValue = value;
    }

    public @NlsSafe String getSource() {
      return mySource;
    }

    public @NlsSafe String getParam() {
      return myParam;
    }

    public @NlsSafe String getValue() {
      return myValue;
    }

    @Override
    public String toString() {
      return myParam + myValue;
    }
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myTree;
  }

  @Override
  protected String getDimensionServiceKey() {
    return getClass().getName();
  }

  @Override
  protected JComponent createCenterPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    myRoot = new BaseNode("", NodeType.veryRoot);
    myTree = new Tree(myRoot);
    new TreeSpeedSearch(myTree);
    TreeUtil.installActions(myTree);
    myTree.setRootVisible(false);
    myTree.setShowsRootHandles(false);
    myTree.setCellRenderer(new MyRenderer());
    fillTree();
    panel.add(ScrollPaneFactory.createScrollPane(myTree), BorderLayout.CENTER);
    return panel;
  }

  private static final class ErrorNode extends BaseNode {

    private ErrorNode(String text) {
      super(text, NodeType.problem);
    }

    @Override
    public String toString() {
      return (String) getUserObject();
    }
  }

  private static class BaseNode extends DefaultMutableTreeNode {
    private final NodeType myNodeType;
    protected BaseNode(Object userObject, NodeType nodeType) {
      super(userObject);
      myNodeType = nodeType;
    }

    public NodeType getNodeType() {
      return myNodeType;
    }

    @Override
    public String toString() {
      if (NodeType.root.equals(myNodeType)) {
        final VirtualFile vf = (VirtualFile)getUserObject();
        return vf.getName() + ' ' + getParentPath(vf);
      }
      if (NodeType.p4configFile.equals(myNodeType)) {
        final File file = (File)getUserObject();
        return PerforceBundle.message("config.file", file.getName()) + ' ' + getParentPath(file);
      }
      return getUserObject().toString();
    }
  }

  private enum NodeType {
    veryRoot,
    root,
    p4configFile,
    configLine,
    problem,
    string,
    ok,
    info,
    noConfig
  }

  private static class MyRenderer extends ColoredTreeCellRenderer {

    public static final SimpleTextAttributes GREEN = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GREEN.darker(), null);

    @Override
    public void customizeCellRenderer(@NotNull JTree tree,
                                      Object value,
                                      boolean selected,
                                      boolean expanded,
                                      boolean leaf,
                                      int row,
                                      boolean hasFocus) {
      if (! (value instanceof BaseNode baseNode)) return;
      final NodeType type = baseNode.getNodeType();
      final Object uo = baseNode.getUserObject();

      if (NodeType.root.equals(type)) {
        setIcon(PlatformIcons.FOLDER_ICON);
        final VirtualFile root = (VirtualFile)uo;
        append(root.getName() + ' ');
        final String fragment;
        fragment = getParentPath(root);
        append(fragment, SimpleTextAttributes.GRAY_ATTRIBUTES);
      } else if (NodeType.p4configFile.equals(type)) {
        final File configFile = (File) uo;
        setIcon(AllIcons.General.Settings);
        append(PerforceBundle.message("config.file", configFile.getName()) + ' ', SimpleTextAttributes.DARK_TEXT);
        final String parentPath = getParentPath(configFile);
        append(parentPath, SimpleTextAttributes.GRAY_ATTRIBUTES);
      } else if (NodeType.noConfig.equals(type)) {
        final String text = (String) uo;
        setIcon(AllIcons.General.Settings);
        append(text, SimpleTextAttributes.DARK_TEXT);
      } else if (NodeType.string.equals(type)) {
        append((String)uo);
      } else if (NodeType.configLine.equals(type)) {
        final ConfigLine cl = (ConfigLine) uo;
        append(cl.getParam());
        append(cl.getValue());
        final String source = cl.getSource();
        if (! StringUtil.isEmptyOrSpaces(source)) {
          append(" (" + source + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
      } else if (NodeType.problem.equals(type)) {
        assert baseNode instanceof ErrorNode;
        append((String)uo, SimpleTextAttributes.ERROR_ATTRIBUTES);
        /*final ErrorNode errorNode = (ErrorNode) baseNode;
        if (ErrorType.notEnoughParameters.equals(errorNode.myErrorType)) {
          //5
        } else if (ErrorType.parameters.equals(errorNode.myErrorType)) {
          //6
        } else if (ErrorType.connection.equals(errorNode.myErrorType)) {
          //7
        } else if (ErrorType.wrongClient.equals(errorNode.myErrorType)) {
          //8
        } else {
          assert true;
        }*/
      } else if (NodeType.ok.equals(type)) {
        append((String)uo, GREEN);
      } else if (NodeType.info.equals(type)) {
        setIcon(AllIcons.General.Information);
        append((String)uo);
      } else {
        assert true;
      }
    }
  }

  private static @NlsSafe String getParentPath(File configFile) {
    final String parent = configFile.getParent().replace('\\', '/');
    return " (" + parent + ")";
  }

  private static @NlsSafe String getParentPath(VirtualFile root) {
    String fragment;
    final VirtualFile parent = root.getParent();
    if (parent != null) {
      fragment = "(" + parent.getPath() + ")";
    } else {
      fragment = "(" + root.getPath() + ")";
    }
    return fragment;
  }
}
