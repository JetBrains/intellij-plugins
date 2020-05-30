// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.refactoring.moveMembers;

import com.intellij.ide.util.PlatformPackageUtil;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.moveMembers.JSMemberInfoModel;
import com.intellij.lang.javascript.refactoring.moveMembers.JSMoveMembersOptions;
import com.intellij.lang.javascript.refactoring.ui.JSMemberSelectionTable;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.refactoring.ui.JSVisibilityPanel;
import com.intellij.lang.javascript.refactoring.util.ActionScriptRefactoringUtil;
import com.intellij.lang.javascript.refactoring.util.JSMemberInfo;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.DelegatingGlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.classMembers.MemberInfoChange;
import com.intellij.refactoring.move.MoveCallback;
import com.intellij.refactoring.ui.RefactoringDialog;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.usageView.UsageViewUtil;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ActionScriptMoveMembersDialog extends RefactoringDialog implements JSMoveMembersOptions {
  @NonNls private static final String RECENTS_KEY = "JSMoveMembersDialog.RECENTS_KEY";
  private final Project myProject;
  private final JSClass mySourceClass;
  private final String mySourceClassName;
  private final List<JSMemberInfo> myMemberInfos = new ArrayList<>();
  private final JSReferenceEditor myTfTargetClassName;
  private JSMemberSelectionTable myTable;
  private final MoveCallback myMoveCallback;

  JSVisibilityPanel myVisibilityPanel;
  private JSMemberInfoModel myMemberInfoModel;

  public ActionScriptMoveMembersDialog(Project project,
                                       JSClass sourceClass,
                                       final JSClass initialTargetClass,
                                       Set<JSElement> preselectMembers,
                                       MoveCallback moveCallback) {
    super(project, true);
    myProject = project;
    mySourceClass = sourceClass;
    myMoveCallback = moveCallback;
    setTitle(JavaScriptBundle.message("move.members.dialog.title"));

    mySourceClassName = mySourceClass.getQualifiedName();

    JSMemberInfo.extractStaticMembers(sourceClass, myMemberInfos, new JSMemberInfo.EmptyFilter<JSAttributeListOwner>());
    for (JSMemberInfo memberInfo : myMemberInfos) {
      memberInfo.setChecked(preselectMembers);
    }
    JSMemberInfo.sortByOffset(myMemberInfos);

    String fqName = initialTargetClass != null && !sourceClass.equals(initialTargetClass) ? initialTargetClass.getQualifiedName() : "";
    myTfTargetClassName = createTargetClassField(project, fqName, getScope(), mySourceClass.getContainingFile());
    init();
  }

  public static JSReferenceEditor createTargetClassField(Project project, String text, GlobalSearchScope scope, PsiElement context) {
    return JSReferenceEditor.forClassName(text, project, RECENTS_KEY, scope, null, null,
                                          RefactoringBundle.message("choose.destination.class"), context);
  }

  private GlobalSearchScope getScope() {
    return getScope(myProject);
  }

  public static GlobalSearchScope getScope(Project project) {
    GlobalSearchScope scope = GlobalSearchScope.projectScope(project); // we don't limit scope to comply with Java
    return new ScopeAllowingFileLocalSymbols(scope);
  }

  private void updateTargetClass() {
    myMemberInfoModel.setTargetClassName(getTargetClassName());
    myTable.fireExternalDataChange();
  }

  @Override
  @Nullable
  public String getMemberVisibility() {
    return myVisibilityPanel.getVisibility();
  }

  @Override
  protected String getDimensionServiceKey() {
    return "#com.intellij.lang.javascript.refactoring.movemethod.JSMoveMembersDialog";
  }

  private JTable createTable() {
    myMemberInfoModel = new JSMemberInfoModel(mySourceClass, null, false);
    myTable = new JSMemberSelectionTable(myMemberInfos, null, null);
    myTable.setMemberInfoModel(myMemberInfoModel);
    myTable.addMemberInfoChangeListener(myMemberInfoModel);
    myMemberInfoModel.memberInfoChanged(new MemberInfoChange<>(myMemberInfos));
    return myTable;
  }

  @Override
  protected JComponent createNorthPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel _panel;
    Box box = Box.createVerticalBox();

    _panel = new JPanel(new BorderLayout());
    JTextField sourceClassField = new JTextField();
    sourceClassField.setText(mySourceClassName);
    sourceClassField.setEditable(false);
    _panel.add(new JLabel(RefactoringBundle.message("move.members.move.members.from.label")), BorderLayout.NORTH);
    _panel.add(sourceClassField, BorderLayout.CENTER);
    box.add(_panel);

    box.add(Box.createVerticalStrut(10));

    _panel = new JPanel(new BorderLayout());
    JLabel label = new JLabel(RefactoringBundle.message("move.members.to.fully.qualified.name.label"));
    label.setLabelFor(myTfTargetClassName);
    _panel.add(label, BorderLayout.NORTH);
    _panel.add(myTfTargetClassName, BorderLayout.CENTER);
    box.add(_panel);

    myTfTargetClassName.addDocumentListener(new DocumentListener() {
      @Override
      public void documentChanged(@NotNull DocumentEvent e) {
        updateTargetClass();
        validateButtons();
      }
    });

    panel.add(box, BorderLayout.CENTER);
    panel.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);

    validateButtons();

    return panel;
  }

  @Override
  protected JComponent createCenterPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    JTable table = createTable();
    if (table.getRowCount() > 0) {
      table.getSelectionModel().addSelectionInterval(0, 0);
    }
    JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(table);
    Border titledBorder = IdeBorderFactory.createTitledBorder(
      RefactoringBundle.message("move.members.members.to.be.moved.border.title"), false);
    Border emptyBorder = BorderFactory.createEmptyBorder(0, 5, 5, 5);
    Border border = BorderFactory.createCompoundBorder(titledBorder, emptyBorder);
    scrollPane.setBorder(border);
    panel.add(scrollPane, BorderLayout.CENTER);

    myVisibilityPanel = new JSVisibilityPanel();
    myVisibilityPanel.setVisibility(null);
    panel.add(myVisibilityPanel, BorderLayout.EAST);
    return panel;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myTfTargetClassName.getChildComponent();
  }

  @Override
  public JSAttributeListOwner[] getSelectedMembers() {
    JSMemberInfo[] infos = JSMemberInfo.getSelected(myTable.getSelectedMemberInfos(), mySourceClass, Conditions.alwaysTrue());
    JSAttributeListOwner[] result = new JSAttributeListOwner[infos.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = infos[i].getMember();
    }
    return result;
  }

  @Override
  public String getTargetClassName() {
    return myTfTargetClassName.getText();
  }

  @Override
  protected void doAction() {
    String message = validateInputData();

    if (message != null) {
      if (message.length() != 0) {
        CommonRefactoringUtil
          .showErrorMessage(StringUtil.capitalizeWords(JavaScriptBundle.message("move.members.refactoring.name"), true), message, null, myProject);
      }
      return;
    }

    myTfTargetClassName.updateRecents();
    invokeRefactoring(new ActionScriptMoveMembersProcessor(getProject(), myMoveCallback, mySourceClass, getScope(), new JSMoveMembersOptions() {
      @Override
      public String getMemberVisibility() {
        return ActionScriptMoveMembersDialog.this.getMemberVisibility();
      }

      //public boolean makeEnumConstant() {
      //  return JSMoveMembersDialog.this.makeEnumConstant();
      //}

      @Override
      public JSAttributeListOwner[] getSelectedMembers() {
        return ActionScriptMoveMembersDialog.this.getSelectedMembers();
      }

      @Override
      public String getTargetClassName() {
        return ActionScriptMoveMembersDialog.this.getTargetClassName();
      }
    }));

    // TODO remember setting
    //JavaRefactoringSettings.getInstance().MOVE_PREVIEW_USAGES = isPreviewUsages();
  }

  @Override
  protected void canRun() throws ConfigurationException {
    if (StringUtil.isEmptyOrSpaces(getTargetClassName())) {
      throw new ConfigurationException(RefactoringBundle.message("no.destination.class.specified"));
    }
  }

  @Nullable
  private String validateInputData() {
    final String fqName = getTargetClassName();
    if (fqName != null && fqName.isEmpty()) {
      return RefactoringBundle.message("no.destination.class.specified");
    }
    else {
      if (!ActionScriptRefactoringUtil.isValidClassName(fqName, true)) {
        return RefactoringBundle.message("0.is.not.a.legal.fq.name", fqName);
      }
      else {
        JSClass targetClass = findOrCreateTargetClass(fqName);
        if (targetClass == null) {
          return "";
        }

        if (mySourceClass.equals(targetClass)) {
          return RefactoringBundle.message("source.and.destination.classes.should.be.different");
        }
        else if (!mySourceClass.getLanguage().equals(targetClass.getLanguage())) {
          return RefactoringBundle
            .message("move.to.different.language", UsageViewUtil.getType(mySourceClass), mySourceClass.getQualifiedName(),
                     targetClass.getQualifiedName());
        }
        else {
          return !targetClass.isWritable() &&
                 !JSRefactoringUtil.checkReadOnlyStatus(targetClass, null, ActionScriptMoveMembersHandler.getRefactoringName()) ? "" : null;
        }
      }
    }
  }

  @Nullable
  private JSClass findOrCreateTargetClass(final String fqName) {
    final String className = StringUtil.getShortName(fqName);
    final String packageName = StringUtil.getPackageName(fqName);

    final GlobalSearchScope scope = getScope();
    final JSClassResolver resolver = JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver();
    PsiElement aClass = resolver.findClassByQName(fqName, scope);
    if (aClass instanceof JSClass) return (JSClass)aClass;

    if (aClass != null) {
      Messages.showErrorDialog(myProject, JavaScriptBundle.message("class.0.cannot.be.created", fqName),
                               StringUtil.capitalizeWords(JavaScriptBundle.message("move.members.refactoring.name"), true));
      return null;
    }

    int answer = Messages.showYesNoDialog(myProject, RefactoringBundle.message("class.0.does.not.exist", fqName),
                                          StringUtil.capitalizeWords(JavaScriptBundle.message("move.members.refactoring.name"), true),
                                          Messages.getQuestionIcon());
    if (answer != Messages.YES) return null;

    Module module = ModuleUtilCore.findModuleForPsiElement(mySourceClass);
    PsiDirectory baseDir = PlatformPackageUtil.getDirectory(mySourceClass);
    final PsiDirectory targetDirectory = JSRefactoringUtil.chooseOrCreateDirectoryForClass(myProject, module, scope, packageName, className,
                                                                                           baseDir, ThreeState.UNSURE);
    if (targetDirectory == null) {
      return null;
    }

    final Ref<Exception> error = new Ref<>();
    final Ref<JSClass> newClass = new Ref<>();
    WriteCommandAction.runWriteCommandAction(myProject, RefactoringBundle.message("create.class.command", fqName), null, () -> {
      try {
        ActionScriptCreateClassOrInterfaceFix.createClass(className, packageName, targetDirectory, false);
        newClass.set((JSClass)resolver.findClassByQName(fqName, scope));
      }
      catch (Exception e) {
        error.set(e);
      }
    });

    if (!error.isNull()) {
      CommonRefactoringUtil.showErrorMessage(JavaScriptBundle.message("move.members.refactoring.name"), error.get().getMessage(), null, myProject);
      return null;
    }
    return newClass.get();
  }

  private static class ScopeAllowingFileLocalSymbols extends DelegatingGlobalSearchScope implements JSResolveUtil.AllowFileLocalSymbols {

    ScopeAllowingFileLocalSymbols(@NotNull GlobalSearchScope baseScope) {
      super(baseScope);
    }
  }
}
