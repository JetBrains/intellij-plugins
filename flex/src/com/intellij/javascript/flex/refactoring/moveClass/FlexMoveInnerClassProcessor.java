// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.refactoring.moveClass;

import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.presentable.JSNamedElementPresenter;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.JSChangeVisibilityUtil;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.lang.javascript.refactoring.util.ActionScriptRefactoringUtil;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringConflictsUtil;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.move.MoveCallback;
import com.intellij.refactoring.rename.RenameUtil;
import com.intellij.refactoring.util.NonCodeUsageInfo;
import com.intellij.refactoring.util.TextOccurrencesUtil;
import com.intellij.usageView.*;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class FlexMoveInnerClassProcessor extends BaseRefactoringProcessor {
  private final JSQualifiedNamedElement myElement;
  private final PsiDirectory myTargetDirectory;
  private final @NotNull String myClassName;
  private final String myPackageName;
  private final boolean mySearchInComments;
  private final boolean mySearchTextOccurences;
  private final @Nullable MoveCallback myMoveCallback;
  private NonCodeUsageInfo[] myNonCodeUsages;

  public FlexMoveInnerClassProcessor(JSQualifiedNamedElement element,
                                     PsiDirectory targetDirectory,
                                     @NotNull String className,
                                     String packageName,
                                     boolean searchInComments,
                                     boolean searchTextOccurences,
                                     @Nullable MoveCallback moveCallback) {
    super(element.getProject());
    myElement = element;
    myTargetDirectory = targetDirectory;
    myClassName = className;
    myPackageName = packageName;
    mySearchInComments = searchInComments;
    mySearchTextOccurences = searchTextOccurences;
    myMoveCallback = moveCallback;
  }

  @Override
  protected @NotNull UsageViewDescriptor createUsageViewDescriptor(UsageInfo @NotNull [] usages) {
    return new FlexMoveInnerClassUsageViewDescriptor();
  }

  @Override
  protected UsageInfo @NotNull [] findUsages() {
    final Collection<UsageInfo> result = Collections.synchronizedCollection(new ArrayList<>());
    ReferencesSearch.search(myElement, new LocalSearchScope(myElement.getContainingFile())).forEach(reference -> {
      final PsiElement element = reference.getElement();
      if (!(element instanceof JSReferenceExpression)) {
        return true;
      }
      if (JSResolveUtil.isSelfReference(element)) {
        return true;
      }
      result.add(new UsageInfo(element));
      return true;
    });

    if (myElement instanceof JSClass) {
      final JSFunction constructor = ((JSClass)myElement).getConstructor();
      if (constructor != null) {
        result.add(new UsageInfo(constructor));
        JSRefactoringUtil.addConstructorUsages((JSClass)myElement, result);
      }
    }
    TextOccurrencesUtil.findNonCodeUsages(myElement, myRefactoringScope, myElement.getName(), mySearchInComments, mySearchTextOccurences,
                                          StringUtil.getQualifiedName(myPackageName, myClassName), result);
    return UsageViewUtil.removeDuplicatedUsages(result.toArray(UsageInfo.EMPTY_ARRAY));
  }

  @Override
  protected void performRefactoring(UsageInfo @NotNull [] usages) {
    try {
      ActionScriptCreateClassOrInterfaceFix.createClass(myClassName, myPackageName, myTargetDirectory, false);
    }
    catch (Exception e) {
      Messages.showErrorDialog(myProject, e.getMessage(), getCommandName());
      return;
    }

    final PsiFile sourceFile = myElement.getContainingFile();

    Collection<String> importsInTargetFile = new HashSet<>();
    Collection<String> namespacesInTargetFile = new HashSet<>();
    List<FormatFixer> formatters = new ArrayList<>();
    //JSRefactoringUtil.addRemovalFormatters(mySourceClass, myMembersToMove, Condition.TRUE, Condition.TRUE, postponedFormatters);

    JSClass targetClass = myElement instanceof JSClass ? (JSClass)myElement : null;
    ActionScriptRefactoringUtil.fixOutgoingReferences(myElement, importsInTargetFile, namespacesInTargetFile, Collections.singletonList(
      ((JSAttributeListOwner)myElement)), targetClass, false, false);

    myElement.setName(myClassName);
    Collection<UsageInfo> usagesToProcess = new ArrayList<>(Arrays.asList(usages));
    for (Iterator<UsageInfo> i = usagesToProcess.iterator(); i.hasNext();) {
      UsageInfo usage = i.next();
      PsiElement element;
      if (usage instanceof NonCodeUsageInfo || (element = usage.getElement()) == null ||
          !PsiTreeUtil.isAncestor(myElement, element, false)) {
        continue;
      }
      if (element instanceof JSReferenceExpression) {
        ((JSReferenceExpression)element).bindToElement(myElement);
      }
      else if (element instanceof PsiNamedElement) {
        ((PsiNamedElement)element).setName(myClassName);
      }
      else {
        continue;
      }
      i.remove();
    }

    final PsiElement clazz =
      ActionScriptClassResolver.findClassByQNameStatic(StringUtil.getQualifiedName(myPackageName, myClassName), GlobalSearchScope.projectScope(myProject));
    PsiElement toInsert = myElement instanceof JSVariable ? JSRefactoringUtil.getVarStatementCopy((JSVariable)myElement) : myElement.copy();
    final PsiElement inserted = clazz.replace(toInsert);
    PsiFile insertedContainingFile = inserted.getContainingFile();
    JSQualifiedNamedElement newClass =
      inserted instanceof JSVarStatement ? ((JSVarStatement)inserted).getVariables()[0] : (JSQualifiedNamedElement)inserted;

    SmartPsiElementPointer<JSQualifiedNamedElement> newClassPointer =
      SmartPointerManager.getInstance(myProject).createSmartPsiElementPointer(newClass);
    JSRefactoringUtil.handleDocCommentAndFormat(inserted, formatters);
    JSRefactoringUtil.deleteWithNoPostponedFormatting(myElement);


    if (!myPackageName.isEmpty()) {
      for (UsageInfo usage : usagesToProcess) {
        if (usage instanceof NonCodeUsageInfo || usage.getFile() != sourceFile) continue;
        final PsiElement element = usage.getElement();
        if (element == null) continue;
        ImportUtils.doImport(element, StringUtil.getQualifiedName(myPackageName, myClassName), true);
      }
    }
    JSRefactoringUtil.postProcess(sourceFile, newClass, Collections.singletonList(sourceFile), importsInTargetFile, namespacesInTargetFile,
                                  formatters, true, false);

    boolean makePublic = false;
    newClass = newClassPointer.getElement();
    List<NonCodeUsageInfo> nonCodeUsages = new ArrayList<>();
    for (UsageInfo usage : usagesToProcess) {
      if (usage instanceof NonCodeUsageInfo) {
        nonCodeUsages.add((NonCodeUsageInfo)usage);
      }
      else {
        JSReferenceExpression refExpr = (JSReferenceExpression)usage.getElement();
        if (refExpr == null) {
          continue;
        }
        makePublic |= JSPsiImplUtils.getQNameForMove(refExpr, newClass) != null;
        refExpr.bindToElement(newClass);
      }
    }

    JSChangeVisibilityUtil.setVisibility((JSAttributeListOwner)newClass,
                                   makePublic ? JSAttributeList.AccessType.PUBLIC : JSAttributeList.AccessType.PACKAGE_LOCAL);
    myNonCodeUsages = nonCodeUsages.toArray(new NonCodeUsageInfo[0]);

    if (myMoveCallback != null) {
      myMoveCallback.refactoringCompleted();
    }

    Navigatable descriptor =
      PsiNavigationSupport.getInstance().createNavigatable(myProject, insertedContainingFile.getVirtualFile(),
                                                           newClass.getTextOffset());
    descriptor.navigate(true);
  }

  @Override
  protected @NotNull String getCommandName() {
    return FlexBundle.message("move.to.upper.level.command.name",
                              new JSNamedElementPresenter(myElement).describeWithShortName(),
                              StringUtil.getQualifiedName(myPackageName, myClassName));
  }

  @Override
  protected boolean preprocessUsages(@NotNull Ref<UsageInfo[]> refUsages) {
    return showConflicts(detectConflicts(), refUsages.get());
  }

  private MultiMap<PsiElement, String> detectConflicts() {
    MultiMap<PsiElement, String> result = new MultiMap<>();

    JSVisibilityUtil.Options options = new JSVisibilityUtil.Options();
    options.overridePackage(myElement, myPackageName);
    JSRefactoringConflictsUtil.checkOutgoingReferencesAccessibility(myElement, Collections.singletonList(myElement), null, true, result,
                                                                    Conditions.alwaysTrue(), options);
    return result;
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

  @Override
  protected void performPsiSpoilingRefactoring() {
    if (myNonCodeUsages != null) {
      RenameUtil.renameNonCodeUsages(myProject, myNonCodeUsages);
    }
  }

  private class FlexMoveInnerClassUsageViewDescriptor extends BaseUsageViewDescriptor {

    FlexMoveInnerClassUsageViewDescriptor() {
      super(myElement);
    }

    @Override
    public String getProcessedElementsHeader() {
      return FlexBundle.message("element.to.be.moved.to.upper.level",
                                new JSNamedElementPresenter(myElement).describeElementKind(),
                                StringUtil.getQualifiedName(myPackageName, myClassName));
    }

    @Override
    public @NotNull String getCodeReferencesText(int usagesCount, int filesCount) {
      return FlexBundle.message("references.in.code.to.inner.0", UsageViewUtil.getLongName(getElements()[0])) +
             UsageViewBundle.getReferencesString(usagesCount, filesCount);
    }

    @Override
    public String getCommentReferencesText(int usagesCount, int filesCount) {
      return RefactoringBundle.message("comments.elements.header", UsageViewBundle.getOccurencesString(usagesCount, filesCount));
    }
  }
}
