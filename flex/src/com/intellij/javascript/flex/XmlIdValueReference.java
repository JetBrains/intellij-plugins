package com.intellij.javascript.flex;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.BasicAttributeValueReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileBasedUserDataCache;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.IdReferenceProvider;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class XmlIdValueReference extends BasicAttributeValueReference {

  public XmlIdValueReference(final PsiElement element) {
    super(element);
  }

  @Nullable
  private static PsiElement getIdValueElement(PsiElement element) {
    if (element instanceof XmlTag) {
      final XmlAttribute attribute = ((XmlTag)element).getAttribute(IdReferenceProvider.ID_ATTR_NAME, null);
      return attribute != null ? attribute.getValueElement() : null;
    }
    else {
      return element;
    }
  }

  @Nullable
  protected static String getIdValue(final PsiElement element) {
    if (element instanceof XmlTag) {
      return ((XmlTag)element).getAttributeValue(IdReferenceProvider.ID_ATTR_NAME);
    }
    return null;
  }

  protected static boolean isAcceptableTagType(final XmlTag subTag) {
    return subTag.getAttributeValue(IdReferenceProvider.ID_ATTR_NAME) != null;
  }

  private static final FileBasedUserDataCache<List<PsiElement>> ourCachedIdsCache = new FileBasedUserDataCache<>() {
    private final Key<CachedValue<List<PsiElement>>> ourCachedIdsValueKey = Key.create("mxml.id.cached.value");

    @Override
    protected List<PsiElement> doCompute(PsiFile file) {
      final List<PsiElement> result = new ArrayList<>();

      file.accept(new XmlRecursiveElementVisitor(true) {
        @Override
        public void visitXmlTag(XmlTag tag) {
          if (isAcceptableTagType(tag)) {
            result.add(tag);
          }
          super.visitXmlTag(tag);
        }
      });
      return result;
    }

    @Override
    protected Key<CachedValue<List<PsiElement>>> getKey() {
      return ourCachedIdsValueKey;
    }
  };

  private void process(PsiElementProcessor<PsiElement> processor) {
    final PsiFile psiFile = getElement().getContainingFile();

    for (PsiElement e : ourCachedIdsCache.compute(psiFile)) {
      if (!processor.execute(e)) return;
    }
  }

  @Override
  @Nullable
  public PsiElement resolve() {
    final Ref<PsiElement> result = new Ref<>();
    process(new PsiElementProcessor<>() {
      final String canonicalText = getCanonicalText();

      @Override
      public boolean execute(@NotNull final PsiElement element) {
        final String idValue = getIdValue(element);
        if (idValue != null && idValue.equals(canonicalText)) {
          result.set(getIdValueElement(element));
          return false;
        }
        return true;
      }
    });

    return result.get();
  }

  @Override
  public Object @NotNull [] getVariants() {
    final List<String> result = new LinkedList<>();

    process(new PsiElementProcessor<>() {
      @Override
      public boolean execute(@NotNull final PsiElement element) {
        result.add(getIdValue(element));
        return true;
      }
    });

    return ArrayUtil.toObjectArray(result);
  }

  @Override
  public boolean isSoft() {
    return false;
  }
}

