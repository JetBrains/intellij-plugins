package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.util.Condition;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.ide.DartLookupElement;
import com.jetbrains.lang.dart.ide.index.DartComponentInfo;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.resolve.ClassNameScopeProcessor;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.UsefulPsiTreeUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.or;

public class DartClassNameCompletionContributor extends CompletionContributor {
  public DartClassNameCompletionContributor() {
    final PsiElementPattern.Capture<PsiElement> idInComponentName =
      psiElement().withSuperParent(1, DartId.class).withSuperParent(2, DartComponentName.class);

    final PatternCondition<PsiElement> notAfterDartType = new PatternCondition<PsiElement>("not after DartType") {
      public boolean accepts(@NotNull final PsiElement element, final ProcessingContext context) {
        // no class name completion must be here: const type name<caret>;
        // and no class name completion must be here: void function(Object name<caret>)
        final PsiElement prev = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(element.getParent().getParent(), true);
        return prev == null || !(prev instanceof DartType) && prev.getNode().getElementType() != DartTokenTypes.VAR;
      }
    };
    final ElementPattern<PsiElement> pattern =
      or(idInComponentName.withSuperParent(4, DartNormalFormalParameter.class).with(notAfterDartType),
         idInComponentName.withSuperParent(3, DartVarAccessDeclaration.class).with(notAfterDartType)
      );

    extend(CompletionType.BASIC, pattern,
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull final CompletionParameters parameters,
                                           final ProcessingContext context,
                                           @NotNull final CompletionResultSet result) {
               final Set<DartComponentName> suggestedVariants = new THashSet<DartComponentName>();
               DartResolveUtil.treeWalkUpAndTopLevelDeclarations(parameters.getPosition(), new ClassNameScopeProcessor(suggestedVariants));

               final Set<String> addedNames = DartLookupElement.appendVariantsToCompletionSet(result, suggestedVariants, false);

               if (parameters.getInvocationCount() > 1) {
                 DartGlobalVariantsCompletionHelper
                   .addAdditionalGlobalVariants(result, parameters.getPosition(), addedNames, new Condition<DartComponentInfo>() {
                                                  @Override
                                                  public boolean value(DartComponentInfo info) {
                                                    final DartComponentType type = info.getComponentType();
                                                    return type == DartComponentType.CLASS || type == DartComponentType.TYPEDEF;
                                                  }
                                                }
                   );
               }
             }
           }
    );
  }
}
