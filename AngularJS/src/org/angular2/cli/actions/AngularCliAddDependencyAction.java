// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.actions;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.CharFilter;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.icons.AllIcons;
import com.intellij.javascript.nodejs.CompletionModuleInfo;
import com.intellij.javascript.nodejs.NodeModuleSearchUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.packageJson.InstalledPackageVersion;
import com.intellij.javascript.nodejs.packageJson.NodeInstalledPackageFinder;
import com.intellij.javascript.nodejs.packageJson.NodePackageBasicInfo;
import com.intellij.javascript.nodejs.packageJson.NpmRegistryService;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.ui.speedSearch.ListWithFilter;
import com.intellij.util.gist.GistManager;
import com.intellij.util.gist.GistManagerImpl;
import com.intellij.util.ui.EmptyIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.angular2.cli.AngularCLIFilter;
import org.angular2.cli.AngularCLIProjectGenerator;
import org.angular2.cli.AngularCliSchematicsRegistryService;
import org.angular2.cli.AngularCliUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class AngularCliAddDependencyAction extends DumbAwareAction {

  private static final NodePackageBasicInfo OTHER =
    new NodePackageBasicInfo("Install package not listed above…", null);
  private static final Logger LOG = Logger.getInstance(AngularCliAddDependencyAction.class);
  private static final long TIMEOUT = 2000;

  public static void runAndShowConsoleLater(@NotNull Project project, @NotNull VirtualFile cli, @NotNull String packageName,
                                            @Nullable String packageVersion, boolean proposeLatestVersionIfNeeded) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      if (project.isDisposed()) {
        return;
      }
      Ref<String> version = new Ref<>(StringUtil.defaultIfEmpty(packageVersion, "latest"));
      boolean proposeLatestVersion = proposeLatestVersionIfNeeded &&
                                     !AngularCliSchematicsRegistryService.getInstance().supportsNgAdd(packageName, version.get(), TIMEOUT);
      ApplicationManager.getApplication().invokeLater(
        () -> {
          if (proposeLatestVersion) {
            //noinspection DialogTitleCapitalization
            switch (Messages.showDialog(
              project,
              "It looks like specified version of package doesn't support 'ng add'.\n\nWould you like to install the latest version of the package?",
              "Install with 'ng add'",
              new String[]{"Install latest version", "Try with current version", Messages.CANCEL_BUTTON},
              0, Messages.getQuestionIcon())) {

              case 0:
                version.set("latest");
                break;
              case 1:
                version.set(packageVersion);
                break;
              default:
                return;
            }
          }
          runAndShowConsole(project, cli, packageName + "@" + version.get(), !proposeLatestVersion);
        }, project.getDisposed());
    });
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) {
      return;
    }

    final VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
    final VirtualFile cli = AngularCliUtil.findAngularCliFolder(project, file);
    final VirtualFile packageJson = PackageJsonUtil.findChildPackageJsonFile(cli);
    if (cli == null || packageJson == null) {
      return;
    }
    if (!AngularCliUtil.hasAngularCLIPackageInstalled(project, cli)) {
      AngularCliUtil.notifyAngularCliNotInstalled(project, cli, "Can't add new Angular dependency");
      return;
    }

    Set<String> existingPackages = PackageJsonUtil.getOrCreateData(packageJson).getAllDependencies();

    SortedListModel<NodePackageBasicInfo> model = new SortedListModel<>(
      Comparator.comparing((NodePackageBasicInfo p) -> p == OTHER ? 1 : 0)
        .thenComparing(NodePackageBasicInfo::getName)
    );
    JBList<NodePackageBasicInfo> list = new JBList<>(model);
    list.setCellRenderer(new ColoredListCellRenderer<NodePackageBasicInfo>() {
      @Override
      protected void customizeCellRenderer(@NotNull JList<? extends NodePackageBasicInfo> list,
                                           NodePackageBasicInfo value,
                                           int index,
                                           boolean selected,
                                           boolean hasFocus) {
        if (!selected && index % 2 == 0) {
          setBackground(UIUtil.getDecoratedRowColor());
        }
        setIcon(JBUI.scale(EmptyIcon.create(5)));
        append(value.getName(), value != OTHER ? SimpleTextAttributes.REGULAR_ATTRIBUTES : SimpleTextAttributes.LINK_ATTRIBUTES, true);
        if (value.getDescription() != null) {
          append(" - " + value.getDescription(), SimpleTextAttributes.GRAY_ATTRIBUTES, false);
        }
      }
    });

    JScrollPane scroll = ScrollPaneFactory.createScrollPane(list);
    scroll.setBorder(JBUI.Borders.empty());
    JComponent pane = ListWithFilter.wrap(list, scroll, NodePackageBasicInfo::getName);

    ComponentPopupBuilder builder = JBPopupFactory
      .getInstance()
      .createComponentPopupBuilder(pane, list)
      .setMayBeParent(true)
      .setRequestFocus(true)
      .setFocusable(true)
      .setFocusOwners(new Component[]{list})
      .setLocateWithinScreenBounds(true)
      .setCancelOnOtherWindowOpen(true)
      .setMovable(true)
      .setResizable(true)
      .setCancelOnWindowDeactivation(false)
      .setTitle("Install with 'ng add'")
      .setCancelOnClickOutside(true)
      .setDimensionServiceKey(project, "org.angular.cli.generate", true)
      .setMinSize(new Dimension(JBUI.scale(350), JBUI.scale(300)))
      .setCancelButton(new IconButton("Close", AllIcons.Actions.Close, AllIcons.Actions.CloseHovered));

    JBPopup popup = builder.createPopup();

    Consumer<NodePackageBasicInfo> action = pkgInfo -> {
      popup.closeOk(null);
      if (pkgInfo == OTHER) {
        chooseCustomPackageAndInstall(project, cli, existingPackages);
      }
      else {
        runAndShowConsole(project, cli, pkgInfo.getName(), false);
      }
    };
    list.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (list.getSelectedValue() == null) return;
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
          action.accept(list.getSelectedValue());
        }
      }
    });
    new DoubleClickListener() {
      @Override
      public boolean onDoubleClick(MouseEvent event) {
        if (list.getSelectedValue() == null) return true;
        action.accept(list.getSelectedValue());
        return true;
      }
    }.installOn(list);
    popup.showCenteredInCurrentWindow(project);
    updateListAsync(list, model, popup, existingPackages);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    final VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
    e.getPresentation().setEnabledAndVisible(
      project != null && AngularCliUtil.findAngularCliFolder(project, file) != null);
  }

  private static void runAndShowConsole(@NotNull Project project, @NotNull VirtualFile cli,
                                        @NotNull String packageSpec, boolean proposeLatestVersionIfNeeded) {
    if (project.isDisposed()) {
      return;
    }
    NodeJsInterpreter interpreter = NodeJsInterpreterManager.getInstance(project).getInterpreter();
    NodeJsLocalInterpreter node = NodeJsLocalInterpreter.tryCast(interpreter);
    try {
      if (node == null) {
        throw new ExecutionException("Cannot find local node interpreter.");
      }

      List<CompletionModuleInfo> modules = new ArrayList<>();
      NodeModuleSearchUtil.findModulesWithName(modules, AngularCLIProjectGenerator.PACKAGE_NAME, cli,
                                               false, node);
      if (modules.isEmpty() || modules.get(0).getVirtualFile() == null) {
        throw new ExecutionException("Angular CLI package is not installed.");
      }
      CompletionModuleInfo module = modules.get(0);
      ProcessHandler handler = NpmPackageProjectGenerator.generate(
        node, new NodePackage(Objects.requireNonNull(module.getVirtualFile()).getPath()),
        pkg -> Objects.requireNonNull(pkg.findBinFile()).getAbsolutePath(),
        cli, VfsUtilCore.virtualToIoFile(cli),
        project, () -> ((GistManagerImpl)GistManager.getInstance()).invalidateData(),
        "Installing " + packageSpec + " for " + cli.getName(),
        new Filter[]{new AngularCLIFilter(project, cli.getPath())},
        "add", packageSpec);
      if (proposeLatestVersionIfNeeded) {
        handler.addProcessListener(new ProcessAdapter() {
          @Override
          public void processTerminated(@NotNull ProcessEvent event) {
            if (event.getExitCode() != 0) {
              installLatestIfFeasible(project, cli, packageSpec);
            }
          }
        });
      }
    }
    catch (Exception e) {
      LOG.error("Failed to execute `ng add`: " + e.getMessage(), e);
    }
  }

  private static void installLatestIfFeasible(@NotNull Project project, @NotNull VirtualFile cli,
                                              @NotNull String packageSpec) {
    if (project.isDisposed()) {
      return;
    }
    VirtualFile packageJson = PackageJsonUtil.findChildPackageJsonFile(cli);
    if (packageJson == null) {
      return;
    }
    NodeInstalledPackageFinder finder = new NodeInstalledPackageFinder(project, packageJson);
    int index = packageSpec.lastIndexOf('@');
    String packageName = index <= 0 ? packageSpec : packageSpec.substring(0, index);
    InstalledPackageVersion pkg = finder.findInstalledPackage(packageName);
    if (pkg == null) {
      return;
    }
    if (!AngularCliSchematicsRegistryService.getInstance().supportsNgAdd(pkg)) {
      ApplicationManager.getApplication().invokeLater(
        () -> {
          //noinspection DialogTitleCapitalization
          if (Messages.OK == Messages.showDialog(
            project,
            "It looks like installed version of package doesn't support 'ng add'.\n\nWould you like to install the latest version of the package instead?",
            "Install with 'ng add'",
            new String[]{"Install latest version", Messages.CANCEL_BUTTON},
            0, Messages.getQuestionIcon())) {

            runAndShowConsole(project, cli, packageName + "@latest", false);
          }
        }, project.getDisposed()
      );
    }
  }

  private static void chooseCustomPackageAndInstall(@NotNull Project project,
                                                    @NotNull VirtualFile cli,
                                                    @NotNull Set<String> existingPackages) {
    SelectCustomPackageDialog dialog = new SelectCustomPackageDialog(project, existingPackages);
    if (dialog.showAndGet()) {
      runAndShowConsole(project, cli, dialog.getPackage(), false);
    }
  }

  private static void updateListAsync(JBList<NodePackageBasicInfo> list,
                                      SortedListModel<NodePackageBasicInfo> model,
                                      JBPopup popup, Set<String> existingPackages) {
    list.setPaintBusy(true);
    model.clear();
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      if (popup.isDisposed()) {
        return;
      }
      Collection<NodePackageBasicInfo> packages = AngularCliSchematicsRegistryService
        .getInstance()
        .getPackagesSupportingNgAdd(20000);
      ApplicationManager.getApplication().invokeLater(() -> {
        packages.forEach(pkg -> {
          if (!existingPackages.contains(pkg.getName())) {
            model.add(pkg);
          }
        });
        model.add(OTHER);
        list.setPaintBusy(false);
      }, o -> popup.isDisposed());
    });
  }

  private static class SelectCustomPackageDialog extends DialogWrapper {

    private final Set<String> myExistingPackages;
    private final Project myProject;
    private EditorTextField myTextEditor;

    SelectCustomPackageDialog(@NotNull Project project, @NotNull Set<String> existingPackages) {
      super(project);
      myProject = project;
      myExistingPackages = existingPackages;
      //noinspection DialogTitleCapitalization
      setTitle("Install with 'ng add'");
      init();
      getOKAction().putValue(Action.NAME, "Install");
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
      return myTextEditor;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
      JPanel panel = new JPanel(new BorderLayout(0, 4));
      myTextEditor = new TextFieldWithAutoCompletion<>(
        myProject, new NodePackagesCompletionProvider(myExistingPackages), false, null);
      myTextEditor.setPreferredWidth(250);
      panel.add(LabeledComponent.create(myTextEditor, "Package name", BorderLayout.NORTH));
      return panel;
    }

    public String getPackage() {
      return myTextEditor.getText();
    }
  }

  private static class NodePackagesCompletionProvider extends TextFieldWithAutoCompletionListProvider<NodePackageBasicInfo> {

    private final Set<String> myExistingPackages;

    protected NodePackagesCompletionProvider(@NotNull Set<String> existingPackages) {
      super(Collections.emptyList());
      myExistingPackages = existingPackages;
    }

    @NotNull
    @Override
    protected String getLookupString(@NotNull NodePackageBasicInfo item) {
      return item.getName();
    }

    @Nullable
    @Override
    public CharFilter.Result acceptChar(char c) {
      return c == '@' || c == '/' ? CharFilter.Result.ADD_TO_PREFIX : null;
    }

    @NotNull
    @Override
    public CompletionResultSet applyPrefixMatcher(@NotNull CompletionResultSet result, @NotNull String prefix) {
      CompletionResultSet res = super.applyPrefixMatcher(result, prefix);
      res.restartCompletionOnAnyPrefixChange();
      return res;
    }

    @NotNull
    @Override
    public Collection<NodePackageBasicInfo> getItems(String prefix, boolean cached, CompletionParameters parameters) {
      if (cached) {
        return Collections.emptyList();
      }
      List<NodePackageBasicInfo> result = new ArrayList<>();
      try {
        NpmRegistryService.getInstance().findPackages(
          ProgressManager.getInstance().getProgressIndicator(),
          NpmRegistryService.namePrefixSearch(prefix), 20, pkg -> true,
          pkg -> {
            if (!myExistingPackages.contains(pkg.getName())) {
              result.add(pkg);
            }
          });
      }
      catch (IOException e) {
        LOG.info(e);
      }
      return result;
    }

    @NotNull
    @Override
    public LookupElementBuilder createLookupBuilder(@NotNull NodePackageBasicInfo item) {
      return super.createLookupBuilder(item)
        .withTailText(item.getDescription() != null ? "  " + item.getDescription() : null, true);
    }
  }
}
