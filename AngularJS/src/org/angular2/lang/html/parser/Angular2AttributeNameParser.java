// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.html.psi.Angular2HtmlEvent.AnimationPhase;
import org.angular2.lang.html.psi.Angular2HtmlEvent.EventType;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

import static com.intellij.openapi.util.Pair.pair;
import static org.angular2.lang.html.psi.PropertyBindingType.*;

public class Angular2AttributeNameParser {

  private static final Map<String, String> ATTR_TO_PROP_MAPPING = ContainerUtil.newHashMap(
    pair("class", "className"),
    pair("for", "htmlFor"),
    pair("formaction", "formAction"),
    pair("innerHtml", "innerHTML"),
    pair("readonly", "readOnly"),
    pair("tabindex", "tabIndex")
  );

  @NotNull
  public static AttributeInfo parseBound(@NotNull String name) {
    AttributeInfo info = parse(name, true);
    return info.type != Angular2AttributeType.REGULAR ? info :
           new PropertyBindingInfo(info.name, info.isCanonical, false, PROPERTY);
  }

  @NotNull
  public static AttributeInfo parse(@NotNull String name, boolean isInTemplateTag) {
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
      return parseVariable(name.substring(4), isInTemplateTag);
    }
    else if (name.startsWith("#")) {
      return parseReference(name.substring(1), false);
    }
    else if (name.startsWith("ref-")) {
      return parseReference(name.substring(4), true);
    }
    else if (name.startsWith("@")) {
      return new PropertyBindingInfo(name.substring(1), false, false, ANIMATION);
    }
    return new AttributeInfo(name, false, Angular2AttributeType.REGULAR);
  }

  @NotNull
  public static String normalizeAttributeName(@NotNull String name) {
    if (StringUtil.startsWithIgnoreCase(name, "data-")) {
      return name.substring(5);
    }
    return name;
  }

  @NotNull
  private static AttributeInfo parsePropertyBindingShort(@NotNull String name, boolean bananaBoxBinding) {
    if (!bananaBoxBinding && name.startsWith("@")) {
      return new PropertyBindingInfo(name.substring(1), false, false, ANIMATION);
    }
    return parsePropertyBindingRest(name, false, bananaBoxBinding);
  }

  @NotNull
  private static AttributeInfo parsePropertyBindingCanonical(@NotNull String name, boolean bananaBoxBinding) {
    if (!bananaBoxBinding && name.startsWith("animate-")) {
      return new PropertyBindingInfo(name.substring(8), true, false, ANIMATION);
    }
    return parsePropertyBindingRest(name, true, bananaBoxBinding);
  }

  @NotNull
  private static AttributeInfo parsePropertyBindingRest(@NotNull String name, boolean isCanonical, boolean bananaBoxBinding) {
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


  @NotNull
  private static AttributeInfo parseEvent(@NotNull String name, boolean isCanonical) {
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

  @NotNull
  private static AttributeInfo parseTemplateBindings(@NotNull String name) {
    return new AttributeInfo(name, false, Angular2AttributeType.TEMPLATE_BINDINGS);
  }

  @NotNull
  private static AttributeInfo parseAnimationEvent(@NotNull String name, boolean isCanonical) {
    int dot = name.indexOf('.');
    if (dot < 0) {
      return new EventInfo(name, isCanonical, AnimationPhase.INVALID,
                           "The animation trigger output event (@" + name +
                           ") is missing its phase value name (start or done are currently supported)");
    }
    String phase = name.substring(dot + 1).toLowerCase(Locale.ENGLISH);
    name = name.substring(0, dot);
    if ("done".equals(phase)) {
      return new EventInfo(name, isCanonical, AnimationPhase.DONE);
    }
    else if ("start".equals(phase)) {
      return new EventInfo(name, isCanonical, AnimationPhase.START);
    }
    return new EventInfo(name, isCanonical, AnimationPhase.INVALID,
                         "The provided animation output phase value '" + phase +
                         "' for '@" + name.substring(0, dot) +
                         "' is not supported (use start or done))");
  }

  @NotNull
  private static AttributeInfo parseVariable(@NotNull String varName, boolean isInTemplateTag) {
    if (!isInTemplateTag) {
      return new AttributeInfo(varName, false, Angular2AttributeType.REGULAR, "\"let-\" is only supported on ng-template elements.");
    }
    else if (varName.contains("-")) {
      return new AttributeInfo(varName, false, Angular2AttributeType.REGULAR, "\"-\" is not allowed in variable names");
    }
    return new AttributeInfo(varName, false, Angular2AttributeType.VARIABLE);
  }

  @NotNull
  private static AttributeInfo parseReference(@NotNull String refName, boolean isCanonical) {
    if (refName.contains("-")) {
      return new AttributeInfo(refName, false, Angular2AttributeType.REGULAR, "\"-\" is not allowed in reference names");
    }
    else if (refName.isEmpty()) {
      return new AttributeInfo("", false, Angular2AttributeType.REGULAR);
    }
    return new AttributeInfo(refName, isCanonical, Angular2AttributeType.REFERENCE);
  }

  public static class AttributeInfo {

    @NotNull
    public final String name;
    @Nullable
    public final String error;
    /**
     * @deprecated Use {@code type} field instead
     */
    @NotNull
    @Deprecated
    public final IElementType elementType;
    @NotNull
    public final Angular2AttributeType type;
    public final boolean isCanonical;

    public AttributeInfo(@NotNull String name, boolean isCanonical, @NotNull Angular2AttributeType type) {
      this(name, isCanonical, type, null);
    }

    public AttributeInfo(@NotNull String name, boolean isCanonical, @NotNull Angular2AttributeType type, @Nullable String error) {
      this.name = name;
      this.error = error;
      this.type = type;
      this.isCanonical = isCanonical;
      //noinspection deprecation
      this.elementType = type.getElementType();
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

    @NotNull
    public final PropertyBindingType bindingType;

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
      switch (this.bindingType) {
        case ANIMATION:
          return (isCanonical ? "animate-" : "@") + name;
        case ATTRIBUTE:
          return "attr." + name;
        case STYLE:
          return "style." + name;
        case CLASS:
          return "class." + name;
        default:
          return name;
      }
    }

    @Override
    public String toString() {
      return "<" + name + "," + bindingType + ">";
    }
  }

  public static class EventInfo extends AttributeInfo {

    @Nullable
    public final AnimationPhase animationPhase;

    @NotNull
    public final EventType eventType;

    public EventInfo(@NotNull String name, boolean isCanonical) {
      super(name, isCanonical, Angular2AttributeType.EVENT);
      eventType = EventType.REGULAR;
      animationPhase = null;
    }

    public EventInfo(@NotNull String name, boolean isCanonical, @NotNull AnimationPhase animationPhase) {
      this(name, isCanonical, animationPhase, null);
    }

    public EventInfo(@NotNull String name, boolean isCanonical, @NotNull AnimationPhase animationPhase, @Nullable String error) {
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
          switch (animationPhase) {
            case DONE:
              return "@" + name + ".done";
            case START:
              return "@" + name + ".start";
            default:
          }
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
