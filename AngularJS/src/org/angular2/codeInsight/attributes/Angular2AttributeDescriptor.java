// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSParameterListElement;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeUtils;
import com.intellij.lang.javascript.psi.types.JSCompositeTypeImpl;
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl;
import com.intellij.lang.javascript.psi.types.JSTypeContext;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations;
import com.intellij.lang.javascript.psi.types.primitives.JSBooleanType;
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType;
import com.intellij.lang.javascript.psi.types.primitives.JSStringType;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiPresentableMetaData;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import icons.AngularJSIcons;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2CodeInsightUtils;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity;
import org.angular2.codeInsight.Angular2TypeEvaluator;
import org.angular2.codeInsight.tags.Angular2XmlElementSourcesResolver;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2DirectiveSelector;
import org.angular2.entities.Angular2Element;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.psi.Angular2HtmlEvent;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.intellij.lang.javascript.psi.types.JSTypeSourceFactory.createTypeSource;
import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.notNull;
import static com.intellij.util.containers.ContainerUtil.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity.IN_SCOPE;
import static org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity.NOT_REACHABLE;
import static org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider.EVENT_ATTR_PREFIX;
import static org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider.getCustomNgAttrs;
import static org.angular2.codeInsight.attributes.Angular2AttributeValueProvider.NG_CLASS_ATTR;
import static org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.isTemplateTag;
import static org.angular2.lang.html.parser.Angular2AttributeType.*;

public class Angular2AttributeDescriptor extends BasicXmlAttributeDescriptor implements XmlAttributeDescriptorEx, PsiPresentableMetaData {

  public static final JSType STRING_TYPE = new JSStringType(true, JSTypeSource.EXPLICITLY_DECLARED, JSTypeContext.INSTANCE);

  private static final Collection<String> ONE_TIME_BINDING_EXCLUDES = newArrayList(NG_CLASS_ATTR);

  @Nullable
  public static Angular2AttributeDescriptor create(@NotNull XmlTag tag, @NotNull String attributeName) {
    return create(tag, attributeName, emptyList());
  }

  @Nullable
  public static Angular2AttributeDescriptor create(@NotNull XmlTag tag, @NotNull String attributeName, @NotNull PsiElement element) {
    return create(tag, attributeName, singletonList(element));
  }

  /**
   * @deprecated Kept for compatibility with NativeScript
   * To be removed in 2019.2
   */
  @Nullable
  @Deprecated
  public static Angular2AttributeDescriptor create(@NotNull String attributeName, @NotNull PsiElement element) {
    return create(null, attributeName, singletonList(element));
  }

  @Nullable
  private static Angular2AttributeDescriptor create(@Nullable XmlTag tag,
                                                    @NotNull String attributeName,
                                                    @NotNull List<PsiElement> elements) {
    if (getCustomNgAttrs().contains(attributeName)) {
      return new Angular2AttributeDescriptor(tag, attributeName, elements, true);
    }
    Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(attributeName, tag);

    if (elements.isEmpty() && info.type == REGULAR) {
      return null;
    }
    boolean implied;
    if (elements.isEmpty()
        && (info.type == TEMPLATE_BINDINGS
            || (info instanceof Angular2AttributeNameParser.EventInfo
                && ((Angular2AttributeNameParser.EventInfo)info).eventType == Angular2HtmlEvent.EventType.REGULAR
                && !info.name.contains(":"))
            || (info instanceof Angular2AttributeNameParser.PropertyBindingInfo
                && ((Angular2AttributeNameParser.PropertyBindingInfo)info).bindingType
                   == PropertyBindingType.PROPERTY))) {
      implied = false;
    }
    else {
      implied = true;
    }
    if (info.type == EVENT) {
      return new Angular2EventHandlerDescriptor(tag, attributeName, info, elements, implied);
    }
    return new Angular2AttributeDescriptor(tag, attributeName, info, elements, implied);
  }

  @NotNull
  public static List<Angular2AttributeDescriptor> getDirectiveDescriptors(@NotNull Angular2Directive directive,
                                                                          @NotNull XmlTag tag) {
    if (!directive.isRegularDirective() && !isTemplateTag(tag)) {
      return emptyList();
    }
    return new DirectiveAttributesProvider(tag, directive).get();
  }

  @NotNull
  private final AttributePriority myPriority;
  private final Angular2XmlElementSourcesResolver myResolver;
  @NotNull
  private final String myAttributeName;
  @NotNull
  private final Angular2AttributeNameParser.AttributeInfo myInfo;
  private final boolean myImplied;

  // Note: xmlTag is nullable only because of compatibility with NativeScript with classes
  // Angular2EventHandlerDescriptor and AngularBindingDescriptor, which are deprecated
  protected Angular2AttributeDescriptor(@Nullable XmlTag xmlTag, //Nullable for compatibility with NativeScript
                                        @NotNull String attributeName,
                                        @NotNull Collection<?> sources,
                                        boolean implied) {
    this(xmlTag, attributeName, Angular2AttributeNameParser.parse(attributeName, xmlTag),
         AttributePriority.NORMAL, sources, implied);
  }

  protected Angular2AttributeDescriptor(@Nullable XmlTag xmlTag, //Nullable for compatibility with NativeScript
                                        @NotNull String attributeName,
                                        @NotNull AttributePriority priority,
                                        @NotNull Collection<?> sources,
                                        boolean implied) {
    this(xmlTag, attributeName, Angular2AttributeNameParser.parse(attributeName, xmlTag),
         priority, sources, implied);
  }

  protected Angular2AttributeDescriptor(@Nullable XmlTag xmlTag, //Nullable for compatibility with NativeScript
                                        @NotNull String attributeName,
                                        @NotNull Angular2AttributeNameParser.AttributeInfo info,
                                        @NotNull Collection<?> sources,
                                        boolean implied) {
    this(xmlTag, attributeName, info, AttributePriority.NORMAL, sources, implied);
  }

  protected Angular2AttributeDescriptor(@Nullable XmlTag tag, //Nullable for compatibility with NativeScript
                                        @NotNull String attributeName,
                                        @NotNull Angular2AttributeNameParser.AttributeInfo info,
                                        @NotNull AttributePriority priority,
                                        @NotNull Collection<?> sources,
                                        boolean implied) {
    myAttributeName = attributeName;
    myResolver = new Angular2XmlElementSourcesResolver(tag, sources);
    myInfo = info;
    myPriority = priority;
    myImplied = implied;
  }

  public Angular2AttributeDescriptor merge(@Nullable XmlAttributeDescriptor other) {
    if (other == null) {
      return this;
    }
    else if (other instanceof Angular2AttributeDescriptor) {
      Angular2AttributeDescriptor ngOther = (Angular2AttributeDescriptor)other;
      assert myAttributeName.equals(ngOther.myAttributeName)
             && myInfo.isEquivalent(ngOther.myInfo)
        : "Cannot merge attributes with different names or non-equivalent infos: "
          + myAttributeName + " " + myInfo.toString() + " != "
          + ngOther.myAttributeName + " " + ngOther.myInfo.toString();
      assert myResolver.getScope() == ngOther.myResolver.getScope()
        : "Cannot merge attributes from different tags";
      Set<Object> sources = new HashSet<>(myResolver.getSources());
      sources.addAll(ngOther.myResolver.getSources());
      return new Angular2AttributeDescriptor(myResolver.getScope(), myAttributeName, myInfo, myPriority,
                                             sources, myImplied || ngOther.myImplied);
    }
    else {
      assert other.getName().equals(myAttributeName);
      Set<Object> elements = new HashSet<>(myResolver.getSources());
      elements.addAll(other.getDeclarations());
      return new Angular2AttributeDescriptor(myResolver.getScope(), myAttributeName, elements, true);
    }
  }

  @Override
  public String getName() {
    return myAttributeName;
  }

  @Override
  public void init(PsiElement element) {}

  @Override
  public boolean isRequired() {
    return false;
  }

  @Override
  public boolean hasIdType() {
    return "id".equals(myAttributeName);
  }

  @Override
  public boolean hasIdRefType() {
    return false;
  }

  @Override
  public boolean isEnumerated() {
    return getEnumeratedValues().length > 0;
  }

  @Override
  public boolean isFixed() {
    return false;
  }

  @Override
  public String getDefaultValue() {
    return null;
  }

  @NotNull
  public List<Angular2Directive> getSourceDirectives() {
    return myResolver.getSourceDirectives();
  }

  public boolean isImplied() {
    return myImplied;
  }

  @Override
  @NotNull
  public String[] getEnumeratedValues() {
    JSType type = getJSType();
    if (type != null && myInfo.type == REGULAR) {
      List<String> values = new ArrayList<>();
      type = TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(type);
      if (type instanceof JSBooleanType) {
        return new String[]{myAttributeName};
      }
      JSTypeUtils.processExpandedType(subType -> {
        if (subType instanceof JSStringLiteralTypeImpl) {
          values.add(((JSStringLiteralTypeImpl)subType).getLiteral());
        }
        return true;
      }, type);
      if (!values.isEmpty()) {
        return ArrayUtil.toStringArray(values);
      }
    }
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  @Override
  protected PsiElement getEnumeratedValueDeclaration(XmlElement xmlElement, String value) {
    return xmlElement;
  }

  @Override
  public PsiElement getDeclaration() {
    Collection<PsiElement> declarations = getDeclarations();
    return declarations.size() == 1 ? getFirstItem(declarations) : null;
  }

  @NotNull
  @Override
  public Collection<PsiElement> getDeclarations() {
    return myResolver.getDeclarations(this::getProperties, this::getSelectors);
  }

  private Collection<? extends PsiElement> getSelectors(@NotNull Angular2Directive directive) {
    return StreamEx.of(directive.getSelector().getSimpleSelectorsWithPsi())
      .flatMap(selector -> StreamEx.of(selector.getNotSelectors())
        .flatCollection(Angular2DirectiveSelector.SimpleSelectorWithPsi::getAttributes)
        .prepend(selector.getAttributes()))
      .filter(s -> myInfo.name.equals(s.getName()))
      .toList();
  }

  @NotNull
  private Collection<PsiElement> getProperties(@NotNull Angular2Directive directive) {
    switch (myInfo.type) {
      case PROPERTY_BINDING:
      case TEMPLATE_BINDINGS:
      case EVENT:
        return StreamEx.of(myInfo.type == EVENT ? directive.getOutputs() : directive.getInputs())
          .filter(input -> myInfo.name.equals(input.getName()))
          .map(Angular2Element::getNavigableElement)
          .toList();
      case BANANA_BOX_BINDING:
        return StreamEx.of(directive.getInOuts())
          .filter(inout -> myInfo.name.equals(inout.first.getName()))
          .flatCollection(inout -> newArrayList(inout.first.getNavigableElement(), inout.second.getNavigableElement()))
          .toList();
      case REGULAR:
        return StreamEx.of(directive.getInputs())
          .filter(input -> myInfo.name.equals(input.getName())
                           && isOneTimeBindingProperty(input))
          .map(Angular2Element::getNavigableElement)
          .toList();
      default:
        return emptyList();
    }
  }

  @Nullable
  @Override
  public String handleTargetRename(@NotNull @NonNls String newTargetName) {
    if (myInfo.type != REGULAR) {
      int start = myAttributeName.lastIndexOf(myInfo.name);
      return myAttributeName.substring(0, start)
             + newTargetName
             + myAttributeName.substring(start + myInfo.name.length());
    }
    else {
      return newTargetName;
    }
  }

  @Override
  public String getTypeName() {
    JSType type = getJSType();
    if (type != null) {
      if ((myInfo instanceof Angular2AttributeNameParser.PropertyBindingInfo
           && ((Angular2AttributeNameParser.PropertyBindingInfo)myInfo).bindingType == PropertyBindingType.PROPERTY)
          || type instanceof JSPrimitiveType
          || isEnumerated()) {
        return StringUtil.shortenTextWithEllipsis(type.getTypeText(), 25, 0, true);
      }
      else if (myInfo instanceof Angular2AttributeNameParser.EventInfo
               && ((Angular2AttributeNameParser.EventInfo)myInfo).eventType == Angular2HtmlEvent.EventType.REGULAR) {
        type = Angular2TypeEvaluator.getEventVariableType(type);
        if (type != null) {
          return type.getTypeText();
        }
      }
    }
    return null;
  }

  @NotNull
  public Pair<LookupElement, String> getLookupElementWithPrefix(@NotNull PrefixMatcher prefixMatcher,
                                                                @NotNull Angular2DeclarationsScope moduleScope) {
    LookupElementInfo info = buildElementInfo(prefixMatcher);
    String currentPrefix = prefixMatcher.getPrefix();
    Pair<String, String> hide = notNull(find(info.hidePrefixesAndSuffixes,
                                             pair -> currentPrefix.startsWith(pair.first)),
                                        () -> pair("", ""));
    String name = StringUtil.trimStart(info.elementName, hide.first);
    DeclarationProximity proximity = myImplied
                                     ? IN_SCOPE
                                     : moduleScope.getDeclarationsProximity(getSourceDirectives());
    if (proximity == NOT_REACHABLE) {
      return pair(null, name);
    }
    LookupElementBuilder element = LookupElementBuilder.create(name)
      .withPresentableText(StringUtil.trimEnd(name, hide.second))
      .withCaseSensitivity(myInfo.type != REGULAR || !myImplied)
      .withIcon(getIcon())
      .withBoldness(proximity == IN_SCOPE && myPriority == AttributePriority.HIGH)
      .withInsertHandler(new Angular2AttributeInsertHandler(shouldInsertHandlerRemoveLeftover(), shouldCompleteValue(), null));
    if (info.lookupStrings != null) {
      element = element.withLookupStrings(map(info.lookupStrings, str -> StringUtil.trimStart(str, hide.first)));
    }
    if (proximity != IN_SCOPE) {
      element = Angular2CodeInsightUtils.wrapWithImportDeclarationModuleHandler(
        Angular2CodeInsightUtils.decorateLookupElementWithModuleSource(element, getSourceDirectives(), proximity, moduleScope),
        myInfo.type == TEMPLATE_BINDINGS ? Angular2TemplateBindings.class : XmlAttribute.class);
    }
    String typeName = getTypeName();
    if (!StringUtil.isEmptyOrSpaces(typeName)) {
      element = element.withTypeText(typeName);
    }
    return pair(PrioritizedLookupElement.withPriority(element, myPriority.getValue(proximity)),
                currentPrefix.substring(hide.first.length()));
  }

  protected LookupElementInfo buildElementInfo(@NotNull PrefixMatcher prefixMatcher) {
    String canonicalPrefix = myInfo.type.getCanonicalPrefix();
    if (canonicalPrefix != null && prefixMatcher.getPrefix().startsWith(canonicalPrefix)) {
      return new LookupElementInfo(Objects.requireNonNull(myInfo.type.buildName(myInfo.getFullName(), true)),
                                   singletonList(pair(canonicalPrefix, "")),
                                   myInfo.type == EVENT ? newArrayList(getName(), EVENT_ATTR_PREFIX + myInfo.getFullName()) : null);
    }
    return new LookupElementInfo(getName(), emptyList(),
                                 myInfo.type == EVENT ? newArrayList(getName(), EVENT_ATTR_PREFIX + myInfo.getFullName()) : null);
  }

  protected boolean shouldInsertHandlerRemoveLeftover() {
    return false;
  }

  private boolean shouldCompleteValue() {
    JSType type = getJSType();
    return myInfo.type != REGULAR
           || (type != null && !(type instanceof JSBooleanType));
  }

  @NotNull
  public Angular2AttributeNameParser.AttributeInfo getInfo() {
    return myInfo;
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return AngularJSIcons.Angular2;
  }

  @Nullable
  public JSType getJSType() {
    Collection<PsiElement> declarations = getDeclarations();
    List<JSType> types = mapNotNull(declarations, element -> {
      if (element instanceof JSFunction) {
        JSParameterListElement[] params = ((JSFunction)element).getParameters();
        if (((JSFunction)element).isSetProperty() && params.length == 1) {
          return params[0].getSimpleType();
        }
      }
      return JSTypeUtils.getTypeOfElement(element);
    });
    if (types.size() == 1) {
      return types.get(0);
    }
    else if (types.size() > 1) {
      if (myInfo.type == BANANA_BOX_BINDING) {
        return types.get(0);
      }
      return JSCompositeTypeImpl.getCommonType(
        types, createTypeSource(getFirstItem(declarations), false), false);
    }
    return null;
  }

  public static boolean isOneTimeBindingProperty(@NotNull Angular2DirectiveProperty property) {
    if (ONE_TIME_BINDING_EXCLUDES.contains(property.getName())) return false;
    if (property.isVirtual()) return true;
    if (property.getType() == null) return false;

    Map<Angular2DirectiveProperty, Boolean> cache = CachedValuesManager.getCachedValue(property.getSourceElement(), () ->
      CachedValueProvider.Result.create(new ConcurrentHashMap<>(), PsiModificationTracker.MODIFICATION_COUNT));
    return cache.computeIfAbsent(property, prop ->
      expandStringLiteralTypes(prop.getType()).isDirectlyAssignableType(STRING_TYPE, null)) == Boolean.TRUE;
  }

  @Contract("null->null") //NON-NLS
  private static JSType expandStringLiteralTypes(@Nullable JSType type) {
    if (type == null) return null;
    type = TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(type);
    return type.transformTypeHierarchy(toApply -> toApply instanceof JSPrimitiveType ? STRING_TYPE : toApply);
  }

  public enum AttributePriority {
    NONE(0),
    LOW(25),
    NORMAL(50),
    HIGH(100);

    private final double myValue;

    AttributePriority(double value) {
      myValue = value;
    }

    public double getValue() {
      return myValue;
    }

    public double getValue(DeclarationProximity proximity) {
      return proximity == IN_SCOPE
             || proximity == DeclarationProximity.EXPORTED_BY_PUBLIC_MODULE
             ? myValue : 0;
    }
  }

  protected static class LookupElementInfo {
    public final String elementName;
    public final List<Pair<String, String>> hidePrefixesAndSuffixes;
    public final List<String> lookupStrings;

    public LookupElementInfo(@NotNull String elementName,
                             @NotNull List<Pair<String, String>> hidePrefixesAndSuffixes,
                             @Nullable List<String> lookupStrings) {
      this.elementName = elementName;
      this.hidePrefixesAndSuffixes = hidePrefixesAndSuffixes;
      this.lookupStrings = lookupStrings;
    }
  }

  private static class DirectiveAttributesProvider {
    private final Angular2Directive myDirective;
    private final XmlTag myTag;
    private List<Angular2AttributeDescriptor> myResult;

    DirectiveAttributesProvider(@NotNull XmlTag tag, @NotNull Angular2Directive directive) {
      myTag = tag;
      myDirective = directive;
    }

    public List<Angular2AttributeDescriptor> get() {
      myResult = new ArrayList<>();
      collectDirectiveDescriptors(myDirective.getInOuts(), this::createBananaBoxBinding);
      collectDirectiveDescriptors(myDirective.getInputs(), this::createBinding);
      collectDirectiveDescriptors(myDirective.getOutputs(), this::createEventHandler);
      collectDirectiveDescriptors(myDirective.getInputs(), this::createOneTimeBinding);
      return myResult;
    }

    private <T> void collectDirectiveDescriptors(
      @NotNull Collection<T> list,
      @NotNull Function<T, ? extends Angular2AttributeDescriptor> factory) {
      list.forEach(el -> doIfNotNull(factory.apply(el), myResult::add));
    }

    @NotNull
    private Angular2AttributeDescriptor createBinding(@NotNull Angular2DirectiveProperty info) {
      return new Angular2AttributeDescriptor(myTag,
                                             PROPERTY_BINDING.buildName(info.getName()),
                                             AttributePriority.HIGH,
                                             singletonList(myDirective),
                                             false);
    }

    @NotNull
    private Angular2AttributeDescriptor createBananaBoxBinding(@NotNull Pair<Angular2DirectiveProperty, Angular2DirectiveProperty> info) {
      return new Angular2AttributeDescriptor(myTag,
                                             BANANA_BOX_BINDING.buildName(info.first.getName()),
                                             AttributePriority.HIGH,
                                             singletonList(myDirective),
                                             false);
    }

    @Nullable
    private Angular2AttributeDescriptor createOneTimeBinding(@NotNull Angular2DirectiveProperty info) {
      return isOneTimeBindingProperty(info)
             ? new Angular2AttributeDescriptor(myTag, info.getName(), AttributePriority.HIGH,
                                               singletonList(myDirective),
                                               false)
             : null;
    }

    @NotNull
    private Angular2EventHandlerDescriptor createEventHandler(@NotNull Angular2DirectiveProperty info) {
      return new Angular2EventHandlerDescriptor(myTag, EVENT.buildName(info.getName()), AttributePriority.HIGH,
                                                singletonList(myDirective),
                                                false);
    }
  }
}
