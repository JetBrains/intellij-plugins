package com.intellij.lang.javascript.flex.actions.htmlwrapper;

import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.*;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CreateHtmlWrapperForm {
  private JPanel myMainPanel;
  private JLabel myModuleLabel;
  private JComboBox myModuleComboBox;
  private JLabel myFlexSdkLabel;
  private FlexSdkComboBoxWithBrowseButton myFlexSdkComboWithBrowse;
  private JComboBox myHtmlWrapperTypeComboBox;
  private JTextField myHtmlWrapperFileNameTextField;
  private TextFieldWithBrowseButton myHtmlWrapperFileLocationTextWithBrowse;
  private JTextField myHTMLPageTitleTextField;
  private JTextField myFlexApplicationNameTextField;
  private JTextField mySWFFileNameTextField;
  private JTextField myWidthTextField;
  private JTextField myHeightTextField;
  private JTextField myBgColorTextField;
  private JLabel myFlashPlayerVersionLabel;
  private FlashPlayerVersionForm myFlashPlayerVersionForm;
  private JCheckBox myCreateRunConfigurationCheckBox;
  private String myCurrentErrorMessage;
  private final List<Listener> myListeners = new ArrayList<Listener>();
  private final DocumentListener documentListener = new DocumentAdapter() {
    protected void textChanged(DocumentEvent e) {
      fireStateChanged();
    }
  };

  private static final Icon ourHtmlWrapperTypeIcon = IconLoader.getIcon("html_wrapper_type_icon.png", CreateHtmlWrapperForm.class);

  public interface Listener {
    void stateChanged();
  }

  public CreateHtmlWrapperForm(final Project project) {
    setupModuleComboBox(project);
    setupFlexSdkComboWithBrowse();
    setupHtmlWrapperTypeComboBox();
    myHtmlWrapperFileNameTextField.getDocument().addDocumentListener(documentListener);
    setupHtmlWrapperFileLocationTextField(project);
    myHTMLPageTitleTextField.getDocument().addDocumentListener(documentListener);
    myFlexApplicationNameTextField.getDocument().addDocumentListener(documentListener);
    mySWFFileNameTextField.getDocument().addDocumentListener(documentListener);
    myWidthTextField.getDocument().addDocumentListener(documentListener);
    myHeightTextField.getDocument().addDocumentListener(documentListener);
    myBgColorTextField.getDocument().addDocumentListener(documentListener);
    myFlashPlayerVersionForm.addDocumentListener(documentListener);

    setOnlyModuleRelatedControlsEnabled();

    final Module[] modules = ModuleManager.getInstance(project).getModules();
    if (modules.length == 1) {
      myModuleComboBox.setSelectedItem(modules[0]);
      updateSdkAndSubsequentControls();
    }
  }

  // *********  public methods  **************

  public void setModuleAndSdkAndWrapperLocation(final Module module, final Sdk flexSdk, final String htmlWrapperFileLocation) {
    myModuleComboBox.setModel(new DefaultComboBoxModel(new Module[]{module}));
    myFlexSdkComboWithBrowse.setSelectedSdkRaw(flexSdk.getName());
    updateSdkAndSubsequentControls();
    myHtmlWrapperFileLocationTextWithBrowse.setText(htmlWrapperFileLocation);
    fireStateChanged();
  }

  public void suggestToCreateRunConfiguration(final boolean b) {
    myCreateRunConfigurationCheckBox.setEnabled(b);
    myCreateRunConfigurationCheckBox.setVisible(b);
  }

  public boolean isCreateRunConfigurationSelected(){
    return myCreateRunConfigurationCheckBox.isVisible() && myCreateRunConfigurationCheckBox.isEnabled();
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  @Nullable
  public HTMLWrapperParameters getHTMLWrapperParameters() {
    if (!myHtmlWrapperTypeComboBox.isEnabled()) {
      // it means that module and/or SDK are not selected properly yet
      return null;
    }

    // HTML wrapper root dir
    final VirtualFile htmlWrapperRootDir = (VirtualFile)myHtmlWrapperTypeComboBox.getSelectedItem();
    if (htmlWrapperRootDir == null) {
      myCurrentErrorMessage = "Select HTML wrapper type";
      return null;
    }

    // HTML wrapper file name
    final String htmlWrapperFileName = myHtmlWrapperFileNameTextField.getText();
    if (htmlWrapperFileName.length() == 0) {
      myCurrentErrorMessage = "Specify HTML wrapper file name";
      return null;
    }
    if (!VfsUtil.isValidName(htmlWrapperFileName)) {
      myCurrentErrorMessage = "Invalid HTML wrapper file name";
      return null;
    }

    // HTML wrapper file location
    final String htmlWrapperLocationPath = myHtmlWrapperFileLocationTextWithBrowse.getText();
    final VirtualFile htmlWrapperFileLocation = LocalFileSystem.getInstance().refreshAndFindFileByPath(htmlWrapperLocationPath);
    if (htmlWrapperLocationPath.trim().length() == 0 || htmlWrapperFileLocation == null) {
      myCurrentErrorMessage = "Choose existing directory for HTML wrapper";
      return null;
    }

    // HTML page title
    final String htmlPageTitle = myHTMLPageTitleTextField.getText();

    // Flex application name
    final String flexAppName = myFlexApplicationNameTextField.getText().trim();
    if (flexAppName.length() == 0) {
      myCurrentErrorMessage = "Specify Flex application name";
      return null;
    }

    // SWF file which is wrapped
    final String swfFileName = mySWFFileNameTextField.getText();
    if (swfFileName.length() == 0) {
      myCurrentErrorMessage = "Specify SWF file to wrap";
      return null;
    }
    if (swfFileName.length() < 5 || !swfFileName.endsWith(".swf")) {
      myCurrentErrorMessage = "SWF file must have '.swf' extension";
      return null;
    }
    final String swfFileNameWithoutExtension = swfFileName.substring(0, swfFileName.lastIndexOf('.'));

    // application width and height
    final String width = myWidthTextField.getText();
    final String height = myHeightTextField.getText();

    // background color
    final String bgColor = myBgColorTextField.getText();

    // minimal flash player version
    int playerVersionMajor = 0;
    int playerVersionMinor = 0;
    int playerVersionRevision = 0;

    if (myFlashPlayerVersionForm.isEnabled()) {
      try {
        playerVersionMajor = Integer.parseInt(myFlashPlayerVersionForm.getPlayerVersionMajor());
        playerVersionMinor = Integer.parseInt(myFlashPlayerVersionForm.getPlayerVersionMinor());
        playerVersionRevision = Integer.parseInt(myFlashPlayerVersionForm.getPlayerVersionRevision());
      }
      catch (NumberFormatException e) {
        myCurrentErrorMessage = "Flash player version is invalid";
        return null;
      }
    }

    myCurrentErrorMessage = null;
    return new HTMLWrapperParameters(htmlWrapperRootDir, htmlWrapperFileName, htmlWrapperFileLocation, htmlPageTitle, flexAppName,
                                     swfFileNameWithoutExtension, width, height, bgColor, playerVersionMajor, playerVersionMinor,
                                     playerVersionRevision);
  }

  public String getCurrentErrorMessage() {
    return myCurrentErrorMessage;
  }

  public void addListener(final Listener listener) {
    myListeners.add(listener);
  }

  public void removeListener(final Listener listener) {
    myListeners.remove(listener);
  }

  // *********  setupXXX methods called once from constructor  **************

  private void setupModuleComboBox(final Project project) {
    myModuleComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FlexUtils.removeIncorrectItemFromComboBoxIfPresent(myModuleComboBox, Module.class);
        updateSdkAndSubsequentControls();
        fireStateChanged();
      }
    });

    myModuleComboBox.setRenderer(new ModulesComboboxWrapper.CellRenderer(myModuleComboBox.getRenderer(), false));

    final Module[] modules = ModuleManager.getInstance(project).getModules();
    final Module[] modulesWithNullDefault = new Module[modules.length + 1];
    modulesWithNullDefault[0] = null; // no module is selected by default
    System.arraycopy(modules, 0, modulesWithNullDefault, 1, modules.length);
    myModuleComboBox.setModel(new DefaultComboBoxModel(modulesWithNullDefault));
  }

  private void setupFlexSdkComboWithBrowse() {
    myFlexSdkComboWithBrowse.getComboBox().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateHTMLWrapperTypeAndSubsequentControls();
        suggestPlayerVersion(myFlexSdkComboWithBrowse.getSelectedSdk());
        fireStateChanged();
      }
    });
  }

  private void setupHtmlWrapperTypeComboBox() {
    myHtmlWrapperTypeComboBox.setRenderer(new ListCellRendererWrapper<VirtualFile>(myHtmlWrapperTypeComboBox.getRenderer()) {
      @Override
      public void customize(JList list, VirtualFile value, int index, boolean selected, boolean hasFocus) {
        if (value != null) {
          setText(value.getName());
          setIcon(ourHtmlWrapperTypeIcon);
        }
      }
    });

    myHtmlWrapperTypeComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setFlashPlayerVersionControlsEnabled(doesWrapperNeedFlashPlayerVersion((VirtualFile)myHtmlWrapperTypeComboBox.getSelectedItem()));
        fireStateChanged();
      }
    });
  }

  private void setupHtmlWrapperFileLocationTextField(final Project project) {
    myHtmlWrapperFileLocationTextWithBrowse.addBrowseFolderListener("Choose location for HTML wrapper", "", project,
                                                                    FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                                                                    new TextComponentAccessor<JTextField>() {
                                                                      public String getText(final JTextField textField) {
                                                                        return textField.getText();
                                                                      }

                                                                      public void setText(final JTextField textField, final String text) {
                                                                        textField.setText(text.replace('\\', '/'));
                                                                      }
                                                                    });
    myHtmlWrapperFileLocationTextWithBrowse.getTextField().getDocument().addDocumentListener(documentListener);
  }

  private void updateSdkAndSubsequentControls() {
    myCurrentErrorMessage = null;
    final Module module = (Module)myModuleComboBox.getSelectedItem();

    if (module == null) {
      myCurrentErrorMessage = "No module selected";
      setOnlyModuleRelatedControlsEnabled();
      return;
    }

    if (!FlexUtils.isFlexModuleOrContainsFlexFacet(module)) {
      setOnlyModuleRelatedControlsEnabled();
      myCurrentErrorMessage = FlexBundle.message("not.flex.module.no.flex.facet", module.getName());
      return;
    }

    final Sdk flexSdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
    myFlexSdkLabel.setEnabled(true);
    myFlexSdkComboWithBrowse.setEnabled(true);
    if (flexSdk != null) {
      myFlexSdkComboWithBrowse.setSelectedSdkRaw(flexSdk.getName());
    }
    suggestHTMLWrapperProperties(module);
    updateHTMLWrapperTypeAndSubsequentControls();
  }

  private void updateHTMLWrapperTypeAndSubsequentControls() {
    myCurrentErrorMessage = null;
    final Sdk sdk = myFlexSdkComboWithBrowse.getSelectedSdk();
    final VirtualFile sdkRoot = sdk == null ? null : sdk.getHomeDirectory();
    if (sdkRoot != null) {
      VirtualFile rootFolderWithHtmlWrappers;
      // Flex 3 and Flex 4
      rootFolderWithHtmlWrappers = sdkRoot.findChild("templates");
      if (rootFolderWithHtmlWrappers == null || !rootFolderWithHtmlWrappers.isDirectory()) {
        // Flex 2
        rootFolderWithHtmlWrappers = sdkRoot.findFileByRelativePath("resources/html-templates");
      }
      if (rootFolderWithHtmlWrappers != null && rootFolderWithHtmlWrappers.isDirectory()) {
        final VirtualFile[] foldersWithHtmlWrappers = findSubfoldersWithHtmlWrappers(rootFolderWithHtmlWrappers);
        if (foldersWithHtmlWrappers.length > 0) {
          // this is the only 'good' exit point of this method
          Arrays.sort(foldersWithHtmlWrappers, new Comparator<VirtualFile>() {
            public int compare(final VirtualFile first, final VirtualFile second) {
              return first.getName().compareTo(second.getName());
            }
          });
          myHtmlWrapperTypeComboBox.setModel(new DefaultComboBoxModel(foldersWithHtmlWrappers));
          setAllControlsEnabled();
          setFlashPlayerVersionControlsEnabled(
            doesWrapperNeedFlashPlayerVersion((VirtualFile)myHtmlWrapperTypeComboBox.getSelectedItem()));
          return;
        }
      }
    }

    myHtmlWrapperTypeComboBox.setModel(new DefaultComboBoxModel());
    myCurrentErrorMessage = "HTML wrapper templates not found in selected SDK";
    setOnlyModuleAndSdkRelatedControlsEnabled();
  }

  private void suggestHTMLWrapperProperties(final Module module) {
    // suggest HTML wrapper file name
    myHtmlWrapperFileNameTextField.setText(module.getName().replaceAll("[^\\p{Alnum}]", "_") + ".html");

    // suggest HTML wrapper file location
    final VirtualFile[] srcRoots = ModuleRootManager.getInstance(module).getSourceRoots();
    if (srcRoots.length > 0) {
      myHtmlWrapperFileLocationTextWithBrowse.setText(srcRoots[0].getPath());
    }
    else {
      final VirtualFile moduleFile = module.getModuleFile();
      if (moduleFile != null) {
        myHtmlWrapperFileLocationTextWithBrowse.setText(moduleFile.getParent().getPath());
      }
    }

    // suggest HTML page title, application name and swf file name
    String outputFileName = "";
    for (FlexBuildConfiguration config : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module)) {
      if (!config.USE_CUSTOM_CONFIG_FILE && config.OUTPUT_FILE_NAME.endsWith(".swf")) {
        outputFileName = config.OUTPUT_FILE_NAME;
        break;
      }
    }

    final String htmlPageAndFlexAppName;
    if (outputFileName.length() > 0) {
      if (outputFileName.indexOf('.') > 0) {
        htmlPageAndFlexAppName = outputFileName.substring(0, outputFileName.indexOf('.'));
      }
      else {
        htmlPageAndFlexAppName = outputFileName;
      }
    }
    else {
      htmlPageAndFlexAppName = module.getName();
    }
    myHTMLPageTitleTextField.setText(htmlPageAndFlexAppName);
    myFlexApplicationNameTextField.setText(htmlPageAndFlexAppName);
    mySWFFileNameTextField.setText(outputFileName);

    final Sdk flexSdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
    suggestPlayerVersion(flexSdk);
  }

  private void suggestPlayerVersion(final Sdk flexSdk) {
    if (flexSdk != null) {
      myFlashPlayerVersionForm.setPlayerVersion(normalizePlayerVersion(TargetPlayerUtils.getTargetPlayerVersion(flexSdk)));
    }
  }

  private static String normalizePlayerVersion(final String playerVersion) {
    final String[] strings = playerVersion.split("[.]");
    switch (strings.length) {
      case 0:
        return "9.0.124";
      case 1:
        return strings[0] + ".0.0";
      case 2:
        return strings[0] + "." + strings[1] + ".0";
      default:
        return strings[0] + "." + strings[1] + "." + strings[2];
    }
  }

  private void setOnlyModuleRelatedControlsEnabled() {
    UIUtil.setEnabled(myMainPanel, false, true);
    UIUtil.setEnabled(myModuleLabel, true, false);
    myModuleComboBox.setEnabled(true);
  }

  private void setOnlyModuleAndSdkRelatedControlsEnabled() {
    setOnlyModuleRelatedControlsEnabled();
    myFlexSdkLabel.setEnabled(true);
    myFlexSdkComboWithBrowse.setEnabled(true);
  }

  private void setAllControlsEnabled() {
    UIUtil.setEnabled(myMainPanel, true, true);
  }

  private void setFlashPlayerVersionControlsEnabled(boolean enabled) {
    myFlashPlayerVersionLabel.setEnabled(enabled);
    myFlashPlayerVersionForm.setEnabled(enabled);
  }

  private void fireStateChanged() {
    for (Listener listener : myListeners.toArray(new Listener[myListeners.size()])) {
      listener.stateChanged();
    }
  }

  // ********* static helpers **************

  private static VirtualFile[] findSubfoldersWithHtmlWrappers(final VirtualFile rootFolderWithHtmlWrappers) {
    final List<VirtualFile> result = new ArrayList<VirtualFile>();
    for (final VirtualFile file : rootFolderWithHtmlWrappers.getChildren()) {
      if (file.isDirectory()) {
        final VirtualFile htmlTemplate = file.findChild(CreateHtmlWrapperAction.HTML_WRAPPER_TEMPLATE_FILE_NAME);
        if (htmlTemplate != null) {
          result.add(file);
        }
      }
    }
    return VfsUtil.toVirtualFileArray(result);
  }

  private static boolean doesWrapperNeedFlashPlayerVersion(final VirtualFile htmlWrapperRootDir) {
    if (htmlWrapperRootDir != null && htmlWrapperRootDir.isDirectory()) {
      final VirtualFile htmlTemplate = htmlWrapperRootDir.findChild("index.template.html");
      if (htmlTemplate != null) {
        try {
          return VfsUtil.loadText(htmlTemplate).indexOf("${" + CreateHtmlWrapperAction.REQUIRED_FLASH_PLAYER_VERSION_MAJOR + "}") != -1;
        }
        catch (IOException e) {
          // ignore
        }
      }
    }
    return false;
  }
}
