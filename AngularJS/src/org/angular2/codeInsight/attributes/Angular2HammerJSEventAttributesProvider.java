// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import one.util.streamex.StreamEx;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static org.angular2.lang.html.parser.Angular2AttributeType.EVENT;

public class Angular2HammerJSEventAttributesProvider implements Angular2AttributesProvider {

  private static final String CANONICAL_PREFIX_BASE = EVENT.getCanonicalPrefix();
  private static final Set<String> EVENTS = ContainerUtil.immutableSet(
    // pan
    "pan",
    "panstart",
    "panmove",
    "panend",
    "pancancel",
    "panleft",
    "panright",
    "panup",
    "pandown",
    // pinch
    "pinch",
    "pinchstart",
    "pinchmove",
    "pinchend",
    "pinchcancel",
    "pinchin",
    "pinchout",
    // press
    "press",
    "pressup",
    // rotate
    "rotate",
    "rotatestart",
    "rotatemove",
    "rotateend",
    "rotatecancel",
    // swipe
    "swipe",
    "swipeleft",
    "swiperight",
    "swipeup",
    "swipedown",
    // tap
    "tap");

  @Override
  public void contributeCompletionResults(@NotNull CompletionResultsConsumer completionResultsConsumer,
                                          @NotNull XmlTag tag,
                                          @NotNull String attributeName) {
    boolean isCanonical = attributeName.startsWith(CANONICAL_PREFIX_BASE);
    completionResultsConsumer.addDescriptors(
      StreamEx.of(EVENTS)
        .map(event -> new Angular2HammerJSEventDescriptor(tag, event, isCanonical))
        .toList());
  }

  @Override
  public @Nullable Angular2AttributeDescriptor getDescriptor(@NotNull XmlTag tag,
                                                             @NotNull String attributeName,
                                                             @NotNull Angular2AttributeNameParser.AttributeInfo info) {
    if (info.type == EVENT && EVENTS.contains(info.name)) {
      return new Angular2HammerJSEventDescriptor(tag, info.name, info.isCanonical);
    }
    return null;
  }

  @Override
  public @NotNull Collection<String> getRelatedAttributes(@NotNull XmlAttributeDescriptor descriptor) {
    return Collections.emptyList();
  }

  private static class Angular2HammerJSEventDescriptor extends Angular2AttributeDescriptor {

    Angular2HammerJSEventDescriptor(@NotNull XmlTag xmlTag,
                                    @NotNull String eventName,
                                    boolean isCanonical) {
      super(xmlTag, Objects.requireNonNull(EVENT.buildName(eventName, isCanonical)), Collections.emptyList(), true);
    }

    @Override
    public String getTypeName() {
      return "HammerInput";
    }
  }
}
