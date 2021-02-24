// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.types.*;
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations;
import com.intellij.lang.javascript.psi.types.primitives.JSBooleanType;
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType;
import com.intellij.lang.javascript.psi.types.primitives.JSStringType;
import com.intellij.openapi.util.AtomicNullableLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.meta.PsiPresentableMetaData;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtilRt;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import com.intellij.xml.impl.XmlEnumerationDescriptor;
import icons.AngularJSIcons;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2CodeInsightUtils;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity;
import org.angular2.codeInsight.Angular2LibrariesHacks;
import org.angular2.codeInsight.tags.Angular2XmlElementSourcesResolver;
import org.angular2.entities.*;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.psi.Angular2HtmlEvent;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.angular2.lang.types.Angular2TypeUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

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

  public static @Nullable Angular2AttributeDescriptor create(@NotNull XmlTag tag, @NotNull String attributeName) {
    return create(tag, attributeName, emptyList());
  }

  public static @Nullable Angular2AttributeDescriptor create(@NotNull XmlTag tag,
                                                             @NotNull String attributeName,
                                                             @NotNull PsiElement element) {
    return create(tag, attributeName, singletonList(element));
  }

  private static @Nullable Angular2AttributeDescriptor create(@NotNull XmlTag tag,
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
    return new Angular2AttributeDescriptor(tag, attributeName, info, elements, implied);
  }

  public static @NotNull List<Angular2AttributeDescriptor> getDirectiveDescriptors(@NotNull Angular2Directive directive,
                                                                                   @NotNull XmlTag tag,
                                                                                   @NotNull Predicate<String> shouldIncludeOneTimeBinding) {
    if (!directive.getDirectiveKind().isRegular() && !isTemplateTag(tag)) {
      return emptyList();
    }
    return new DirectiveAttributesProvider(tag, directive, shouldIncludeOneTimeBinding).get();
  }

  private final @NotNull AttributePriority myPriority;
  private final Angular2XmlElementSourcesResolver myResolver;
  private final @NotNull String myAttributeName;
  private final @NotNull Angular2AttributeNameParser.AttributeInfo myInfo;
  private final boolean myImplied;
  private final AtomicNullableLazyValue<JSType> myJSType = AtomicNullableLazyValue.createValue(this::buildJSType);
  private final XmlAttributeDescriptor myOverriddenHtmlDescriptor;

  protected Angular2AttributeDescriptor(@NotNull XmlTag xmlTag,
                                        @NotNull String attributeName,
                                        @NotNull Collection<?> sources,
                                        boolean implied) {
    this(xmlTag, attributeName, Angular2AttributeNameParser.parse(attributeName, xmlTag),
         AttributePriority.NORMAL, sources, implied);
  }

  protected Angular2AttributeDescriptor(@NotNull XmlTag xmlTag,
                                        @NotNull String attributeName,
                                        @NotNull AttributePriority priority,
                                        @NotNull Collection<?> sources,
                                        boolean implied) {
    this(xmlTag, attributeName, Angular2AttributeNameParser.parse(attributeName, xmlTag),
         priority, sources, implied);
  }

  protected Angular2AttributeDescriptor(@NotNull XmlTag xmlTag,
                                        @NotNull String attributeName,
                                        @NotNull Angular2AttributeNameParser.AttributeInfo info,
                                        @NotNull Collection<?> sources,
                                        boolean implied) {
    this(xmlTag, attributeName, info, AttributePriority.NORMAL, sources, implied);
  }

  protected Angular2AttributeDescriptor(@NotNull XmlTag tag,
                                        @NotNull String attributeName,
                                        @NotNull Angular2AttributeNameParser.AttributeInfo info,
                                        @NotNull AttributePriority priority,
                                        @NotNull Collection<?> sources,
                                        boolean implied) {
    myAttributeName = attributeName;
    myResolver = new Angular2XmlElementSourcesResolver(tag, sources, this::getProperties, this::getSelectors);
    myInfo = info;
    myPriority = priority;
    myImplied = implied;
    myOverriddenHtmlDescriptor = info.type == REGULAR ? findInstance(sources, XmlAttributeDescriptor.class)
                                                      : null;
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
      assert other.getName().equalsIgnoreCase(myAttributeName)
        : "Cannot merge attributes with different names: " + myAttributeName + " != " + other.getName();
      Set<Object> elements = new HashSet<>(myResolver.getSources());
      elements.addAll(other.getDeclarations());
      elements.add(other);
      return new Angular2AttributeDescriptor(myResolver.getScope(), myAttributeName, elements, true);
    }
  }

  public XmlAttributeDescriptor cloneWithName(String attributeName) {
    return new Angular2AttributeDescriptor(myResolver.getScope(), attributeName,
                                           myInfo, myPriority, myResolver.getSources(), myImplied);
  }


  @Override
  public String getName() {
    return myAttributeName;
  }

  @Override
  public void init(PsiElement element) {}

  @Override
  public String validateValue(XmlElement context, String value) {
    if (myOverriddenHtmlDescriptor != null) {
      return myOverriddenHtmlDescriptor.validateValue(context, value);
    }
    return super.validateValue(context, value);
  }

  @Override
  public boolean isRequired() {
    if (myOverriddenHtmlDescriptor != null) {
      return myOverriddenHtmlDescriptor.isRequired();
    }
    return false;
  }

  @Override
  public boolean hasIdType() {
    if (myOverriddenHtmlDescriptor != null) {
      return myOverriddenHtmlDescriptor.hasIdType();
    }
    return "id".equals(myAttributeName);
  }

  @Override
  public boolean hasIdRefType() {
    if (myOverriddenHtmlDescriptor != null) {
      return myOverriddenHtmlDescriptor.hasIdRefType();
    }
    return false;
  }

  @Override
  public boolean isEnumerated() {
    if (myOverriddenHtmlDescriptor != null) {
      return myOverriddenHtmlDescriptor.isEnumerated();
    }
    String[] values = getEnumeratedValues();
    return values != null && values.length > 0;
  }

  @Override
  public boolean isFixed() {
    if (myOverriddenHtmlDescriptor != null) {
      return myOverriddenHtmlDescriptor.isFixed();
    }
    return false;
  }

  @Override
  public String getDefaultValue() {
    if (myOverriddenHtmlDescriptor != null) {
      return myOverriddenHtmlDescriptor.getDefaultValue();
    }
    return null;
  }

  @Override
  public boolean isList() {
    if (myOverriddenHtmlDescriptor instanceof XmlEnumerationDescriptor) {
      return ((XmlEnumerationDescriptor<?>)myOverriddenHtmlDescriptor).isList();
    }
    return super.isList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public PsiReference[] getValueReferences(XmlElement element, @NotNull String text) {
    if (myOverriddenHtmlDescriptor instanceof XmlEnumerationDescriptor) {
      return ((XmlEnumerationDescriptor<XmlElement>)myOverriddenHtmlDescriptor).getValueReferences(element, text);
    }
    return super.getValueReferences(element, text);
  }

  @Override
  public boolean isEnumerated(XmlElement context) {
    if (myOverriddenHtmlDescriptor instanceof XmlEnumerationDescriptor) {
      return ((XmlEnumerationDescriptor<?>)myOverriddenHtmlDescriptor).isEnumerated(context);
    }
    return super.isEnumerated(context);
  }

  @Override
  public String[] getValuesForCompletion() {
    if (myOverriddenHtmlDescriptor instanceof XmlEnumerationDescriptor) {
      return ((XmlEnumerationDescriptor<?>)myOverriddenHtmlDescriptor).getValuesForCompletion();
    }
    return super.getValuesForCompletion();
  }

  public @NotNull List<Angular2Directive> getSourceDirectives() {
    return myResolver.getSourceDirectives();
  }

  public boolean isImplied() {
    return myImplied;
  }

  @Override
  public String @Nullable [] getEnumeratedValues() {
    if (myOverriddenHtmlDescriptor != null) {
      return myOverriddenHtmlDescriptor.getEnumeratedValues();
    }
    JSType type;
    if (myInfo.type == REGULAR && (type = getJSType()) != null) {
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
        return ArrayUtilRt.toStringArray(values);
      }
    }
    return ArrayUtilRt.EMPTY_STRING_ARRAY;
  }

  @Override
  public PsiElement getValueDeclaration(XmlElement attributeValue, String value) {
    if (myOverriddenHtmlDescriptor instanceof XmlEnumerationDescriptor<?>) {
      return ((XmlEnumerationDescriptor<?>)myOverriddenHtmlDescriptor).getValueDeclaration(attributeValue, value);
    }
    return super.getValueDeclaration(attributeValue, value);
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

  @Override
  public @NotNull Collection<PsiElement> getDeclarations() {
    return myResolver.getDeclarations();
  }

  private Collection<? extends PsiElement> getSelectors(@NotNull Angular2Directive directive) {
    return StreamEx.of(directive.getSelector().getSimpleSelectorsWithPsi())
      .flatMap(selector -> StreamEx.of(selector.getNotSelectors())
        .flatCollection(Angular2DirectiveSelector.SimpleSelectorWithPsi::getAttributes)
        .prepend(selector.getAttributes()))
      .filter(s -> myInfo.name.equals(s.getName()))
      .toList();
  }

  public @NotNull Collection<PsiElement> getProperties(@NotNull Angular2Directive directive) {
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
        final StreamEx<PsiElement> inputsPsiElements =
          StreamEx.of(directive.getInputs())
            .filter(property -> myInfo.name.equals(property.getName()) && isOneTimeBindingProperty(property))
            .map(Angular2Element::getNavigableElement);

        final StreamEx<PsiElement> attributesPsiElements =
          StreamEx.of(directive.getAttributes())
            .filter(a -> myInfo.name.equals(a.getName()))
            .map(Angular2Element::getNavigableElement);

        return inputsPsiElements.append(attributesPsiElements).toList();
      default:
        return emptyList();
    }
  }

  @Override
  public @Nullable String handleTargetRename(@NotNull @NonNls String newTargetName) {
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

  @NonNls
  @Override
  public String getTypeName() {
    if (myInfo instanceof Angular2AttributeNameParser.EventInfo
        && ((Angular2AttributeNameParser.EventInfo)myInfo).eventType == Angular2HtmlEvent.EventType.REGULAR) {
      return doIfNotNull(this.getEventVariableType(), JSType::getTypeText);
    }

    JSType type = getJSType();
    if (type != null
        && ((myInfo instanceof Angular2AttributeNameParser.PropertyBindingInfo
             && ((Angular2AttributeNameParser.PropertyBindingInfo)myInfo).bindingType == PropertyBindingType.PROPERTY)
            || type instanceof JSPrimitiveType
            || isEnumerated())) {
      return StringUtil.shortenTextWithEllipsis(type.getTypeText(), 25, 0, true);
    }
    return null;
  }

  public @NotNull Pair<LookupElement, String> getLookupElementWithPrefix(@NotNull PrefixMatcher prefixMatcher,
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
      .withInsertHandler(new Angular2AttributeInsertHandler(shouldInsertHandlerRemoveLeftover(), this::shouldCompleteValue, null));
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
    JSType type;
    return (myInfo.type != REGULAR && myInfo.type != I18N)
           || ((type = getJSType()) != null && !(type instanceof JSBooleanType));
  }

  public @NotNull Angular2AttributeNameParser.AttributeInfo getInfo() {
    return myInfo;
  }

  @Override
  public @Nullable Icon getIcon() {
    return AngularJSIcons.Angular2;
  }

  public @Nullable JSType getJSType() {
    return myJSType.getValue();
  }

  private JSType buildJSType() {
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
    JSType type = property.getType();
    if (type == null) return true;

    Map<Angular2DirectiveProperty, Boolean> cache = CachedValuesManager.getCachedValue(property.getSourceElement(), () ->
      CachedValueProvider.Result.create(new ConcurrentHashMap<>(), PsiModificationTracker.MODIFICATION_COUNT));
    return cache.computeIfAbsent(property, prop ->
      expandStringLiteralTypes(type)
        .isDirectlyAssignableType(STRING_TYPE, JSTypeComparingContextService.createProcessingContextWithCache(
          property.getSourceElement()))) == Boolean.TRUE;
  }

  @Contract("null -> null") //NON-NLS
  private static JSType expandStringLiteralTypes(@Nullable JSType type) {
    if (type == null) return null;
    type = TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(type);
    return type.transformTypeHierarchy(toApply -> toApply instanceof JSPrimitiveType ? STRING_TYPE : toApply);
  }

  public @Nullable JSType getEventVariableType() {
    if (myInfo instanceof Angular2AttributeNameParser.EventInfo
        && ((Angular2AttributeNameParser.EventInfo)myInfo).eventType == Angular2HtmlEvent.EventType.REGULAR) {

      JSType extractedType = Angular2TypeUtils.extractEventVariableType(getJSType());
      if (extractedType != null) {
        return extractedType;
      }

      JSType eventMap = Angular2TypeUtils.getElementEventMap(
        Angular2TypeUtils.createJSTypeSourceForXmlElement(myResolver.getScope()));
      if (eventMap != null) {
        JSRecordType.PropertySignature signature = eventMap.asRecordType().findPropertySignature(myInfo.name);
        JSType type = signature != null ? signature.getJSType() : null;
        if (type != null) {
          return type;
        }
      }
    }
    return null;
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
    private final Predicate<String> myShouldIncludeOneTimeBinding;
    private final XmlTag myTag;
    private List<Angular2AttributeDescriptor> myResult;

    DirectiveAttributesProvider(@NotNull XmlTag tag,
                                @NotNull Angular2Directive directive,
                                @NotNull Predicate<String> shouldIncludeOneTimeBinding) {
      myTag = tag;
      myDirective = directive;
      myShouldIncludeOneTimeBinding = shouldIncludeOneTimeBinding;
    }

    public List<Angular2AttributeDescriptor> get() {
      myResult = new ArrayList<>();
      collectDirectiveDescriptors(myDirective.getInOuts(), this::createBananaBoxBinding);
      collectDirectiveDescriptors(myDirective.getInputs(), this::createBinding);
      collectDirectiveDescriptors(myDirective.getOutputs(), this::createEventHandler);
      collectDirectiveDescriptors(myDirective.getInputs(), this::createOneTimeBinding);
      collectDirectiveDescriptors(myDirective.getAttributes(), this::createAttributeBinding);
      //noinspection unchecked
      Optional.ofNullable(Angular2LibrariesHacks.hackIonicComponentAttributeNames(myDirective, this::createOneTimeBinding))
        .ifPresent(creator -> collectDirectiveDescriptors((Collection<Angular2DirectiveProperty>)myDirective.getInputs(), creator));
      return myResult;
    }


    private <T> void collectDirectiveDescriptors(
      @NotNull Collection<T> list,
      @NotNull Function<T, ? extends Angular2AttributeDescriptor> factory) {
      list.forEach(el -> doIfNotNull(factory.apply(el), myResult::add));
    }

    private @NotNull Angular2AttributeDescriptor createBinding(@NotNull Angular2DirectiveProperty info) {
      return new Angular2AttributeDescriptor(myTag,
                                             PROPERTY_BINDING.buildName(info.getName()),
                                             AttributePriority.HIGH,
                                             singletonList(myDirective),
                                             false);
    }

    private @NotNull Angular2AttributeDescriptor createBananaBoxBinding(@NotNull Pair<Angular2DirectiveProperty, Angular2DirectiveProperty> info) {
      return new Angular2AttributeDescriptor(myTag,
                                             BANANA_BOX_BINDING.buildName(info.first.getName()),
                                             AttributePriority.HIGH,
                                             singletonList(myDirective),
                                             false);
    }

    private @Nullable Angular2AttributeDescriptor createOneTimeBinding(@NotNull Angular2DirectiveProperty info) {
      return createOneTimeBinding(info, info.getName());
    }

    private @Nullable Angular2AttributeDescriptor createOneTimeBinding(@NotNull Angular2DirectiveProperty info, String attributeName) {
      return myShouldIncludeOneTimeBinding.test(attributeName)
             && isOneTimeBindingProperty(info)
             ? new Angular2AttributeDescriptor(myTag, attributeName, AttributePriority.HIGH,
                                               singletonList(myDirective),
                                               false)
             : null;
    }

    private @NotNull Angular2AttributeDescriptor createAttributeBinding(@NotNull Angular2DirectiveAttribute info) {
      return new Angular2AttributeDescriptor(
        myTag,
        info.getName(),
        AttributePriority.HIGH,
        singletonList(myDirective),
        false
      );
    }

    private @NotNull Angular2AttributeDescriptor createEventHandler(@NotNull Angular2DirectiveProperty info) {
      return new Angular2AttributeDescriptor(myTag, EVENT.buildName(info.getName()), AttributePriority.HIGH,
                                             singletonList(myDirective),
                                             false);
    }
  }
}
