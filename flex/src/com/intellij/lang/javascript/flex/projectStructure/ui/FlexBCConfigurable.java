// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.execution.ExecutionBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.CompilerOptionInfo;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.build.FlexCompilationUtils;
import com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil;
import com.intellij.lang.javascript.flex.build.InfoFromConfigFile;
import com.intellij.lang.javascript.flex.projectStructure.FlexBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.ModuleEditor;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectStructureElementConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureElement;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.HoverHyperlinkLabel;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import com.intellij.ui.dsl.listCellRenderer.BuilderKt;
import com.intellij.ui.navigation.Place;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.HyperlinkEvent;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.intellij.lang.javascript.flex.projectStructure.ui.AirPackagingConfigurableBase.AirDescriptorInfoProvider;

public class FlexBCConfigurable extends ProjectStructureElementConfigurable<ModifiableFlexBuildConfiguration>
  implements CompositeConfigurable.Item, Place.Navigator {
  public static final String LOCATION_ON_TAB = "FlashBuildConfiguration.locationOnTab";

  public enum Location {
    Nature("nature"),
    MainClass("main-class"),
    OutputFileName("output-file-name"),
    OutputFolder("output-folder"),
    HtmlTemplatePath("html-template-path"),
    RLMs("runtime-loaded-modules"),
    RuntimeStyleSheets("runtime-style-sheets");

    public final String errorId;

    Location(final String errorId) {
      this.errorId = errorId;
    }
  }

  private final JPanel myMainPanel;

  private final JLabel myNatureLabel;
  private final HoverHyperlinkLabel myChangeNatureHyperlink;

  private final JTextField myNameField;

  private final JPanel myOptimizeForPanel;
  private final JComboBox myOptimizeForCombo;

  private final JLabel myMainClassLabel;
  private final JSReferenceEditor myMainClassComponent;
  private final JLabel myMainClassWarning;
  private final JTextField myOutputFileNameTextField;
  private final JLabel myOutputFileNameWarning;
  private final TextFieldWithBrowseButton myOutputFolderField;
  private final JLabel myOutputFolderWarning;

  private final JCheckBox myUseHTMLWrapperCheckBox;
  private final JLabel myWrapperFolderLabel;
  private final TextFieldWithBrowseButton myWrapperTemplateTextWithBrowse;
  private final JButton myCreateHtmlWrapperTemplateButton;

  private final JLabel myRLMLabel;
  private final TextFieldWithBrowseButton.NoPathCompletion myRLMTextWithBrowse;
  private Collection<FlexBuildConfiguration.RLMInfo> myRLMs;

  private final JLabel myCssFilesLabel;
  private final TextFieldWithBrowseButton.NoPathCompletion myCssFilesTextWithBrowse;
  private Collection<String> myCssFilesToCompile;

  private final JCheckBox mySkipCompilationCheckBox;
  private final JLabel myWarning;

  private final Module myModule;
  private final ModifiableFlexBuildConfiguration myConfiguration;
  private final @NotNull FlexProjectConfigurationEditor myConfigEditor;
  private final ProjectSdksModel mySdksModel;
  private final StructureConfigurableContext myContext;
  private String myName;

  private DependenciesConfigurable myDependenciesConfigurable;
  private CompilerOptionsConfigurable myCompilerOptionsConfigurable;
  private @Nullable AirDesktopPackagingConfigurable myAirDesktopPackagingConfigurable;
  private @Nullable AndroidPackagingConfigurable myAndroidPackagingConfigurable;
  private @Nullable IOSPackagingConfigurable myIOSPackagingConfigurable;

  private final BuildConfigurationProjectStructureElement myStructureElement;

  private final Disposable myDisposable;

  private final UserActivityListener myUserActivityListener;
  private boolean myFreeze;

  public FlexBCConfigurable(final Module module,
                            final ModifiableFlexBuildConfiguration bc,
                            final Runnable bcNatureModifier,
                            final @NotNull FlexProjectConfigurationEditor configEditor,
                            final ProjectSdksModel sdksModel,
                            final StructureConfigurableContext context) {
    super(false, null);
    myModule = module;
    myConfiguration = bc;
    myConfigEditor = configEditor;
    mySdksModel = sdksModel;
    myContext = context;
    {
      myChangeNatureHyperlink = new HoverHyperlinkLabel("Change...");

      rebuildMainClassFilter();
      myMainClassComponent = JSReferenceEditor.forClassName("", myModule.getProject(), null,
                                                            myModule.getModuleScope(false), null,
                                                            Conditions.alwaysTrue(), // no filtering until IDEA-83046
                                                            ExecutionBundle.message("choose.main.class.dialog.title"));
    }
    {
      // GUI initializer generated by IntelliJ IDEA GUI Designer
      // >>> IMPORTANT!! <<<
      // DO NOT EDIT OR ADD ANY CODE HERE!
      myMainPanel = new JPanel();
      myMainPanel.setLayout(new GridLayoutManager(12, 3, new Insets(0, 0, 0, 0), -1, -1));
      myMainClassLabel = new JLabel();
      myMainClassLabel.setText("Main class:");
      myMainClassLabel.setDisplayedMnemonic('M');
      myMainClassLabel.setDisplayedMnemonicIndex(0);
      myMainPanel.add(myMainClassLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                            GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                            null, 0, false));
      final JLabel label1 = new JLabel();
      label1.setText("Output file name:");
      label1.setDisplayedMnemonic('T');
      label1.setDisplayedMnemonicIndex(2);
      myMainPanel.add(label1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                  false));
      final JLabel label2 = new JLabel();
      label2.setText("Output folder:");
      label2.setDisplayedMnemonic('F');
      label2.setDisplayedMnemonicIndex(7);
      myMainPanel.add(label2, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                  false));
      final Spacer spacer1 = new Spacer();
      myMainPanel.add(spacer1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                                                   GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 10), new Dimension(-1, 10),
                                                   new Dimension(-1, 10), 0, false));
      final Spacer spacer2 = new Spacer();
      myMainPanel.add(spacer2, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                                                   GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 10), new Dimension(-1, 10),
                                                   new Dimension(-1, 10), 0, false));
      final Spacer spacer3 = new Spacer();
      myMainPanel.add(spacer3, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                                                   GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
      final JPanel panel1 = new JPanel();
      panel1.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
      myMainPanel.add(panel1, new GridConstraints(7, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
                                                  null, 0, false));
      myUseHTMLWrapperCheckBox = new JCheckBox();
      myUseHTMLWrapperCheckBox.setText("Use HTML wrapper");
      myUseHTMLWrapperCheckBox.setMnemonic('U');
      myUseHTMLWrapperCheckBox.setDisplayedMnemonicIndex(0);
      panel1.add(myUseHTMLWrapperCheckBox, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                               GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                               GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myWrapperFolderLabel = new JLabel();
      myWrapperFolderLabel.setText("Folder with template:");
      myWrapperFolderLabel.setDisplayedMnemonic('W');
      myWrapperFolderLabel.setDisplayedMnemonicIndex(7);
      panel1.add(myWrapperFolderLabel,
                 new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                     GridConstraints.SIZEPOLICY_FIXED, null, null, null, 3, false));
      myWrapperTemplateTextWithBrowse = new TextFieldWithBrowseButton();
      panel1.add(myWrapperTemplateTextWithBrowse,
                 new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                     GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myCreateHtmlWrapperTemplateButton = new JButton();
      myCreateHtmlWrapperTemplateButton.setText("Create...");
      myCreateHtmlWrapperTemplateButton.setMnemonic('R');
      myCreateHtmlWrapperTemplateButton.setDisplayedMnemonicIndex(1);
      panel1.add(myCreateHtmlWrapperTemplateButton,
                 new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                     GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      final Spacer spacer4 = new Spacer();
      panel1.add(spacer4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                                              GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 10), new Dimension(-1, 10),
                                              new Dimension(-1, 10), 0, false));
      myRLMLabel = new JLabel();
      myRLMLabel.setText("Runtime-loaded modules:");
      myRLMLabel.setDisplayedMnemonic('E');
      myRLMLabel.setDisplayedMnemonicIndex(6);
      panel1.add(myRLMLabel,
                 new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                     GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myRLMTextWithBrowse = new TextFieldWithBrowseButton.NoPathCompletion();
      panel1.add(myRLMTextWithBrowse, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                          GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                                          null, null, 0, false));
      myCssFilesLabel = new JLabel();
      myCssFilesLabel.setText("Runtime style sheets:");
      myCssFilesLabel.setDisplayedMnemonic('I');
      myCssFilesLabel.setDisplayedMnemonicIndex(4);
      panel1.add(myCssFilesLabel,
                 new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                     GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myCssFilesTextWithBrowse = new TextFieldWithBrowseButton.NoPathCompletion();
      panel1.add(myCssFilesTextWithBrowse, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                               GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                                               null, null, 0, false));
      mySkipCompilationCheckBox = new JCheckBox();
      mySkipCompilationCheckBox.setText("Skip compilation (use build configuration for code highlighting only) ");
      mySkipCompilationCheckBox.setMnemonic('S');
      mySkipCompilationCheckBox.setDisplayedMnemonicIndex(0);
      myMainPanel.add(mySkipCompilationCheckBox, new GridConstraints(9, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                     GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                                                     null, null, null, 0, false));
      final Spacer spacer5 = new Spacer();
      myMainPanel.add(spacer5, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                                                   GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 10), new Dimension(-1, 10),
                                                   new Dimension(-1, 10), 0, false));
      final JPanel panel2 = new JPanel();
      panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 0, -1));
      myMainPanel.add(panel2, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
                                                  null, 0, false));
      myOutputFileNameTextField = new JTextField();
      myOutputFileNameTextField.setText("");
      panel2.add(myOutputFileNameTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                                                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                                                new Dimension(150, -1), null, 0, false));
      myOutputFileNameWarning = new JLabel();
      panel2.add(myOutputFileNameWarning, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
                                                              GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
                                                              null, null, 0, false));
      myWarning = new JLabel();
      myWarning.setText("Warning placeholder");
      myMainPanel.add(myWarning, new GridConstraints(11, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, 1,
                                                     GridConstraints.SIZEPOLICY_FIXED, new Dimension(50, -1), null, null, 0, false));
      final JPanel panel3 = new JPanel();
      panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 0, -1));
      myMainPanel.add(panel3, new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
                                                  null, 0, false));
      myOutputFolderField = new TextFieldWithBrowseButton();
      myOutputFolderField.setText("");
      panel3.add(myOutputFolderField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                          GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                                          null, null, 0, false));
      myOutputFolderWarning = new JLabel();
      panel3.add(myOutputFolderWarning, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
                                                            GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                            null, 0, false));
      final JPanel panel4 = new JPanel();
      panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 0, -1));
      myMainPanel.add(panel4, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
                                                  null, 0, false));
      myMainClassComponent.setText("");
      panel4.add(myMainClassComponent, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                                           GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                                           null, null, 0, false));
      myMainClassWarning = new JLabel();
      panel4.add(myMainClassWarning, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
                                                         GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                         null, 0, false));
      final JPanel panel5 = new JPanel();
      panel5.setLayout(new GridLayoutManager(1, 3, new Insets(2, 1, 0, 0), -1, -1));
      myMainPanel.add(panel5, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
                                                  null, 0, false));
      myNatureLabel = new JLabel();
      myNatureLabel.setText("Flex application for web (Flash Player)");
      panel5.add(myNatureLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE,
                                                    GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                    false));
      final Spacer spacer6 = new Spacer();
      panel5.add(spacer6, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                              GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
      myChangeNatureHyperlink.setText("Change...");
      panel5.add(myChangeNatureHyperlink, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_NONE,
                                                              GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                              GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                              null, null, null, 0, false));
      myOptimizeForPanel = new JPanel();
      myOptimizeForPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
      myMainPanel.add(myOptimizeForPanel, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                              GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                              GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                              null, null, null, 0, false));
      final JLabel label3 = new JLabel();
      label3.setText("Optimize for:");
      label3.setDisplayedMnemonic('O');
      label3.setDisplayedMnemonicIndex(0);
      myOptimizeForPanel.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                         GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                         null, 0, false));
      myOptimizeForCombo = new JComboBox();
      final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
      myOptimizeForCombo.setModel(defaultComboBoxModel1);
      myOptimizeForPanel.add(myOptimizeForCombo,
                             new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                                 GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                 false));
      final JLabel label4 = new JLabel();
      label4.setText("Type:");
      myMainPanel.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                  false));
      final JLabel label5 = new JLabel();
      label5.setText("Name:");
      label5.setDisplayedMnemonic('N');
      label5.setDisplayedMnemonicIndex(0);
      myMainPanel.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                  false));
      myNameField = new JTextField();
      myMainPanel.add(myNameField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                                       GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                                       new Dimension(200, -1), null, 0, false));
      final Spacer spacer7 = new Spacer();
      myMainPanel.add(spacer7, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                   GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
      myMainClassLabel.setLabelFor(myMainClassComponent);
      label1.setLabelFor(myOutputFileNameTextField);
      label2.setLabelFor(myOutputFolderField);
      myWrapperFolderLabel.setLabelFor(myWrapperTemplateTextWithBrowse);
      myRLMLabel.setLabelFor(myRLMTextWithBrowse);
      myCssFilesLabel.setLabelFor(myCssFilesTextWithBrowse);
      label3.setLabelFor(myOptimizeForCombo);
      label5.setLabelFor(myNameField);
    }
    myName = bc.getName();

    myStructureElement = new BuildConfigurationProjectStructureElement(bc, module, context) {
      @Override
      protected void libraryReplaced(final @NotNull Library library, final @Nullable Library replacement) {
        myDependenciesConfigurable.libraryReplaced(library, replacement);
      }
    };

    myRLMs = Collections.emptyList();
    myCssFilesToCompile = Collections.emptyList();

    myDisposable = Disposer.newDisposable();

    myUserActivityListener = new UserActivityListener() {
      @Override
      public void stateChanged() {
        if (myFreeze) {
          return;
        }

        try {
          apply();
        }
        catch (ConfigurationException ignored) {
        }

        myContext.getDaemonAnalyzer().queueUpdate(myStructureElement);
        myContext.getDaemonAnalyzer().queueUpdateForAllElementsWithErrors();

        final FlexBCConfigurator configurator = FlexBuildConfigurationsExtension.getInstance().getConfigurator();
        final List<ModifiableFlexBuildConfiguration> bcs = configurator.getBCsByOutputPath(myConfiguration.getActualOutputFilePath());
        if (bcs != null) {
          myContext.getDaemonAnalyzer().queueUpdates(ContainerUtil.mapNotNull(bcs, bc -> {
            if (bc == myConfiguration) return null;
            return configurator.getBCConfigurable(bc).myStructureElement;
          }));
        }
      }
    };

    final UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.register(myMainPanel);
    watcher.addUserActivityListener(myUserActivityListener, myDisposable);

    createChildConfigurables();

    myChangeNatureHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      protected void hyperlinkActivated(final @NotNull HyperlinkEvent e) {
        bcNatureModifier.run();
      }
    });

    myNameField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        setDisplayName(myNameField.getText().trim());
      }
    });

    myOutputFolderField.addBrowseFolderListener(module.getProject(), FileChooserDescriptorFactory.createSingleFolderDescriptor());

    initHtmlWrapperControls();
    initRLMControls();
    initCSSControls();

    myOptimizeForCombo.setModel(new CollectionComboBoxModel(new ArrayList<>(Collections.singletonList("")), ""));
    myOptimizeForCombo.setRenderer(BuilderKt.textListCellRenderer("", value -> {
      return "".equals(value) ? "<no optimization>" : null;
    }));

    myMainClassWarning.setIcon(AllIcons.General.BalloonWarning12);
    myOutputFileNameWarning.setIcon(AllIcons.General.BalloonWarning12);
    myOutputFolderWarning.setIcon(AllIcons.General.BalloonWarning12);

    myWarning.setIcon(UIUtil.getBalloonWarningIcon());
  }

  /** @noinspection ALL */
  public JComponent $$$getRootComponent$$$() { return myMainPanel; }

  private void initHtmlWrapperControls() {
    myUseHTMLWrapperCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateControls();
        IdeFocusManager.getInstance(myModule.getProject()).requestFocus(myWrapperTemplateTextWithBrowse.getTextField(), true);
      }
    });

    myWrapperTemplateTextWithBrowse.addBrowseFolderListener(myModule.getProject(),
                                                            FileChooserDescriptorFactory.createSingleFolderDescriptor()
                                                              .withTitle("Select Folder with HTML Wrapper Template")
                                                              .withDescription(
                                                                "Folder must contain 'index.template.html' file which must contain '${swf}' macro."));

    myCreateHtmlWrapperTemplateButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final Sdk sdk = myDependenciesConfigurable.getCurrentSdk();
        if (sdk == null || sdk.getSdkType() == FlexmojosSdkType.getInstance()) {
          final SelectFlexSdkDialog dialog = new SelectFlexSdkDialog(myModule.getProject(), CreateHtmlWrapperTemplateDialog.getTitleText(),
                                                                     FlexBundle.message("take.wrapper.template.from.sdk"));
          if (dialog.showAndGet()) {
            final Sdk dialogSdk = dialog.getSdk();
            if (dialogSdk != null) {
              showHtmlWrapperCreationDialog(dialogSdk);
            }
          }
        }
        else {
          showHtmlWrapperCreationDialog(sdk);
        }
      }
    });
  }

  private void initRLMControls() {
    myRLMTextWithBrowse.getTextField().setEditable(false);
    myRLMTextWithBrowse.setButtonIcon(AllIcons.Actions.ShowViewer);
    myRLMTextWithBrowse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final RLMsDialog dialog = new RLMsDialog(myModule, myRLMs);
        if (dialog.showAndGet()) {
          myRLMs = dialog.getRLMs();
          updateRLMsText();
        }
      }
    });
  }

  private void initCSSControls() {
    myCssFilesTextWithBrowse.getTextField().setEditable(false);
    myCssFilesTextWithBrowse.setButtonIcon(AllIcons.Actions.ShowViewer);
    myCssFilesTextWithBrowse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final List<StringBuilder> value = new ArrayList<>();
        for (String cssFilePath : myCssFilesToCompile) {
          value.add(new StringBuilder(cssFilePath));
        }
        final RepeatableValueDialog dialog =
          new RepeatableValueDialog(myModule.getProject(), FlexBundle.message("css.files.to.compile.dialog.title"), value,
                                    CompilerOptionInfo.CSS_FILES_INFO_FOR_UI);
        if (dialog.showAndGet()) {
          final List<StringBuilder> newValue = dialog.getCurrentList();
          myCssFilesToCompile = new ArrayList<>(newValue.size());
          for (StringBuilder cssPath : newValue) {
            myCssFilesToCompile.add(cssPath.toString());
          }
          updateCssFilesText();
        }
      }
    });
  }

  private void showHtmlWrapperCreationDialog(final @NotNull Sdk sdk) {
    String path = myWrapperTemplateTextWithBrowse.getText().trim();
    if (path.isEmpty()) {
      path = FlexUtils.getContentOrModuleFolderPath(myModule) + "/" + CreateHtmlWrapperTemplateDialog.HTML_TEMPLATE_FOLDER_NAME;
    }
    final CreateHtmlWrapperTemplateDialog dialog =
      new CreateHtmlWrapperTemplateDialog(myModule, sdk, myOutputFolderField.getText().trim(), path);
    if (dialog.showAndGet()) {
      myWrapperTemplateTextWithBrowse.setText(FileUtil.toSystemDependentName(dialog.getWrapperFolderPath()));
    }
  }

  public void createChildConfigurables() {
    final BuildConfigurationNature nature = myConfiguration.getNature();

    if (myDependenciesConfigurable != null) {
      myDependenciesConfigurable.removeUserActivityListeners();
      myDependenciesConfigurable.disposeUIResources();
    }
    if (myCompilerOptionsConfigurable != null) {
      myCompilerOptionsConfigurable.removeUserActivityListeners();
      myCompilerOptionsConfigurable.disposeUIResources();
    }
    if (myAirDesktopPackagingConfigurable != null) {
      myAirDesktopPackagingConfigurable.removeUserActivityListeners();
      myAirDesktopPackagingConfigurable.disposeUIResources();
    }
    if (myAndroidPackagingConfigurable != null) {
      myAndroidPackagingConfigurable.removeUserActivityListeners();
      myAndroidPackagingConfigurable.disposeUIResources();
    }
    if (myIOSPackagingConfigurable != null) {
      myIOSPackagingConfigurable.removeUserActivityListeners();
      myIOSPackagingConfigurable.disposeUIResources();
    }

    myDependenciesConfigurable = new DependenciesConfigurable(myConfiguration, myModule.getProject(), myConfigEditor, mySdksModel,
                                                              getModulesConfigurator().getProjectStructureConfigurable());
    myDependenciesConfigurable.addUserActivityListener(myUserActivityListener, myDisposable);

    myCompilerOptionsConfigurable =
      new CompilerOptionsConfigurable(myModule, nature, myDependenciesConfigurable, myConfiguration.getCompilerOptions());
    myCompilerOptionsConfigurable.addUserActivityListener(myUserActivityListener, myDisposable);

    myCompilerOptionsConfigurable.addAdditionalOptionsListener(new CompilerOptionsConfigurable.OptionsListener() {
      @Override
      public void configFileChanged(final String additionalConfigFilePath) {
        checkIfConfigFileOverridesOptions(additionalConfigFilePath);
      }

      @Override
      public void additionalOptionsChanged(final String additionalOptions) {
        // may be parse additionalOptions in the same way as config file
      }
    });

    final AirDescriptorInfoProvider airDescriptorInfoProvider = new AirDescriptorInfoProvider() {
      @Override
      public String getMainClass() {
        return myMainClassComponent.getText().trim();
      }

      @Override
      public String getAirVersion() {
        final Sdk sdk = myDependenciesConfigurable.getCurrentSdk();
        return sdk == null || sdk.getVersionString() == null
               ? ""
               : StringUtil.notNullize(FlexCommonUtils.getAirVersion(sdk.getHomePath(), sdk.getVersionString()));
      }

      @Override
      public String[] getExtensionIDs() {
        return FlexCompilationUtils.getAirExtensionIDs(myConfigEditor.getModifiableRootModel(myModule),
                                                       myDependenciesConfigurable.getEditableObject());
      }

      @Override
      public boolean isAndroidPackagingEnabled() {
        return myAndroidPackagingConfigurable != null && myAndroidPackagingConfigurable.isPackagingEnabled();
      }

      @Override
      public boolean isIOSPackagingEnabled() {
        return myIOSPackagingConfigurable != null && myIOSPackagingConfigurable.isPackagingEnabled();
      }

      @Override
      public void setCustomDescriptorForAndroidAndIOS(final String descriptorPath) {
        assert myAndroidPackagingConfigurable != null && myIOSPackagingConfigurable != null;
        myAndroidPackagingConfigurable.setUseCustomDescriptor(descriptorPath);
        myIOSPackagingConfigurable.setUseCustomDescriptor(descriptorPath);
      }
    };

    myAirDesktopPackagingConfigurable = nature.isDesktopPlatform() && nature.isApp()
                                        ? new AirDesktopPackagingConfigurable(myModule, myConfiguration.getAirDesktopPackagingOptions(),
                                                                              airDescriptorInfoProvider)
                                        : null;
    if (myAirDesktopPackagingConfigurable != null) {
      myAirDesktopPackagingConfigurable.addUserActivityListener(myUserActivityListener, myDisposable);
    }

    myAndroidPackagingConfigurable = nature.isMobilePlatform() && nature.isApp()
                                     ? new AndroidPackagingConfigurable(myModule, myConfiguration.getAndroidPackagingOptions(),
                                                                        airDescriptorInfoProvider)
                                     : null;
    if (myAndroidPackagingConfigurable != null) {
      myAndroidPackagingConfigurable.addUserActivityListener(myUserActivityListener, myDisposable);
    }

    myIOSPackagingConfigurable = nature.isMobilePlatform() && nature.isApp()
                                 ? new IOSPackagingConfigurable(myModule, myConfiguration.getIosPackagingOptions(),
                                                                airDescriptorInfoProvider)
                                 : null;
    if (myIOSPackagingConfigurable != null) {
      myIOSPackagingConfigurable.addUserActivityListener(myUserActivityListener, myDisposable);
    }
  }

  private void checkIfConfigFileOverridesOptions(final String configFilePath) {
    final InfoFromConfigFile info = FlexCompilerConfigFileUtil.getInfoFromConfigFile(configFilePath);
    overriddenValuesChanged(info.getMainClass(myModule), info.getOutputFileName(), info.getOutputFolderPath());
    myDependenciesConfigurable.overriddenTargetPlayerChanged(info.getTargetPlayer());
  }

  /**
   * Called when {@link CompilerOptionsConfigurable} is initialized and when path to additional config file is changed
   * {@code null} parameter value means that the value is not overridden in additional config file
   */
  public void overriddenValuesChanged(final @Nullable String mainClass,
                                      final @Nullable String outputFileName,
                                      final @Nullable String outputFolderPath) {
    myMainClassWarning.setToolTipText(FlexBundle.message("actual.value.from.config.file.0", mainClass));
    myMainClassWarning.setVisible(myMainClassComponent.isVisible() && mainClass != null);

    myOutputFileNameWarning.setToolTipText(FlexBundle.message("actual.value.from.config.file.0", outputFileName));
    myOutputFileNameWarning.setVisible(outputFileName != null);

    myOutputFolderWarning.setToolTipText(
      FlexBundle.message("actual.value.from.config.file.0", FileUtil.toSystemDependentName(StringUtil.notNullize(outputFolderPath))));
    myOutputFolderWarning.setVisible(outputFolderPath != null);

    final String warning = myMainClassWarning.isVisible() && outputFileName == null && outputFolderPath == null
                           ? FlexBundle.message("overridden.in.config.file", "Main class", mainClass)
                           : !myMainClassWarning.isVisible() && outputFileName != null && outputFolderPath != null
                             ? FlexBundle.message("overridden.in.config.file", "Output path",
                                                  FileUtil.toSystemDependentName(outputFolderPath + "/" + outputFileName))
                             : FlexBundle.message("main.class.and.output.overridden.in.config.file");
    myWarning.setText(warning);

    myWarning.setVisible(myMainClassWarning.isVisible() || myOutputFileNameWarning.isVisible() || myOutputFolderWarning.isVisible());
  }

  @Override
  public @Nls String getDisplayName() {
    return myName;
  }

  @Override
  public void updateName() {
    myFreeze = true;
    try {
      myNameField.setText(getDisplayName());
    }
    finally {
      myFreeze = false;
    }
  }

  @Override
  public void setDisplayName(final String name) {
    myName = name;
  }

  @Override
  public String getBannerSlogan() {
    return "Build Configuration '" + myName + "'";
  }

  public Module getModule() {
    return myModule;
  }

  public String getModuleName() {
    final ModuleEditor moduleEditor = getModulesConfigurator().getModuleEditor(myModule);
    assert moduleEditor != null : myModule;
    return moduleEditor.getName();
  }

  private ModulesConfigurator getModulesConfigurator() {
    return myContext.getModulesConfigurator();
  }

  public Icon getIcon() {
    return myConfiguration.getIcon();
  }

  @Override
  public ModifiableFlexBuildConfiguration getEditableObject() {
    return myConfiguration;
  }

  @Override
  public String getHelpTopic() {
    return "BuildConfigurationPage.General";
  }

  @Override
  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  private void updateControls() {
    final TargetPlatform targetPlatform = myConfiguration.getTargetPlatform();
    final OutputType outputType = myConfiguration.getOutputType();

    myOptimizeForPanel.setVisible(false /*outputType == OutputType.RuntimeLoadedModule*/);

    final boolean showMainClass = outputType == OutputType.Application || outputType == OutputType.RuntimeLoadedModule;
    myMainClassLabel.setVisible(showMainClass);
    myMainClassComponent.setVisible(showMainClass);

    final boolean wrapperApplicable = targetPlatform == TargetPlatform.Web && outputType == OutputType.Application;

    myUseHTMLWrapperCheckBox.setVisible(wrapperApplicable);
    myWrapperFolderLabel.setVisible(wrapperApplicable);
    myWrapperTemplateTextWithBrowse.setVisible(wrapperApplicable);
    myCreateHtmlWrapperTemplateButton.setVisible(wrapperApplicable);

    if (wrapperApplicable) {
      myWrapperFolderLabel.setEnabled(myUseHTMLWrapperCheckBox.isSelected());
      myWrapperTemplateTextWithBrowse.setEnabled(myUseHTMLWrapperCheckBox.isSelected());
      myCreateHtmlWrapperTemplateButton.setEnabled(myUseHTMLWrapperCheckBox.isSelected());
    }

    final boolean canHaveRLMsAndRuntimeStylesheets = FlexCommonUtils.canHaveRLMsAndRuntimeStylesheets(outputType, targetPlatform);

    myRLMLabel.setVisible(canHaveRLMsAndRuntimeStylesheets);
    myRLMTextWithBrowse.setVisible(canHaveRLMsAndRuntimeStylesheets);
    updateRLMsText();

    myCssFilesLabel.setVisible(canHaveRLMsAndRuntimeStylesheets);
    myCssFilesTextWithBrowse.setVisible(canHaveRLMsAndRuntimeStylesheets);
    updateCssFilesText();
  }

  private void updateRLMsText() {
    final String s = StringUtil.join(myRLMs, info -> info.MAIN_CLASS, ", ");
    myRLMTextWithBrowse.setText(s);
  }

  private void updateCssFilesText() {
    final String s = StringUtil.join(myCssFilesToCompile, path -> PathUtil.getFileName(path), ", ");
    myCssFilesTextWithBrowse.setText(s);
  }

  public String getTreeNodeText() {
    return myConfiguration.getShortText();
  }

  public OutputType getOutputType() {
    // immutable field
    return myConfiguration.getOutputType();
  }

  @Override
  public boolean isModified() {
    if (!myConfiguration.getName().equals(myName)) return true;
    if (!myConfiguration.getOptimizeFor().equals(myOptimizeForCombo.getSelectedItem())) return true;
    if (!myConfiguration.getMainClass().equals(myMainClassComponent.getText().trim())) return true;
    if (!myConfiguration.getOutputFileName().equals(myOutputFileNameTextField.getText().trim())) return true;
    if (!myConfiguration.getOutputFolder().equals(FileUtil.toSystemIndependentName(myOutputFolderField.getText().trim()))) return true;
    if (myConfiguration.isUseHtmlWrapper() != myUseHTMLWrapperCheckBox.isSelected()) return true;
    if (!myConfiguration.getWrapperTemplatePath()
      .equals(FileUtil.toSystemIndependentName(myWrapperTemplateTextWithBrowse.getText().trim()))) {
      return true;
    }
    if (!myConfiguration.getRLMs().equals(myRLMs)) return true;
    if (!myConfiguration.getCssFilesToCompile().equals(myCssFilesToCompile)) return true;
    if (myConfiguration.isSkipCompile() != mySkipCompilationCheckBox.isSelected()) return true;

    if (myDependenciesConfigurable.isModified()) return true;
    if (myCompilerOptionsConfigurable.isModified()) return true;
    if (myAirDesktopPackagingConfigurable != null && myAirDesktopPackagingConfigurable.isModified()) return true;
    if (myAndroidPackagingConfigurable != null && myAndroidPackagingConfigurable.isModified()) return true;
    if (myIOSPackagingConfigurable != null && myIOSPackagingConfigurable.isModified()) return true;

    return false;
  }

  @Override
  public void apply() throws ConfigurationException {
    applyOwnTo(myConfiguration);

    myDependenciesConfigurable.apply();
    myCompilerOptionsConfigurable.apply();
    if (myAirDesktopPackagingConfigurable != null) myAirDesktopPackagingConfigurable.apply();
    if (myAndroidPackagingConfigurable != null) myAndroidPackagingConfigurable.apply();
    if (myIOSPackagingConfigurable != null) myIOSPackagingConfigurable.apply();
    // main class validation is based on live settings from dependencies tab
    rebuildMainClassFilter();
  }

  private void rebuildMainClassFilter() {
  }

  private void applyOwnTo(ModifiableFlexBuildConfiguration configuration) throws ConfigurationException {
    if (StringUtil.isEmptyOrSpaces(myName)) {
      throw new ConfigurationException("Module '" + getModuleName() + "': build configuration name is empty");
    }
    configuration.setName(myName);
    configuration.setOptimizeFor((String)myOptimizeForCombo.getSelectedItem());
    configuration.setMainClass(myMainClassComponent.getText().trim());
    configuration.setOutputFileName(myOutputFileNameTextField.getText().trim());
    configuration.setOutputFolder(FileUtil.toSystemIndependentName(myOutputFolderField.getText().trim()));
    configuration.setUseHtmlWrapper(myUseHTMLWrapperCheckBox.isSelected());
    configuration.setWrapperTemplatePath(FileUtil.toSystemIndependentName(myWrapperTemplateTextWithBrowse.getText().trim()));
    configuration.setRLMs(myRLMs);
    configuration.setCssFilesToCompile(myCssFilesToCompile);
    configuration.setSkipCompile(mySkipCompilationCheckBox.isSelected());
  }

  @Override
  public void reset() {
    myFreeze = true;
    try {
      setDisplayName(myConfiguration.getName());
      myNatureLabel.setText(myConfiguration.getNature().getPresentableText());
      myOptimizeForCombo.setSelectedItem(myConfiguration.getOptimizeFor());

      myMainClassComponent.setText(myConfiguration.getMainClass());
      myOutputFileNameTextField.setText(myConfiguration.getOutputFileName());
      myOutputFolderField.setText(FileUtil.toSystemDependentName(myConfiguration.getOutputFolder()));
      myUseHTMLWrapperCheckBox.setSelected(myConfiguration.isUseHtmlWrapper());
      myWrapperTemplateTextWithBrowse.setText(FileUtil.toSystemDependentName(myConfiguration.getWrapperTemplatePath()));
      myRLMs = new ArrayList<>(myConfiguration.getRLMs());
      myCssFilesToCompile = new ArrayList<>(myConfiguration.getCssFilesToCompile());
      mySkipCompilationCheckBox.setSelected(myConfiguration.isSkipCompile());

      updateControls();
      overriddenValuesChanged(null, null, null); // no warnings initially

      myDependenciesConfigurable.reset();
      myCompilerOptionsConfigurable.reset();
      if (myAirDesktopPackagingConfigurable != null) myAirDesktopPackagingConfigurable.reset();
      if (myAndroidPackagingConfigurable != null) myAndroidPackagingConfigurable.reset();
      if (myIOSPackagingConfigurable != null) myIOSPackagingConfigurable.reset();
    }
    finally {
      myFreeze = false;
    }
    rebuildMainClassFilter();
    myContext.getDaemonAnalyzer().queueUpdate(myStructureElement);
  }

  @Override
  public void disposeUIResources() {
    myDependenciesConfigurable.disposeUIResources();
    myCompilerOptionsConfigurable.disposeUIResources();
    if (myAirDesktopPackagingConfigurable != null) myAirDesktopPackagingConfigurable.disposeUIResources();
    if (myAndroidPackagingConfigurable != null) myAndroidPackagingConfigurable.disposeUIResources();
    if (myIOSPackagingConfigurable != null) myIOSPackagingConfigurable.disposeUIResources();
    Disposer.dispose(myDisposable);
  }

  //public ModifiableFlexBuildConfiguration getCurrentConfiguration() {
  //  final ModifiableFlexBuildConfiguration configuration = Factory.createBuildConfiguration();
  //  try {
  //    applyTo(configuration, false);
  //  }
  //  catch (ConfigurationException ignored) {
  //    // no validation
  //  }
  //  return configuration;
  //}

  private List<NamedConfigurable> getChildren() {
    final List<NamedConfigurable> children = new ArrayList<>();

    children.add(myDependenciesConfigurable);
    children.add(myCompilerOptionsConfigurable);
    ContainerUtil.addIfNotNull(children, myAirDesktopPackagingConfigurable);
    ContainerUtil.addIfNotNull(children, myAndroidPackagingConfigurable);
    ContainerUtil.addIfNotNull(children, myIOSPackagingConfigurable);

    return children;
  }

  public CompositeConfigurable wrapInTabs() {
    return new CompositeConfigurable(this, getChildren(), null);
  }

  public void updateTabs(final CompositeConfigurable compositeConfigurable) {
    final List<NamedConfigurable> children = compositeConfigurable.getChildren();
    assert children.get(0) == this : children.get(0).getDisplayName();

    for (int i = children.size() - 1; i >= 1; i--) {
      compositeConfigurable.removeChildAt(i);
    }

    for (NamedConfigurable child : getChildren()) {
      compositeConfigurable.addChild(child);
    }
  }

  public DependenciesConfigurable getDependenciesConfigurable() {
    return myDependenciesConfigurable;
  }

  public boolean isParentFor(final DependenciesConfigurable dependenciesConfigurable) {
    return myDependenciesConfigurable == dependenciesConfigurable;
  }

  public void addSharedLibrary(final Library library) {
    myDependenciesConfigurable.addSharedLibraries(Collections.singletonList(library));
  }

  public static FlexBCConfigurable unwrap(CompositeConfigurable c) {
    return c.getMainChild();
  }

  @Override
  public String getTabTitle() {
    return getTabName();
  }

  @Override
  public ProjectStructureElement getProjectStructureElement() {
    return myStructureElement;
  }

  @Override
  public ActionCallback navigateTo(final @Nullable Place place, final boolean requestFocus) {
    if (place != null && place.getPath(LOCATION_ON_TAB) instanceof Location location) {
      return switch (location) {
        case Nature -> IdeFocusManager.findInstance().requestFocus(myChangeNatureHyperlink, true);
        case MainClass -> IdeFocusManager.findInstance().requestFocus(myMainClassComponent.getChildComponent(), true);
        case OutputFileName -> IdeFocusManager.findInstance().requestFocus(myOutputFileNameTextField, true);
        case OutputFolder -> IdeFocusManager.findInstance().requestFocus(myOutputFolderField.getChildComponent(), true);
        case HtmlTemplatePath -> IdeFocusManager.findInstance().requestFocus(myWrapperTemplateTextWithBrowse.getChildComponent(), true);
        case RuntimeStyleSheets -> IdeFocusManager.findInstance().requestFocus(myCssFilesTextWithBrowse.getChildComponent(), true);
        case RLMs -> IdeFocusManager.findInstance().requestFocus(myRLMTextWithBrowse.getChildComponent(), true);
      };
    }
    return ActionCallback.DONE;
  }

  public static String getTabName() {
    return FlexBundle.message("bc.tab.general.display.name");
  }
}
