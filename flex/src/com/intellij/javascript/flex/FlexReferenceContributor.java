package com.intellij.javascript.flex;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.BasicAttributeValueReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileBasedUserDataCache;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.xml.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.text.StringTokenizer;
import com.intellij.xml.util.XmlTagUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// this class is not a reference contributor any more, it is kept for MXML Design Preview plugin compatibility
public class FlexReferenceContributor {
  static final String TRANSITION_TAG_NAME = "Transition";
  public static final String SOURCE_ATTR_NAME = "source";
  public static final String DESTINATION_ATTR_NAME = "destination";
  static final String DELIMS = ", ";
  public static final String CLASS_REFERENCE = "ClassReference";

  public static boolean isClassReferenceType(final String type) {
    return "Class".equals(type) || FlexCommonTypeNames.IFACTORY.equals(type);
  }

  public static class StateReference extends BasicAttributeValueReference implements EmptyResolveMessageProvider, PsiPolyVariantReference {
    private static final String DUMMY_STATE_GROUP_TAG = "DummyStateGroupTag";

    private static FileBasedUserDataCache<Map<String, XmlTag>> statesCache = new FileBasedUserDataCache<Map<String, XmlTag>>() {
      public Key<CachedValue<Map<String, XmlTag>>> ourDataKey = Key.create("mx.states");

      @Override
      protected Map<String, XmlTag> doCompute(PsiFile file) {
        final Map<String, XmlTag> tags = new THashMap<>();

        file.getOriginalFile().accept(new XmlRecursiveElementVisitor() {
          @Override
          public void visitXmlTag(XmlTag tag) {
            super.visitXmlTag(tag);

            if ("State".equals(tag.getLocalName())) {
              String name = tag.getAttributeValue(FlexStateElementNames.NAME);
              if (name != null) tags.put(name, tag);
              String groups = tag.getAttributeValue(FlexStateElementNames.STATE_GROUPS);

              if (groups != null) {
                StringTokenizer tokenizer = new StringTokenizer(groups, DELIMS);
                while (tokenizer.hasMoreElements()) {
                  String s = tokenizer.nextElement();

                  XmlTag cachedTag = tags.get(s);
                  if (cachedTag == null) {
                    PsiFile fromText = PsiFileFactory.getInstance(tag.getProject())
                      .createFileFromText("dummy.mxml", FlexApplicationComponent.MXML,
                                          "<" + DUMMY_STATE_GROUP_TAG + " name=\"" + s + "\" />");
                    cachedTag = ((XmlFile)fromText).getDocument().getRootTag();
                    tags.put(s, cachedTag);
                  }
                }
              }
            }
          }
        });
        return tags;
      }

      @Override
      protected Key<CachedValue<Map<String, XmlTag>>> getKey() {
        return ourDataKey;
      }
    };


    private final boolean myStateGroupsOnly;

    public StateReference(PsiElement element) {
      super(element);
      myStateGroupsOnly = false;
    }

    public StateReference(PsiElement element, TextRange range) {
      this(element, range, false);
    }

    public StateReference(PsiElement element, TextRange range, boolean stateGroupsOnly) {
      super(element, range);
      myStateGroupsOnly = stateGroupsOnly;
    }

    public PsiElement resolve() {
      ResolveResult[] results = multiResolve(false);
      return results.length == 1 ? results[0].getElement() : null;
    }

    @NotNull
    public ResolveResult[] multiResolve(boolean incompleteCode) {
      final List<ResolveResult> result = new ArrayList<>(1);
      process(new StateProcessor() {
        public boolean process(@NotNull final XmlTag t, @NotNull String name) {
          result.add(new ResolveResult() {
            public PsiElement getElement() {
              return t.getAttribute(FlexStateElementNames.NAME).getValueElement();
            }

            public boolean isValidResult() {
              return true;
            }
          });
          return true;
        }

        public String getHint() {
          return getCanonicalText();
        }
      });
      return result.toArray(new ResolveResult[result.size()]);
    }

    interface StateProcessor {
      boolean process(@NotNull XmlTag t, @NotNull String name);

      @Nullable
      String getHint();
    }

    @NotNull
    public Object[] getVariants() {
      final Set<String> list = new THashSet<>();

      process(new StateProcessor() {
        public boolean process(@NotNull XmlTag t, @NotNull String name) {
          list.add(name);
          return true;
        }

        public String getHint() {
          return null;
        }
      });

      final PsiElement parent = myElement instanceof XmlAttributeValue ? myElement.getParent() : null;
      final PsiElement tag = parent instanceof XmlAttribute ? parent.getParent() : null;

      if (tag instanceof XmlTag && TRANSITION_TAG_NAME.equals(((XmlTag)tag).getLocalName())) {
        list.add("*");
      }

      return ArrayUtil.toObjectArray(list);
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
      for (ResolveResult r : multiResolve(false)) {
        if (myElement.getManager().areElementsEquivalent(element, r.getElement())) return true;
      }
      return false;
    }

    private boolean process(StateProcessor processor) {
      String s = processor.getHint();

      Map<String, XmlTag> map = statesCache.compute(getElement().getContainingFile());
      if (s == null) {
        for (Map.Entry<String, XmlTag> t : map.entrySet()) {
          XmlTag tag = t.getValue();
          if (myStateGroupsOnly && !DUMMY_STATE_GROUP_TAG.equals(tag.getName())) continue;
          if (!processor.process(tag, t.getKey())) return false;
        }
      }
      else {
        XmlTag tag = map.get(s);
        if (myStateGroupsOnly && !DUMMY_STATE_GROUP_TAG.equals(tag.getName())) return true;
        if (tag != null) return processor.process(tag, s);
      }
      return true;
    }

    public boolean isSoft() {
      return false;
    }

    @NotNull
    public String getUnresolvedMessagePattern() {
      return FlexBundle.message("cannot.resolve.state");
    }

    public PsiElement handleElementRename(final String newElementName) throws IncorrectOperationException {
      if (myElement instanceof XmlTag) {
        final XmlToken startTagNameElement = XmlTagUtil.getStartTagNameElement((XmlTag)myElement);
        if (startTagNameElement != null) {
          final TextRange rangeInTagNameElement = myRange.shiftRight(-(startTagNameElement.getTextOffset() - myElement.getTextOffset()));
          final TextRange startTagNameElementRange =
            startTagNameElement.getTextRange().shiftRight(-myElement.getTextRange().getStartOffset());
          if (startTagNameElementRange.contains(rangeInTagNameElement)) {
            final StringBuilder newName = new StringBuilder(startTagNameElement.getText());
            newName.replace(rangeInTagNameElement.getStartOffset(), rangeInTagNameElement.getEndOffset(), newElementName);
            ((XmlTag)myElement).setName(newName.toString());
          }
        }
        return myElement;
      }

      return super.handleElementRename(newElementName);
    }
  }
}
