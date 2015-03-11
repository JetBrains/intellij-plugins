package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Condition;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.DartTokenTypes;
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
        final PsiElement parent = UsefulPsiTreeUtil
          .getPrevSiblingSkipWhiteSpacesAndComments(element.getParent().getParent(), true);
        if (parent == null) return true;
        // there should be class name completion after on
        if (parent.getText().equals(DartTokenTypes.ON.toString())) {
          return true;
        }
        // no class name completion must be here: const type name<caret>;
        // and no class name completion must be here: void function(Object name<caret>)
        return !(parent instanceof DartType
                 || parent.getNode().getElementType() == DartTokenTypes.VAR);
      }
    };
    final ElementPattern<PsiElement> pattern =
      or(idInComponentName.withSuperParent(4, DartNormalFormalParameter.class).with(notAfterDartType),
         idInComponentName.withSuperParent(3, DartVarAccessDeclaration.class).with(notAfterDartType)
      );

    extend(CompletionType.BASIC, pattern,
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               final Set<DartComponentName> suggestedVariants = new THashSet<DartComponentName>();
               DartResolveUtil.treeWalkUpAndTopLevelDeclarations(parameters.getPosition(), new ClassNameScopeProcessor(suggestedVariants));

               for (DartComponentName variant : suggestedVariants) {
                 result.addElement(LookupElementBuilder.create(variant));
               }
               if (parameters.getInvocationCount() > 1) {
                 DartGlobalVariantsCompletionHelper.addAdditionalGlobalVariants(
                   result, parameters.getPosition(), suggestedVariants,
                   new Condition<DartComponentInfo>() {
                     @Override
                     public boolean value(DartComponentInfo info) {
                       return info.getType() == DartComponentType.CLASS;
                     }
                   }
                 );
               }
             }
           }
    );
  }
}
