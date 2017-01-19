package com.google.jstestdriver.idea.assertFramework.support;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.assertFramework.library.JsLibraryHelper;
import com.intellij.lang.javascript.library.JSLibraryMappings;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.SwingHelper;
import com.intellij.util.ui.UIUtil;
import com.intellij.webcore.ScriptingFrameworkDescriptor;
import com.intellij.webcore.libraries.ScriptingLibraryMappings;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import com.intellij.webcore.libraries.ui.ModuleScopeSelectionView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ChooseScopeAndCreateLibraryDialog extends DialogWrapper {

  private static final Logger LOG = Logger.getInstance(ChooseScopeAndCreateLibraryDialog.class);

  private final Project myProject;
  private final ModuleScopeSelectionView myModuleSelector;
  private final JTextField myLibraryNameTextField;
  private final JPanel myComponent;
  private final JsLibraryHelper myLibraryHelper;

  public ChooseScopeAndCreateLibraryDialog(@NotNull Project project,
                                           @NotNull String desiredLibraryName,
                                           @NotNull List<VirtualFile> libraryFiles,
                                           @NotNull ScriptingFrameworkDescriptor frameworkDescriptor,
                                           @Nullable VirtualFile requestor,
                                           boolean warnAboutOutsideCode) {
    super(project);
    myProject = project;
    myLibraryHelper = new JsLibraryHelper(myProject, desiredLibraryName, libraryFiles, frameworkDescriptor);

    setTitle("Coding Assistance For " + desiredLibraryName);

    myModuleSelector = new ModuleScopeSelectionView(project, requestor, true, true);
    myLibraryNameTextField = createTextField(myLibraryHelper);
    List<Component> components = Lists.newArrayList();
    if (!myLibraryHelper.hasReusableLibraryModel()) {
      components.addAll(Arrays.asList(
        createDescription(warnAboutOutsideCode),
        Box.createVerticalStrut(10)
      ));
    }
    components.addAll(Arrays.asList(
      createLibraryNamePanel(),
      Box.createVerticalStrut(5),
      createCompletionPanel()
    ));

    myComponent = SwingHelper.newLeftAlignedVerticalPanel(components);
    super.init();
  }

  private static JTextField createTextField(JsLibraryHelper helper) {
    JTextField textField = new JTextField(helper.getJsLibraryName());
    textField.setEnabled(!helper.hasReusableLibraryModel());
    Dimension prefSize = textField.getPreferredSize();
    textField.setPreferredSize(new Dimension((int) (prefSize.width * 1.2), prefSize.height));
    return textField;
  }

  private static JComponent createDescription(boolean warnAboutOutsideCode) {
    List<Component> components = Lists.newArrayList();
    if (warnAboutOutsideCode) {
      JLabel warnLabel = new JLabel(UIUtil.getBalloonWarningIcon());
      warnLabel.setText(" Added files have been placed outside of the project.");
      components.add(warnLabel);
      components.add(Box.createVerticalStrut(10));
    }
    components.add(new JLabel("JavaScript library will be created to provide coding assistance."));
    return SwingHelper.newLeftAlignedVerticalPanel(components);
  }

  @NotNull
  private JPanel createCompletionPanel() {
    JPanel completionPanel = new JPanel(new BorderLayout());
    completionPanel.add(myModuleSelector.getComponent(), BorderLayout.CENTER);
    completionPanel.setBorder(IdeBorderFactory.createTitledBorder("Code completion"));
    return completionPanel;
  }

  @NotNull
  private Component createLibraryNamePanel() {
    JPanel libraryNamePanel = new JPanel(new GridBagLayout());
    libraryNamePanel.add(new JLabel("Library name:"), new GridBagConstraints(
      0, 0,
      1, 1,
      0.0, 0.0,
      GridBagConstraints.WEST,
      GridBagConstraints.NONE,
      new JBInsets(0, 0, 0, 5),
      0, 0
    ));
    libraryNamePanel.add(myLibraryNameTextField, new GridBagConstraints(
      1, 0,
      1, 1,
      1.0, 0.0,
      GridBagConstraints.WEST,
      GridBagConstraints.HORIZONTAL,
      JBUI.emptyInsets(),
      0, 0
    ));
    return libraryNamePanel;
  }

  @Override
  protected JComponent createCenterPanel() {
    return myComponent;
  }

  @Override
  public ValidationInfo doValidate() {
    if (myLibraryHelper.hasReusableLibraryModel()) {
      return null;
    }
    String text=  myLibraryNameTextField.getText();
    if (StringUtil.isEmpty(text)) {
      return new ValidationInfo("Library name is empty", myLibraryNameTextField);
    }
    boolean exists = myLibraryHelper.doesJavaScriptLibraryModelExist(myLibraryNameTextField.getText());
    if (exists) {
      return new ValidationInfo("Library with such name already exists", myLibraryNameTextField);
    }
    return null;
  }

  @Override
  protected void doOKAction() {
    ErrorMessage errorMessage = WriteAction.compute(() -> createLibraryAndAssociate());
    if (errorMessage != null) {
      Messages.showErrorDialog(errorMessage.getDescription(), "Adding " + myLibraryHelper.getJsLibraryName());
      LOG.warn(errorMessage.getDescription(), errorMessage.getThrowable());
    }
    super.doOKAction();
  }

  @Nullable
  private ErrorMessage createLibraryAndAssociate() {
    String libraryName = myLibraryNameTextField.getText();
    ScriptingLibraryModel libraryModel = myLibraryHelper.getOrCreateJsLibraryModel(libraryName);
    try {
      ScriptingLibraryMappings libraryMappings = ServiceManager.getService(myProject, JSLibraryMappings.class);
      if (myModuleSelector.isAssociateWithProjectView()) {
        if (myModuleSelector.isAssociateWithProjectRequested()) {
          libraryMappings.associateWithProject(libraryModel.getName());
          LOG.info("Library '" + libraryModel.getName() + "' has been successfully associated with the project");
        }
        else {
          libraryMappings.disassociateWithProject(libraryModel.getName());
        }
      }
      else {
        for (Module module : myModuleSelector.getSelectedModules()) {
          ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
          VirtualFile[] roots = moduleRootManager.getContentRoots();
          for (VirtualFile root : roots) {
            libraryMappings.associate(root, libraryModel.getName(), false);
            LOG.info("Library '" + libraryModel.getName() + "' has been associated with " + root);
          }
        }
      }
      myLibraryHelper.commit();
      return null;
    } catch (Exception ex) {
      return new ErrorMessage("Unable to associate '" + libraryName + "' JavaScript library", ex);
    }
  }

  private static class ErrorMessage {
    private final String myDescription;
    private final Throwable myThrowable;

    private ErrorMessage(@NotNull String description, @Nullable Throwable throwable) {
      myDescription = description;
      myThrowable = throwable;
    }

    @NotNull
    public String getDescription() {
      return myDescription;
    }

    @Nullable
    public Throwable getThrowable() {
      return myThrowable;
    }
  }
}
