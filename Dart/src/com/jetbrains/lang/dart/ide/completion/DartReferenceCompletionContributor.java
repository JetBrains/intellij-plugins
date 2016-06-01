package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.RowIcon;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.ide.DartLookupElement;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.resolve.ComponentNameScopeProcessor;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class DartReferenceCompletionContributor extends CompletionContributor {

  private static final RowIcon PUBLIC_STATIC_CONST_ICON = new RowIcon(2);
  private static final RowIcon PUBLIC_FINAL_FIELD_ICON = new RowIcon(2);

  static {
    PUBLIC_STATIC_CONST_ICON.setIcon(new LayeredIcon(AllIcons.Nodes.Field, AllIcons.Nodes.StaticMark, AllIcons.Nodes.FinalMark), 0);
    PUBLIC_STATIC_CONST_ICON.setIcon(PlatformIcons.PUBLIC_ICON, 1);
    PUBLIC_FINAL_FIELD_ICON.setIcon(new LayeredIcon(AllIcons.Nodes.Field, AllIcons.Nodes.FinalMark), 0);
    PUBLIC_FINAL_FIELD_ICON.setIcon(PlatformIcons.PUBLIC_ICON, 1);
  }

  public DartReferenceCompletionContributor() {
    final PsiElementPattern.Capture<PsiElement> idInReference =
      psiElement().withSuperParent(1, DartId.class).withSuperParent(2, DartReference.class);

    extend(CompletionType.BASIC,
           idInReference.andNot(psiElement().withSuperParent(3, DartLibraryReferenceList.class)),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull final CompletionParameters parameters,
                                           final ProcessingContext context,
                                           @NotNull final CompletionResultSet resultSet) {
               final DartReference reference = PsiTreeUtil.getParentOfType(parameters.getPosition(), DartReference.class);
               if (reference != null) {
                 final Set<String> addedNames = DartReferenceCompletionContributor.addCompletionVariants(resultSet, reference);

                 if (parameters.getInvocationCount() > 1 && DartResolveUtil.aloneOrFirstInChain(reference)) {
                   DartGlobalVariantsCompletionHelper.addAdditionalGlobalVariants(resultSet, reference, addedNames, null);
                 }
               }
             }
           }
    );

    // references after show/hide in import/export directive
    extend(CompletionType.BASIC,
           idInReference.withSuperParent(3, DartLibraryReferenceList.class),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               final DartImportOrExportStatement statement =
                 PsiTreeUtil.getParentOfType(parameters.getPosition(), DartImportOrExportStatement.class);
               final VirtualFile vFile = parameters.getOriginalFile().getVirtualFile();
               if (statement != null && vFile != null) {
                 final VirtualFile importedFile = DartResolveUtil.getImportedFile(statement.getProject(), vFile, statement.getUriString());
                 if (importedFile != null) {
                   final Set<DartComponentName> suggestedVariants = new THashSet<DartComponentName>();
                   DartResolveUtil
                     .processTopLevelDeclarations(statement, new ComponentNameScopeProcessor(suggestedVariants), importedFile, null);

                   DartLookupElement.appendVariantsToCompletionSet(result, suggestedVariants, false);
                 }
               }
             }
           }
    );
  }

  // returns added names
  private static Set<String> addCompletionVariants(@NotNull final CompletionResultSet resultSet,
                                                   @NotNull final DartReference reference) {
    final Set<DartComponentName> variants = new THashSet<DartComponentName>();

    DartClass dartClass = null;

    if (DartResolveUtil.aloneOrFirstInChain(reference)) {
      DartResolveUtil.treeWalkUpAndTopLevelDeclarations(reference, new ComponentNameScopeProcessor(variants));
      dartClass = PsiTreeUtil.getParentOfType(reference, DartClass.class);
    }

    final DartReference leftReference = DartResolveUtil.getLeftReference(reference);
    if (leftReference != null) {
      final DartClassResolveResult classResolveResult = leftReference.resolveDartClass();
      dartClass = classResolveResult.getDartClass();

      // import prefix
      if (PsiTreeUtil.getParentOfType(leftReference.resolve(), DartImportStatement.class) != null) {
        DartResolveUtil.processDeclarationsInImportedFileByImportPrefix(reference, leftReference.getText(),
                                                                        new ComponentNameScopeProcessor(variants), null);
      }
    }

    if (dartClass != null) {
      if (dartClass.isEnum()) {
        if (leftReference != null && Comparing.equal(dartClass.getComponentName(), leftReference.resolve())) {
          resultSet.consume(LookupElementBuilder.create("values")
                              .withTypeText("List<" + dartClass.getName() + ">")
                              .withIcon(PUBLIC_STATIC_CONST_ICON));
          variants.addAll(DartResolveUtil.getComponentNames(dartClass.getEnumConstantDeclarationList()));
        }
        else {
          resultSet.consume(LookupElementBuilder.create("index").withTypeText("int").withIcon(PUBLIC_FINAL_FIELD_ICON));
          variants.addAll(DartResolveUtil.getComponentNames(dartClass.getMethods())); // inherited from Object are here
        }
      }
      else {
        final boolean needFilterPrivateMembers = !DartResolveUtil.sameLibrary(reference, dartClass);
        variants.addAll(DartResolveUtil.getComponentNames(dartClass.getFields(), needFilterPrivateMembers));
        variants.addAll(DartResolveUtil.getComponentNames(dartClass.getMethods(), needFilterPrivateMembers));
        variants.addAll(DartResolveUtil.getComponentNames(
          ContainerUtil.filter(
            dartClass.getConstructors(),
            component -> component instanceof DartNamedConstructorDeclaration || component instanceof DartFactoryConstructorDeclaration
          ),
          needFilterPrivateMembers
        ));
      }
    }

    final boolean typeInNew = reference.getParent() instanceof DartType && reference.getParent().getParent() instanceof DartNewExpression;

    if (typeInNew) {
      final Set<DartComponentName> constructors = new THashSet<DartComponentName>();
      for (DartComponentName componentName : variants) {
        final PsiElement parent = componentName.getParent();
        if (!(parent instanceof DartClass)) continue;
        constructors.addAll(DartResolveUtil.getComponentNames(ContainerUtil.filter(
                                                                ((DartClass)parent).getConstructors(),
                                                                component -> {
                                                                  boolean namedOrFactory =
                                                                    component instanceof DartNamedConstructorDeclaration ||
                                                                    component instanceof DartFactoryConstructorDeclaration;
                                                                  return namedOrFactory && component.isPublic();
                                                                }
                                                              )
        ));
      }
      variants.addAll(constructors);
    }
    return DartLookupElement.appendVariantsToCompletionSet(resultSet, variants, typeInNew);
  }
}
