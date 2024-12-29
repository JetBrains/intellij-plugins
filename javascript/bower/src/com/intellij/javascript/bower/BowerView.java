package com.intellij.javascript.bower;

import com.intellij.ide.IdeBundle;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageField;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.util.concurrency.ThreadingAssertions;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.SwingHelper;
import com.intellij.util.ui.UIUtil;
import com.intellij.webcore.ui.PathShortener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class BowerView {
  private final Project myProject;
  private final JPanel myComponent;
  private final NodePackageField myBowerPackageField;
  private final TextFieldWithBrowseButton myBowerJsonField;
  private final BowerPackagesView myPackagesView;
  private final Color myNormalForeground;
  private volatile boolean myAllowUpdates = true;

  public BowerView(@NotNull Project project) {
    myProject = project;
    myBowerPackageField = new NodePackageField(project, BowerSettingsManager.BOWER_PACKAGE_NAME,
                                               () -> NodeJsInterpreterManager.getInstance(myProject).getInterpreter());
    myBowerJsonField = createBowerJsonField(project);
    myPackagesView = new BowerPackagesView(project);
    myNormalForeground = myBowerJsonField.getChildComponent().getForeground();
    //noinspection DialogTitleCapitalization
    JPanel panel = FormBuilder.createFormBuilder()
      .setAlignLabelOnRight(true)
      .addLabeledComponent(BowerBundle.message("bower.package"), myBowerPackageField)
      .addLabeledComponent(BowerBundle.message("bower.json"), myBowerJsonField)
      .getPanel();

    myBowerPackageField.addSelectionListener(pkg -> updateLaterIfAllowed());
    listenForChanges(myBowerJsonField.getChildComponent());

    myComponent = createResult(panel, myPackagesView.getComponent());
  }

  private static JPanel createResult(@NotNull JComponent top, @NotNull JComponent bottom) {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(top, new GridBagConstraints(
      0, 0,
      1, 1,
      1.0, 0.0,
      GridBagConstraints.NORTHWEST,
      GridBagConstraints.HORIZONTAL,
      JBInsets.emptyInsets(),
      0, 0
    ));
    panel.add(bottom, new GridBagConstraints(
      0, 1,
      1, 1,
      1.0, 1.0,
      GridBagConstraints.NORTHWEST,
      GridBagConstraints.BOTH,
      JBInsets.emptyInsets(),
      0, 0
    ));
    return panel;
  }

  private void listenForChanges(@NotNull JTextComponent textComponent) {
    textComponent.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        updateLaterIfAllowed();
      }
    });
  }

  private void updateLaterIfAllowed() {
    if (myAllowUpdates) {
      updateLater();
    }
  }

  private void updateLater() {
    UIUtil.invokeLaterIfNeeded(() -> update());
  }

  private void update() {
    ThreadingAssertions.assertEventDispatchThread();
    BowerSettings.Builder builder = new BowerSettings.Builder(myProject);
    builder.setBowerPackage(myBowerPackageField.getSelected());
    builder.setBowerJsonPath(PathShortener.getAbsolutePath(myBowerJsonField.getTextField()));
    List<BowerValidationInfo> validationInfos = validate();
    myPackagesView.onSettingsChanged(builder.build(), validationInfos);
  }

  private @NotNull List<BowerValidationInfo> validate() {
    List<BowerValidationInfo> infos = new ArrayList<>();
    processValidationInfo(infos, null, validateBowerPackage());
    JTextField bowerJsonTextField = myBowerJsonField.getTextField();
    BowerValidationInfo bowerJsonInfo = validateFilePathField(bowerJsonTextField,
                                                              PathShortener.getAbsolutePath(bowerJsonTextField),
                                                              "bower.json");
    processValidationInfo(infos, bowerJsonTextField, bowerJsonInfo);
    return infos;
  }

  private void processValidationInfo(@NotNull List<BowerValidationInfo> infos,
                                     @Nullable Component component,
                                     @Nullable BowerValidationInfo info) {
    if (info != null) {
      infos.add(info);
    }
    if (component instanceof JTextComponent) {
      component.setForeground(info != null ? JBColor.RED : myNormalForeground);
    }
  }

  private @Nullable BowerValidationInfo validateBowerPackage() {
    NodePackage selected = myBowerPackageField.getSelected();
    String errorMessage = selected.getErrorMessage(BowerSettingsManager.BOWER_PACKAGE_NAME);
    if (errorMessage != null) {
      return new BowerValidationInfo(myBowerPackageField,
                                     BowerBundle.message("bower.correct.path", BowerValidationInfo.LINK_TEMPLATE),
                                     BowerBundle.message("bower.package.name"));
    }
    return null;
  }

  @SuppressWarnings("SameParameterValue")
  private static @Nullable BowerValidationInfo validateFilePathField(Component component, String path, @NlsSafe String fieldName) {
    File file = new File(path);
    if (file.isFile()) {
      return null;
    }
    return new BowerValidationInfo(component, BowerBundle.message("bower.correct.path", BowerValidationInfo.LINK_TEMPLATE), fieldName);
  }

  private static @NotNull TextFieldWithBrowseButton createBowerJsonField(@NotNull Project project) {
    TextFieldWithBrowseButton textFieldWithBrowseButton = new TextFieldWithBrowseButton();
    var descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor().withTitle(IdeBundle.message("dialog.title.select.0", "bower.json"));
    SwingHelper.installFileCompletionAndBrowseDialog(project, textFieldWithBrowseButton, descriptor);
    PathShortener.enablePathShortening(textFieldWithBrowseButton.getTextField(), null);
    return textFieldWithBrowseButton;
  }

  public @NotNull JComponent getComponent() {
    return myComponent;
  }

  public @NotNull BowerSettings getSettings() {
    return new BowerSettings.Builder(myProject)
      .setBowerPackage(myBowerPackageField.getSelected())
      .setBowerJsonPath(PathShortener.getAbsolutePath(myBowerJsonField.getTextField()))
      .build();
  }

  public void setSettings(@NotNull BowerSettings settings) {
    myAllowUpdates = false;
    try {
      myBowerPackageField.setSelected(settings.getBowerPackage());
      myBowerJsonField.setText(FileUtil.toSystemDependentName(settings.getBowerJsonPath()));
      updateLater();
    }
    finally {
      myAllowUpdates = true;
    }
  }
}
