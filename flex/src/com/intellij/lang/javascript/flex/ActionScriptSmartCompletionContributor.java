package com.intellij.lang.javascript.flex;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.completion.JSLookupUtilImpl;
import com.intellij.lang.javascript.completion.JSSmartCompletionContributor;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.lang.javascript.psi.resolve.VariantsProcessor;
import com.intellij.lang.javascript.search.JSClassSearch;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author yole
 */
public class ActionScriptSmartCompletionContributor extends JSSmartCompletionContributor {
  @Nullable
  @Override
  public List<Object> getSmartCompletionVariants(@NotNull PsiElement location) {
    final PsiElement parent = location.getParent();

    if (parent instanceof JSArgumentList &&
        ((JSArgumentList)parent).getArguments()[0] == location &&
        ((JSReferenceExpression)location).getQualifier() == null
      ) {
      final JSExpression calledExpr = ((JSCallExpression) parent.getParent()).getMethodExpression();

      if (calledExpr instanceof JSReferenceExpression) {
        final JSReferenceExpression expression = (JSReferenceExpression)calledExpr;
        final @NonNls String s = expression.getReferencedName();

        if ("addEventListener".equals(s) ||
            "removeEventListener".equals(s) ||
            "willTrigger".equals(s) ||
            "hasEventListener".equals(s)
          ) {
          final List<Object> variants = new ArrayList<Object>();
          final MyEventSubclassesProcessor subclassesProcessor = new MyEventSubclassesProcessor(location, variants);
          subclassesProcessor.findAcceptableVariants(expression);
          return variants;
        }
      }
    }

    return null;
  }

  public static @Nullable JSClass findClassOfQualifier(JSReferenceExpression expression) {
    JSExpression qualifier = expression.getQualifier();

    JSClass clazzToProcess = null;

    if (qualifier != null) {
      qualifier = PsiUtilBase.getOriginalElement(qualifier, qualifier.getClass());
      clazzToProcess = qualifier != null ? JSResolveUtil.findClassOfQualifier(qualifier, qualifier.getContainingFile()):null;
    }

    if (clazzToProcess == null) {
      clazzToProcess = JSResolveUtil.getClassOfContext(expression);
    }
    return clazzToProcess;
  }

  public static Map<String, String> getEventsMap(JSClass clazzToProcess) {
    if (clazzToProcess == null) return Collections.emptyMap();

    final Map<String, String> eventsMap = new THashMap<String, String>();
    class EventsDataCollector extends ResolveProcessor implements JSResolveUtil.MetaDataProcessor {

      public EventsDataCollector() {
        super(null);

        setToProcessHierarchy(true);
        setToProcessMembers(false);
        setTypeContext(true);
        setLocalResolve(true);
      }

      public boolean process(final @NotNull JSAttribute jsAttribute) {
        if ("Event".equals(jsAttribute.getName())) {
          final JSAttributeNameValuePair eventAttr = jsAttribute.getValueByName("name");
          JSAttributeNameValuePair typeAttr = jsAttribute.getValueByName("type");

          if (eventAttr != null && typeAttr != null) {
            final String simpleValue = eventAttr.getSimpleValue();
            if (simpleValue != null) {
              eventsMap.put(simpleValue, typeAttr.getSimpleValue());
            }
          }
        }
        return true;
      }

      public boolean handleOtherElement(final PsiElement el, final PsiElement context, final Ref<PsiElement> continuePassElement) {
        return true;
      }

      @Override
      public boolean execute(@NotNull PsiElement element, ResolveState state) {
        if (element instanceof JSClass) {
          JSResolveUtil.processMetaAttributesForClass(element, this, true);
        }
        return true;
      }
    }

    final EventsDataCollector eventsDataCollector = new EventsDataCollector();
    if (clazzToProcess instanceof XmlBackedJSClassImpl) {
      XmlFile file = (XmlFile)clazzToProcess.getParent().getContainingFile();
      if (file != null && JavaScriptSupportLoader.isFlexMxmFile(file)) {
        final XmlDocument xmlDocument = file.getDocument();
        final XmlTag rootTag = xmlDocument == null ? null : xmlDocument.getRootTag();
        final XmlTag[] tags = rootTag == null ? XmlTag.EMPTY
                                              : MxmlJSClass.findLanguageSubTags(rootTag, FlexPredefinedTagNames.METADATA);
        JSResolveUtil.JSInjectedFilesVisitor injectedFilesVisitor = new JSResolveUtil.JSInjectedFilesVisitor() {
          @Override
          protected void process(JSFile file) {
            for(PsiElement element:file.getChildren()) {
              if (element instanceof JSAttributeList) {
                JSResolveUtil.processAttributeList(eventsDataCollector, null, (JSAttributeList)element, true, true);
              }
            }
          }
        };
        for(XmlTag tag: tags) {
          JSResolveUtil.processInjectedFileForTag(tag, injectedFilesVisitor);
        }
      }
    }

    clazzToProcess.processDeclarations(eventsDataCollector, ResolveState.initial(), clazzToProcess, clazzToProcess);
    return eventsMap;
  }

  private static class MyEventSubclassesProcessor extends ResolveProcessor {
    private final JavaScriptIndex index;
    private final PsiElement myExpr;
    private final List<Object> myVariants;
    private final ResolveState state = new ResolveState();
    private Map<String, String> myEventsMap = new THashMap<String, String>();

    public MyEventSubclassesProcessor(final PsiElement expr, final List<Object> variants) {
      super(null);
      myExpr = expr;
      myVariants = variants;
      index = JavaScriptIndex.getInstance(myExpr.getProject());

      setToProcessHierarchy(true);
    }

    public boolean process(final JSClass clazz) {
      clazz.processDeclarations(this, state, clazz, clazz);

      return true;
    }

    public boolean execute(@NotNull final PsiElement element, final ResolveState state) {
      if (element instanceof JSVariable) {
        final JSVariable variable = (JSVariable)element;
        final JSAttributeList attributeList = variable.getAttributeList();

        if (attributeList != null &&
            attributeList.getAccessType() == JSAttributeList.AccessType.PUBLIC &&
            attributeList.hasModifier(JSAttributeList.ModifierType.STATIC) &&
            "String".equals(variable.getTypeString())
          ) {
          final String s = variable.getInitializerText();
          if (s != null && StringUtil.startsWith(s, "\"") && StringUtil.endsWith(s,"\"") ) {
            String key = StringUtil.stripQuotesAroundValue(s);
            String event = myEventsMap.get(key);
            if (event == null) return true;
            PsiElement parent = JSResolveUtil.findParent(element);
            if (!(parent instanceof JSClass) || !event.equals(((JSClass)parent).getQualifiedName())) return true;

            String name = variable.getName();
            LookupElement lookupItem = JSLookupUtilImpl.createPrioritizedLookupItem(
              variable,
              ((JSClass)parent).getName() + "." + name,
              VariantsProcessor.LookupPriority.SMART_PROPRITY, false, true
            );

            ((LookupItem)lookupItem).addLookupStrings(name);
            myVariants.add(lookupItem);
          }
        }
      }

      return true;
    }

    public void findAcceptableVariants(JSReferenceExpression expression) {
      JSClass clazzToProcess = findClassOfQualifier(expression);

      if (clazzToProcess == null) return;
      myEventsMap = getEventsMap(clazzToProcess);

      final PsiElement eventClass1 = JSResolveUtil.unwrapProxy(
        JSResolveUtil.findClassByQName(FlexCommonTypeNames.FLASH_EVENT_FQN, index, ModuleUtilCore.findModuleForPsiElement(expression)));
      if ((eventClass1 instanceof JSClass)) {
        setToProcessMembers(true);
        setTypeContext(false);

        final Set<String> visited = new THashSet<String>();
        for (JSClass cls : JSClassSearch.searchClassInheritors((JSClass)eventClass1, true, expression.getResolveScope()).findAll()) {
          if (!visited.add(cls.getQualifiedName())) continue;
          process(cls);
        }
      }

      final PsiElement eventClass2 = JSResolveUtil.unwrapProxy(
        JSResolveUtil.findClassByQName(FlexCommonTypeNames.STARLING_EVENT_FQN, index, ModuleUtilCore.findModuleForPsiElement(expression)));
      if ((eventClass2 instanceof JSClass)) {
        setToProcessMembers(true);
        setTypeContext(false);

        final Set<String> visited = new THashSet<String>();
        for (JSClass cls : JSClassSearch.searchClassInheritors((JSClass)eventClass2, true, expression.getResolveScope()).findAll()) {
          if (!visited.add(cls.getQualifiedName())) continue;
          process(cls);
        }
      }
    }
  }
}
