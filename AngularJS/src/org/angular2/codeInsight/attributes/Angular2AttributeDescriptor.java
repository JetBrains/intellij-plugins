// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.*;
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
import com.intellij.psi.xml.XmlElement;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ArrayUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import icons.AngularJSIcons;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity;
import org.angular2.codeInsight.Angular2Processor;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.psi.Angular2HtmlEvent;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.function.Function;

import static com.intellij.lang.javascript.psi.types.JSTypeSourceFactory.createTypeSource;
import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.notNull;
import static com.intellij.util.containers.ContainerUtil.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider.getCustomNgAttrs;
import static org.angular2.codeInsight.attributes.Angular2AttributeValueProvider.NG_CLASS_ATTR;
import static org.angular2.lang.html.parser.Angular2AttributeType.*;

public class Angular2AttributeDescriptor extends BasicXmlAttributeDescriptor implements XmlAttributeDescriptorEx, PsiPresentableMetaData {

  public static final JSType STRING_TYPE = new JSStringType(true, JSTypeSource.EXPLICITLY_DECLARED, JSTypeContext.INSTANCE);

  private static final Collection<String> ONE_TIME_BINDING_EXCLUDES = newArrayList(NG_CLASS_ATTR);

  @Nullable
  public static Angular2AttributeDescriptor create(@NotNull String attributeName) {
    return create(attributeName, emptyList());
  }

  @Nullable
  public static Angular2AttributeDescriptor create(@NotNull String attributeName, @NotNull PsiElement element) {
    return create(attributeName, singletonList(element));
  }

  @Nullable
  private static Angular2AttributeDescriptor create(@NotNull String attributeName,
                                                    @NotNull List<PsiElement> elements) {
    if (getCustomNgAttrs().contains(attributeName)) {
      return new Angular2AttributeDescriptor(attributeName, false, elements);
    }
    Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(attributeName, true);

    Collection<Angular2Directive> source;
    if (elements.isEmpty() && info.type == REGULAR) {
      return null;
    }
    if (elements.isEmpty()
        && (info.type == TEMPLATE_BINDINGS
            || (info instanceof Angular2AttributeNameParser.EventInfo
                && ((Angular2AttributeNameParser.EventInfo)info).eventType == Angular2HtmlEvent.EventType.REGULAR
                && !info.name.contains(":"))
            || (info instanceof Angular2AttributeNameParser.PropertyBindingInfo
                && ((Angular2AttributeNameParser.PropertyBindingInfo)info).bindingType
                   == PropertyBindingType.PROPERTY))) {
      source = emptyList();
    }
    else {
      source = null;
    }
    if (info.type == EVENT) {
      return new Angular2EventHandlerDescriptor(attributeName, info, source, elements);
    }
    return new Angular2AttributeDescriptor(attributeName, info, source, elements);
  }

  @NotNull
  public static List<Angular2AttributeDescriptor> getDirectiveDescriptors(@NotNull Angular2Directive directive,
                                                                          boolean isTemplateTagContext) {
    if (directive.isTemplate() && !isTemplateTagContext) {
      return emptyList();
    }
    return new DirectiveAttributesProvider().get(directive);
  }

  @NotNull
  private final AttributePriority myPriority;
  @NotNull
  private final PsiElement[] myElements;
  @Nullable
  private final List<Angular2Directive> mySourceDirectives;
  @NotNull
  private final String myAttributeName;
  @NotNull
  private final Angular2AttributeNameParser.AttributeInfo myInfo;

  protected Angular2AttributeDescriptor(@NotNull String attributeName,
                                        boolean isInTemplateTag,
                                        @NotNull Collection<PsiElement> elements) {
    this(attributeName, Angular2AttributeNameParser.parse(attributeName, isInTemplateTag),
         AttributePriority.NORMAL, null, elements);
  }

  protected Angular2AttributeDescriptor(@NotNull String attributeName,
                                        boolean isInTemplateTag,
                                        @NotNull Collection<Angular2Directive> sourceDirectives,
                                        @NotNull Collection<PsiElement> elements) {
    this(attributeName, Angular2AttributeNameParser.parse(attributeName, isInTemplateTag),
         AttributePriority.NORMAL, sourceDirectives, elements);
  }

  protected Angular2AttributeDescriptor(@NotNull String attributeName,
                                        @NotNull Angular2AttributeNameParser.AttributeInfo info,
                                        @Nullable Collection<Angular2Directive> sourceDirectives,
                                        @NotNull Collection<PsiElement> elements) {
    this(attributeName, info, AttributePriority.NORMAL, sourceDirectives, elements);
  }

  protected Angular2AttributeDescriptor(@NotNull String attributeName,
                                        boolean isInTemplateTag,
                                        @NotNull AttributePriority priority,
                                        @NotNull Collection<PsiElement> elements) {
    this(attributeName, Angular2AttributeNameParser.parse(attributeName, isInTemplateTag), priority,
         null, elements);
  }

  protected Angular2AttributeDescriptor(@NotNull String attributeName,
                                        boolean isInTemplateTag,
                                        @NotNull AttributePriority priority,
                                        @NotNull Collection<Angular2Directive> sourceDirectives,
                                        @NotNull Collection<PsiElement> elements) {
    this(attributeName, Angular2AttributeNameParser.parse(attributeName, isInTemplateTag), priority,
         sourceDirectives, elements);
  }

  private Angular2AttributeDescriptor(@NotNull String attributeName,
                                      @NotNull Angular2AttributeNameParser.AttributeInfo info,
                                      @NotNull AttributePriority priority,
                                      @Nullable Collection<Angular2Directive> sourceDirectives,
                                      @NotNull Collection<PsiElement> elements) {
    myAttributeName = attributeName;
    myElements = elements.toArray(PsiElement.EMPTY_ARRAY);
    mySourceDirectives = sourceDirectives != null ? immutableList(sourceDirectives.toArray(Angular2Directive.EMPTY_ARRAY)) : null;
    myInfo = info;
    myPriority = priority;
  }

  public Angular2AttributeDescriptor merge(XmlAttributeDescriptor other) {
    if (other instanceof Angular2AttributeDescriptor) {
      Angular2AttributeDescriptor ngOther = (Angular2AttributeDescriptor)other;
      assert myAttributeName.equals(ngOther.myAttributeName)
             && myInfo.isEquivalent(ngOther.myInfo)
        : "Cannot merge attributes with different names or non-equivalent infos: "
          + myAttributeName + " " + myInfo.toString() + " != "
          + ngOther.myAttributeName + " " + ngOther.myInfo.toString();
      Set<PsiElement> elements = new HashSet<>(asList(myElements));
      elements.addAll(asList(ngOther.myElements));
      Set<Angular2Directive> sources;
      if (mySourceDirectives == null || ngOther.mySourceDirectives == null) {
        sources = null;
      }
      else {
        sources = new HashSet<>(mySourceDirectives);
        sources.addAll(ngOther.mySourceDirectives);
      }
      return new Angular2AttributeDescriptor(myAttributeName, myInfo, myPriority,
                                             sources, elements);
    }
    else {
      assert other.getName().equals(myAttributeName);
      Set<PsiElement> elements = new HashSet<>(asList(myElements));
      elements.add(other.getDeclaration());
      return new Angular2AttributeDescriptor(myAttributeName, false, elements);
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

  @Nullable
  public List<Angular2Directive> getSourceDirectives() {
    return mySourceDirectives;
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
    return ArrayUtil.getFirstElement(myElements);
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
        type = Angular2Processor.getEventVariableType(type);
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
    DeclarationProximity proximity = mySourceDirectives == null
                                     ? DeclarationProximity.IN_SCOPE
                                     : moduleScope.getDeclarationsProximity(mySourceDirectives);
    if (proximity == DeclarationProximity.NOT_REACHABLE) {
      return pair(null, name);
    }
    LookupElementBuilder element = LookupElementBuilder.create(name)
      .withPresentableText(StringUtil.trimEnd(name, hide.second))
      .withCaseSensitivity(myInfo.type != REGULAR || (myElements.length > 0 && !(myElements[0] instanceof JSPsiElementBase)))
      .withIcon(getIcon())
      .withBoldness(proximity == DeclarationProximity.IN_SCOPE && myPriority == AttributePriority.HIGH)
      // TODO add directive import on insert
      .withInsertHandler(new Angular2AttributeInsertHandler(shouldInsertHandlerRemoveLeftover(), shouldCompleteValue(), null));
    if (info.lookupStrings != null) {
      element = element.withLookupStrings(map(info.lookupStrings, str -> StringUtil.trimStart(str, hide.first)));
    }
    if (proximity != DeclarationProximity.IN_SCOPE) {
      element = element.withItemTextForeground(SimpleTextAttributes.GRAYED_ATTRIBUTES.getFgColor());
    }
    String typeName = getTypeName();
    if (!StringUtil.isEmptyOrSpaces(typeName)) {
      element = element.withTypeText(typeName);
    }
    return pair(PrioritizedLookupElement.withPriority(element, myPriority.getValue(proximity == DeclarationProximity.IN_SCOPE)),
                currentPrefix.substring(hide.first.length()));
  }

  protected LookupElementInfo buildElementInfo(@NotNull PrefixMatcher prefixMatcher) {
    String canonicalPrefix = myInfo.type.getCanonicalPrefix();
    if (canonicalPrefix != null && prefixMatcher.getPrefix().startsWith(canonicalPrefix)) {
      return new LookupElementInfo(Objects.requireNonNull(myInfo.type.buildName(myInfo.getFullName(), true)),
                                   singletonList(pair(canonicalPrefix, "")),
                                   myInfo.type == EVENT ? newArrayList(getName(), "on" + myInfo.getFullName()) : null);
    }
    return new LookupElementInfo(getName(), emptyList(),
                                 myInfo.type == EVENT ? newArrayList(getName(), "on" + myInfo.getFullName()) : null);
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
    List<JSType> types = mapNotNull(myElements, element -> {
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
        types, createTypeSource(myElements[0], false), false);
    }
    return null;
  }

  static boolean isOneTimeBindingProperty(@NotNull Angular2DirectiveProperty property) {
    return !ONE_TIME_BINDING_EXCLUDES.contains(property.getName())
           && (property.isVirtual()
               || (property.getType() != null
                   && expandStringLiteralTypes(property.getType()).isDirectlyAssignableType(STRING_TYPE, null)));
  }

  @Contract("null->null")
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

    public double getValue(boolean isInScope) {
      return isInScope ? myValue : 0;
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
    private Angular2Directive myDirective;
    private List<Angular2AttributeDescriptor> myResult;

    public List<Angular2AttributeDescriptor> get(@NotNull Angular2Directive directive) {
      myDirective = directive;
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
      return new Angular2AttributeDescriptor(PROPERTY_BINDING.buildName(info.getName()),
                                             false, AttributePriority.HIGH,
                                             singletonList(myDirective),
                                             singletonList(info.getNavigableElement()));
    }

    @NotNull
    private Angular2AttributeDescriptor createBananaBoxBinding(@NotNull Pair<Angular2DirectiveProperty, Angular2DirectiveProperty> info) {
      return new Angular2AttributeDescriptor(BANANA_BOX_BINDING.buildName(info.first.getName()), false, AttributePriority.HIGH,
                                             singletonList(myDirective),
                                             newArrayList(info.first.getNavigableElement(),
                                                          info.second.getNavigableElement()));
    }

    @Nullable
    private Angular2AttributeDescriptor createOneTimeBinding(@NotNull Angular2DirectiveProperty info) {
      return isOneTimeBindingProperty(info)
             ? new Angular2AttributeDescriptor(info.getName(), false, AttributePriority.HIGH,
                                               singletonList(myDirective),
                                               singletonList(info.getNavigableElement()))
             : null;
    }

    @NotNull
    private Angular2EventHandlerDescriptor createEventHandler(@NotNull Angular2DirectiveProperty info) {
      return new Angular2EventHandlerDescriptor(EVENT.buildName(info.getName()), false, AttributePriority.HIGH,
                                                singletonList(myDirective),
                                                singletonList(info.getNavigableElement()));
    }
  }
}
