package com.jetbrains.lang.dart.ide.generation;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.ide.util.MemberChooser;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public abstract class BaseDartGenerateHandler implements LanguageCodeInsightActionHandler {
  @Override
  public boolean isValidFor(Editor editor, PsiFile file) {
    return file instanceof DartFile;
  }

  @Override
  public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    invoke(project, editor, file, editor.getCaretModel().getOffset());
  }

  public void invoke(Project project, Editor editor, PsiFile file, int offset) {
    if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;
    final DartClass dartClass =
      PsiTreeUtil.getParentOfType(file.findElementAt(offset), DartClassDefinition.class);
    if (dartClass == null) return;

    final List<DartComponent> candidates = new ArrayList<DartComponent>();
    collectCandidates(dartClass, candidates);

    List<DartNamedElementNode> selectedElements = Collections.emptyList();
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      selectedElements = ContainerUtil.map(candidates, new Function<DartComponent, DartNamedElementNode>() {
        @Override
        public DartNamedElementNode fun(DartComponent namedComponent) {
          return new DartNamedElementNode(namedComponent);
        }
      });
    }
    else if (!candidates.isEmpty()) {
      final MemberChooser<DartNamedElementNode> chooser =
        createMemberChooserDialog(project, dartClass, candidates, getTitle());
      chooser.show();
      selectedElements = chooser.getSelectedElements();
    }

    final BaseCreateMethodsFix createMethodsFix = createFix(dartClass);
    doInvoke(project, editor, file, selectedElements, createMethodsFix);
  }

  protected void doInvoke(final Project project,
                          final Editor editor,
                          final PsiFile file,
                          final Collection<DartNamedElementNode> selectedElements,
                          final BaseCreateMethodsFix createMethodsFix) {
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

  protected abstract BaseCreateMethodsFix createFix(DartClass dartClass);

  protected abstract String getTitle();

  protected void collectCandidates(DartClass aClass, List<DartComponent> candidates) {
    final List<DartClass> superClasses = new ArrayList<DartClass>();
    final List<DartClass> superInterfaces = new ArrayList<DartClass>();

    DartResolveUtil.collectSupers(superClasses, superInterfaces, aClass);

    List<DartComponent> classMembers = DartResolveUtil.getNamedSubComponents(aClass);
    List<DartComponent> superClassesMembers = new ArrayList<DartComponent>();
    for (DartClass superClass : superClasses) {
      superClassesMembers.addAll(DartResolveUtil.getNamedSubComponents(superClass));
    }
    List<DartComponent> superInterfacesMembers = new ArrayList<DartComponent>();
    for (DartClass superInterface : superInterfaces) {
      superInterfacesMembers.addAll(DartResolveUtil.getNamedSubComponents(superInterface));
    }

    final Condition<DartComponent> notConstructorCondition = new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        return DartComponentType.typeOf(component) != DartComponentType.CONSTRUCTOR;
      }
    };
    classMembers = ContainerUtil.filter(classMembers, notConstructorCondition);
    superClassesMembers = ContainerUtil.filter(superClassesMembers, notConstructorCondition);
    superInterfacesMembers = ContainerUtil.filter(superInterfacesMembers, notConstructorCondition);

    final Map<Pair<String, Boolean>, DartComponent> classMembersMap = DartResolveUtil.namedComponentToMap(classMembers);
    final Map<Pair<String, Boolean>, DartComponent> superClassesMembersMap = DartResolveUtil.namedComponentToMap(superClassesMembers);
    final Map<Pair<String, Boolean>, DartComponent> superInterfacesMembersMap = DartResolveUtil.namedComponentToMap(superInterfacesMembers);

    collectCandidates(classMembersMap, superClassesMembersMap, superInterfacesMembersMap, candidates);
  }

  /**
   * @param classMembersMap           of (component name, isGetter) -> component
   * @param superClassesMembersMap    of (component name, isGetter) -> component
   * @param superInterfacesMembersMap of (component name, isGetter) -> component
   * @param candidates                to process
   */
  protected abstract void collectCandidates(Map<Pair<String, Boolean>, DartComponent> classMembersMap,
                                            Map<Pair<String, Boolean>, DartComponent> superClassesMembersMap,
                                            Map<Pair<String, Boolean>, DartComponent> superInterfacesMembersMap,
                                            List<DartComponent> candidates);

  @Nullable
  protected JComponent getOptionsComponent(DartClass jsClass, final Collection<DartComponent> candidates) {
    return null;
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }

  protected MemberChooser<DartNamedElementNode> createMemberChooserDialog(final Project project,
                                                                          final DartClass dartClass,
                                                                          final Collection<DartComponent> candidates,
                                                                          String title) {
    final MemberChooser<DartNamedElementNode> chooser = new MemberChooser<DartNamedElementNode>(
      ContainerUtil.map(candidates, new Function<DartComponent, DartNamedElementNode>() {
        @Override
        public DartNamedElementNode fun(DartComponent namedComponent) {
          return new DartNamedElementNode(namedComponent);
        }
      }).toArray(new DartNamedElementNode[candidates.size()]), false, true, project, false) {

      protected void init() {
        super.init();
        myTree.addTreeSelectionListener(new TreeSelectionListener() {
          public void valueChanged(final TreeSelectionEvent e) {
            setOKActionEnabled(myTree.getSelectionCount() > 0);
          }
        });
      }

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
}
