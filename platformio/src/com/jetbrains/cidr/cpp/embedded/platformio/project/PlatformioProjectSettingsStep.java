package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.CapturingProcessRunner;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioBaseConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class PlatformioProjectSettingsStep extends ProjectSettingsStepBase<Ref<BoardInfo>> {
  private final Tree myTree;

  public PlatformioProjectSettingsStep(DirectoryProjectGenerator<Ref<BoardInfo>> projectGenerator,
                                       AbstractNewProjectStep.AbstractCallback<Ref<BoardInfo>> callback) {
    super(projectGenerator, callback);
    myTree = new Tree(new DefaultTreeModel(new DeviceTreeNode(null, DeviceTreeNode.TYPE.ROOT, "", BoardInfo.EMPTY)));
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
    myTree.getEmptyText().setText(ClionEmbeddedPlatformioBundle.message("gathering.info"));
    myTree.setRootVisible(false);

    myTree.addTreeSelectionListener(e -> {
      TreePath selectionPath = e.getNewLeadSelectionPath();
      BoardInfo boardInfo;
      if (selectionPath != null) {
        boardInfo = ((DeviceTreeNode)selectionPath.getLastPathComponent()).getBoardInfo();
      }
      else {
        boardInfo = BoardInfo.EMPTY;
      }
      userSelected(boardInfo);
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
      .addToTop(new JBLabel(
        ClionEmbeddedPlatformioBundle.message("available.boards.frameworks")).withBorder(BorderFactory.createEmptyBorder(0, 5, 3, 0)))
      .addToCenter(scrollPane)
      .withPreferredHeight(0)
      .withBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

    try {

      new Task.Backgroundable(null, ClionEmbeddedPlatformioBundle.message("platformio.boards.list.query")) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          myTree.getEmptyText().setText(ClionEmbeddedPlatformioBundle.message("gathering.info"));
          String myPioUtility = PlatformioBaseConfiguration.findPlatformio();
          if (myPioUtility == null) {
            String platformioIsNotFound = ClionEmbeddedPlatformioBundle.message("platformio.utility.is.not.found");
            setErrorText(platformioIsNotFound);
            myTree.getEmptyText().setText(platformioIsNotFound);
            myTree.getEmptyText()
              .appendLine(ClionEmbeddedPlatformioBundle.message("open.settings.link"), SimpleTextAttributes.LINK_ATTRIBUTES,
                          e -> PlatformioService.openSettings(getProject()))
              .appendLine(ClionEmbeddedPlatformioBundle.message("install.guide"), SimpleTextAttributes.LINK_ATTRIBUTES,
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
              setErrorText(ClionEmbeddedPlatformioBundle.message("utility.timeout"));
            }
            else if (output.getExitCode() != 0) {
              setErrorText(ClionEmbeddedPlatformioBundle.message("platformio.exit.code", output.getExitCode()));
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
      @NlsSafe String errorMessage = String.valueOf(ExceptionUtil.getMessage(e));
      setErrorText(errorMessage);
      @NlsSafe String className = e.getClass().getSimpleName();
      PlatformioService.NOTIFICATION_GROUP
        .createNotification(ClionEmbeddedPlatformioBundle.message("platformio.utility.failed"), errorMessage, NotificationType.WARNING)
        .setSubtitle(className)
        .notify(null);
    }
    checkValid();
    return panel;
  }

  private void userSelected(@NotNull BoardInfo boardInfo) {
    getPeer().getSettings().set(boardInfo);
    checkValid();
  }

  @Override
  public boolean checkValid() {
    if (!super.checkValid()) return false;
    BoardInfo settings = getPeer().getSettings().get();
    if (settings.getParameters().length == 0) {
      setWarningText(ClionEmbeddedPlatformioBundle.message("please.select.target"));
      return false;
    }
    else {
      setErrorText(null);
      return true;
    }
  }
}
