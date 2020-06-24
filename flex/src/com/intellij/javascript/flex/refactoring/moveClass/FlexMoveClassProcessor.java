package com.intellij.javascript.flex.refactoring.moveClass;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.presentable.Capitalization;
import com.intellij.lang.javascript.presentable.JSFormatUtil;
import com.intellij.lang.javascript.presentable.JSNamedElementPresenter;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringConflictsUtil;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.move.MoveCallback;
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesProcessor;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.refactoring.util.TextOccurrencesUtil;
import com.intellij.usageView.*;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FlexMoveClassProcessor extends MoveFilesOrDirectoriesProcessor {

  private final Collection<? extends JSQualifiedNamedElement> myElements;
  private final String myTargetPackage;

  public FlexMoveClassProcessor(Collection<? extends JSQualifiedNamedElement> elements,
                                PsiDirectory targetDirectory,
                                String targetPackage,
                                boolean searchInComments,
                                boolean searchForTextOccurencies,
                                @Nullable MoveCallback callback) {
    super(elements.iterator().next().getProject(),
          ContainerUtil.map2Array(elements, PsiElement.class, (Function<JSQualifiedNamedElement, PsiElement>)e -> e.getContainingFile()),
          targetDirectory, true, searchInComments, searchForTextOccurencies, callback, null);
    myElements = elements;
    myTargetPackage = targetPackage;
  }

  @NotNull
  @Override
  protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo @NotNull [] usages) {
    return new FlexMoveClassUsageViewDescriptor();
  }

  @NotNull
  @Override
  protected String getCommandName() {
    StringBuilder s = new StringBuilder();
    for (JSQualifiedNamedElement element : myElements) {
      if (s.length() > 0) {
        s.append(", ");
      }
      s.append(new JSNamedElementPresenter(element).describeWithQualifiedName());
    }
    return FlexBundle.message("move.command.name", s, JSFormatUtil.formatPackage(myTargetPackage));
  }

  @Override
  protected UsageInfo @NotNull [] findUsages() {
    Collection<UsageInfo> result = Collections.synchronizedCollection(new ArrayList<>());
    result.addAll(Arrays.asList(super.findUsages()));
    for (JSQualifiedNamedElement element : myElements) {
      if (element instanceof JSClass) {
        JSRefactoringUtil.addConstructorUsages((JSClass)element, result);
      }
      TextOccurrencesUtil.findNonCodeUsages(element, myRefactoringScope, element.getQualifiedName(), mySearchInComments, mySearchInNonJavaFiles,
                                            StringUtil.getQualifiedName(myTargetPackage, StringUtil.notNullize(element.getName())), result);
    }
    return result.toArray(UsageInfo.EMPTY_ARRAY);
  }

  @Override
  protected boolean preprocessUsages(@NotNull Ref<UsageInfo[]> refUsages) {
    return showConflicts(detectConflicts(refUsages.get()), refUsages.get());
  }

  @Override
  protected boolean isPreviewUsages(UsageInfo @NotNull [] usages) {
    if (UsageViewUtil.reportNonRegularUsages(usages, myProject)) {
      return true;
    }
    else {
      return super.isPreviewUsages(usages);
    }
  }

  private MultiMap<PsiElement, String> detectConflicts(UsageInfo[] usages) {
    MultiMap<PsiElement, String> conflicts = new MultiMap<>();

    final Collection<PsiElement> filesToMove = Arrays.asList(myElementsToMove);
    JSVisibilityUtil.Options options = new JSVisibilityUtil.Options();
    for (PsiElement file : filesToMove) {
      options.overridePackage(file, myTargetPackage);
    }

    for (UsageInfo usage : usages) {
      final PsiElement element = usage.getElement();
      if (!(element instanceof JSReferenceExpression)) {
        continue;
      }

      if (CommonRefactoringUtil.isAncestor(element, filesToMove)) {
        continue;
      }

      JSReferenceExpression refExpr = (JSReferenceExpression)element;
      final PsiElement resolved = refExpr.resolve();
      if (!(resolved instanceof JSQualifiedNamedElement)) {
        continue;
      }

      PsiElement containingClass = null;
      if (resolved instanceof JSFunction &&
          ((JSFunction)resolved).isConstructor() &&
          myElements.contains(containingClass = resolved.getParent()) || myElements.contains(resolved)) {

        JSRefactoringConflictsUtil
          .checkAccessibility((JSAttributeListOwner)resolved, (JSClass)containingClass, null, refExpr, conflicts, true, options);
      }
    }

    for (PsiElement fileToMove : filesToMove) {
      JSRefactoringConflictsUtil.checkOutgoingReferencesAccessibility(fileToMove, filesToMove, null, true, conflicts,
                                                                      Conditions.alwaysTrue(), options);
    }

    // TODO module conflicts
    //JSRefactoringConflictsUtil.analyzeModuleConflicts(myProject, myElements, usages, myTargetDirectory, conflicts);
    return conflicts;
  }

  @Override
  protected void retargetUsages(UsageInfo @NotNull [] usages, @NotNull Map<PsiElement, PsiElement> oldToNewMap) {
    super.retargetUsages(usages, oldToNewMap);
    for (UsageInfo usage : usages) {
      if (usage instanceof JSRefactoringUtil.ConstructorUsageInfo) {
        final JSRefactoringUtil.ConstructorUsageInfo constuctorUsage = (JSRefactoringUtil.ConstructorUsageInfo)usage;
        final JSReferenceExpression ref = constuctorUsage.getElement();
        if (ref != null && constuctorUsage.getSubject().isValid()) {
          ref.bindToElement(constuctorUsage.getSubject().getContainingFile());
        }
      }
    }
  }

  private class FlexMoveClassUsageViewDescriptor extends BaseUsageViewDescriptor {

    FlexMoveClassUsageViewDescriptor() {
      super(PsiUtilCore.toPsiElementArray(myElements));
    }

    @Override
    public String getProcessedElementsHeader() {
      if (getElements().length == 1) {
        return FlexBundle.message("element.to.be.moved.to",
                                  new JSNamedElementPresenter(getElements()[0], Capitalization.UpperCase).describeElementKind(),
                                  JSFormatUtil.formatPackage(myTargetPackage));
      }
      else {
        return FlexBundle.message("elements.to.be.moved.to", JSFormatUtil.formatPackage(myTargetPackage));
      }
    }

    @NotNull
    @Override
    public String getCodeReferencesText(int usagesCount, int filesCount) {
      String prefix;
      if (getElements().length == 1) {
        prefix = FlexBundle.message("references.in.code.to.0", UsageViewUtil.getLongName(getElements()[0]));
      }
      else {
        prefix = RefactoringBundle.message("references.found.in.code");
      }
      return prefix + UsageViewBundle.getReferencesString(usagesCount, filesCount);
    }

    @Override
    public String getCommentReferencesText(int usagesCount, int filesCount) {
      return RefactoringBundle.message("comments.elements.header", UsageViewBundle.getOccurencesString(usagesCount, filesCount));
    }
  }
}
