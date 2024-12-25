package com.jetbrains.plugins.meteor.spacebars.templates.helpers;

import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.dmarcotte.handlebars.psi.impl.HbPsiElementImpl;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.JSQualifiedName;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.JSNamespaceMembersIndex;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ProcessingContext;
import com.jetbrains.plugins.meteor.spacebars.templates.MeteorHelpersFrameworkIndexingHandler;
import com.jetbrains.plugins.meteor.spacebars.templates.MeteorTemplateIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class MeteorSpacebarsCompletionContributor extends CompletionContributor {

  public MeteorSpacebarsCompletionContributor() {
    extend(CompletionType.BASIC, possibleTag(MeteorSpacebarsReferenceContributor.Holder.OPEN_PART_TOKEN), getPartialTagProvider());
    extend(CompletionType.BASIC, possibleTag(MeteorSpacebarsReferenceContributor.Holder.OPEN_TOKEN), getTagProvider());
    extend(CompletionType.BASIC, possibleBlockTag(), getTagProvider());
  }

  public static PsiElementPattern.Capture<PsiElement> possibleBlockTag() {
    return PlatformPatterns.psiElement(HbTokenTypes.ID).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        if (context == null || !(context.getParent() instanceof HbPsiElementImpl)) return false;
        return MeteorSpacebarsReferenceContributor.isAcceptBlockTag(context.getParent());
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));
  }

  public static PsiElementPattern.Capture<PsiElement> possibleTag(final TokenSet openToken) {
    return PlatformPatterns.psiElement(HbTokenTypes.ID).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        if (context == null || !(context.getParent() instanceof HbPsiElementImpl)) return false;

        return MeteorSpacebarsReferenceContributor.isAcceptTag(context.getParent(), openToken);
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));
  }

  private static @NotNull CompletionProvider<CompletionParameters> getTagProvider() {
    return new CompletionProvider<>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        Project project = position.getProject();
        {
          String namespace = MeteorHelpersFrameworkIndexingHandler.NAMESPACE_GLOBAL_HELPERS;
          Collection<JSElement> elements = StubIndex.getElements(JSNamespaceMembersIndex.KEY,
                                                                 namespace,
                                                                 project,
                                                                 GlobalSearchScope.allScope(project),
                                                                 JSElement.class);

          if (!elements.isEmpty()) {
            result.addAllElements(getLookupElements(elements, namespace));
          }
        }

        {
          final String templateName = MeteorMustacheTagPsiReference.getTemplateName(position.getParent());
          if (StringUtil.isEmpty(templateName)) return;

          String namespace = MeteorHelpersFrameworkIndexingHandler
            .getTemplateNamespace(templateName);
          Collection<JSElement> elementsTemplate = StubIndex.getElements(JSNamespaceMembersIndex.KEY,
                                                                         namespace,
                                                                         project,
                                                                         GlobalSearchScope.allScope(project),
                                                                         JSElement.class);

          if (!elementsTemplate.isEmpty()) {
            result.addAllElements(getLookupElements(elementsTemplate, namespace));
          }
        }
      }
    };
  }


  private static @NotNull CompletionProvider<CompletionParameters> getPartialTagProvider() {
    return new CompletionProvider<>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        result.addAllElements(getIndexedElements(parameters.getPosition()));
      }
    };
  }

  private static @NotNull List<LookupElement> getIndexedElements(@NotNull PsiElement element) {
    Collection<String> keys = MeteorTemplateIndex.getKeys(element.getProject());
    List<LookupElement> result = new ArrayList<>(keys.size());
    for (String s : keys) {
      result.add(LookupElementBuilder.create(s));
    }

    return result;
  }

  private static @NotNull List<LookupElement> getLookupElements(@NotNull Collection<JSElement> elements, @NotNull String namespace) {
    List<LookupElement> result = new ArrayList<>(elements.size());
    for (JSElement element : elements) {
      String name = element.getName();
      if (!StringUtil.isEmpty(name)) {
        result.add(LookupElementBuilder.create(name));
      }
      else if (element instanceof JSImplicitElementProvider) {
        JSElementIndexingData data = ((JSImplicitElementProvider)element).getIndexingData();
        if (data != null && data.getImplicitElements() != null) {
          for (JSImplicitElement implicitElement : data.getImplicitElements()) {
            JSQualifiedName implicitElementNamespace = implicitElement.getNamespace();
            if (implicitElementNamespace != null &&
                StringUtil.equals(implicitElementNamespace.getQualifiedName(), namespace) &&
                !StringUtil.isEmpty(implicitElement.getName())) {
              result.add(LookupElementBuilder.create(implicitElement.getName()));
              break;
            }
          }
        }
      }
    }
    return result;
  }
}
