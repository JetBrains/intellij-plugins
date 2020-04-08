package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.CapturingProcessRunner;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioBaseConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class PlatformioProjectSettingsStep extends ProjectSettingsStepBase<Ref<String[]>> {

  public static final String GATHERING_INFO = "Gathering info...";
  private final Tree myTree;

  public PlatformioProjectSettingsStep(DirectoryProjectGenerator<Ref<String[]>> projectGenerator,
                                       AbstractNewProjectStep.AbstractCallback<Ref<String[]>> callback) {
    super(projectGenerator, callback);
    myTree = new Tree(new DefaultTreeModel(new DeviceTreeNode(null, DeviceTreeNode.TYPE.ROOT, "")));
    myTree.setCellRenderer(new ColoredTreeCellRenderer() {
      @Override
      public void customizeCellRenderer(@NotNull JTree tree,
                                        Object value,
                                        boolean selected,
                                        boolean expanded,
                                        boolean leaf,
                                        int row,
                                        boolean hasFocus) {
        DeviceTreeNode node = (DeviceTreeNode)value;
        append(node.getName());
        setIcon(node.getType().getIcon());
      }
    });
    myTree.setPaintBusy(true);
    myTree.getEmptyText().setText(GATHERING_INFO);
    myTree.setRootVisible(false);

    myTree.addTreeSelectionListener(e -> {
      TreePath selectionPath = e.getNewLeadSelectionPath();
      String[] cmdParameter = ArrayUtil.EMPTY_STRING_ARRAY;
      if (selectionPath != null) {
        cmdParameter = ((DeviceTreeNode)selectionPath.getLastPathComponent()).getCliKeys();
      }
      userSelected(cmdParameter);
    });
    new TreeSpeedSearch(myTree, DeviceTreeNode::searchText, true);
  }

  @Override
  public boolean isDumbAware() {
    return true;
  }

  @Override
  protected JPanel createAdvancedSettings() {

    JBScrollPane scrollPane = new JBScrollPane(myTree);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    BorderLayoutPanel panel = new BorderLayoutPanel(0, 0)
      .addToTop(new JBLabel("Available Boards & Frameworks:").withBorder(BorderFactory.createEmptyBorder(0, 5, 3, 0)))
      .addToCenter(scrollPane)
      .withPreferredHeight(0)
      .withBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

    try {

      new Task.Backgroundable(null, "PlatformIO Boards List Query") {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          myTree.getEmptyText().setText(GATHERING_INFO);
          String myPioUtility = PlatformioBaseConfiguration.findPlatformio();
          if (myPioUtility == null) {
            setErrorText(PlatformioService.PLATFORMIO_IS_NOT_FOUND);
            myTree.getEmptyText().setText(PlatformioService.PLATFORMIO_IS_NOT_FOUND);
            myTree.getEmptyText().appendSecondaryText(PlatformioService.INSTALL_GUIDE, SimpleTextAttributes.LINK_ATTRIBUTES,
                                                      e -> PlatformioService.openInstallGuide());
            return;
          }
          GeneralCommandLine commandLine = new GeneralCommandLine()
            .withExePath(myPioUtility)
            .withParameters("boards", "--json-output")
            .withWorkDirectory(FileUtil.getTempDirectory())
            .withRedirectErrorStream(true);

          try {
            ProcessOutput output = new CapturingProcessRunner(new CapturingProcessHandler(commandLine))
              .runProcess(indicator, 60000);
            if (output.isTimeout()) {
              setErrorText("Timeout while running PlatformIO utility");
            }
            else if (output.getExitCode() != 0) {
              setErrorText("PlatformIO utility returned exit code " + output.getExitCode());
            }
            else {
              DeviceTreeNode parsedRoot = BoardsJsonParser.parse(output.getStdout());
              ApplicationManager.getApplication().invokeLater(
                () -> {
                  myTree.setModel(new DefaultTreeModel(parsedRoot));
                  myTree.setPaintBusy(false);
                });
            }
          }
          catch (ExecutionException e) {
            setErrorText(e.getMessage());
          }
        }
      }.queue();
    }
    catch (Throwable e) {
      String errorMessage = String.valueOf(ExceptionUtil.getMessage(e));
      setErrorText(errorMessage);
      Notification notification =
        PlatformioService.NOTIFICATION_GROUP.createNotification(
          "PlatformIO utility failed",
          e.getClass().getSimpleName(),
          errorMessage, NotificationType.WARNING);
      Notifications.Bus.notify(notification);
    }
    checkValid();
    return panel;
  }

  private void userSelected(String[] newCmdParameter) {
    getPeer().getSettings().set(newCmdParameter);
    checkValid();
  }

  @Override
  public boolean checkValid() {
    if (!super.checkValid()) return false;
    Ref<String[]> settings = getPeer().getSettings();
    if (settings.isNull() || settings.get().length == 0) {
      setWarningText("Please select target board or framework");
      return false;
    }
    else {
      setErrorText(null);
      return true;
    }
  }
}
