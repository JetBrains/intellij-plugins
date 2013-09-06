package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.ide.DartLookupElement;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.psi.impl.DartExportStatementImpl;
import com.jetbrains.lang.dart.psi.impl.DartImportStatementImpl;
import com.jetbrains.lang.dart.psi.impl.DartPsiCompositeElementImpl;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Created by fedorkorotkov.
 */
public class DartReferenceCompletionContributor extends CompletionContributor {
  public DartReferenceCompletionContributor() {
    final PsiElementPattern.Capture<PsiElement> idInReference =
      psiElement().withSuperParent(1, DartId.class).withSuperParent(2, DartReference.class);
    extend(CompletionType.BASIC,
           idInReference.andNot(psiElement().withSuperParent(3, DartLibraryReferenceList.class)),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               final DartReference reference = PsiTreeUtil.getParentOfType(parameters.getPosition(), DartReference.class);
               if (reference != null) {
                 final THashSet<DartComponentName> variants = new THashSet<DartComponentName>();
                 for (LookupElement element : DartReferenceCompletionContributor.addCompletionVariants(reference, variants)) {
                   result.addElement(element);
                 }
                 if (parameters.getInvocationCount() > 1 && DartResolveUtil.aloneOrFirstInChain(reference)) {
                   DartGlobalVariantsHelper.addAdditionalGlobalVariants(result, reference, variants, null);
                 }
               }
             }
           });
    extend(CompletionType.BASIC,
           idInReference.withSuperParent(3, DartLibraryReferenceList.class),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               final DartReference reference = PsiTreeUtil.getParentOfType(parameters.getPosition(), DartReference.class);
               final PsiElement library = resolveLibrary(reference);
               if (library != null) {
                 final Set<DartComponentName> suggestedVariants = new THashSet<DartComponentName>();
                 DartResolveUtil.processTopLevelDeclarations(reference, new ComponentNameScopeProcessor(suggestedVariants),
                                                             DartResolveUtil.getRealVirtualFile(library.getContainingFile()), null);
                 for (DartLookupElement element : DartLookupElement.convert(suggestedVariants, false)) {
                   result.addElement(element);
                 }
               }
             }

             @Nullable
             public PsiElement resolveLibrary(DartReference reference) {
               final DartPsiCompositeElementImpl statement =
                 PsiTreeUtil.getParentOfType(reference, DartImportStatementImpl.class, DartExportStatementImpl.class);
               final DartPathOrLibraryReference pathOrLibraryReference =
                 PsiTreeUtil.getChildOfType(statement, DartPathOrLibraryReference.class);
               return pathOrLibraryReference != null ? pathOrLibraryReference.resolve() : null;
             }
           });
  }

  private static Collection<DartLookupElement> addCompletionVariants(@NotNull DartReference reference,
                                                                     Set<DartComponentName> suggestedVariants) {
    DartClass dartClass = null;
    // if do not contain references
    if (DartResolveUtil.aloneOrFirstInChain(reference)) {
      final PsiScopeProcessor processor = new ComponentNameScopeProcessor(suggestedVariants);
      DartResolveUtil.treeWalkUpAndTopLevelDeclarations(reference, processor);
      dartClass = PsiTreeUtil.getParentOfType(reference, DartClass.class);
    }

    final DartReference leftReference = DartResolveUtil.getLeftReference(reference);
    if (leftReference != null) {
      final DartClassResolveResult classResolveResult = leftReference.resolveDartClass();
      dartClass = classResolveResult.getDartClass();
      // prefix
      if (PsiTreeUtil.getParentOfType(leftReference.resolve(), DartImportStatement.class, DartExportStatement.class) != null) {
        final VirtualFile virtualFile = DartResolveUtil.getFileByPrefix(reference.getContainingFile(), leftReference.getText());
        DartResolveUtil.processTopLevelDeclarations(reference, new ComponentNameScopeProcessor(suggestedVariants), virtualFile, null);
      }
    }

    if (dartClass != null) {
      final boolean needFilterPrivateMembers = !DartResolveUtil.sameLibrary(reference, dartClass);
      suggestedVariants.addAll(DartResolveUtil.getComponentNames(dartClass.getFields(), needFilterPrivateMembers));
      suggestedVariants.addAll(DartResolveUtil.getComponentNames(dartClass.getMethods(), needFilterPrivateMembers));
      suggestedVariants.addAll(DartResolveUtil.getComponentNames(
        ContainerUtil.filter(
          dartClass.getConstructors(),
          new Condition<DartComponent>() {
            @Override
            public boolean value(DartComponent component) {
              return component instanceof DartNamedConstructorDeclaration || component instanceof DartFactoryConstructorDeclaration;
            }
          }),
        needFilterPrivateMembers
      ));
    }

    final boolean typeInNew = reference.getParent() instanceof DartType && reference.getParent().getParent() instanceof DartNewExpression;

    if (typeInNew) {
      final Set<DartComponentName> constructors = new THashSet<DartComponentName>();
      for (DartComponentName componentName : suggestedVariants) {
        final PsiElement parent = componentName.getParent();
        if (!(parent instanceof DartClass)) continue;
        constructors.addAll(DartResolveUtil.getComponentNames(ContainerUtil.filter(
          ((DartClass)parent).getConstructors(),
          new Condition<DartComponent>() {
            @Override
            public boolean value(DartComponent component) {
              boolean namedOrFactory = component instanceof DartNamedConstructorDeclaration ||
                                       component instanceof DartFactoryConstructorDeclaration;
              return namedOrFactory && component.isPublic();
            }
          })
        ));
      }
      suggestedVariants.addAll(constructors);
    }
    return DartLookupElement.convert(suggestedVariants, typeInNew);
  }
}
