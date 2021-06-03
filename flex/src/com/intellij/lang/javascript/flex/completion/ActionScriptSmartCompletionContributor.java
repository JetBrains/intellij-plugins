// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.completion.JSCompletionContributor;
import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.completion.JSLookupUtilImpl;
import com.intellij.lang.javascript.completion.JSSmartCompletionContributor;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.index.JSPackageIndex;
import com.intellij.lang.javascript.index.JSPackageIndexInfo;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.lang.javascript.psi.types.*;
import com.intellij.lang.javascript.search.JSClassSearch;
import com.intellij.lang.javascript.types.TypeFromUsageDetector;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Query;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.lang.javascript.psi.resolve.AccessibilityProcessingHandler.processWithStatic;
import static com.intellij.lang.javascript.psi.types.JSNamedType.createType;


public class ActionScriptSmartCompletionContributor extends JSSmartCompletionContributor {
  @Nullable
  @Override
  public List<LookupElement> getSmartCompletionVariants(final @NotNull JSReferenceExpression location) {
    final PsiElement parent = location.getParent();

    List<LookupElement> variants = new ArrayList<>();
    if (parent instanceof JSArgumentList &&
        ((JSArgumentList)parent).getArguments()[0] == location &&
        location.getQualifier() == null
      ) {
      final JSExpression calledExpr = ((JSCallExpression)parent.getParent()).getMethodExpression();

      if (calledExpr instanceof JSReferenceExpression) {
        final JSReferenceExpression expression = (JSReferenceExpression)calledExpr;
        @NonNls final String s = expression.getReferencedName();

        if (ActionScriptResolveUtil.ADD_EVENT_LISTENER_METHOD.equals(s) ||
            ActionScriptResolveUtil.REMOVE_EVENT_LISTENER_METHOD.equals(s) ||
            "willTrigger".equals(s) ||
            "hasEventListener".equals(s)
          ) {
          final MyEventSubclassesProcessor subclassesProcessor = new MyEventSubclassesProcessor(location, variants);
          subclassesProcessor.findAcceptableVariants(expression);
          return variants;
        }
      }
    }

    JSType expectedClassType = JSTypeUtils.getValuableType(findClassType(parent));
    if (expectedClassType != null) {
      JSClass clazz = expectedClassType.resolveClass();

      if (clazz != null && !JSGenericTypeImpl.isGenericActionScriptVectorType(expectedClassType)) {
        final Set<String> processedCandidateNames = new THashSet<>(50);
        Query<JSClass> query;

        if (clazz.isInterface()) {
          query = JSClassSearch.searchInterfaceImplementations(clazz, true, location.getResolveScope());
        }
        else {
          final String name = clazz.getName();
          if (name != null) {
            LookupElement lookupItem = JSLookupUtilImpl
              .createPrioritizedLookupItem(clazz, name, JSLookupPriority.MATCHED_TYPE_PRIORITY);

            variants.add(lookupItem);
          }
          processedCandidateNames.add(clazz.getQualifiedName());
          query = JSClassSearch.searchClassInheritors(clazz, true, location.getResolveScope());
        }

        addAllClassesFromQuery(variants, query, parent, processedCandidateNames);
        if (clazz.isInterface()) {
          IElementType opSign;

          if (parent instanceof JSBinaryExpression &&
              ((opSign = ((JSBinaryExpression)parent).getOperationSign()) == JSTokenTypes.AS_KEYWORD ||
               opSign == JSTokenTypes.IS_KEYWORD
              )) {
            addAllClassesFromQuery(variants, JSClassSearch.searchClassInheritors(clazz, true, location.getResolveScope()), parent,
                                   processedCandidateNames);
          }
        }

        final JSCompletionContributor contributor = JSCompletionContributor.getInstance();
        if (!contributor.isDoingSmartCodeCompleteAction()) {
          contributor.setAlreadyUsedClassesSet(processedCandidateNames);
        }
      }
      else {
        String typeText = expectedClassType.getTypeText();
        if (!(expectedClassType instanceof JSAnyType)) {
          variants.add(JSLookupUtilImpl.createPrioritizedLookupItem(
            clazz,
            ImportUtils.importAndShortenReference(typeText, parent, false, true).first + "()",
            JSLookupPriority.SMART_PRIORITY
          ));
        }

        JSResolveUtil.GenericSignature signature = JSResolveUtil.extractGenericSignature(typeText);
        if (signature != null) {
          variants.add(JSLookupUtilImpl.createPrioritizedLookupItem(
            createType(JSCommonTypeNames.ARRAY_CLASS_NAME, JSTypeSourceFactory.createTypeSource(parent), JSContext.INSTANCE).resolveClass(),
            "<" + ImportUtils.importAndShortenReference(signature.genericType, parent, false, true).first + ">" + "[]",
            JSLookupPriority.SMART_PRIORITY
          ));
        }
      }
      return variants.isEmpty() ? Collections.emptyList() : variants;
    }
    else if (location.getQualifier() == null) {
      if (JSResolveUtil.isExprInStrictTypeContext(location)) {
        if (parent instanceof JSVariable || parent instanceof JSFunction) {
          JSType type = TypeFromUsageDetector.detectTypeFromUsage(parent);
          if (type == null && parent instanceof JSVariable) {
            PsiElement parent2 = parent.getParent();
            PsiElement grandParent = parent2 instanceof JSVarStatement ? parent2.getParent() : null;
            if (grandParent instanceof JSForInStatement &&
                ((JSForInStatement)grandParent).isForEach() &&
                parent2 == ((JSForInStatement)grandParent).getVarDeclaration()
              ) {
              JSExpression expression = ((JSForInStatement)grandParent).getCollectionExpression();
              if (expression != null) {
                JSType expressionType = JSResolveUtil.getExpressionJSType(expression);
                if (expressionType != null) {
                  final JSType componentType = JSTypeUtils.getIndexableComponentType(expressionType);
                  if (componentType != null) {
                    type = componentType;
                  }
                }
              }
            }
          }

          final String qualifiedNameMatchingType = type != null ? JSTypeUtils.getQualifiedNameMatchingType(type, false) : null;
          if (qualifiedNameMatchingType != null) {
            String qName = JSDialectSpecificHandlersFactory.forElement(location).getImportHandler()
              .resolveTypeName(qualifiedNameMatchingType, location)
              .getQualifiedName();
            variants.add(JSLookupUtilImpl.createPrioritizedLookupItem(
              JSDialectSpecificHandlersFactory.forElement(location).getClassResolver().findClassByQName(qName, location),
              ImportUtils.importAndShortenReference(qName, parent, false, true).first,
              JSLookupPriority.SMART_PRIORITY
            ));
          }
        }
      }
      else {
        variants = addVariantsForUnqualifiedReference(location);
      }
    }
    return variants.isEmpty() ? null : variants;
  }

  @Override
  protected void processClasses(PsiElement parentInOriginalTree, final SinkResolveProcessor<?> processor) {
    final Project project = parentInOriginalTree.getProject();
    final GlobalSearchScope resolveScope = JSResolveUtil.getResolveScope(parentInOriginalTree);
    final LinkedHashSet<String> qualifiedNames = new LinkedHashSet<>();
    JSPackageIndex.processElementsInScopeRecursive("", new JSPackageIndex.PackageQualifiedElementsProcessor() {
      @Override
      public boolean process(String qualifiedName, JSPackageIndexInfo.Kind kind, boolean isPublic) {
        if (kind != JSPackageIndexInfo.Kind.FUNCTION && kind != JSPackageIndexInfo.Kind.VARIABLE) return true;
        qualifiedNames.add(qualifiedName);
        return true;
      }
    }, resolveScope, project);
    for (String qualifiedName : qualifiedNames) {
      PsiElement element = JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver()
        .findClassByQName(qualifiedName, resolveScope);
      if (element != null && !processor.execute(element, ResolveState.initial())) {
        return;
      }
    }
  }

  private static void addAllClassesFromQuery(List<LookupElement> variants,
                                             Query<JSClass> query,
                                             PsiElement place,
                                             Set<String> processedCandidateNames) {
    Collection<JSClass> all = query.findAll();
    String packageName = place != null ? JSResolveUtil.getPackageNameFromPlace(place) : "";

    for (JSClass result : all) {
      if (ActionScriptResolveUtil.hasExcludeClassMetadata(result)) continue;
      if (!ActionScriptResolveUtil.isAccessibleFromCurrentActionScriptPackage(result, packageName, place)) continue;
      if (!processedCandidateNames.add(result.getQualifiedName())) continue;
      variants.add(JSLookupUtilImpl.createPrioritizedLookupItem(result, result.getName(), JSLookupPriority.SMART_PRIORITY));
    }
  }

  @Nullable
  public static JSClass findClassOfQualifier(JSReferenceExpression expression) {
    JSExpression qualifier = expression.getQualifier();

    JSClass clazzToProcess = null;

    if (qualifier != null) {
      qualifier = PsiUtilCore.getOriginalElement(qualifier, qualifier.getClass());
      clazzToProcess = qualifier != null ? ActionScriptResolveUtil.findClassOfQualifier(qualifier, qualifier.getContainingFile()) : null;
    }

    if (clazzToProcess == null) {
      clazzToProcess = JSResolveUtil.getClassOfContext(expression);
    }
    return clazzToProcess;
  }

  public static Map<String, String> getEventsMap(JSClass clazzToProcess) {
    if (clazzToProcess == null) return Collections.emptyMap();

    final Map<String, String> eventsMap = new THashMap<>();
    class EventsDataCollector extends ResolveProcessor implements ActionScriptResolveUtil.MetaDataProcessor {

      EventsDataCollector() {
        super(null);

        setToProcessHierarchy(true);
        setToProcessMembers(false);
        setTypeContext(true);
        setLocalResolve(true);
      }

      @Override
      public boolean process(@NotNull final JSAttribute jsAttribute) {
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

      @Override
      public boolean handleOtherElement(final PsiElement el, final PsiElement context, final Ref<PsiElement> continuePassElement) {
        return true;
      }

      @Override
      public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        if (element instanceof JSClass) {
          ActionScriptResolveUtil.processMetaAttributesForClass(element, this, true);
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
            for (PsiElement element : file.getChildren()) {
              if (element instanceof JSAttributeList) {
                ActionScriptResolveUtil.processAttributeList(eventsDataCollector, null, (JSAttributeList)element, true, true);
              }
            }
          }
        };
        for (XmlTag tag : tags) {
          JSResolveUtil.processInjectedFileForTag(tag, injectedFilesVisitor);
        }
      }
    }

    clazzToProcess.processDeclarations(eventsDataCollector, ResolveState.initial(), clazzToProcess, clazzToProcess);
    return eventsMap;
  }

  @Override
  protected int processContextClass(@NotNull JSReferenceExpression location,
                                    JSType expectedType,
                                    PsiElement parent,
                                    List<LookupElement> variants,
                                    int qualifiedStaticVariantsStart,
                                    SinkResolveProcessor<?> processor,
                                    JSClass ourClass) {
    JSClass clazz = expectedType.resolveClass();
    if (clazz != null && !clazz.isEquivalentTo(ourClass)) {
      qualifiedStaticVariantsStart = processor.getResultSink().getResultCount();
      processStaticsOf(clazz, processor, ourClass);
    }

    if (ourClass != null &&
        clazz != null &&
        JSInheritanceUtil.isParentClass(ourClass, clazz, false) &&
        !JSResolveUtil.calculateStaticFromContext(location) &&
        JSCompletionContributor.getInstance().isDoingSmartCodeCompleteAction()
      ) {
      variants.add(JSLookupUtilImpl.createPrioritizedLookupItem(
        null, "this", JSLookupPriority.SMART_PRIORITY
      ));
    }
    if (parent instanceof JSArgumentList) {
      JSParameterItem param = JSResolveUtil.findParameterForUsedArgument(location, (JSArgumentList)parent);
      if (param instanceof JSParameter) {
        PsiElement element = JSResolveUtil.findParent(((JSParameter)param).getParent().getParent());

        if (element instanceof JSClass && !element.isEquivalentTo(ourClass) && !element.isEquivalentTo(clazz)) {
          processStaticsOf((JSClass)element, processor, ourClass);
        }
      }
    }
    return qualifiedStaticVariantsStart;
  }

  private static class MyEventSubclassesProcessor extends ResolveProcessor {
    private final JavaScriptIndex index;
    private final PsiElement myExpr;
    private final List<LookupElement> myVariants;
    private final ResolveState state = new ResolveState();
    private Map<String, String> myEventsMap = new THashMap<>();

    MyEventSubclassesProcessor(final PsiElement expr, final List<LookupElement> variants) {
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

    @Override
    public boolean execute(@NotNull final PsiElement element, @NotNull final ResolveState state) {
      if (element instanceof JSVariable) {
        final JSVariable variable = (JSVariable)element;
        final JSAttributeList attributeList = variable.getAttributeList();

        if (attributeList != null &&
            attributeList.getAccessType() == JSAttributeList.AccessType.PUBLIC &&
            attributeList.hasModifier(JSAttributeList.ModifierType.STATIC) &&
            JSNamedType.isNamedTypeWithName(variable.getJSType(), JSCommonTypeNames.STRING_CLASS_NAME)
          ) {
          final String s = variable.getLiteralOrReferenceInitializerText();
          if (s != null && StringUtil.startsWith(s, "\"") && StringUtil.endsWith(s, "\"")) {
            String key = StringUtil.unquoteString(s);
            String event = myEventsMap.get(key);
            if (event == null) return true;
            PsiElement parent = JSResolveUtil.findParent(element);
            if (!(parent instanceof JSClass) || !event.equals(((JSClass)parent).getQualifiedName())) return true;

            String name = variable.getName();
            LookupElement lookupItem = JSLookupUtilImpl.createPrioritizedLookupItem(
              variable,
              ((JSClass)parent).getName() + "." + name,
              JSLookupPriority.SMART_PRIORITY,
              false,
              null,
              false,
              name
            );

            if (lookupItem != null) {
              myVariants.add(lookupItem);
            }
          }
        }
      }

      return true;
    }

    public void findAcceptableVariants(JSReferenceExpression expression) {
      JSClass clazzToProcess = findClassOfQualifier(expression);

      if (clazzToProcess == null) return;
      myEventsMap = getEventsMap(clazzToProcess);

      final PsiElement eventClass1 = ActionScriptClassResolver
        .findClassByQName(FlexCommonTypeNames.FLASH_EVENT_FQN, index, ModuleUtilCore.findModuleForPsiElement(expression));
      addElementsFromClass(expression, eventClass1);

      final PsiElement eventClass2 = ActionScriptClassResolver
        .findClassByQName(FlexCommonTypeNames.STARLING_EVENT_FQN, index, ModuleUtilCore.findModuleForPsiElement(expression));
      addElementsFromClass(expression, eventClass2);
    }

    private void addElementsFromClass(JSReferenceExpression expression, PsiElement eventClass2) {
      if ((eventClass2 instanceof JSClass)) {
        setToProcessMembers(true);
        setTypeContext(false);

        final Set<String> visited = new THashSet<>();
        for (JSClass cls : JSClassSearch.searchClassInheritors((JSClass)eventClass2, true, expression.getResolveScope()).findAll()) {
          if (!visited.add(cls.getQualifiedName())) continue;
          process(cls);
        }
      }
    }
  }

  private static void processStaticsOf(JSClass parameterClass, ResolveProcessor processor, @Nullable JSClass contextClass) {
    processor.configureClassScope(contextClass);

    processWithStatic(processor, true, () -> {
      processor.setTypeName(parameterClass.getQualifiedName());
      return parameterClass.processDeclarations(processor, ResolveState.initial(), parameterClass, parameterClass);
    });
  }
}
