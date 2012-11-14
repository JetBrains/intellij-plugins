package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.TemplateKindCombo;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.impl.AllFileTemplatesConfigurable;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceFix;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class NewFlexComponentDialog extends CreateFileFromTemplateDialog {
  protected static final String PARENT_COMPONENT = "PARENT_COMPONENT";
  public static final String PARENT_COMPONENT_RECENTS_KEY = "CreateFlexComponent.ParentComponent";

  private JTextField myNameField;
  private JLabel myKindLabel;
  private JLabel myUpDownHint;
  private JPanel myContentPane;
  protected JSReferenceEditor myParentComponentField;
  private TemplateKindCombo myKindCombo;
  private JLabel myParentComponentLabel;
  protected final Project myProject;
  @Nullable private final PsiDirectory myDirectory;

  public NewFlexComponentDialog(Project project, @Nullable PsiDirectory directory) {
    super(project);
    myProject = project;
    myDirectory = directory;

    myUpDownHint.setIcon(PlatformIcons.UP_DOWN_ARROWS);
    myKindLabel.setLabelFor(myKindCombo.getChildComponent());
    myKindCombo.registerUpDownHint(myNameField);

    myParentComponentLabel.setLabelFor(myParentComponentField.getChildComponent());

    fillTemplates(null);

    myKindCombo.setButtonListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String selectedName = myKindCombo.getSelectedName();
        final ShowSettingsUtil util = ShowSettingsUtil.getInstance();
        util.editConfigurable(myProject, new AllFileTemplatesConfigurable());
        fillTemplates(selectedName);
      }
    });

    myKindCombo.getChildComponent().addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        myParentComponentField.setEnabled(NewFlexComponentAction.isClassifierTemplate(myKindCombo.getSelectedName()));
      }
    });
    init();
  }

  private void fillTemplates(String toSelect) {
    myKindCombo.clear();
    for (FileTemplate fileTemplate : CreateClassOrInterfaceFix
      .getApplicableTemplates(CreateClassOrInterfaceFix.FLEX_TEMPLATES_EXTENSIONS)) {
      String templateName = fileTemplate.getName();
      String shortName = CreateClassOrInterfaceFix.getTemplateShortName(templateName);
      Icon icon = FileTypeManager.getInstance().getFileTypeByExtension(fileTemplate.getExtension()).getIcon();
      myKindCombo.addItem(shortName, icon, templateName);
    }
    myKindCombo.setSelectedName(toSelect);
  }

  @Override
  protected JComponent createCenterPanel() {
    return myContentPane;
  }

  @Override
  protected JTextField getNameField() {
    return myNameField;
  }

  @Override
  protected TemplateKindCombo getKindCombo() {
    return myKindCombo;
  }

  @Override
  public void show() {
    super.show();
    if (getExitCode() == OK_EXIT_CODE) {
      myParentComponentField.updateRecents();
    }
  }

  private void createUIComponents() {
    Module module = myDirectory != null ? ModuleUtil.findModuleForPsiElement(myDirectory) : null;
    GlobalSearchScope scope = getParentComponentScope(module, myProject);
    myParentComponentField = createParentComponentField(myProject, scope);
    myParentComponentField.setHeightProvider(new Computable<Integer>() {
      @Override
      public Integer compute() {
        return myKindCombo.getChildComponent().getPreferredSize().height;
      }
    });
  }

  public static GlobalSearchScope getParentComponentScope(@Nullable Module module, @NotNull Project project) {
    return module != null ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module) : GlobalSearchScope.allScope(project);
  }

  public static JSReferenceEditor createParentComponentField(Project project, GlobalSearchScope scope) {
    return JSReferenceEditor.forClassName("", project, PARENT_COMPONENT_RECENTS_KEY, scope,
                                null, new Condition<JSClass>() {
        @Override
        public boolean value(JSClass jsClass) {
          JSAttributeList attributeList;
          return !jsClass.isInterface() &&
                 (attributeList = jsClass.getAttributeList()) != null &&
                 !attributeList.hasModifier(JSAttributeList.ModifierType.FINAL);
        }
      }, FlexBundle.message("choose.parent.component.dialog.title"));
  }
}
