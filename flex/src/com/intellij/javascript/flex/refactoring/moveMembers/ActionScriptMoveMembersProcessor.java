// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.refactoring.moveMembers;

import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.presentable.JSNamedElementPresenter;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.JSChangeVisibilityUtil;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.lang.javascript.refactoring.moveMembers.JSMoveMembersOptions;
import com.intellij.lang.javascript.refactoring.util.ActionScriptRefactoringUtil;
import com.intellij.lang.javascript.refactoring.util.JSMemberUsageInfo;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringConflictsUtil;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.move.MoveCallback;
import com.intellij.refactoring.move.MoveHandler;
import com.intellij.refactoring.move.MoveMemberViewDescriptor;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ActionScriptMoveMembersProcessor extends BaseRefactoringProcessor {
  private static final Logger LOG = Logger.getInstance(ActionScriptMoveMembersProcessor.class.getName());

  private final JSClass myTargetClass;
  private final Set<JSAttributeListOwner> myMembersToMove = new LinkedHashSet<>();
  private final MoveCallback myMoveCallback;
  private final JSClass mySourceClass;
  private final @Nullable String myNewVisibility; // "null" means "as is"
  private String myCommandName = JavaScriptBundle.message("move.members.refactoring.name");

  public ActionScriptMoveMembersProcessor(Project project,
                                          MoveCallback moveCallback,
                                          JSClass sourceClass,
                                          GlobalSearchScope scope,
                                          JSMoveMembersOptions options) {
    super(project);
    myMoveCallback = moveCallback;
    mySourceClass = sourceClass;

    JSAttributeListOwner[] members = options.getSelectedMembers();
    myMembersToMove.clear();
    ContainerUtil.addAll(myMembersToMove, members);

    myTargetClass = (JSClass)JSDialectSpecificHandlersFactory.forLanguage(FlexSupportLoader.ECMA_SCRIPT_L4).getClassResolver()
      .findClassByQName(options.getTargetClassName(), scope);

    setCommandName(sourceClass, members);

    myNewVisibility = options.getMemberVisibility();
  }

  @Override
  protected @NotNull String getCommandName() {
    return myCommandName;
  }

  private void setCommandName(JSClass sourceClass, final PsiElement[] members) {
    StringBuilder commandName = new StringBuilder();
    commandName.append(MoveHandler.getRefactoringName());
    commandName.append(" ");
    boolean first = true;
    for (PsiElement member : members) {
      if (member instanceof JSFunction &&
          ((JSFunction)member).isSetProperty() &&
          sourceClass.findFunctionByNameAndKind(((JSFunction)member).getName(), JSFunction.FunctionKind.GETTER) != null) {
        continue;
      }
      if (!first) commandName.append(", ");
      commandName.append(new JSNamedElementPresenter(member).getShortName());
      if (member instanceof JSFunction) {
        commandName.append("()");
      }
      first = false;
    }

    myCommandName = commandName.toString();
  }

  @Override
  protected @NotNull UsageViewDescriptor createUsageViewDescriptor(UsageInfo @NotNull [] usages) {
    return new MoveMemberViewDescriptor(PsiUtilCore.toPsiElementArray(myMembersToMove));
  }

  @Override
  protected UsageInfo @NotNull [] findUsages() {
    return JSRefactoringUtil.getUsages(myMembersToMove, myTargetClass);
  }

  @Override
  protected void refreshElements(PsiElement @NotNull [] elements) {
    LOG.assertTrue(myMembersToMove.size() == elements.length);
    myMembersToMove.clear();
    for (PsiElement element : elements) {
      myMembersToMove.add((JSAttributeListOwner)element);
    }
  }

  @Override
  protected void performRefactoring(final UsageInfo @NotNull [] usages) {
    try {
      final Collection<PsiFile> filesWithUsages = ActionScriptRefactoringUtil.qualifyIncomingReferences(usages);

      Collection<String> importsInTargetFile = new HashSet<>();
      Collection<String> namespacesInTargetFile = new HashSet<>();
      List<FormatFixer> postponedFormatters = new ArrayList<>();

      JSRefactoringUtil.addRemovalFormatters(mySourceClass, myMembersToMove, Conditions.alwaysTrue(), Conditions.alwaysTrue(), postponedFormatters);

      for (JSAttributeListOwner member : myMembersToMove) {
        final RefactoringElementListener elementListener = getTransaction().getElementListener(member);
        ActionScriptRefactoringUtil
          .fixOutgoingReferences(member, importsInTargetFile, namespacesInTargetFile, myMembersToMove, myTargetClass, false,
                                                myNewVisibility != null && !JSVisibilityUtil.ESCALATE_VISIBILITY.equals(myNewVisibility));
        if (member instanceof JSVariable) {
          // process attribute list is a part of VarStatement
          ActionScriptRefactoringUtil
            .fixOutgoingReferences(member.getAttributeList(), importsInTargetFile, namespacesInTargetFile, myMembersToMove, myTargetClass,
                                   false, myNewVisibility != null && !JSVisibilityUtil.ESCALATE_VISIBILITY.equals(myNewVisibility));
        }

        JSAttributeListOwner newMember = doMove(member, myTargetClass, postponedFormatters);
        elementListener.elementMoved(newMember);
        fixVisibility(newMember, usages);
        postponedFormatters.add(FormatFixer.create(JSRefactoringUtil.getElementToFormat(newMember), FormatFixer.Mode.Reformat));
      }

      JSRefactoringUtil
        .postProcess(mySourceClass, myTargetClass, filesWithUsages, importsInTargetFile, namespacesInTargetFile, postponedFormatters, true,
                     false);

      myMembersToMove.clear();
      if (myMoveCallback != null) {
        myMoveCallback.refactoringCompleted();
      }
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
  }

  private void fixVisibility(JSAttributeListOwner member, final UsageInfo[] usages) throws IncorrectOperationException {
    JSAttributeList modifierList = member.getAttributeList();

    if (myNewVisibility == null) return;

    if (JSVisibilityUtil.ESCALATE_VISIBILITY.equals(myNewVisibility)) {
      JSAttributeList.AccessType visibility = modifierList.getAccessType();
      for (UsageInfo usage : usages) {
        if (usage instanceof JSMemberUsageInfo) {
          final PsiElement place = usage.getElement();
          if (place != null) {
            visibility = JSVisibilityUtil.getEscalatedVisibility(member, place, visibility, JSVisibilityUtil.DEFAULT_OPTIONS);
            if (visibility == JSAttributeList.AccessType.PUBLIC) break; // can't escalate more
          }
        }
      }
      JSChangeVisibilityUtil.setVisibility(member, visibility);
    }
    else {
      JSChangeVisibilityUtil.setVisibility(member, myNewVisibility);
    }
  }

  @Override
  protected boolean preprocessUsages(@NotNull Ref<UsageInfo[]> refUsages) {
    if (myTargetClass == null) {
      return true;
    }
    final MultiMap<PsiElement, String> conflicts = new MultiMap<>();
    JSRefactoringConflictsUtil.checkMembersAlreadyExist(myMembersToMove, myTargetClass, conflicts);
    for (JSAttributeListOwner member : myMembersToMove) {
      JSRefactoringConflictsUtil.checkOutgoingReferencesAccessibility(member, myMembersToMove, myTargetClass, true, conflicts,
                                                                      Conditions.alwaysTrue(), JSVisibilityUtil.DEFAULT_OPTIONS);
    }

    final UsageInfo[] usages = refUsages.get();
    JSRefactoringConflictsUtil.checkIncomingReferencesAccessibility(usages, myTargetClass, myNewVisibility, conflicts,
                                                                    JSVisibilityUtil.DEFAULT_OPTIONS);
    JSRefactoringConflictsUtil.analyzeModuleConflicts(myProject, myMembersToMove, usages, myTargetClass, conflicts);
    return showConflicts(conflicts, usages);
  }

  @Override
  public void doRun() {
    if (myMembersToMove.isEmpty()) {
      String message = RefactoringBundle.message("no.members.selected");
      CommonRefactoringUtil.showErrorMessage(JavaScriptBundle.message("move.members.refactoring.name"), message, null, myProject);
      return;
    }
    super.doRun();
  }

  public static JSAttributeListOwner doMove(@NotNull JSAttributeListOwner member,
                                            @NotNull JSClass targetClass,
                                            List<? super FormatFixer> formatters) {
    final PsiElement insert;
    if (member instanceof JSFunction) {
      insert = member.copy();
    }
    else {
      insert = JSRefactoringUtil.getVarStatementCopy((JSVariable)member);
    }

    final PsiElement inserted = JSRefactoringUtil.addMemberToTargetClass(targetClass, insert);
    formatters.add(FormatFixer.create(inserted, FormatFixer.Mode.Reformat));
    JSRefactoringUtil.handleDocCommentAndFormat(inserted, formatters);
    JSRefactoringUtil.deleteWithNoPostponedFormatting(member);
    return inserted instanceof JSFunction ? (JSAttributeListOwner)inserted : PsiTreeUtil.getChildOfType(inserted, JSVariable.class);
  }
}
