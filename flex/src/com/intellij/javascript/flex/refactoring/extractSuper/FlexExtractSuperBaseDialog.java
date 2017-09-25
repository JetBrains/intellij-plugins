/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.javascript.flex.refactoring.extractSuper;

import com.intellij.ide.util.PlatformPackageUtil;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.lang.javascript.refactoring.extractSuper.JSExtractSuperMode;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.refactoring.util.JSMemberInfo;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.classMembers.MemberInfoBase;
import com.intellij.refactoring.extractSuperclass.ExtractSuperBaseDialog;
import com.intellij.refactoring.util.DocCommentPolicy;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

public abstract class FlexExtractSuperBaseDialog extends ExtractSuperBaseDialog<JSClass, JSMemberInfo> {

  private static final String DESTINATION_PACKAGE_RECENT_KEY = "JSExtractSuperBase.RECENT_KEYS";

  private JRadioButton myExtractAndTurnRefsRB;

  public FlexExtractSuperBaseDialog(@NotNull JSClass sourceClass, @NotNull List<JSMemberInfo> members, String refactoringName) {
    super(sourceClass.getProject(), sourceClass, members, refactoringName);
  }

  @Override
  protected void customizeRadiobuttons(Box box, ButtonGroup buttonGroup) {
    myExtractAndTurnRefsRB = new JRadioButton(JSBundle.message("extract.0.turn.refs", StringUtil.decapitalize(getEntityName())));
    buttonGroup.add(myExtractAndTurnRefsRB);
    box.add(myExtractAndTurnRefsRB, 1);
    myExtractAndTurnRefsRB.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        updateDialog();
      }
    });
  }

  @Override
  protected ComponentWithBrowseButton createPackageNameField() {
    String packageName = StringUtil.getPackageName(mySourceClass.getQualifiedName());
    return JSReferenceEditor.forPackageName(packageName, myProject, DESTINATION_PACKAGE_RECENT_KEY, getScope(),
                                            RefactoringBundle.message("choose.destination.package"));
  }

  @Override
  protected String getTargetPackageName() {
    return ((JSReferenceEditor)myPackageNameField).getText();
  }

  @Override
  protected JTextField createSourceClassField() {
    JTextField result = new JTextField(mySourceClass.getQualifiedName());
    result.setEditable(false);
    return result;
  }

  @Override
  @Nullable
  protected String validateName(String name) {
    if (name != null && name.isEmpty()) {
      return RefactoringBundle.message("no.destination.class.specified");
    }
    if (!JSUtils.isValidClassName(name, false)) {
      return JSBundle.message("0.is.not.a.legal.name", name);
    }
    return null;
  }

  @Override
  protected void preparePackage() throws OperationFailedException {
    final Module module = ModuleUtil.findModuleForPsiElement(mySourceClass);
    final PsiDirectory baseDir = PlatformPackageUtil.getDirectory(mySourceClass);
    myTargetDirectory = JSRefactoringUtil.chooseOrCreateDirectoryForClass(myProject, module, getScope(), getTargetPackageName(),
                                                                          getExtractedSuperName(), baseDir, ThreeState.UNSURE);
    if (myTargetDirectory == null) {
      throw new OperationFailedException(""); // just cancel
    }
  }

  private GlobalSearchScope getScope() {
    final Module module = ModuleUtil.findModuleForPsiElement(mySourceClass);
    final PsiDirectory baseDir = PlatformPackageUtil.getDirectory(mySourceClass);
    if (getMode() == JSExtractSuperMode.RenameImplementation) {
      return PlatformPackageUtil.adjustScope(baseDir, GlobalSearchScope.moduleWithDependentsScope(module), true, false);
    }
    else {
      return PlatformPackageUtil.adjustScope(baseDir, GlobalSearchScope.moduleWithDependenciesScope(module), false, true);
    }
  }

  @Override
  protected String getDestinationPackageRecentKey() {
    return DESTINATION_PACKAGE_RECENT_KEY;
  }

  @Override
  public Collection<JSMemberInfo> getSelectedMemberInfos() {
    // consider getters and setters
    return Arrays.asList(JSMemberInfo.getSelected(myMemberInfos, mySourceClass, Conditions.alwaysTrue()));
  }

  @Override
  protected void executeRefactoring() {
    invokeRefactoring(createProcessor());
  }

  protected JSExtractSuperMode getMode() {
    if (isExtractSuperclass()) {
      return JSExtractSuperMode.ExtractSuper;
    }
    else if (myExtractAndTurnRefsRB != null && myExtractAndTurnRefsRB.isSelected()) {
      return JSExtractSuperMode.ExtractSuperTurnRefs;
    }
    else {
      return JSExtractSuperMode.RenameImplementation;
    }
  }


  @Override
  protected String getDocCommentPanelName() {
    return JSBundle.message("asdoc");
  }

  @Override
  protected int getDocCommentPolicySetting() {
    return DocCommentPolicy.MOVE; // TODO
  }

  @Override
  protected void setDocCommentPolicySetting(int policy) {
    // TODO
  }

  @Override
  protected void updateDialog() {
    super.updateDialog();
    boolean canSetPackage =
      getMode() != JSExtractSuperMode.RenameImplementation || !JSResolveUtil.isFileLocalSymbol(mySourceClass);
    myPackageNameField.setEnabled(canSetPackage);
    myPackageNameLabel.setEnabled(canSetPackage);
    ((JSReferenceEditor)myPackageNameField).setScope(getScope());
  }
}
