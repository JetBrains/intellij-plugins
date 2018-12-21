// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.completion.XmlAttributeInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
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
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import icons.AngularJSIcons;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.intellij.lang.javascript.psi.types.JSTypeSourceFactory.createTypeSource;
import static java.util.Collections.singletonList;
import static org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider.getCustomNgAttrs;
import static org.angular2.lang.html.parser.Angular2AttributeType.*;

public class Angular2AttributeDescriptor extends BasicXmlAttributeDescriptor implements XmlAttributeDescriptorEx, PsiPresentableMetaData {

  private static final JSType STRING_TYPE = new JSStringType(true, JSTypeSource.EXPLICITLY_DECLARED, JSTypeContext.INSTANCE);

  @Nullable
  public static Angular2AttributeDescriptor create(@NotNull String attributeName) {
    return create(attributeName, Collections.emptyList());
  }

  @Nullable
  public static Angular2AttributeDescriptor create(@NotNull String attributeName, @NotNull PsiElement element) {
    return create(attributeName, singletonList(element));
  }

  @Nullable
  public static Angular2AttributeDescriptor create(@NotNull String attributeName,
                                                   @NotNull List<PsiElement> elements) {
    if (getCustomNgAttrs().contains(attributeName)) {
      return new Angular2AttributeDescriptor(attributeName, false, elements);
    }
    Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(attributeName, true);
    if (elements.isEmpty()
        && (info.type == REGULAR
            || info.type == TEMPLATE_BINDINGS
            || (info instanceof Angular2AttributeNameParser.EventInfo
                && ((Angular2AttributeNameParser.EventInfo)info).eventType == Angular2HtmlEvent.EventType.REGULAR
                && !info.name.contains(":"))
            || (info instanceof Angular2AttributeNameParser.PropertyBindingInfo
                && ((Angular2AttributeNameParser.PropertyBindingInfo)info).bindingType == PropertyBindingType.PROPERTY))) {
      return null;
    }
    if (info.type == EVENT) {
      return new Angular2EventHandlerDescriptor(attributeName, info, elements);
    }
    return new Angular2AttributeDescriptor(attributeName, info, elements);
  }

  @NotNull
  public static List<XmlAttributeDescriptor> getDirectiveDescriptors(@NotNull Angular2Directive directive, boolean isTemplateTagContext) {
    if (directive.isTemplate() && !isTemplateTagContext) {
      return Collections.emptyList();
    }
    List<XmlAttributeDescriptor> result = new ArrayList<>();
    addDirectiveDescriptors(directive.getInOuts(), Angular2AttributeDescriptor::createBananaBoxBinding, result);
    addDirectiveDescriptors(directive.getInputs(), Angular2AttributeDescriptor::createBinding, result);
    addDirectiveDescriptors(directive.getOutputs(), Angular2AttributeDescriptor::createEventHandler, result);
    addDirectiveDescriptors(directive.getInputs(), Angular2AttributeDescriptor::createOneTimeBinding, result);
    return result;
  }

  @NotNull
  private final AttributePriority myPriority;
  @NotNull
  private final PsiElement[] myElements;
  @NotNull
  private final String myAttributeName;
  @NotNull
  private final Angular2AttributeNameParser.AttributeInfo myInfo;

  protected Angular2AttributeDescriptor(@NotNull String attributeName,
                                        boolean isInTemplateTag,
                                        @NotNull Collection<PsiElement> elements) {
    this(attributeName, Angular2AttributeNameParser.parse(attributeName, isInTemplateTag),
         AttributePriority.NORMAL, elements);
  }

  protected Angular2AttributeDescriptor(@NotNull String attributeName,
                                        @NotNull Angular2AttributeNameParser.AttributeInfo info,
                                        @NotNull Collection<PsiElement> elements) {
    this(attributeName, info, AttributePriority.NORMAL, elements);
  }

  protected Angular2AttributeDescriptor(@NotNull String attributeName,
                                        boolean isInTemplateTag,
                                        @NotNull AttributePriority priority,
                                        @NotNull Collection<PsiElement> elements) {
    this(attributeName, Angular2AttributeNameParser.parse(attributeName, isInTemplateTag), priority, elements);
  }

  protected Angular2AttributeDescriptor(@NotNull String attributeName,
                                        @NotNull Angular2AttributeNameParser.AttributeInfo info,
                                        @NotNull AttributePriority priority,
                                        @NotNull Collection<PsiElement> elements) {
    myAttributeName = attributeName;
    myElements = elements.toArray(PsiElement.EMPTY_ARRAY);
    myInfo = info;
    myPriority = priority;
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

  public LookupElement getLookupElement() {
    LookupElementBuilder element = LookupElementBuilder.create(getName())
      .withCaseSensitivity(myInfo.type != REGULAR || (myElements.length > 0 && !(myElements[0] instanceof JSPsiElementBase)))
      .withIcon(getIcon())
      .withBoldness(myPriority == AttributePriority.HIGH);
    String typeName = getTypeName();
    if (!StringUtil.isEmptyOrSpaces(typeName)) {
      element = element.withTypeText(typeName);
    }
    if (shouldCompleteValue()) {
      element = element.withInsertHandler(XmlAttributeInsertHandler.INSTANCE);
    }
    return PrioritizedLookupElement.withPriority(element, myPriority.getValue());
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
  private JSType getJSType() {
    List<JSType> types = ContainerUtil.mapNotNull(myElements, JSTypeUtils::getTypeOfElement);
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
    return property.isVirtual()
           || (property.getType() != null
               && expandStringLiteralTypes(property.getType()).isDirectlyAssignableType(STRING_TYPE, null));
  }

  private static <T> void addDirectiveDescriptors(@NotNull Collection<T> list,
                                                  @NotNull Function<T, ? extends XmlAttributeDescriptor> factory,
                                                  @NotNull List<XmlAttributeDescriptor> result) {
    list.forEach(el -> ObjectUtils.doIfNotNull(factory.apply(el), result::add));
  }

  @NotNull
  private static Angular2AttributeDescriptor createBinding(@NotNull Angular2DirectiveProperty info) {
    return new Angular2AttributeDescriptor(PROPERTY_BINDING.buildName(info.getName()), false, AttributePriority.HIGH,
                                           singletonList(info.getNavigableElement()));
  }

  @NotNull
  private static Angular2AttributeDescriptor createBananaBoxBinding(@NotNull Pair<Angular2DirectiveProperty, Angular2DirectiveProperty> info) {
    return new Angular2AttributeDescriptor(BANANA_BOX_BINDING.buildName(info.first.getName()), false, AttributePriority.HIGH,
                                           ContainerUtil.newArrayList(info.first.getNavigableElement(),
                                                                      info.second.getNavigableElement()));
  }

  @Nullable
  private static Angular2AttributeDescriptor createOneTimeBinding(@NotNull Angular2DirectiveProperty info) {
    return isOneTimeBindingProperty(info)
           ? new Angular2AttributeDescriptor(info.getName(), false, AttributePriority.HIGH,
                                             singletonList(info.getNavigableElement()))
           : null;
  }

  @NotNull
  private static Angular2EventHandlerDescriptor createEventHandler(@NotNull Angular2DirectiveProperty info) {
    return new Angular2EventHandlerDescriptor(EVENT.buildName(info.getName()), false, AttributePriority.HIGH,
                                              singletonList(info.getNavigableElement()));
  }

  @Contract("null->null")
  private static JSType expandStringLiteralTypes(@Nullable JSType type) {
    if (type == null) return null;
    type = TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(type);
    return type.transformTypeHierarchy(toApply -> toApply instanceof JSPrimitiveType ? STRING_TYPE : toApply);
  }

  public enum AttributePriority {
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
  }
}
