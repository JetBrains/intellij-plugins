package com.intellij.javascript.flex.refactoring.moveClass;

import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSNamedElementKind;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringConflictsUtil;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
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
import com.intellij.util.Processor;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FlexMoveInnerClassProcessor extends BaseRefactoringProcessor {
  private final JSQualifiedNamedElement myElement;
  private final PsiDirectory myTargetDirectory;
  private final String myClassName;
  private final String myPackageName;
  private final boolean mySearchInComments;
  private final boolean mySearchTextOccurences;
  @Nullable private final MoveCallback myMoveCallback;
  private NonCodeUsageInfo[] myNonCodeUsages;

  public FlexMoveInnerClassProcessor(JSQualifiedNamedElement element,
                                     PsiDirectory targetDirectory,
                                     String className,
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
  protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usages) {
    return new FlexMoveInnerClassUsageViewDescriptor();
  }

  @NotNull
  @Override
  protected UsageInfo[] findUsages() {
    final Collection<UsageInfo> result = Collections.synchronizedCollection(new ArrayList<UsageInfo>());
    ReferencesSearch.search(myElement, new LocalSearchScope(myElement.getContainingFile())).forEach(new Processor<PsiReference>() {
      @Override
      public boolean process(PsiReference reference) {
        final PsiElement element = reference.getElement();
        if (!(element instanceof JSReferenceExpression)) {
          return true;
        }
        if (JSResolveUtil.isSelfReference(element)) {
          return true;
        }
        result.add(new UsageInfo(element));
        return true;
      }
    });

    if (myElement instanceof JSClass) {
      JSRefactoringUtil.addConstructorUsages((JSClass)myElement, true, result);
    }
    TextOccurrencesUtil.findNonCodeUsages(myElement, myElement.getName(), mySearchInComments, mySearchTextOccurences,
                                          StringUtil.getQualifiedName(myPackageName, myClassName), result);
    return UsageViewUtil.removeDuplicatedUsages(result.toArray(new UsageInfo[result.size()]));
  }

  @Override
  protected void performRefactoring(UsageInfo[] usages) {
    try {
      CreateClassOrInterfaceAction.createClass(myClassName, myPackageName, myTargetDirectory, false);
    }
    catch (Exception e) {
      Messages.showErrorDialog(myProject, e.getMessage(), getCommandName());
      return;
    }

    final PsiFile sourceFile = myElement.getContainingFile();

    Collection<String> importsInTargetFile = new HashSet<String>();
    Collection<String> namespacesInTargetFile = new HashSet<String>();
    List<FormatFixer> formatters = new ArrayList<FormatFixer>();
    //JSRefactoringUtil.addRemovalFormatters(mySourceClass, myMembersToMove, Condition.TRUE, Condition.TRUE, postponedFormatters);

    JSRefactoringUtil.fixOutgoingReferences(myElement, importsInTargetFile, namespacesInTargetFile, Collections.singletonList(
      ((JSAttributeListOwner)myElement)), null, false, false);

    myElement.setName(myClassName);
    Collection<UsageInfo> usagesToProcess = new ArrayList<UsageInfo>(Arrays.asList(usages));
    for (Iterator<UsageInfo> i = usagesToProcess.iterator(); i.hasNext();) {
      UsageInfo usage = i.next();
      PsiElement element;
      if (usage instanceof NonCodeUsageInfo || !((element = usage.getElement()) instanceof JSReferenceExpression) ||
          !PsiTreeUtil.isAncestor(myElement, element, false)) {
        continue;
      }
      ((JSReferenceExpression)element).bindToElement(myElement);
      i.remove();
    }

    final PsiElement clazz =
      JSResolveUtil.findClassByQName(StringUtil.getQualifiedName(myPackageName, myClassName), GlobalSearchScope.projectScope(myProject));
    PsiElement toInsert = myElement instanceof JSVariable ? JSRefactoringUtil.getVarStatementCopy((JSVariable)myElement) : myElement.copy();
    final PsiElement inserted = clazz.replace(toInsert);
    JSQualifiedNamedElement newClass =
      inserted instanceof JSVarStatement ? ((JSVarStatement)inserted).getVariables()[0] : (JSQualifiedNamedElement)inserted;
    JSRefactoringUtil.handleDocCommentAndFormat(inserted, formatters);
    JSRefactoringUtil.deleteWithNoPostponedFormatting(myElement);

    if (myPackageName.length() > 0) {
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
    List<NonCodeUsageInfo> nonCodeUsages = new ArrayList<NonCodeUsageInfo>();
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

    JSVisibilityUtil.setVisibility((JSAttributeListOwner)newClass,
                                   makePublic ? JSAttributeList.AccessType.PUBLIC : JSAttributeList.AccessType.PACKAGE_LOCAL);
    myNonCodeUsages = nonCodeUsages.toArray(new NonCodeUsageInfo[nonCodeUsages.size()]);

    if (myMoveCallback != null) {
      myMoveCallback.refactoringCompleted();
    }

    OpenFileDescriptor descriptor =
      new OpenFileDescriptor(myProject, inserted.getContainingFile().getVirtualFile(), newClass.getTextOffset());
    FileEditorManager.getInstance(myProject).openTextEditor(descriptor, true);
  }

  @Override
  protected String getCommandName() {
    return FlexBundle.message("move.to.upper.level.command.name",
                              StringUtil.decapitalize(JSBundle.message(JSNamedElementKind.kind(myElement).humanReadableKey())),
                              myElement.getName(), StringUtil.getQualifiedName(myPackageName, myClassName));
  }

  @Override
  protected boolean preprocessUsages(Ref<UsageInfo[]> refUsages) {
    return showConflicts(detectConflicts(), refUsages.get());
  }

  private MultiMap<PsiElement, String> detectConflicts() {
    MultiMap<PsiElement, String> result = new MultiMap<PsiElement, String>();
    myElement.getContainingFile().putUserData(JSVisibilityUtil.OVERRIDE_PACKAGE, myPackageName);

    try {
      JSRefactoringConflictsUtil.checkOutgoingReferencesAccessibility(myElement, Collections.singletonList(myElement), null, true, result,
                                                                      Conditions.<PsiElement>alwaysTrue());
    }
    finally {
      myElement.getContainingFile().putUserData(JSVisibilityUtil.OVERRIDE_PACKAGE, null);
    }
    return result;
  }

  @Override
  protected boolean isPreviewUsages(UsageInfo[] usages) {
    if (UsageViewUtil.hasNonCodeUsages(usages)) {
      WindowManager.getInstance().getStatusBar(myProject).setInfo(
        RefactoringBundle.message("occurrences.found.in.comments.strings.and.non.java.files"));
      return true;
    }
    else {
      return super.isPreviewUsages(usages);
    }
  }

  protected void performPsiSpoilingRefactoring() {
    if (myNonCodeUsages != null) {
      RenameUtil.renameNonCodeUsages(myProject, myNonCodeUsages);
    }
  }

  private class FlexMoveInnerClassUsageViewDescriptor extends BaseUsageViewDescriptor {

    public FlexMoveInnerClassUsageViewDescriptor() {
      super(myElement);
    }

    @Override
    public String getProcessedElementsHeader() {
      return FlexBundle.message("element.to.be.moved.to.upper.level",
                                StringUtil.decapitalize(JSBundle.message(JSNamedElementKind.kind(myElement).humanReadableKey())),
                                StringUtil.getQualifiedName(myPackageName, myClassName));
    }

    @Override
    public String getCodeReferencesText(int usagesCount, int filesCount) {
      return FlexBundle.message("references.in.code.to.inner.0", UsageViewUtil.getLongName(getElements()[0])) +
             UsageViewBundle.getReferencesString(usagesCount, filesCount);
    }

    @Override
    public String getCommentReferencesText(int usagesCount, int filesCount) {
      return RefactoringBundle.message("comments.elements.header", UsageViewBundle.getOccurencesString(usagesCount, filesCount));
    }
  }
}
