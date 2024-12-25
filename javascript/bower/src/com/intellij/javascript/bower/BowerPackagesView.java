package com.intellij.javascript.bower;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StartupUiUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.webcore.packaging.PackagesNotificationPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BowerPackagesView {

  private static final Logger LOG = Logger.getInstance(BowerPackagesView.class);

  private final Project myProject;
  private final BowerInstalledPackagesPanel myPackagesPanel;
  private final PackagesNotificationPanel myPackagesNotificationPanel;
  private final JPanel myComponent;
  private BowerSettings myCurrentSettings;

  public BowerPackagesView(@NotNull Project project) {
    myProject = project;
    JPanel panel = new JPanel(new BorderLayout(0, 0));
    panel.setBorder(IdeBorderFactory.createTitledBorder(BowerBundle.message("bower.packages.view.dependencies"), false, JBUI.insetsTop(8)).setShowLine(false));
    myPackagesNotificationPanel = new PackagesNotificationPanel();
    myPackagesPanel = new BowerInstalledPackagesPanel(project, myPackagesNotificationPanel);
    panel.add(myPackagesPanel, BorderLayout.CENTER);
    panel.add(myPackagesNotificationPanel.getComponent(), BorderLayout.SOUTH);
    myComponent = panel;
  }

  public @NotNull JComponent getComponent() {
    return myComponent;
  }

  public void onSettingsChanged(@NotNull BowerSettings settings, @NotNull List<BowerValidationInfo> errors) {
    if (settings.equals(myCurrentSettings)) {
      return;
    }
    myCurrentSettings = settings;
    BowerPackagingService service = null;
    myPackagesNotificationPanel.hide();
    myPackagesNotificationPanel.removeAllLinkHandlers();
    if (errors.isEmpty()) {
      service = new BowerPackagingService(myProject, settings);
      checkVersion(settings);
    }
    else {
      showErrors(errors);
    }
    myPackagesPanel.updatePackages(service);
  }

  private void showErrors(@NotNull List<BowerValidationInfo> errors) {
    List<String> errorHtmlDescriptions = ContainerUtil.map(errors, info -> info.getErrorHtmlDescription());

    String styleTag = UIUtil.getCssFontDeclaration(StartupUiUtil.getLabelFont());
    String html = HtmlChunk.html().children(HtmlChunk.raw(styleTag), HtmlChunk.body().child(
      HtmlChunk.div().style("padding-left:4px").addRaw(
        StringUtil.join(errorHtmlDescriptions, HtmlChunk.div().style("padding-top:2px").toString())
      )
    )).toString();
    for (final BowerValidationInfo error : errors) {
      String linkText = error.getLinkText();
      final Component component = error.getComponent();
      if (linkText != null && component != null) {
        myPackagesNotificationPanel.addLinkHandler(linkText, () -> IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(component, true)));
      }
    }
    myPackagesNotificationPanel.showError(html, null, null);
  }

  private void checkVersion(final @NotNull BowerSettings settings) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      try {
        ProcessOutput output = BowerCommandLineUtil.runBowerCommand(null, settings, "--version");
        SemVer semVer = SemVer.parseFromText(output.getStdout().trim());
        if (semVer != null && semVer.getMajor() < 1) {
          UIUtil.invokeLaterIfNeeded(
            () -> myPackagesNotificationPanel.showError(
              BowerBundle.message("bower.required.version.notification.content"), null, null));
        }
      }
      catch (ExecutionException e) {
        LOG.warn("Cannot fetch bower version", e);
      }
    });
  }
}
