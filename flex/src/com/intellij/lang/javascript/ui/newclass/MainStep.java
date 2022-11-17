// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.ui.newclass;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.fileTemplates.impl.AllFileTemplatesConfigurable;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.util.PlatformPackageUtil;
import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.JSExpressionStatement;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.impl.PublicInheritorFilter;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.ui.ActionScriptPackageChooserDialog;
import com.intellij.lang.javascript.ui.JSClassChooserDialog;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.TestSourcesFilter;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.EventDispatcher;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MainStep extends AbstractWizardStepEx {

  private final WizardModel myModel;
  protected final Project myProject;
  private final SortedListModel<String> myInterfacesListModel;
  @Nullable private Module myModule;
  private final String myPackageNameInitial;
  @Nullable private final JSClass myBaseClassifier;
  private final PsiElement myContext;

  private final JPanel myPanel;
  private final JTextField myClassNameTextField;
  private final JLabel myUpDownHint;
  private JSReferenceEditor myPackageCombo;
  private final ComboboxWithBrowseButton myTemplateComboWithBrowse;
  private final JLabel mySuperClassLabel;
  private JSReferenceEditor mySuperClassField;
  private final JLabel myInterfacesLabel;
  private final JPanel myInterfacesPanel;
  private final JPanel myPlaceholderPanel;

  private static final String DESTINATION_PACKAGE_RECENT_KEY = "CreateActionScriptClassDialog.DESTINATION_PACKAGE_RECENT_KEY";
  private final JBList myInterfacesList;
  private Collection<String> myLastInterfaces;
  private String myLastSuperclass;
  private final @NlsContexts.DialogTitle String mySuperclassChooserTitle;
  private Collection<String> myUnsetAttributes;
  private final Supplier<List<FileTemplate>> myApplicableTemplatesProvider;

  private final EventDispatcher<ChangeListener> myResizeDispatcher;

  static final Object ID = new Object();
  private final JPanel mySpacer;
  private final JLabel myNameLabel;

  @NotNull
  @Override
  public Object getStepId() {
    return ID;
  }

  @Nullable
  @Override
  public Object getNextStepId() {
    return myUnsetAttributes.isEmpty() ? null : CustomVariablesStep.ID;
  }

  @Nullable
  @Override
  public Object getPreviousStepId() {
    return null;
  }

  @Override
  public boolean isComplete() {
    return canFinish();
  }

  @Override
  public void commit(final CommitType commitType) throws CommitStepException {
    myModel.setClassName(getClassName());
    myModel.setPackageName(getPackageName());
    myModel.setSuperclassFqn(getSuperclassFqn());
    myModel.setInterfacesFqns(myInterfacesListModel.getItems());
    String templateName = ((FileTemplate)myTemplateComboWithBrowse.getComboBox().getSelectedItem()).getName();
    myModel.setTemplateName(templateName);
    myModel.setCustomVariables(templateName, myUnsetAttributes);

    if (commitType == CommitType.Finish) {
      myPackageCombo.updateRecents();
    }
  }

  @Override
  public JComponent getComponent() {
    return myPanel;
  }

  public MainStep(WizardModel model, final Project project,
                  final @Nullable String initialClassName,
                  final boolean isClassNameEditable,
                  final String packageName,
                  final @Nullable JSClass baseClassifier,
                  final boolean baseClassEditable,
                  final String templateName,
                  final PsiElement context,
                  final @NlsContexts.DialogTitle String superclassChooserTitle,
                  Supplier<List<FileTemplate>> applicableTemplatesProvider) {
    super(null);
    myModel = model;
    myProject = project;
    myPackageNameInitial = packageName;
    myBaseClassifier = baseClassifier;
    myContext = context;
    mySuperclassChooserTitle = superclassChooserTitle;
    myApplicableTemplatesProvider = applicableTemplatesProvider;

    createUIComponents();

    myPanel = new JPanel(new GridBagLayout());

    myNameLabel = new JLabel();
    setLabelTextAndMnemonic(myNameLabel, JavaScriptBundle.message("create.class.name.label"));
    Insets insets = new Insets(0, 0, 5, 0);
    myPanel.add(myNameLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
                                                    GridBagConstraints.HORIZONTAL, insets, 0, 0));

    myClassNameTextField = new JTextField();
    myClassNameTextField.setMinimumSize(new Dimension(300, myClassNameTextField.getMinimumSize().height));
    myPanel.add(myClassNameTextField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0, GridBagConstraints.SOUTH,
                                                             GridBagConstraints.HORIZONTAL, insets, 0, 0));
    myNameLabel.setLabelFor(myClassNameTextField);

    myUpDownHint = new JLabel();
    myPanel.add(myUpDownHint, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
                                                     GridBagConstraints.HORIZONTAL, new Insets(0, 10, 5, 5), 0, 0));

    final JLabel packageLabel = new JLabel();
    setLabelTextAndMnemonic(packageLabel, JavaScriptBundle.message("create.class.package.label"));
    myPanel.add(packageLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST,
                                                     GridBagConstraints.HORIZONTAL, insets, 0, 0));
    myPanel.add(myPackageCombo, new GridBagConstraints(1, 1, 2, 1, 1.0, 0, GridBagConstraints.SOUTH,
                                                       GridBagConstraints.HORIZONTAL, insets, 0, 0));

    JLabel templateLabel = new JLabel();
    setLabelTextAndMnemonic(templateLabel, JavaScriptBundle.message("create.class.template.label"));
    myPanel.add(templateLabel, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST,
                                                      GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    myTemplateComboWithBrowse = new ComboboxWithBrowseButton();
    myPanel.add(myTemplateComboWithBrowse, new GridBagConstraints(1, 2, 2, 1, 1.0, 0, GridBagConstraints.SOUTH,
                                                                  GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    templateLabel.setLabelFor(myTemplateComboWithBrowse.getChildComponent());

    mySpacer = new JPanel();
    myPanel.add(mySpacer, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST,
                                                 GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

    mySuperClassLabel = new JLabel();
    setLabelTextAndMnemonic(mySuperClassLabel, JavaScriptBundle.message("create.class.superclass.label"));
    myPanel.add(mySuperClassLabel, new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.WEST,
                                                          GridBagConstraints.HORIZONTAL, insets, 0, 0));
    myPanel.add(mySuperClassField, new GridBagConstraints(1, 4, 2, 1, 1.0, 0, GridBagConstraints.SOUTH,
                                                          GridBagConstraints.HORIZONTAL, insets, 0, 0));

    myInterfacesLabel = new JLabel();
    setLabelTextAndMnemonic(myInterfacesLabel, JavaScriptBundle.message("create.class.interfaces.label"));
    myNameLabel.setMinimumSize(mySuperClassLabel.getMinimumSize());
    myPanel.add(myInterfacesLabel, new GridBagConstraints(0, 5, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
                                                          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    myInterfacesPanel = new JPanel(new BorderLayout());
    myPanel.add(myInterfacesPanel, new GridBagConstraints(1, 5, 2, 1, 1.0, 1.0, GridBagConstraints.WEST,
                                                          GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    myPlaceholderPanel = new JPanel();
    myPanel.add(myPlaceholderPanel, new GridBagConstraints(1, 6, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
                                                           GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    // call of createUIComponents() is placed here by UI Designer instrumenter (before first method call which is setTitle())
    // myModule is initialized in createUIComponents() because it is used there
    // myModule = ModuleUtil.findModuleForPsiElement(context);

    UserActivityWatcher w = new UserActivityWatcher();
    w.register(myPanel);

    myClassNameTextField.setEditable(isClassNameEditable);

    myInterfacesListModel = new SortedListModel<>(StringUtil::naturalCompare);
    final String baseInterfaceFqn;
    if (myBaseClassifier != null && myBaseClassifier.isInterface()) {
      myInterfacesListModel.add(baseInterfaceFqn = myBaseClassifier.getQualifiedName());
    }
    else {
      baseInterfaceFqn = null;
    }

    myInterfacesList = new JBList(myInterfacesListModel);
    if (baseInterfaceFqn != null) {
      myInterfacesList.setCellRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(final JList list,
                                                      final Object value,
                                                      final int index,
                                                      final boolean isSelected,
                                                      final boolean cellHasFocus) {
          JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
                                                                    cellHasFocus);
          if (baseInterfaceFqn.equals(value)) {
            label.setForeground(UIUtil.getLabelDisabledForeground());
          }
          else {
            label.setForeground(UIUtil.getLabelForeground());
          }
          return label;
        }
      });
    }
    myInterfacesList.disableEmptyText();
    JPanel p = ToolbarDecorator.createDecorator(myInterfacesList).setAddAction(anActionButton -> {
      String chooserTitle = JavaScriptBundle.message("choose.super.interface.title");
      if (DumbService.getInstance(project).isDumb()) {
        Messages.showWarningDialog(JavaScriptBundle.message("class.chooser.not.available.in.dumb.mode"), chooserTitle);
        return;
      }
      JSClassChooserDialog chooser =
        new JSClassChooserDialog(project, chooserTitle, getSuperclassScope(), null,
                                 jsClass -> jsClass.isInterface() && myInterfacesListModel.indexOf(jsClass.getQualifiedName()) == -1);
      if (chooser.showDialog()) {
        JSClass selected = chooser.getSelectedClass();
        if (selected != null) {
          myInterfacesListModel.add(selected.getQualifiedName());
        }
      }
    }).setMoveUpAction(null).setMoveDownAction(null).
      setRemoveActionUpdater(e -> myBaseClassifier == null ||
                              !myBaseClassifier.isInterface() ||
                              !ArrayUtil.contains(myBaseClassifier.getQualifiedName(), myInterfacesList.getSelectedValues())).setVisibleRowCount(2).createPanel();
    myInterfacesPanel.add(p, BorderLayout.CENTER);
    myInterfacesLabel.setLabelFor(myInterfacesList);
    p.setMinimumSize(p.getPreferredSize());
    initUpDownHint();

    fillTemplates(templateName);

    packageLabel.setLabelFor(myPackageCombo.getChildComponent());
    packageLabel.setPreferredSize(mySuperClassLabel.getPreferredSize());

    mySuperClassLabel.setLabelFor(mySuperClassField);
    mySuperClassLabel.setText(JavaScriptBundle.message("superclass.label.text"));
    if (!baseClassEditable && myBaseClassifier != null && !myBaseClassifier.isInterface()) {
      mySuperClassLabel.setEnabled(false);
      mySuperClassField.setEnabled(false);
      mySuperClassField.setText(myBaseClassifier.getQualifiedName());
    }

    myTemplateComboWithBrowse.addActionListener(e -> {
      FileTemplate template = (FileTemplate)myTemplateComboWithBrowse.getComboBox().getSelectedItem();
      final ShowSettingsUtil util = ShowSettingsUtil.getInstance();
      util.editConfigurable(project, new AllFileTemplatesConfigurable(myProject));
      fillTemplates(template.getName());
      updateOnTemplateChange();
      fireStateChanged();
    });

    myTemplateComboWithBrowse.getComboBox().setRenderer(SimpleListCellRenderer.<FileTemplate>create(
      (label, value, index) -> {
      if (value != null) {
        label.setText(ActionScriptCreateClassOrInterfaceFix.getTemplateShortName(value.getName()));
        label.setIcon(ActionScriptCreateClassOrInterfaceFix.getTemplateIcon(value));
      }
    }));

    // need to register this listener *after* user activity watcher
    myTemplateComboWithBrowse.getComboBox().addItemListener(e -> updateOnTemplateChange());

    myClassNameTextField.setText(initialClassName);
    //myPackageCombo.getButton().setPreferredSize(myTemplateComboWithBrowse.getButton().getPreferredSize());

    myResizeDispatcher = EventDispatcher.create(ChangeListener.class);
    updateOnTemplateChange();
    w.addUserActivityListener(this::fireStateChanged);
  }


  protected boolean canFinish() {
    if (!LanguageNamesValidation.isIdentifier(JavascriptLanguage.INSTANCE, getClassName(), myProject)) {
      return false;
    }

    String text = getPackageName();
    if (text.length() == 0) {
      return true;
    }
    else {
      ASTNode node = JSChangeUtil.createJSTreeFromText(myProject, text);
      PsiElement elt;
      return node != null &&
             (elt = node.getPsi()) instanceof JSExpressionStatement &&
             (elt = ((JSExpressionStatement)elt).getExpression()) instanceof JSReferenceExpression &&
             ((JSReferenceExpression)elt).getReferencedName() != null &&
             elt.textMatches(text);
    }
  }

  private void updateOnTemplateChange() {
    Map<String, Object> properties = ActionScriptCreateClassOrInterfaceFix.createProperties("", "", "");

    FileTemplate template = (FileTemplate)myTemplateComboWithBrowse.getComboBox().getSelectedItem();
    String[] unsetAttributes;
    try {
      unsetAttributes = FileTemplateUtil.calculateAttributes(template.getText(), properties, false, myProject);
    }
    catch (Exception ex) {
      // broken template
      unsetAttributes = ArrayUtilRt.EMPTY_STRING_ARRAY;
    }

    boolean superInterfaces = ArrayUtil.contains(ActionScriptCreateClassOrInterfaceFix.SUPER_INTERFACES, unsetAttributes);
    boolean changed = false;
    if (superInterfaces) {
      changed = myInterfacesPanel.isVisible();
      myPlaceholderPanel.setVisible(false);
      myInterfacesLabel.setVisible(true);
      myInterfacesPanel.setVisible(true);
      if (myLastInterfaces != null) {
        myInterfacesListModel.addAll(myLastInterfaces);
        myLastInterfaces = null;
      }
    }
    else {
      if (myInterfacesPanel.isVisible()) {
        myLastInterfaces = myInterfacesListModel.getItems();
        changed = true;
      }
      myInterfacesLabel.setVisible(false);
      myInterfacesPanel.setVisible(false);
      myPlaceholderPanel.setVisible(true);
      myInterfacesListModel.clear();
    }

    if (ArrayUtil.contains(ActionScriptCreateClassOrInterfaceFix.SUPERCLASS, unsetAttributes)) {
      changed |= !mySuperClassLabel.isVisible();
      mySuperClassLabel.setVisible(true);
      mySuperClassField.setVisible(true);
      if (myLastSuperclass != null) {
        mySuperClassField.setText(myLastSuperclass);
        myLastSuperclass = null;
      }
    }
    else {
      if (mySuperClassField.isVisible()) {
        myLastSuperclass = getSuperclassFqn();
        changed = true;
      }
      mySuperClassLabel.setVisible(false);
      mySuperClassField.setVisible(false);
      mySuperClassField.setText(null);
    }

    mySpacer.setVisible(mySuperClassLabel.isVisible() || myInterfacesLabel.isVisible());

    if (changed) {
      myResizeDispatcher.getMulticaster().stateChanged(new ChangeEvent(this));
    }

    Set<String> attrs = ContainerUtil.set(unsetAttributes);
    attrs.remove(ActionScriptCreateClassOrInterfaceFix.SUPERCLASS);
    attrs.remove(ActionScriptCreateClassOrInterfaceFix.SUPER_INTERFACES);
    attrs.remove(FileTemplate.ATTRIBUTE_NAME);
    // TODO remove when IDEA-60112 is fixed
    attrs.remove("foreach");
    attrs.remove("i");
    myUnsetAttributes = attrs;
  }

  public void addListener(@NotNull final ChangeListener listener,
                          @NotNull final Disposable parentDisposable) {
    myResizeDispatcher.addListener(listener, parentDisposable);
  }

  protected String getSuperclassFqn() {
    return mySuperClassField.getText().trim();
  }

  protected boolean isSuperclassFieldEnabled() {
    return mySuperClassField.isVisible();
  }

  private void initUpDownHint() {
    if (!myClassNameTextField.isEditable()) {
      myUpDownHint.setVisible(false);
      return;
    }

    myUpDownHint.setText("");
    myUpDownHint.setIcon(PlatformIcons.UP_DOWN_ARROWS);

    final AnAction arrow = new AnAction() {
      @Override
      public void actionPerformed(@NotNull final AnActionEvent e) {
        if (e.getInputEvent() instanceof KeyEvent) {
          final int code = ((KeyEvent)e.getInputEvent()).getKeyCode();
          final int delta = code == KeyEvent.VK_DOWN ? 1 : code == KeyEvent.VK_UP ? -1 : 0;

          final JComboBox comboBox = myTemplateComboWithBrowse.getComboBox();
          final int size = comboBox.getModel().getSize();
          if (size == 0) {
            return;
          }
          int next = comboBox.getSelectedIndex() + delta;
          if (next < 0 || next >= size) {
            if (!UISettings.getInstance().getCycleScrolling()) {
              return;
            }
            next = (next + size) % size;
          }
          comboBox.setSelectedIndex(next);
        }
      }
    };
    final KeyboardShortcut up = new KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), null);
    final KeyboardShortcut down = new KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), null);
    arrow.registerCustomShortcutSet(new CustomShortcutSet(up, down), myClassNameTextField);
  }

  private void fillTemplates(final String templateName) {
    List<FileTemplate> applicableTemplates = myApplicableTemplatesProvider.get();
    myTemplateComboWithBrowse.getComboBox().setModel(new DefaultComboBoxModel(ArrayUtil.toObjectArray(applicableTemplates)));
    FileTemplate selectedTemplate = ContainerUtil.find(applicableTemplates, fileTemplate -> fileTemplate.getName().equals(templateName));
    if (selectedTemplate == null && !applicableTemplates.isEmpty()) {
      selectedTemplate = applicableTemplates.get(0);
    }
    myTemplateComboWithBrowse.getComboBox().setSelectedItem(selectedTemplate);
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myClassNameTextField.isEditable() ? myClassNameTextField : myPackageCombo.getChildComponent();
  }

  private String getClassName() {
    return myClassNameTextField.getText().trim();
  }

  private String getPackageName() {
    return myPackageCombo.getText().trim();
  }

  private void createUIComponents() {
    // need to configure myModule prior to getTargetClassScopeAndBaseDir() call
    myModule = ModuleUtilCore.findModuleForPsiElement(myContext);
    myPackageCombo =
      ActionScriptPackageChooserDialog
        .createPackageReferenceEditor(myPackageNameInitial, myProject, DESTINATION_PACKAGE_RECENT_KEY, myModel.getTargetClassScopeAndBaseDir().first,
                                      RefactoringBundle.message("choose.destination.package"));
    myPackageCombo.setHeightProvider(() -> myTemplateComboWithBrowse.getChildComponent().getPreferredSize().height);

    final GlobalSearchScope superClassScope = getSuperclassScope();
    final Condition<JSClass> filter;
    if (myModule == null || myBaseClassifier == null || myBaseClassifier.isInterface() || !filterByBaseClass()) {
      filter = this::canBeSuperClass;
    }
    else {
      String fqn = myBaseClassifier.getQualifiedName();
      filter = new PublicInheritorFilter(myProject, fqn, superClassScope, false);
    }
    String fqn =
      StringUtil.notNullize(myBaseClassifier != null && !myBaseClassifier.isInterface() ? myBaseClassifier.getQualifiedName() : null);
    mySuperClassField =
      JSReferenceEditor.forClassName(fqn, myProject, null, superClassScope, null, filter, mySuperclassChooserTitle);
  }

  protected boolean canBeSuperClass(final JSClass jsClass) {
    return !jsClass.isInterface() && !jsClass.getAttributeList().hasModifier(JSAttributeList.ModifierType.FINAL);
  }

  public void setSuperclassLabelText(@Nls String text) {
    setLabelTextAndMnemonic(mySuperClassLabel, text);
    myNameLabel.setMinimumSize(new Dimension(Math.max(mySuperClassLabel.getMinimumSize().width, myNameLabel.getMinimumSize().width),
                                             myNameLabel.getMinimumSize().height));
  }

  private static void setLabelTextAndMnemonic(final JLabel label, final @Nls String text) {
    label.setText(UIUtil.removeMnemonic(text));
    label.setDisplayedMnemonic(text.charAt(UIUtil.getDisplayMnemonicIndex(text) + 1));
  }

  protected GlobalSearchScope getSuperclassScope() {
    PsiDirectory baseDir = myContext instanceof PsiDirectory ? (PsiDirectory)myContext : PlatformPackageUtil.getDirectory(myContext);
    boolean test = baseDir != null && TestSourcesFilter.isTestSources(baseDir.getVirtualFile(), myProject);
    return myModule == null
           ? GlobalSearchScope.allScope(myProject)
           : GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule, test);
  }

  protected boolean filterByBaseClass() {
    return true;
  }
}
