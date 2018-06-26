package org.angularjs.cli.actions;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.CharFilter;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.filters.Filter;
import com.intellij.icons.AllIcons;
import com.intellij.javascript.nodejs.CompletionModuleInfo;
import com.intellij.javascript.nodejs.NodeModuleSearchUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.packageJson.NodePackageBasicInfo;
import com.intellij.javascript.nodejs.packageJson.NpmRegistryService;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
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
import org.angularjs.cli.AngularCLIFilter;
import org.angularjs.cli.AngularCLIProjectGenerator;
import org.angularjs.cli.AngularCliSchematicsRegistryService;
import org.angularjs.cli.BlueprintsLoaderKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class AngularCliAddDependencyAction extends DumbAwareAction {

  private static final NodePackageBasicInfo OTHER =
    new NodePackageBasicInfo("other...", "Specify other compatible package not listed above");
  private static final Logger LOG = Logger.getInstance(AngularCliAddDependencyAction.class);

  public static void runAndShowConsole(@NotNull Project project, @NotNull VirtualFile cli, @NotNull String packageSpec) {
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
      NpmPackageProjectGenerator.generate(node, new NodePackage(Objects.requireNonNull(module.getVirtualFile()).getPath()),
                                          pkg -> Objects.requireNonNull(pkg.findBinFile()).getAbsolutePath(), cli,
                                          VfsUtilCore.virtualToIoFile(cli),
                                          project,
                                          () -> ((GistManagerImpl)GistManager.getInstance()).invalidateData(),
                                          "Adding dependency " + packageSpec + " to " + cli.getName(),
                                          new Filter[]{new AngularCLIFilter(project, cli.getPath())},
                                          "add", packageSpec);
    }
    catch (Exception e) {
      LOG.error("Failed to execute `ng add`: " + e.getMessage(), e);
    }
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) {
      return;
    }

    final VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
    final VirtualFile cli = BlueprintsLoaderKt.findAngularCliFolder(project, file);
    if (cli == null) {
      return;
    }

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
        append(value.getName(), value != OTHER ? SimpleTextAttributes.REGULAR_ATTRIBUTES : SimpleTextAttributes.SYNTHETIC_ATTRIBUTES, true);
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
      .setMinSize(new Dimension(JBUI.scale(200), JBUI.scale(200)))
      .setCancelButton(new IconButton("Close", AllIcons.Actions.Close, AllIcons.Actions.CloseHovered));

    JBPopup popup = builder.createPopup();

    Consumer<NodePackageBasicInfo> action = pkgInfo -> {
      popup.closeOk(null);
      if (pkgInfo == OTHER) {
        chooseCustomPackageAndInstall(project, cli);
      }
      else {
        runAndShowConsole(project, cli, pkgInfo.getName());
      }
    };
    list.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (list.getSelectedValue() == null) return;
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
          action.accept(list.getSelectedValue());
        }
      }
    });
    new DoubleClickListener() {
      public boolean onDoubleClick(MouseEvent event) {
        if (list.getSelectedValue() == null) return true;
        action.accept(list.getSelectedValue());
        return true;
      }
    }.installOn(list);
    popup.showCenteredInCurrentWindow(project);
    updateListAsync(list, model, popup);
  }

  private static void chooseCustomPackageAndInstall(Project project, VirtualFile cli) {
    SelectCustomPackageDialog dialog = new SelectCustomPackageDialog(project);
    if (dialog.showAndGet()) {
      runAndShowConsole(project, cli, dialog.getPackage());
    }
  }

  private static void updateListAsync(JBList<NodePackageBasicInfo> list,
                                      SortedListModel<NodePackageBasicInfo> model,
                                      JBPopup popup) {
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
        if (popup.isDisposed()) {
          return;
        }
        packages.forEach(model::add);
        model.add(OTHER);
        list.setPaintBusy(false);
      });
    });
  }

  @Override
  public void update(AnActionEvent e) {
    if (e == null) {
      return;
    }
    final Project project = e.getProject();
    final VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
    e.getPresentation().setEnabledAndVisible(
      project != null && BlueprintsLoaderKt.findAngularCliFolder(project, file) != null);
  }

  private static class SelectCustomPackageDialog extends DialogWrapper {

    private final Project myProject;
    private EditorTextField myTextEditor;

    public SelectCustomPackageDialog(Project project) {
      super(project);
      myProject = project;
      setTitle("Add Angular Dependency");
      init();
      getOKAction().putValue(Action.NAME, "Add");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
      JPanel panel = new JPanel(new BorderLayout(0, 4));
      myTextEditor = new TextFieldWithAutoCompletion<>(myProject, new NodePackagesCompletionProvider(), false, null);
      myTextEditor.setPreferredWidth(250);
      panel.add(LabeledComponent.create(myTextEditor, "Enter package name to install with 'ng add'", BorderLayout.NORTH));
      return panel;
    }

    public String getPackage() {
      return myTextEditor.getText();
    }
  }

  private static class NodePackagesCompletionProvider extends TextFieldWithAutoCompletionListProvider<NodePackageBasicInfo> {

    protected NodePackagesCompletionProvider() {
      super(Collections.emptyList());
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
        NpmRegistryService.getInstance().findPackages(ProgressManager.getInstance().getProgressIndicator(),
                                                      NpmRegistryService.namePrefixSearch(prefix), 20, pkg -> true, result::add);
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
