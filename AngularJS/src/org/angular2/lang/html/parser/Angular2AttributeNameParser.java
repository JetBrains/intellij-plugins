// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.html.psi.Angular2HtmlEvent.AnimationPhase;
import org.angular2.lang.html.psi.Angular2HtmlEvent.EventType;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static com.intellij.psi.xml.XmlElementType.XML_ATTRIBUTE;
import static org.angular2.lang.html.parser.Angular2HtmlElementTypes.*;
import static org.angular2.lang.html.psi.PropertyBindingType.*;

public class Angular2AttributeNameParser {


  @NotNull
  public static AttributeInfo parseBound(@NotNull String name) {
    AttributeInfo info = parse(name, true);
    return info.elementType != XML_ATTRIBUTE ? info :
           new PropertyBindingInfo(info.name, false, PROPERTY);
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
      return parseEvent(name.substring(3));
    }
    else if (name.startsWith("(") && name.endsWith(")")) {
      return parseEvent(name.substring(1, name.length() - 1));
    }
    else if (name.startsWith("*")) {
      return parseTemplateBindings(name.substring(1));
    }
    else if (name.startsWith("let-")) {
      return parseVariable(name.substring(4), isInTemplateTag);
    }
    else if (name.startsWith("#")) {
      return parseReference(name.substring(1));
    }
    else if (name.startsWith("ref-")) {
      return parseReference(name.substring(4));
    }
    else if (name.startsWith("@")) {
      return new PropertyBindingInfo(name.substring(1), false, ANIMATION);
    }
    return new AttributeInfo(name, XML_ATTRIBUTE);
  }

  @NotNull
  private static String normalizeAttributeName(@NotNull String name) {
    return StringUtil.trimStart(name, "data-");
  }

  @NotNull
  private static AttributeInfo parsePropertyBindingShort(@NotNull String name, boolean bananaBoxBinding) {
    if (!bananaBoxBinding && name.startsWith("@")) {
      return new PropertyBindingInfo(name.substring(1), false, ANIMATION);
    }
    return parsePropertyBindingRest(name, bananaBoxBinding);
  }

  @NotNull
  private static AttributeInfo parsePropertyBindingCanonical(@NotNull String name, boolean bananaBoxBinding) {
    if (!bananaBoxBinding && name.startsWith("animate-")) {
      return new PropertyBindingInfo(name.substring(8), false, ANIMATION);
    }
    return parsePropertyBindingRest(name, bananaBoxBinding);
  }

  @NotNull
  private static AttributeInfo parsePropertyBindingRest(@NotNull String name, boolean bananaBoxBinding) {
    if (name.startsWith("attr.")) {
      return new PropertyBindingInfo(name.substring(5), bananaBoxBinding, ATTRIBUTE);
    }
    if (name.startsWith("class.")) {
      return new PropertyBindingInfo(name.substring(6), bananaBoxBinding, CLASS);
    }
    if (name.startsWith("style.")) {
      return new PropertyBindingInfo(name.substring(6), bananaBoxBinding, STYLE);
    }
    return new PropertyBindingInfo(name, bananaBoxBinding, PROPERTY);
  }


  @NotNull
  private static AttributeInfo parseEvent(@NotNull String name) {
    if (name.startsWith("@")) {
      name = name.substring(1);
    }
    else if (name.startsWith("animate-")) {
      name = name.substring(8);
    }
    else {
      return new EventInfo(name);
    }
    return parseAnimationEvent(name);
  }

  @NotNull
  private static AttributeInfo parseTemplateBindings(@NotNull String name) {
    return new AttributeInfo(name, TEMPLATE_BINDINGS);
  }

  @NotNull
  private static AttributeInfo parseAnimationEvent(@NotNull String name) {
    int dot = name.indexOf('.');
    if (dot < 0) {
      return new EventInfo(name, AnimationPhase.INVALID,
                           "The animation trigger output event (@" + name +
                           ") is missing its phase value name (start or done are currently supported)");
    }
    String phase = name.substring(dot + 1).toLowerCase(Locale.ENGLISH);
    name = name.substring(0, dot);
    if ("done".equals(phase)) {
      return new EventInfo(name, AnimationPhase.DONE);
    }
    else if ("start".equals(phase)) {
      return new EventInfo(name, AnimationPhase.START);
    }
    return new EventInfo(name, AnimationPhase.INVALID,
                         "The provided animation output phase value '" + phase +
                         "' for '@" + name.substring(0, dot) +
                         "' is not supported (use start or done))");
  }

  @NotNull
  private static AttributeInfo parseVariable(@NotNull String varName, boolean isInTemplateTag) {
    if (!isInTemplateTag) {
      return new AttributeInfo(varName, XML_ATTRIBUTE, "\"let-\" is only supported on ng-template elements.");
    }
    else if (varName.contains("-")) {
      return new AttributeInfo(varName, XML_ATTRIBUTE, "\"-\" is not allowed in variable names");
    }
    return new AttributeInfo(varName, VARIABLE);
  }

  @NotNull
  private static AttributeInfo parseReference(@NotNull String refName) {
    if (refName.contains("-")) {
      return new AttributeInfo(refName, XML_ATTRIBUTE, "\"-\" is not allowed in reference names");
    }
    else if (refName.isEmpty()) {
      return new AttributeInfo("", XML_ATTRIBUTE);
    }
    return new AttributeInfo(refName, REFERENCE);
  }

  public static class AttributeInfo {

    @NotNull
    public final String name;
    @Nullable
    public final String error;
    @NotNull
    public final IElementType elementType;

    public AttributeInfo(@NotNull String name, @NotNull IElementType elementType) {
      this(name, elementType, null);
    }

    public AttributeInfo(@NotNull String name, @NotNull IElementType elementType, @Nullable String error) {
      this.name = name;
      this.error = error;
      this.elementType = elementType;
    }

    @Override
    public String toString() {
      return "<" + name + ">";
    }
  }

  public static class PropertyBindingInfo extends AttributeInfo {

    @NotNull
    public final PropertyBindingType bindingType;

    public PropertyBindingInfo(@NotNull String name, boolean bananaBoxBinding, @NotNull PropertyBindingType bindingType) {
      super(name, bananaBoxBinding ? BANANA_BOX_BINDING : PROPERTY_BINDING);
      this.bindingType = bindingType;
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

    public EventInfo(@NotNull String name) {
      super(name, EVENT);
      eventType = EventType.REGULAR;
      animationPhase = null;
    }

    public EventInfo(@NotNull String name, @NotNull AnimationPhase animationPhase) {
      this(name, animationPhase, null);
    }

    public EventInfo(@NotNull String name, @NotNull AnimationPhase animationPhase, @Nullable String error) {
      super(name, EVENT, error);
      this.animationPhase = animationPhase;
      this.eventType = EventType.ANIMATION;
    }

    @Override
    public String toString() {
      return "<" + name + ", " + eventType + (eventType == EventType.ANIMATION ? ", " + animationPhase : "") + ">";
    }
  }
}
