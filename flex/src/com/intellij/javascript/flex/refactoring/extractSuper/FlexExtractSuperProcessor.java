// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.refactoring.extractSuper;

import com.intellij.javascript.flex.refactoring.RenameMoveUtils;
import com.intellij.lang.actionscript.psi.ActionScriptPsiImplUtil;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.ECMAScriptImportOptimizer;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.presentable.JSFormatUtil;
import com.intellij.lang.javascript.presentable.JSNamedElementPresenter;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.lang.javascript.refactoring.extractSuper.JSConvertReferencesToSuperUtil;
import com.intellij.lang.javascript.refactoring.extractSuper.JSExtractSuperMode;
import com.intellij.lang.javascript.refactoring.memberPullUp.JSPullUpConflictsUtil;
import com.intellij.lang.javascript.refactoring.memberPullUp.JSPullUpHelper;
import com.intellij.lang.javascript.refactoring.util.JSInterfaceContainmentVerifier;
import com.intellij.lang.javascript.refactoring.util.JSMemberInfo;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringConflictsUtil;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.ui.UsageViewDescriptorAdapter;
import com.intellij.refactoring.util.RefactoringDescriptionLocation;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.CollectionFactory;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class FlexExtractSuperProcessor extends BaseRefactoringProcessor {

  private static final Logger LOG = Logger.getInstance(FlexExtractSuperProcessor.class.getName());

  private JSClass mySourceClass;
  private final JSMemberInfo[] myMembersToMove;
  @NotNull
  private final String myTargetName;
  private final String myTargetPackage;
  private final int myDocCommentPolicy;
  private final JSExtractSuperMode myMode;
  private final boolean myClassNotInterface;
  private final PsiDirectory myTagretDirectory;

  private JSClass myTargetClass;
  private SmartPsiElementPointer<JSClass> myTargetClassPtr;
  private Collection<JSElement> myMembersAfterMove;

  public FlexExtractSuperProcessor(JSClass sourceClass,
                                   JSMemberInfo[] membersToMove,
                                   @NotNull String targetName,
                                   String targetPackage,
                                   int docCommentPolicy,
                                   JSExtractSuperMode mode,
                                   boolean classNotInterface,
                                   PsiDirectory targetDirectory) {
    super(sourceClass.getProject());
    mySourceClass = sourceClass;
    myMembersToMove = membersToMove;
    myTargetName = targetName;
    myTargetPackage = targetPackage;
    myDocCommentPolicy = docCommentPolicy;
    myMode = mode;
    myClassNotInterface = classNotInterface;
    myTagretDirectory = targetDirectory;
  }

  @NotNull
  @Override
  protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo @NotNull [] usages) {
    return new JSExtractInterfaceUsageViewDescriptor();
  }

  @Override
  protected UsageInfo @NotNull [] findUsages() {
    if (myMode == JSExtractSuperMode.ExtractSuper) {
      return UsageInfo.EMPTY_ARRAY; // user doesn't want to update usages
    }

    final Collection<JSReferenceExpression> candidates = Collections.synchronizedCollection(new ArrayList<>());
    ReferencesSearch.search(mySourceClass, mySourceClass.getUseScope()).forEach(psiReference -> {
      PsiElement element = psiReference.getElement();
      if (!(element instanceof JSReferenceExpression)) {
        return true;
      }
      if (element == mySourceClass.getNameIdentifier()) {
        return true;
      }
      if (element.getParent() instanceof JSImportStatement) {
        // we'll run optimize imports for the file if we change any reference in it
        return true;
      }

      candidates.add((JSReferenceExpression)element);
      return true;
    });

    JSConvertReferencesToSuperUtil
      util = new JSConvertReferencesToSuperUtil(myMembersToMove, myMembersAfterMove, myMode, mySourceClass, myTargetClass);

    final Map<PsiElement, JSConvertReferencesToSuperUtil.Status> variablesResults = new HashMap<>();
    final Collection<UsageInfo> result = Collections.synchronizedCollection(new ArrayList<>());
    for (JSReferenceExpression candidate : candidates) {
      if (util.canTurnReferenceToSuper(candidate, variablesResults) ^ myMode == JSExtractSuperMode.RenameImplementation) {
        result.add(new UsageInfo((PsiElement)candidate));
      }
    }

    if (myMode == JSExtractSuperMode.RenameImplementation) {
      // we need to make an explicit search for class constuctor to push down it's usages
      JSRefactoringUtil.addConstructorUsages(mySourceClass, result);
    }

    return result.toArray(UsageInfo.EMPTY_ARRAY);
  }


  @Override
  protected void refreshElements(PsiElement @NotNull [] elements) {
    super.refreshElements(elements);
  }

  @Override
  protected boolean preprocessUsages(@NotNull Ref<UsageInfo[]> refUsages) {
    return showConflicts(detectConflicts(refUsages.get()), refUsages.get());
  }

  private MultiMap<PsiElement, String> detectConflicts(UsageInfo[] usageInfos) {
    if (myMode == JSExtractSuperMode.ExtractSuper || myMode == JSExtractSuperMode.ExtractSuperTurnRefs) {
      // since we create *public* superclass/interface in the same module, we don't check it's accessibility
      JSInterfaceContainmentVerifier v = JSInterfaceContainmentVerifier.create(Arrays.asList(myMembersToMove));
      return JSPullUpConflictsUtil.checkConflicts(myMembersToMove, mySourceClass, createFakeClass(), v, JSVisibilityUtil.DEFAULT_OPTIONS);
    }
    else {
      MultiMap<PsiElement, String> conflicts =
        new MultiMap<>(Collections.synchronizedMap(CollectionFactory.createSmallMemoryFootprintMap())) {
          @NotNull
          @Override
          protected Collection<String> createCollection() {
            return Collections.synchronizedCollection(super.createCollection());
          }
        };

      // we create subclass with the same visibility as the source class, so let's check it accessibility by references that need to be pushed down
      checkIncomingReferencesToSubclass(usageInfos, conflicts, JSVisibilityUtil.DEFAULT_OPTIONS);

      checkIncomingReferencesToPushedMembers(conflicts, JSVisibilityUtil.DEFAULT_OPTIONS);

      checkOutgoingReferences(conflicts, JSVisibilityUtil.DEFAULT_OPTIONS);
      return conflicts;
    }
  }

  private void checkIncomingReferencesToPushedMembers(MultiMap<PsiElement, String> conflicts, @NotNull JSVisibilityUtil.Options options) {
    if (StringUtil.getPackageName(mySourceClass.getQualifiedName()).equals(myTargetPackage)) {
      return; // optimization
    }

    final JSClass subClass = createFakeClass();
    for (final JSFunction method : mySourceClass.getFunctions()) {
      checkIncomingReferencesToMovedMember(subClass, method, conflicts, options);
    }
    for (final JSField field : mySourceClass.getFields()) {
      checkIncomingReferencesToMovedMember(subClass, field, conflicts, options);
    }
  }

  private void checkIncomingReferencesToMovedMember(final JSClass subClass,
                                                    final JSAttributeListOwner member,
                                                    final MultiMap<PsiElement, String> conflicts,
                                                    final @NotNull JSVisibilityUtil.Options options) {
    if (JSConvertReferencesToSuperUtil.willBeInSuperclass(member, myMembersToMove, myMembersAfterMove)) {
      return;
    }
    ReferencesSearch.search(member, member.getUseScope()).forEach(psiReference -> {
      PsiElement usageElement = psiReference.getElement();
      if (usageElement instanceof JSReferenceExpression) {
        if (!PsiTreeUtil.isAncestor(mySourceClass, usageElement, true)) {
          if (!JSVisibilityUtil.isAccessible(member, (JSAttributeList)null, subClass, usageElement, options)) {
            PsiElement location = JSRefactoringConflictsUtil.getUsageLocation(usageElement);
            JSRefactoringConflictsUtil.reportInaccessibleElement(member, subClass, location, true, conflicts, new HashSet<>());
          }
        }
      }
      return true;
    });
  }

  private void checkOutgoingReferences(MultiMap<PsiElement, String> conflicts, @NotNull JSVisibilityUtil.Options options) {
    // check that outgoing references are accessible at new location
    if (StringUtil.getPackageName(mySourceClass.getQualifiedName()).equals(myTargetPackage)) {
      return; // optimization
    }

    JSClass subClass = createFakeClass();
    JSRefactoringConflictsUtil
      .checkOutgoingReferencesAccessibility(mySourceClass, Collections.singletonList(mySourceClass), subClass, true, conflicts,
                                            Conditions.alwaysTrue(), options);
  }

  private void checkIncomingReferencesToSubclass(UsageInfo[] usageInfos,
                                                 MultiMap<PsiElement, String> conflicts,
                                                 @NotNull JSVisibilityUtil.Options options) {
    if (StringUtil.getPackageName(mySourceClass.getQualifiedName()).equals(myTargetPackage)) {
      return; // optimization
    }

    JSClass subClass = createFakeClass();
    for (UsageInfo usageInfo : usageInfos) {
      if (!JSVisibilityUtil.isAccessible(subClass, (JSAttributeList)null, null, usageInfo.getElement(), options)) {
        reportConflict(subClass, usageInfo.getElement(), conflicts);
      }
    }
  }

  private JSClass createFakeClass() {
    String accessModifier;
    boolean isInterface;
    if (myMode == JSExtractSuperMode.RenameImplementation) {
      final String namespace = ActionScriptPsiImplUtil.getNamespaceValue(mySourceClass.getAttributeList());
      if (namespace != null) {
        accessModifier = namespace;
      }
      else {
        accessModifier = JSFormatUtil.formatVisibility(mySourceClass.getAttributeList().getAccessType());
      }
      isInterface = mySourceClass.isInterface();
    }
    else {
      accessModifier = JSFormatUtil.formatVisibility(JSAttributeList.AccessType.PUBLIC);
      isInterface = !myClassNotInterface;
    }

    try {
      String text = ActionScriptCreateClassOrInterfaceFix.getClassText(myTargetName, myTargetPackage, isInterface, accessModifier, myProject);
      JSFile file = (JSFile)PsiFileFactory.getInstance(myProject).createFileFromText(myTargetName + ".as", text);
      return JSPsiImplUtils.findClass(file);
    }
    catch (IOException e) {
      LOG.error(e);
      return null;
    }
  }

  @Override
  protected void performRefactoring(UsageInfo @NotNull [] usages) {
    List<FormatFixer> formatters = new ArrayList<>();
    if (myMode == JSExtractSuperMode.ExtractSuper) {
      createSuperClassifier(formatters);
    }
    else {
      if (myMode == JSExtractSuperMode.ExtractSuperTurnRefs) {
        createSuperClassifier(formatters);
      }
      else if (ActionScriptResolveUtil.isFileLocalSymbol(mySourceClass)) {
        renameOriginalFileLocalClass(formatters);
      }
      else {
        renameOriginalClass(formatters);
      }
      if (myTargetClass != null) {
        rebindReferencesToTarget(usages, formatters);
      }
    }
    JSRefactoringUtil.format(formatters);
  }

  private void rebindReferencesToTarget(UsageInfo @NotNull [] usages, List<FormatFixer> formatters) {
    bindRefsToTarget(usages, formatters);
    Collection<UsageInfo> usagesInMovedMembers = new ArrayList<>();
    Map<PsiElement, JSConvertReferencesToSuperUtil.Status> variablesResults = new HashMap<>();
    JSConvertReferencesToSuperUtil util = new JSConvertReferencesToSuperUtil(myMembersToMove, myMembersAfterMove, myMode, mySourceClass, myTargetClass);

    for (JSElement memberAfterMove : myMembersAfterMove) {
      findUsagesAfterMove(memberAfterMove, usagesInMovedMembers, variablesResults, util);
    }
    bindRefsToTarget(usagesInMovedMembers.toArray(UsageInfo.EMPTY_ARRAY), formatters
    );
  }

  private void findUsagesAfterMove(JSElement scope,
                                   final Collection<UsageInfo> result,
                                   final Map<PsiElement, JSConvertReferencesToSuperUtil.Status> variablesResults,
                                   JSConvertReferencesToSuperUtil util) {
    if (scope instanceof JSClass) {
      return;
    }

    scope.accept(new JSRecursiveElementVisitor() {
      @Override
      public void visitJSReferenceExpression(@NotNull JSReferenceExpression node) {
        if (util.getSubjectClass(node).isEquivalentTo(node.resolve())) {
          if (util.canTurnReferenceToSuper(node, variablesResults) ^ myMode == JSExtractSuperMode.RenameImplementation) {
            result.add(new UsageInfo((PsiElement)node));
          }
        }
        super.visitJSReferenceExpression(node);
      }
    });
  }

  private void bindRefsToTarget(UsageInfo[] usages, List<FormatFixer> postponedFormatters) {
    Collection<PsiFile> filesToOptimizeImports = new HashSet<>();
    myTargetClass = myTargetClassPtr.getElement();
    String qName = myTargetClass.getQualifiedName();
    for (UsageInfo usage : usages) {
      JSReferenceExpression refExpr = (JSReferenceExpression)usage.getElement();
      if (refExpr == null) {
        // usage is not valid
        continue;
      }

      // remove qualifier if any
      if (refExpr.getQualifier() instanceof JSReferenceExpression) {
        refExpr.deleteChildRange(refExpr.getQualifier(), refExpr.getQualifier().getNextSibling());
      }
      refExpr = JSReferenceExpressionImpl.bindToElement(refExpr, qName, myTargetClass, false);
      // rebind won't insert import statement
      if (qName.contains(".") &&
          JSPsiImplUtils.differentPackageName(StringUtil.getPackageName(qName), JSResolveUtil.getPackageNameFromPlace(refExpr))) {
        ImportUtils.doImport(refExpr, qName, false);
        filesToOptimizeImports.add(refExpr.getContainingFile());
        myTargetClass = myTargetClassPtr.getElement();
      }
    }
    for (PsiFile affectedFile : filesToOptimizeImports) {
      postponedFormatters.addAll(ECMAScriptImportOptimizer.executeNoFormat(affectedFile));
    }
  }

  private void createSuperClassifier(List<FormatFixer> formatters) {
    try {
      ActionScriptCreateClassOrInterfaceFix.createClass(myTargetName, myTargetPackage, myTagretDirectory, !myClassNotInterface);
    }
    catch (Exception e) {
      Messages.showErrorDialog(mySourceClass.getProject(), e.getMessage(), getCommandName());
      return;
    }

    String targetFqn = StringUtil.getQualifiedName(myTargetPackage, myTargetName);
    myTargetClass = (JSClass)JSDialectSpecificHandlersFactory.forElement(mySourceClass).getClassResolver().findClassByQName(targetFqn, mySourceClass);
    myTargetClassPtr = myTargetClass == null ? null : SmartPointerManager.getInstance(myProject).createSmartPsiElementPointer(myTargetClass);

    if (myClassNotInterface && !(mySourceClass instanceof XmlBackedJSClass)
        && mySourceClass.getExtendsList() != null && mySourceClass.getExtendsList().getReferenceTexts().length > 0) {
      JSClass existingSuperClass = mySourceClass.getSuperClasses()[0];

      JSRefactoringUtil.removeFromReferenceList(mySourceClass.getExtendsList(), existingSuperClass, formatters);
      JSRefactoringUtil.addToSupersList(myTargetClass, existingSuperClass.getName(), false);
      if (ImportUtils.needsImport(myTargetPackage, existingSuperClass)) {
        ImportUtils.insertImportStatements(myTargetClass, Collections.singletonList(existingSuperClass.getQualifiedName()));
        formatters.addAll(ECMAScriptImportOptimizer.executeNoFormat(myTargetClass.getContainingFile()));
      }
    }

    JSRefactoringUtil.addToSupersList(mySourceClass, targetFqn, !myClassNotInterface);

    if (!(mySourceClass instanceof XmlBackedJSClass) && ImportUtils.needsImport(mySourceClass, myTargetPackage)) {
      // implements list in MXML class does not require import statement
      PsiFile file = mySourceClass.getContainingFile();
      ImportUtils.insertImportStatements(mySourceClass, Collections.singletonList(targetFqn));

      formatters.addAll(ECMAScriptImportOptimizer.executeNoFormat(file));
    }

    myMembersAfterMove = new JSPullUpHelper(mySourceClass, myTargetClass, myMembersToMove, myDocCommentPolicy).moveMembersToBase(formatters);

  }

  private void renameOriginalClass(List<FormatFixer> formatters) {
    try {
      String sourceClassQName = mySourceClass.getQualifiedName();
      String superClassifierText = ActionScriptCreateClassOrInterfaceFix
        .getClassText(mySourceClass.getName(), StringUtil.getPackageName(sourceClassQName), !myClassNotInterface,
                      JSFormatUtil.formatVisibility(JSAttributeList.AccessType.PUBLIC), myProject);
      PsiFile sourceFile = mySourceClass.getContainingFile();

      // we copy all the file content to keep file-local stuff
      String filename = myTargetName + "." + FileUtilRt.getExtension(sourceFile.getName());

      PsiFile file = PsiFileFactory.getInstance(myProject).createFileFromText(filename, sourceFile.getText());
      file = (PsiFile)myTagretDirectory.add(file);

      if (mySourceClass instanceof XmlBackedJSClassImpl) {
        myTargetClass = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)file);
        LOG.assertTrue(myTargetClass != null);
      }
      else {
        UserDataHolder dataHolder = new UserDataHolderBase();
        RenameMoveUtils.prepareMovedFile((JSFile)sourceFile, dataHolder);
        RenameMoveUtils.updateMovedFile((JSFile)file, dataHolder);

        JSQualifiedNamedElement element = JSPsiImplUtils.findQualifiedElement((JSFile)file);
        if (element instanceof JSClass) {
          myTargetClass = (JSClass)element;
          // class already has new name, so getConstructor() will return null
          JSFunction constructor = myTargetClass.findFunctionByNameAndKind(mySourceClass.getName(), JSFunction.FunctionKind.SIMPLE);
          if (constructor != null) {
            constructor.setName(myTargetName);
          }
        }
        else {
          LOG.error("class not found in generated file:\n" + file.getText());
        }
      }
      myTargetClassPtr = myTargetClass == null ? null : SmartPointerManager.getInstance(myProject).createSmartPsiElementPointer(myTargetClass);
      List<JSMemberInfo> membersToMove = new ArrayList<>();
      JSMemberInfo.extractSameMembers(myTargetClass, mySourceClass, myMembersToMove, membersToMove);
      for (JSMemberInfo info : membersToMove) {
        info.setChecked(true);
      }
      Document document;
      if (mySourceClass instanceof XmlBackedJSClassImpl) {
        VirtualFile vFile = sourceFile.getVirtualFile();
        vFile.rename(this, FileUtilRt.getNameWithoutExtension(vFile.getName()) +
                           "." + JavaScriptSupportLoader.ECMA_SCRIPT_L4_FILE_EXTENSION);
        document = FileDocumentManager.getInstance().getDocument(vFile);
      }
      else {
        document = PsiDocumentManager.getInstance(myProject).getDocument(sourceFile);
      }
      if (document == null) {
        LOG.error("document not found for " + sourceFile);
      }
      document.setText(superClassifierText);
      PsiDocumentManager.getInstance(myProject).commitDocument(document);

      JSRefactoringUtil.addToSupersList(myTargetClass, sourceClassQName, !myClassNotInterface);
      sourceFile = PsiDocumentManager.getInstance(myProject).getPsiFile(document);
      JSClass superClass = PsiTreeUtil.getNextSiblingOfType(sourceFile.getFirstChild().getFirstChild(), JSClass.class);

      JSMemberInfo[] infosArray = JSMemberInfo.getSelected(membersToMove, myTargetClass, Conditions.alwaysTrue());
      JSMemberInfo.sortByOffset(infosArray);
      myMembersAfterMove = new JSPullUpHelper(myTargetClass, superClass, infosArray, myDocCommentPolicy).moveMembersToBase(formatters);
    }
    catch (IOException | IncorrectOperationException e) {
      Messages.showErrorDialog(mySourceClass.getProject(), e.getMessage(), getCommandName());
    }
  }

  @NotNull
  public JSClass createJSClass(@NotNull Project project, PsiFile file, @NonNls @NotNull String text) {
    @NonNls String filename = file.getName() ;
    PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(filename, file.getFileType(), text);
    return PsiTreeUtil.getParentOfType(psiFile.findElementAt(0), JSClass.class);
  }

  private void renameOriginalFileLocalClass(List<FormatFixer> formatters) {
    JSRefactoringUtil.addToSupersList(mySourceClass, mySourceClass.getName(), !myClassNotInterface);
    String superClassifierText = (myClassNotInterface ? "class" : "interface") +
    " " + mySourceClass.getName() + "{}";

    mySourceClass.setName(myTargetName);

    SmartPsiElementPointer<JSClass> subClassPointer =
      SmartPointerManager.getInstance(myProject).createSmartPsiElementPointer(mySourceClass);
    PsiFile file = mySourceClass.getContainingFile();

    JSClass jsClass = createJSClass(myProject, mySourceClass.getContainingFile(), superClassifierText);
    jsClass = (JSClass)file.addBefore(jsClass, mySourceClass);

    mySourceClass = jsClass;
    myTargetClass = subClassPointer.getElement();
    myTargetClassPtr = myTargetClass == null ? null : SmartPointerManager.getInstance(myProject).createSmartPsiElementPointer(myTargetClass);
    formatters.add(FormatFixer.create(mySourceClass, myTargetClass, FormatFixer.Mode.Reformat));
    myMembersAfterMove = new JSPullUpHelper(myTargetClass, mySourceClass, myMembersToMove, myDocCommentPolicy).moveMembersToBase(formatters);
  }

  @NotNull
  @Override
  protected String getCommandName() {
    if (myMode == JSExtractSuperMode.RenameImplementation) {
      return JavaScriptBundle.message("extract.subclass.command.name", StringUtil.getQualifiedName(myTargetPackage, myTargetName),
                                      new JSNamedElementPresenter(mySourceClass).describeWithShortName());
    }
    else {
      return RefactoringBundle.message(myClassNotInterface ? "extract.superclass.command.name" : "extract.interface.command.name",
                                       StringUtil.getQualifiedName(myTargetPackage, myTargetName),
                                       new JSNamedElementPresenter(mySourceClass).describeWithShortName());
    }
  }


  private static void reportConflict(JSAttributeListOwner subject,
                                     PsiElement from,
                                     MultiMap<PsiElement, String> conflicts) {
    String message = RefactoringBundle.message("0.with.1.visibility.is.not.accessible.from.2",
                                               ElementDescriptionUtil.getElementDescription(subject,
                                                                                            RefactoringDescriptionLocation.WITHOUT_PARENT),
                                               JSFormatUtil.formatVisibility(subject.getAttributeList().getAccessType()),
                                               ElementDescriptionUtil.getElementDescription(
                                                 JSRefactoringConflictsUtil.getUsageLocation(from), RefactoringDescriptionLocation.WITH_PARENT));

    message = StringUtil.capitalize(message);
    conflicts.putValue(from, message);
  }

  private class JSExtractInterfaceUsageViewDescriptor extends UsageViewDescriptorAdapter {
    @Override
    public PsiElement @NotNull [] getElements() {
      return new PsiElement[]{mySourceClass};
    }

    @Override
    public String getProcessedElementsHeader() {
      return RefactoringBundle.message(myClassNotInterface ? "members.to.form.superclass" : "members.to.form.interface");
    }
  }
}
