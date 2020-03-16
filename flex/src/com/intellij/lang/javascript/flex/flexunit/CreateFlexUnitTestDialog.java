// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.ide.projectView.actions.MarkRootActionBase;
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
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.classMembers.MemberInfoBase;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateFlexUnitTestDialog extends DialogWrapper {

  private JPanel myMainPanel;
  private JTextField myTestClassNameTextField;
  private JSReferenceEditor myPackageCombo;
  private JCheckBox myCreateTestSourceFolderCheckBox;
  private JTextField myTestSourceFolderTextField;
  private JSReferenceEditor mySuperClassField;
  private JCheckBox mySetUpCheckBox;
  private JCheckBox myTearDownCheckBox;
  private JSMemberSelectionPanel myMemberSelectionPanel;

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

  private static String suggestTestSourceRootPath(final Module module, final VirtualFile file) {
    if (file != null) {
      final ContentEntry contentEntry = MarkRootActionBase.findContentEntry(ModuleRootManager.getInstance(module), file);
      if (contentEntry != null) {
        boolean mavenStyle = false;
        for (VirtualFile srcRoot : contentEntry.getSourceFolderFiles()) {
          if (srcRoot.getUrl().equals(contentEntry.getUrl() + "/src/main/flex")) {
            mavenStyle = true;
            break;
          }
        }

        final String basePath = VfsUtilCore.urlToPath(contentEntry.getUrl()) + (mavenStyle ? "/src/test/flex" : "/testSrc");
        String path = basePath;
        int i = 0;
        while (LocalFileSystem.getInstance().findFileByPath(path) != null) {
          path = basePath + (++i);
        }

        return path;
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

  private void createUIComponents() {
    final Module module = ModuleUtilCore.findModuleForPsiElement(myContextClass);
    assert module != null;

    myPackageCombo = JSReferenceEditor.forPackageName(StringUtil.getPackageName(myContextClass.getQualifiedName()), module.getProject(),
                                                      null, getTestClassPackageScope(module),
                                                      RefactoringBundle.message("choose.destination.package"));

    final Condition<JSClass> filter = jsClass -> {
      final JSAttributeList attributeList = jsClass.getAttributeList();
      return !jsClass.isInterface() && attributeList != null && !attributeList.hasModifier(JSAttributeList.ModifierType.FINAL);
    };

    mySuperClassField = JSReferenceEditor.forClassName("", module.getProject(), null, getSuperClassScope(module), null, filter,
                                                       JavaScriptBundle.message("choose.super.class.title"));


    final List<JSMemberInfo> memberInfos = new ArrayList<>();
    JSMemberInfo.extractClassMembers(myContextClass, memberInfos, new MemberInfoBase.Filter<JSAttributeListOwner>() {
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

  @Override
  protected ValidationInfo doValidate() {
    if (myCreateTestSourceFolderCheckBox.isVisible() && myCreateTestSourceFolderCheckBox.isSelected()) {
      final String path = FileUtil.toSystemIndependentName(myTestSourceFolderTextField.getText().trim());

      if (path.isEmpty()) return new ValidationInfo("Path is empty", myTestSourceFolderTextField);
      if (LocalFileSystem.getInstance().findFileByPath(path) != null) {
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
        PropertiesComponent.getInstance(myModule.getProject()).setValue(CREATE_TEST_SOURCE_FOLDER_KEY, myCreateTestSourceFolderCheckBox.isSelected(), true);
      }

      super.doOKAction();
    }
  }

  @Nullable
  private static PsiDirectory createTestSourceFolderAndPackage(final Module module, final String srcRootPath, final String packageName) {
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
            final ContentEntry contentEntry = MarkRootActionBase.findContentEntry(model, folder1);
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

  @Nullable
  private static PsiDirectory findExistingTestSourceRoot(final Module module) {
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

  @Nullable
  public JSClass getSuperClass() {
    return mySuperClass;
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
