package com.intellij.javascript.flex.mxml;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClass;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassProvider;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataCache;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author yole
 */
public class MxmlJSClassProvider extends XmlBackedJSClassProvider {
  @NonNls public static final String SCRIPT_TAG_NAME = "Script";

  private static final Key<CachedValue<XmlTag[]>> CHILD_INLINE_COMPONENTS_TAGS_KEY = Key.create("child.inline.components.tags");
  private static final UserDataCache<CachedValue<XmlTag[]>, XmlTag, Object> ourChildComponentsTagsCache =
    new UserDataCache<CachedValue<XmlTag[]>, XmlTag, Object>() {
      protected CachedValue<XmlTag[]> compute(final XmlTag tag, final Object p) {
        return CachedValuesManager.getManager(tag.getProject()).createCachedValue(new CachedValueProvider<XmlTag[]>() {
          public Result<XmlTag[]> compute() {
            final Collection<XmlTag> result = new ArrayList<>();
            tag.processElements(new PsiElementProcessor() {
              public boolean execute(@NotNull PsiElement element) {
                if (element instanceof XmlTag) {
                  XmlTag tag = (XmlTag)element;
                  if (XmlBackedJSClassImpl.isComponentTag(tag)) {
                    final XmlTag[] subtags = tag.getSubTags();
                    if (subtags.length > 0) {
                      result.add(subtags[0]);
                    }
                  }
                  else {
                    tag.processElements(this, null);
                  }
                }
                return true;
              }
            }, null);
            return new Result<>(result.toArray(new XmlTag[result.size()]), tag);
          }
        }, false);
      }
    };

  public static MxmlJSClassProvider getInstance() {
    for (XmlBackedJSClassProvider provider : Extensions.getExtensions(EP_NAME)) {
      if (provider instanceof MxmlJSClassProvider) {
        return (MxmlJSClassProvider)provider;
      }
    }
    assert false;
    return null;
  }

  @Override
  public boolean hasJSClass(XmlFile file) {
    return JavaScriptSupportLoader.isMxmlOrFxgFile(file);
  }

  @Override
  public boolean isScriptTag(XmlTag tag) {
    return SCRIPT_TAG_NAME.equals(tag.getLocalName());
  }

  @Override
  public boolean canCreateClassFromTag(XmlTag tag) {
    return isComponentSubTag(tag);
  }

  @Override
  public XmlTag getClassOwnerTag(XmlTag tag) {
    if (isComponentSubTag(tag)) {
      return tag.getParentTag().getSubTags()[0];
    }
    return tag;
  }

  @Override
  public XmlBackedJSClass createClassFromTag(XmlTag tag) {
    XmlFile file = (XmlFile)tag.getContainingFile();
    if (file.getRootTag() == tag && JavaScriptSupportLoader.isMxmlOrFxgFile(file)) {
      return new MxmlJSClass(tag);
    }
    if (isComponentSubTag(tag)) {
      return new MxmlJSClass(tag.getParentTag().getSubTags()[0]);
    }
    return null;
  }

  private static boolean isComponentSubTag(XmlTag tag) {
    XmlTag parentTag = tag.getParentTag();
    return parentTag != null && XmlBackedJSClassImpl.isComponentTag(parentTag);
  }

  @Override
  public Collection<? extends JSClass> getChildClasses(XmlFile file) {
    return getChildInlineComponents(file.getRootTag(), true);
  }

  private static void collectComponentsTagRecursively(XmlTag[] parents, Collection<XmlTag> result) {
    ContainerUtil.addAll(result, parents);
    for (XmlTag parent : parents) {
      collectComponentsTagRecursively(ourChildComponentsTagsCache.get(CHILD_INLINE_COMPONENTS_TAGS_KEY, parent, null).getValue(), result);
    }
  }

  public static Collection<XmlBackedJSClass> getChildInlineComponents(XmlTag rootTag, final boolean recursive) {
    final XmlTag[] directChildren = ourChildComponentsTagsCache.get(CHILD_INLINE_COMPONENTS_TAGS_KEY, rootTag, null).getValue();
    Collection<XmlTag> allChildren;
    if (recursive) {
      allChildren = new ArrayList<>();
      collectComponentsTagRecursively(directChildren, allChildren);
    }
    else {
      allChildren = Arrays.asList(directChildren);
    }

    Collection<XmlBackedJSClass> result = new ArrayList<>(allChildren.size());
    for (XmlTag tag : allChildren) {
      result.add(XmlBackedJSClassFactory.getInstance().getXmlBackedClass(tag));
    }

    return result;
  }

}
