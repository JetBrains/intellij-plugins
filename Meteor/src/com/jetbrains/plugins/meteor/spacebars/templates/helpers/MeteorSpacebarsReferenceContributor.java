package com.jetbrains.plugins.meteor.spacebars.templates.helpers;

import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.dmarcotte.handlebars.psi.HbHash;
import com.dmarcotte.handlebars.psi.HbMustacheName;
import com.dmarcotte.handlebars.psi.impl.*;
import com.intellij.lang.Language;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.plugins.meteor.spacebars.lang.SpacebarsLanguageDialect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MeteorSpacebarsReferenceContributor extends PsiReferenceContributor {
  static final class Holder {
    private static final PsiReference[] EMPTY_RESULT = PsiReference.EMPTY_ARRAY;
    static final TokenSet OPEN_TOKEN = TokenSet.create(HbTokenTypes.OPEN, HbTokenTypes.OPEN_UNESCAPED);
    static final TokenSet OPEN_PART_TOKEN = TokenSet.create(HbTokenTypes.OPEN_PARTIAL);
  }
  private enum TagTypes {
    TAG,
    PARTIAL_TAG,
    BLOCK_TAG
  }

  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(possibleMustacheTag(Holder.OPEN_TOKEN), new SpacebarsTagPsiReferenceProvider(TagTypes.TAG));
    registrar.registerReferenceProvider(possibleMustacheTag(Holder.OPEN_PART_TOKEN), new SpacebarsTagPsiReferenceProvider(TagTypes.PARTIAL_TAG));
    registrar.registerReferenceProvider(possibleMustacheBlockTag(), new SpacebarsTagPsiReferenceProvider(TagTypes.BLOCK_TAG));
  }

  public static PsiElementPattern.Capture<HbPsiElementImpl> possibleMustacheBlockTag() {
    return PlatformPatterns.psiElement(HbPsiElementImpl.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        return isAcceptWithOpenToken(element);
      }

      public boolean isAcceptWithOpenToken(Object element) {
        return isAcceptBlockTag(element);
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));
  }

  public static boolean isAcceptBlockTag(Object element) {
    HbPsiElementImpl hbElement = (HbPsiElementImpl)(element);
    PsiElement parentElement = hbElement.getParent();
    if (!(parentElement instanceof HbPathImpl)) return false;
    parentElement = parentElement.getParent();
    if (!(parentElement instanceof HbMustacheName)) return false;


    PsiFile file = hbElement.getContainingFile();
    if (file == null) {
      return false;
    }

    Language language = file.getLanguage();
    if (!language.isKindOf(SpacebarsLanguageDialect.INSTANCE)) return false;


    PsiElement blockWrapper = PsiTreeUtil.getParentOfType(parentElement, HbOpenBlockMustacheImpl.class, HbHash.class);
    if (blockWrapper == null || blockWrapper instanceof HbHash) return false;

    if (blockWrapper.getFirstChild().getNode().getElementType() != HbTokenTypes.OPEN_BLOCK) return false;


    PsiElement firstParam = ContainerUtil.find(blockWrapper.getChildren(),
                                               element1 -> element1.getNode().getElementType() == HbTokenTypes.PARAM);

    return firstParam != null && firstParam.equals(parentElement.getParent());
  }

  public static PsiElementPattern.Capture<HbPsiElementImpl> possibleMustacheTag(final TokenSet openToken) {
    return PlatformPatterns.psiElement(HbPsiElementImpl.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        return isAcceptTag(element, openToken);
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));
  }

  public static boolean isAcceptTag(Object element, TokenSet openToken) {
    HbPsiElementImpl hbElement = (HbPsiElementImpl)(element);

    PsiElement parentElement = hbElement.getParent();
    if (!(parentElement instanceof HbPathImpl)) return false;

    PsiFile file = hbElement.getContainingFile();
    if (file == null) {
      return false;
    }
    Language language = file.getLanguage();
    if (!language.isKindOf(SpacebarsLanguageDialect.INSTANCE)) return false;

    //can resolve only elementName in path {{elementName.propertyName.subProperty}}
    if ((parentElement.getFirstChild() != hbElement)) return false;

    //skip non-mustache construction
    PsiElement mustacheParent = PsiTreeUtil.getParentOfType(parentElement, HbSimpleMustacheImpl.class, HbPartialImpl.class, HbHash.class);
    if (mustacheParent == null || mustacheParent instanceof HbHash) return false;

    //skip {{> templateName}} or {{template}} over openToken
    if (!openToken.contains(mustacheParent.getFirstChild().getNode().getElementType())) return false;

    return true;
  }

  private static final class SpacebarsTagPsiReferenceProvider extends PsiReferenceProvider {
    private final TagTypes myTagTypes;

    private SpacebarsTagPsiReferenceProvider(TagTypes tagTypes) {
      myTagTypes = tagTypes;
    }

    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
      HbPsiElementImpl hbElement = (HbPsiElementImpl)(element);
      String propertyName = hbElement.getText();
      if (StringUtil.isEmptyOrSpaces(propertyName)) return Holder.EMPTY_RESULT;

      return wrap(switch (myTagTypes) {
        case BLOCK_TAG, TAG -> new MeteorMustacheTagPsiReference(hbElement, propertyName);
        case PARTIAL_TAG -> new MeteorMustachePartialTagPsiReference(hbElement, propertyName);
      });
    }

    private static PsiReference[] wrap(PsiReference ref) {
      return new PsiReference[]{ref};
    }
  }
}
