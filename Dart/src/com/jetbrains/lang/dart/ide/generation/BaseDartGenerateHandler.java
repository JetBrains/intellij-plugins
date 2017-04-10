package com.jetbrains.lang.dart.ide.generation;

import com.intellij.ide.util.MemberChooser;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.ide.DartNamedElementNode;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartClassDefinition;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class BaseDartGenerateHandler implements LanguageCodeInsightActionHandler {
  @Override
  public boolean isValidFor(@NotNull final Editor editor, @NotNull final PsiFile file) {
    return file instanceof DartFile &&
           PsiTreeUtil.getParentOfType(file.findElementAt(editor.getCaretModel().getOffset()), DartClass.class) != null;
  }

  @Override
  public void invoke(@NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file) {
    invoke(project, editor, file, editor.getCaretModel().getOffset());
  }

  public void invoke(@NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file, final int offset) {
    final DartClass dartClass = PsiTreeUtil.getParentOfType(file.findElementAt(offset), DartClassDefinition.class);
    if (dartClass == null) return;

    final List<DartComponent> candidates = new ArrayList<>();
    collectCandidates(dartClass, candidates);

    List<DartNamedElementNode> selectedElements = Collections.emptyList();
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      selectedElements = ContainerUtil.map(candidates, DartNamedElementNode::new);
    }
    else if (!candidates.isEmpty()) {
      final MemberChooser<DartNamedElementNode> chooser = createMemberChooserDialog(project, dartClass, candidates, getTitle());
      chooser.show();
      if (chooser.getExitCode() != DialogWrapper.OK_EXIT_CODE) return;

      selectedElements = chooser.getSelectedElements();
    }

    doInvoke(project, editor, file, selectedElements, createFix(dartClass));
  }

  protected void doInvoke(@NotNull final Project project,
                          @NotNull final Editor editor,
                          @NotNull final PsiFile file,
                          @NotNull final Collection<DartNamedElementNode> selectedElements,
                          @NotNull final BaseCreateMethodsFix createMethodsFix) {
    Runnable runnable = new Runnable() {
      public void run() {
        createMethodsFix.addElementsToProcessFrom(selectedElements);
        createMethodsFix.beforeInvoke(project, editor, file);

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            try {
              createMethodsFix.invoke(project, editor, file);
            }
            catch (IncorrectOperationException ex) {
              Logger.getInstance(getClass().getName()).error(ex);
            }
          }
        });
      }
    };

    if (CommandProcessor.getInstance().getCurrentCommand() == null) {
      CommandProcessor.getInstance().executeCommand(project, runnable, getClass().getName(), null);
    }
    else {
      runnable.run();
    }
  }

  @NotNull
  protected abstract BaseCreateMethodsFix createFix(@NotNull final DartClass dartClass);

  @NotNull
  protected abstract String getTitle();

  protected abstract void collectCandidates(@NotNull final DartClass dartClass, @NotNull final List<DartComponent> candidates);

  private final static Condition<DartComponent> NOT_CONSTRUCTOR_CONDITION =
    component -> DartComponentType.typeOf(component) != DartComponentType.CONSTRUCTOR;

  private final static Condition<DartComponent> NOT_STATIC_CONDITION = component -> !component.isStatic();

  @NotNull
  protected final Map<Pair<String, Boolean>, DartComponent> computeClassMembersMap(@NotNull final DartClass dartClass,
                                                                                   final boolean doIncludeStatics) {
    List<DartComponent> classMembers = DartResolveUtil.getNamedSubComponents(dartClass);
    classMembers = ContainerUtil.filter(classMembers, NOT_CONSTRUCTOR_CONDITION);
    if (!doIncludeStatics) {
      classMembers = ContainerUtil.filter(classMembers, NOT_STATIC_CONDITION);
    }
    return DartResolveUtil.namedComponentToMap(classMembers);
  }

  @NotNull
  protected final Map<Pair<String, Boolean>, DartComponent> computeSuperClassesMemberMap(@NotNull final DartClass dartClass) {
    final List<DartClass> superClasses = new ArrayList<>();
    final List<DartClass> superInterfaces = new ArrayList<>();

    DartResolveUtil.collectSupers(superClasses, superInterfaces, dartClass);

    List<DartComponent> superClassesMembers = new ArrayList<>();
    for (DartClass superClass : superClasses) {
      superClassesMembers.addAll(DartResolveUtil.getNamedSubComponents(superClass));
    }

    superClassesMembers = ContainerUtil.filter(superClassesMembers, NOT_CONSTRUCTOR_CONDITION);
    superClassesMembers = ContainerUtil.filter(superClassesMembers, NOT_STATIC_CONDITION);

    return DartResolveUtil.namedComponentToMap(superClassesMembers);
  }

  @NotNull
  protected final Map<Pair<String, Boolean>, DartComponent> computeSuperInterfacesMembersMap(@NotNull final DartClass dartClass) {
    final List<DartClass> superClasses = new ArrayList<>();
    final List<DartClass> superInterfaces = new ArrayList<>();

    DartResolveUtil.collectSupers(superClasses, superInterfaces, dartClass);

    List<DartComponent> superInterfacesMembers = new ArrayList<>();
    for (DartClass superInterface : superInterfaces) {
      superInterfacesMembers.addAll(DartResolveUtil.getNamedSubComponents(superInterface));
    }

    superInterfacesMembers = ContainerUtil.filter(superInterfacesMembers, NOT_CONSTRUCTOR_CONDITION);
    superInterfacesMembers = ContainerUtil.filter(superInterfacesMembers, NOT_STATIC_CONDITION);

    return DartResolveUtil.namedComponentToMap(superInterfacesMembers);
  }

  @Nullable
  protected JComponent getOptionsComponent(DartClass jsClass, final Collection<DartComponent> candidates) {
    return null;
  }

  protected MemberChooser<DartNamedElementNode> createMemberChooserDialog(@NotNull final Project project,
                                                                          @NotNull final DartClass dartClass,
                                                                          @NotNull final Collection<DartComponent> candidates,
                                                                          @NotNull final String title) {
    final List<DartNamedElementNode> nodes = ContainerUtil.map(candidates, DartNamedElementNode::new);
    final MemberChooser<DartNamedElementNode> chooser =
      new MemberChooser<DartNamedElementNode>(nodes.toArray(new DartNamedElementNode[0]), doAllowEmptySelection(), true, project, false) {

        protected JComponent createCenterPanel() {
          final JComponent superComponent = super.createCenterPanel();
          final JComponent optionsComponent = getOptionsComponent(dartClass, candidates);
          if (optionsComponent == null) {
            return superComponent;
          }
          else {
            final JPanel panel = new JPanel(new BorderLayout());
            panel.add(superComponent, BorderLayout.CENTER);
            panel.add(optionsComponent, BorderLayout.SOUTH);
            return panel;
          }
        }
      };

    chooser.setTitle(title);
    chooser.setCopyJavadocVisible(false);
    return chooser;
  }

  protected boolean doAllowEmptySelection() {
    return false;
  }
}
