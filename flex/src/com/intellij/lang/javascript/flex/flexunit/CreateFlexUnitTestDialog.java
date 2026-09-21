// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.ide.projectView.actions.MarkRootsManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.ui.JSMemberSelectionPanel;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.refactoring.util.JSMemberInfo;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.lang.javascript.ui.ActionScriptPackageChooserDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.classMembers.MemberInfoBase;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ThreeState;
import com.intellij.util.text.UniqueNameGenerator;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateFlexUnitTestDialog extends DialogWrapper {

  private final JPanel myMainPanel;
  private final JTextField myTestClassNameTextField;
  private final JSReferenceEditor myPackageCombo;
  private final JCheckBox myCreateTestSourceFolderCheckBox;
  private final JTextField myTestSourceFolderTextField;
  private final JSReferenceEditor mySuperClassField;
  private final JCheckBox mySetUpCheckBox;
  private final JCheckBox myTearDownCheckBox;
  private final JSMemberSelectionPanel myMemberSelectionPanel;

  private final Module myModule;
  private final JSClass myContextClass;
  private PsiDirectory myTargetDirectory;
  private JSClass mySuperClass;
  private final PsiDirectory myExistingTestSourceRoot;

  private static final String CREATE_TEST_SOURCE_FOLDER_KEY = "CreateTestSourceFolder";

  public CreateFlexUnitTestDialog(final Module module, final JSClass contextClass) {
    super(module.getProject());
    myModule = module;
    myContextClass = contextClass;
    {
      final Module moduleFormContextClass = ModuleUtilCore.findModuleForPsiElement(myContextClass);
      assert moduleFormContextClass != null;

      myPackageCombo =
        ActionScriptPackageChooserDialog.createPackageReferenceEditor(StringUtil.getPackageName(myContextClass.getQualifiedName()),
                                                                      moduleFormContextClass.getProject(),
                                                                      null, getTestClassPackageScope(moduleFormContextClass),
                                                                      RefactoringBundle.message("choose.destination.package"));

      final Condition<JSClass> filter = jsClass -> {
        final JSAttributeList attributeList = jsClass.getAttributeList();
        return !jsClass.isInterface() && attributeList != null && !attributeList.hasModifier(JSAttributeList.ModifierType.FINAL);
      };

      mySuperClassField = JSReferenceEditor.forClassName("", moduleFormContextClass.getProject(), null, getSuperClassScope(moduleFormContextClass), null, filter,
                                                         JavaScriptBundle.message("choose.super.class.title"));


      final List<JSMemberInfo> memberInfos = new ArrayList<>();
      JSMemberInfo.extractClassMembers(myContextClass, memberInfos, new MemberInfoBase.Filter<>() {
        @Override
        public boolean includeMember(final JSAttributeListOwner member) {
          final JSAttributeList attributeList = member.getAttributeList();
          return member instanceof JSFunction &&
                 ((JSFunction)member).getKind() != JSFunction.FunctionKind.CONSTRUCTOR &&
                 attributeList != null &&
                 attributeList.getAccessType() == JSAttributeList.AccessType.PUBLIC;
        }
      });
      myMemberSelectionPanel = new JSMemberSelectionPanel("Generate test methods for:", memberInfos, null);
    }
    {
      // GUI initializer generated by IntelliJ IDEA GUI Designer
      // >>> IMPORTANT!! <<<
      // DO NOT EDIT OR ADD ANY CODE HERE!
      myMainPanel = new JPanel();
      myMainPanel.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
      final JLabel label1 = new JLabel();
      label1.setText("Test class name:");
      label1.setDisplayedMnemonic('T');
      label1.setDisplayedMnemonicIndex(0);
      myMainPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                  false));
      myTestClassNameTextField = new JTextField();
      myMainPanel.add(myTestClassNameTextField,
                      new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                          new Dimension(150, -1), null, 0, false));
      myMainPanel.add(myMemberSelectionPanel, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                  GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                  GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
      final JLabel label2 = new JLabel();
      label2.setText("Super class:");
      label2.setDisplayedMnemonic('S');
      label2.setDisplayedMnemonicIndex(0);
      myMainPanel.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                  false));
      final JLabel label3 = new JLabel();
      label3.setText("Generate:");
      myMainPanel.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                  false));
      myMainPanel.add(mySuperClassField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                             GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                                             null, null, 0, false));
      final JLabel label4 = new JLabel();
      label4.setText("Destination package:");
      label4.setDisplayedMnemonic('D');
      label4.setDisplayedMnemonicIndex(0);
      myMainPanel.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                  false));
      myMainPanel.add(myPackageCombo, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                          GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                                          null, null, 0, false));
      final JPanel panel1 = new JPanel();
      panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
      myMainPanel.add(panel1, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
                                                  null, 0, false));
      mySetUpCheckBox = new JCheckBox();
      mySetUpCheckBox.setText("setUp()");
      mySetUpCheckBox.setMnemonic('U');
      mySetUpCheckBox.setDisplayedMnemonicIndex(3);
      panel1.add(mySetUpCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                      GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myTearDownCheckBox = new JCheckBox();
      myTearDownCheckBox.setText("tearDown()");
      myTearDownCheckBox.setMnemonic('E');
      myTearDownCheckBox.setDisplayedMnemonicIndex(1);
      panel1.add(myTearDownCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                         GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                         GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      final Spacer spacer1 = new Spacer();
      panel1.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                              GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
      myCreateTestSourceFolderCheckBox = new JCheckBox();
      myCreateTestSourceFolderCheckBox.setText("Create test source folder:");
      myCreateTestSourceFolderCheckBox.setMnemonic('C');
      myCreateTestSourceFolderCheckBox.setDisplayedMnemonicIndex(0);
      myMainPanel.add(myCreateTestSourceFolderCheckBox,
                      new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myTestSourceFolderTextField = new JTextField();
      myMainPanel.add(myTestSourceFolderTextField,
                      new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                          new Dimension(150, -1), null, 0, false));
      label1.setLabelFor(myTestClassNameTextField);
      label2.setLabelFor(mySuperClassField);
      label4.setLabelFor(myPackageCombo);
    }
    myTestClassNameTextField.setText(myContextClass.getName() + "Test");
    setTitle(CodeInsightBundle.message("intention.create.test"));

    myExistingTestSourceRoot = findExistingTestSourceRoot(module);

    myCreateTestSourceFolderCheckBox.setVisible(myExistingTestSourceRoot == null);
    myTestSourceFolderTextField.setVisible(myExistingTestSourceRoot == null);
    myTestSourceFolderTextField
      .setText(FileUtil.toSystemDependentName(suggestTestSourceRootPath(module, contextClass.getContainingFile().getVirtualFile())));

    myCreateTestSourceFolderCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        myTestSourceFolderTextField.setEnabled(myCreateTestSourceFolderCheckBox.isSelected());
      }
    });

    myCreateTestSourceFolderCheckBox
      .setSelected(PropertiesComponent.getInstance(module.getProject()).getBoolean(CREATE_TEST_SOURCE_FOLDER_KEY, true));
    myTestSourceFolderTextField.setEnabled(myCreateTestSourceFolderCheckBox.isSelected());

    init();
  }

  /** @noinspection ALL */
  public JComponent $$$getRootComponent$$$() { return myMainPanel; }

  private static String suggestTestSourceRootPath(final Module module, final VirtualFile file) {
    if (file != null) {
      final ContentEntry contentEntry = MarkRootsManager.findContentEntry(ModuleRootManager.getInstance(module), file);
      if (contentEntry != null) {
        boolean mavenStyle = false;
        for (VirtualFile srcRoot : contentEntry.getSourceFolderFiles()) {
          if (srcRoot.getUrl().equals(contentEntry.getUrl() + "/src/main/flex")) {
            mavenStyle = true;
            break;
          }
        }

        final String basePath = VfsUtilCore.urlToPath(contentEntry.getUrl()) + (mavenStyle ? "/src/test/flex" : "/testSrc");
        return UniqueNameGenerator.generateUniqueNameOneBased(basePath, p -> StandardFileSystems.local().findFileByPath(p) == null);
      }
    }
    return "";
  }

  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myTestClassNameTextField;
  }

  @Override
  protected ValidationInfo doValidate() {
    if (myCreateTestSourceFolderCheckBox.isVisible() && myCreateTestSourceFolderCheckBox.isSelected()) {
      final String path = FileUtil.toSystemIndependentName(myTestSourceFolderTextField.getText().trim());

      if (path.isEmpty()) return new ValidationInfo("Path is empty", myTestSourceFolderTextField);
      if (StandardFileSystems.local().findFileByPath(path) != null) {
        return new ValidationInfo("File or folder already exists", myTestSourceFolderTextField);
      }

      boolean underContentRoot = false;
      for (VirtualFile contentRoot : ModuleRootManager.getInstance(myModule).getContentRoots()) {
        if (path.startsWith(contentRoot.getPath() + "/")) {
          underContentRoot = true;
          break;
        }
      }

      if (!underContentRoot) {
        return new ValidationInfo("Test source folder must be under module content root", myTestSourceFolderTextField);
      }
    }

    return null;
  }

  @Override
  protected void doOKAction() {
    final String superClassFqn = mySuperClassField.getText().trim();
    final PsiElement element = ActionScriptClassResolver.findClassByQNameStatic(superClassFqn, getSuperClassScope(myModule));
    mySuperClass = element instanceof JSClass ? (JSClass)element : null;

    if (myCreateTestSourceFolderCheckBox.isVisible() && myCreateTestSourceFolderCheckBox.isSelected()) {
      myTargetDirectory = createTestSourceFolderAndPackage(myModule, myTestSourceFolderTextField.getText().trim(), getPackageName());
    }

    if (myTargetDirectory == null) {
      myTargetDirectory = JSRefactoringUtil
        .chooseOrCreateDirectoryForClass(myModule.getProject(), myModule, getTestClassPackageScope(myModule), getPackageName(),
                                         getTestClassName(), myExistingTestSourceRoot, ThreeState.YES);
    }

    if (myTargetDirectory != null) {
      if (myCreateTestSourceFolderCheckBox.isVisible()) {
        PropertiesComponent.getInstance(myModule.getProject())
          .setValue(CREATE_TEST_SOURCE_FOLDER_KEY, myCreateTestSourceFolderCheckBox.isSelected(), true);
      }

      super.doOKAction();
    }
  }

  public @Nullable JSClass getSuperClass() {
    return mySuperClass;
  }

  private static @Nullable PsiDirectory createTestSourceFolderAndPackage(final Module module,
                                                                         final String srcRootPath,
                                                                         final String packageName) {
    final String path = FileUtil.toSystemIndependentName(srcRootPath);

    VirtualFile contentRoot = null;
    for (VirtualFile root : ModuleRootManager.getInstance(module).getContentRoots()) {
      if (path.startsWith(root.getPath() + "/")) {
        contentRoot = root;
        break;
      }
    }

    if (contentRoot != null) {
      final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
      try {
        final VirtualFile finalContentRoot = contentRoot;
        final VirtualFile folder = ApplicationManager.getApplication().runWriteAction((NullableComputable<VirtualFile>)() -> {
          try {
            final VirtualFile srcRoot =
              VfsUtil.createDirectoryIfMissing(finalContentRoot, path.substring((finalContentRoot.getPath() + "/").length()));
            final VirtualFile folder1 =
              packageName.isEmpty() ? srcRoot : VfsUtil.createDirectoryIfMissing(srcRoot, packageName.replace('.', '/'));
            final ContentEntry contentEntry = MarkRootsManager.findContentEntry(model, folder1);
            if (contentEntry != null) {
              contentEntry.addSourceFolder(srcRoot, true);
              model.commit();

              return folder1;
            }
          }
          catch (IOException ignore) {/*unlucky*/}
          return null;
        });


        return folder == null ? null : PsiManager.getInstance(module.getProject()).findDirectory(folder);
      }
      finally {
        if (model.isWritable()) {
          model.dispose();
        }
      }
    }

    return null;
  }

  private static GlobalSearchScope getSuperClassScope(final Module module) {
    return GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
  }

  private static GlobalSearchScope getTestClassPackageScope(final Module module) {
    return GlobalSearchScope.moduleWithDependentsScope(module);
  }

  public String getTestClassName() {
    return myTestClassNameTextField.getText().trim();
  }

  public String getPackageName() {
    return myPackageCombo.getText().trim();
  }

  public PsiDirectory getTargetDirectory() {
    return myTargetDirectory;
  }

  private static @Nullable PsiDirectory findExistingTestSourceRoot(final Module module) {
    PsiDirectory testSourceRoot = null;
    final ModuleRootManager manager = ModuleRootManager.getInstance(module);
    for (VirtualFile srcRoot : manager.getSourceRoots(true)) {
      if (manager.getFileIndex().isInTestSourceContent(srcRoot)) {
        testSourceRoot = PsiManager.getInstance(module.getProject()).findDirectory(srcRoot);
        break;
      }
    }
    return testSourceRoot;
  }

  public boolean isGenerateSetUp() {
    return mySetUpCheckBox.isSelected();
  }

  public boolean isGenerateTearDown() {
    return myTearDownCheckBox.isSelected();
  }

  public JSMemberInfo[] getSelectedMemberInfos() {
    return JSMemberInfo.getSelected(myMemberSelectionPanel.getTable().getSelectedMemberInfos(), myContextClass, Conditions.alwaysTrue());
  }
}
