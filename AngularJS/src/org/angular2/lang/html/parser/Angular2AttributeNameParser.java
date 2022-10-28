// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.util.HtmlUtil;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.psi.Angular2HtmlEvent.AnimationPhase;
import org.angular2.lang.html.psi.Angular2HtmlEvent.EventType;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.isTemplateTag;
import static org.angular2.lang.html.psi.PropertyBindingType.*;
import static org.angular2.web.Angular2WebSymbolsQueryConfigurator.*;

public final class Angular2AttributeNameParser {

  @NonNls
  public static final Map<String, String> ATTR_TO_PROP_MAPPING = Map.ofEntries(
    Map.entry("class", "className"),
    Map.entry("for", "htmlFor"),
    Map.entry("formaction", "formAction"),
    Map.entry("innerHtml", "innerHTML"),
    Map.entry("readonly", "readOnly"),
    Map.entry("tabindex", "tabIndex")
  );

  public static @NotNull AttributeInfo parseBound(@NotNull String name) {
    AttributeInfo info = parse(name);
    return info.type != Angular2AttributeType.REGULAR ? info :
           new PropertyBindingInfo(info.name, info.isCanonical, false, PROPERTY);
  }

  public static AttributeInfo parse(@NotNull String name) {
    return parse(name, ELEMENT_NG_TEMPLATE);
  }

  public static @NotNull AttributeInfo parse(@NotNull String name, @Nullable XmlTag tag) {
    return parse(name, tag != null ? tag.getLocalName() : ELEMENT_NG_TEMPLATE);
  }

  public static @NotNull AttributeInfo parse(@NotNull String name, @NotNull String tagName) {
    name = normalizeAttributeName(name);
    if (name.startsWith("bindon-")) {
      return parsePropertyBindingCanonical(name.substring(7), true);
    }
    else if (name.startsWith("[(") && name.endsWith(")]")) {
      return parsePropertyBindingShort(name.substring(2, name.length() - 2), true);
    }
    else if (name.startsWith("bind-")) {
      return parsePropertyBindingCanonical(name.substring(5), false);
    }
    else if (name.startsWith("[") && name.endsWith("]")) {
      return parsePropertyBindingShort(name.substring(1, name.length() - 1), false);
    }
    else if (name.startsWith("on-")) {
      return parseEvent(name.substring(3), true);
    }
    else if (name.startsWith("(") && name.endsWith(")")) {
      return parseEvent(name.substring(1, name.length() - 1), false);
    }
    else if (name.startsWith("*")) {
      return parseTemplateBindings(name.substring(1));
    }
    else if (name.startsWith("let-")) {
      return parseLet(name, name.substring(4), isTemplateTag(tagName));
    }
    else if (name.startsWith("#")) {
      return parseReference(name, name.substring(1), false);
    }
    else if (name.startsWith("ref-")) {
      return parseReference(name, name.substring(4), true);
    }
    else if (name.startsWith("@")) {
      return new PropertyBindingInfo(name.substring(1), false, false, ANIMATION);
    }
    else if (name.equals(ATTR_SELECT) && tagName.equals(ELEMENT_NG_CONTENT)) {
      return new AttributeInfo(name, false, Angular2AttributeType.NG_CONTENT_SELECTOR);
    }
    else if (name.startsWith("i18n-")) {
      return new AttributeInfo(name.substring(5), false, Angular2AttributeType.I18N);
    }
    return new AttributeInfo(name, false, Angular2AttributeType.REGULAR);
  }

  public static @NotNull String normalizeAttributeName(@NotNull String name) {
    if (StringUtil.startsWithIgnoreCase(name, HtmlUtil.HTML5_DATA_ATTR_PREFIX)) {
      return name.substring(5);
    }
    return name;
  }

  private static @NotNull AttributeInfo parsePropertyBindingShort(@NotNull String name, boolean bananaBoxBinding) {
    if (!bananaBoxBinding && name.startsWith("@")) {
      return new PropertyBindingInfo(name.substring(1), false, false, ANIMATION);
    }
    return parsePropertyBindingRest(name, false, bananaBoxBinding);
  }

  private static @NotNull AttributeInfo parsePropertyBindingCanonical(@NotNull String name, boolean bananaBoxBinding) {
    if (!bananaBoxBinding && name.startsWith("animate-")) {
      return new PropertyBindingInfo(name.substring(8), true, false, ANIMATION);
    }
    return parsePropertyBindingRest(name, true, bananaBoxBinding);
  }

  private static @NotNull AttributeInfo parsePropertyBindingRest(@NotNull String name, boolean isCanonical, boolean bananaBoxBinding) {
    if (name.startsWith("attr.")) {
      return new PropertyBindingInfo(name.substring(5), isCanonical, bananaBoxBinding, ATTRIBUTE);
    }
    if (name.startsWith("class.")) {
      return new PropertyBindingInfo(name.substring(6), isCanonical, bananaBoxBinding, CLASS);
    }
    if (name.startsWith("style.")) {
      return new PropertyBindingInfo(name.substring(6), isCanonical, bananaBoxBinding, STYLE);
    }
    return new PropertyBindingInfo(name, isCanonical, bananaBoxBinding, PROPERTY);
  }


  private static @NotNull AttributeInfo parseEvent(@NotNull String name, boolean isCanonical) {
    if (name.startsWith("@")) {
      name = name.substring(1);
    }
    else if (name.startsWith("animate-")) {
      name = name.substring(8);
    }
    else {
      return new EventInfo(name, isCanonical);
    }
    return parseAnimationEvent(name, isCanonical);
  }

  private static @NotNull AttributeInfo parseTemplateBindings(@NotNull String name) {
    return new AttributeInfo(name, false, Angular2AttributeType.TEMPLATE_BINDINGS);
  }

  private static @NotNull AttributeInfo parseAnimationEvent(@NotNull String name, boolean isCanonical) {
    int dot = name.indexOf('.');
    if (dot < 0) {
      return new EventInfo(name, isCanonical, AnimationPhase.INVALID,
                           Angular2Bundle.message("angular.parse.template.animation-trigger-missing-phase-value",
                                                  name));
    }
    String phase = StringUtil.toLowerCase(name.substring(dot + 1));
    name = name.substring(0, dot);
    if ("done".equals(phase)) {
      return new EventInfo(name, isCanonical, AnimationPhase.DONE);
    }
    else if ("start".equals(phase)) {
      return new EventInfo(name, isCanonical, AnimationPhase.START);
    }
    return new EventInfo(name, isCanonical, AnimationPhase.INVALID,
                         Angular2Bundle.message("angular.parse.template.animation-trigger-wrong-output-phase",
                                                phase, name.substring(0, dot)));
  }

  private static @NotNull AttributeInfo parseLet(@NotNull String attrName, @NotNull String varName, boolean isInTemplateTag) {
    if (!isInTemplateTag) {
      return new AttributeInfo(attrName, false, Angular2AttributeType.REGULAR,
                               Angular2Bundle.message("angular.parse.template.let-only-on-ng-template"));
    }
    else if (varName.contains("-")) {
      return new AttributeInfo(attrName, false, Angular2AttributeType.REGULAR,
                               Angular2Bundle.message("angular.parse.template.let-dash-not-allowed-in-name"));
    }
    else if (varName.isEmpty()) {
      return new AttributeInfo(attrName, false, Angular2AttributeType.REGULAR);
    }
    return new AttributeInfo(varName, false, Angular2AttributeType.LET);
  }

  private static @NotNull AttributeInfo parseReference(@NotNull String attrName, @NotNull String refName, boolean isCanonical) {
    if (refName.contains("-")) {
      return new AttributeInfo(attrName, false, Angular2AttributeType.REGULAR,
                               Angular2Bundle.message("angular.parse.template.ref-var-dash-not-allowed-in-name"));
    }
    else if (refName.isEmpty()) {
      return new AttributeInfo(attrName, false, Angular2AttributeType.REGULAR);
    }
    return new AttributeInfo(refName, isCanonical, Angular2AttributeType.REFERENCE);
  }

  public static class AttributeInfo {

    public final @NotNull String name;
    public final @Nullable @Nls String error;
    public final @NotNull Angular2AttributeType type;
    public final boolean isCanonical;

    public AttributeInfo(@NotNull String name, boolean isCanonical, @NotNull Angular2AttributeType type) {
      this(name, isCanonical, type, null);
    }

    public AttributeInfo(@NotNull String name, boolean isCanonical, @NotNull Angular2AttributeType type, @Nullable @Nls String error) {
      this.name = name;
      this.error = error;
      this.type = type;
      this.isCanonical = isCanonical;
    }

    public boolean isEquivalent(@Nullable AttributeInfo otherInfo) {
      return otherInfo != null
             && name.equals(otherInfo.name)
             && type == otherInfo.type;
    }

    public String getFullName() {
      return name;
    }

    @Override
    public String toString() {
      return "<" + name + ">";
    }
  }

  public static class PropertyBindingInfo extends AttributeInfo {

    public final @NotNull PropertyBindingType bindingType;

    public PropertyBindingInfo(@NotNull String name,
                               boolean isCanonical,
                               boolean bananaBoxBinding,
                               @NotNull PropertyBindingType bindingType) {
      super(ATTR_TO_PROP_MAPPING.getOrDefault(name, name), isCanonical,
            bananaBoxBinding ? Angular2AttributeType.BANANA_BOX_BINDING : Angular2AttributeType.PROPERTY_BINDING);
      this.bindingType = bindingType;
    }

    @Override
    public boolean isEquivalent(@Nullable AttributeInfo otherInfo) {
      return otherInfo instanceof PropertyBindingInfo
             && bindingType == ((PropertyBindingInfo)otherInfo).bindingType
             && super.isEquivalent(otherInfo);
    }

    @Override
    public String getFullName() {
      return switch (this.bindingType) {
        case ANIMATION -> (isCanonical ? "animate-" : "@") + name;
        case ATTRIBUTE -> "attr." + name;
        case STYLE -> "style." + name;
        case CLASS -> "class." + name;
        default -> name;
      };
    }

    @Override
    public String toString() {
      return "<" + name + "," + bindingType + ">";
    }
  }

  public static class EventInfo extends AttributeInfo {

    public final @Nullable AnimationPhase animationPhase;

    public final @NotNull EventType eventType;

    public EventInfo(@NotNull String name, boolean isCanonical) {
      super(name, isCanonical, Angular2AttributeType.EVENT);
      eventType = EventType.REGULAR;
      animationPhase = null;
    }

    public EventInfo(@NotNull String name, boolean isCanonical, @NotNull AnimationPhase animationPhase) {
      this(name, isCanonical, animationPhase, null);
    }

    public EventInfo(@NotNull String name, boolean isCanonical, @NotNull AnimationPhase animationPhase, @Nullable @Nls String error) {
      super(name, isCanonical, Angular2AttributeType.EVENT, error);
      this.animationPhase = animationPhase;
      this.eventType = EventType.ANIMATION;
    }

    @Override
    public boolean isEquivalent(@Nullable AttributeInfo otherInfo) {
      return otherInfo instanceof EventInfo
             && eventType == ((EventInfo)otherInfo).eventType
             && animationPhase == ((EventInfo)otherInfo).animationPhase
             && super.isEquivalent(otherInfo);
    }

    @Override
    public String getFullName() {
      if (eventType == EventType.ANIMATION) {
        if (animationPhase != null) {
          return switch (animationPhase) {
            case DONE -> "@" + name + ".done";
            case START -> "@" + name + ".start";
            default -> "@" + name;
          };
        }
        return "@" + name;
      }
      return name;
    }

    @Override
    public String toString() {
      return "<" + name + ", " + eventType + (eventType == EventType.ANIMATION ? ", " + animationPhase : "") + ">";
    }
  }
}
