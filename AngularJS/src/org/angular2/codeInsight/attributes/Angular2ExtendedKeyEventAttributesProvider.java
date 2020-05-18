// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.openapi.util.text.StringUtil.notNullize;
import static com.intellij.openapi.util.text.StringUtil.*;
import static com.intellij.util.containers.ContainerUtil.*;
import static org.angular2.codeInsight.attributes.Angular2AttributeDescriptor.AttributePriority.*;
import static org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider.EVENT_ATTR_PREFIX;
import static org.angular2.lang.html.parser.Angular2AttributeType.EVENT;

public class Angular2ExtendedKeyEventAttributesProvider implements Angular2AttributesProvider {

  @NonNls private static final String KEYDOWN_EVENT_BASE_PREFIX = "keydown.";
  @NonNls private static final String KEYUP_EVENT_BASE_PREFIX = "keyup.";

  private static final String CANONICAL_PREFIX_BASE = EVENT.getCanonicalPrefix();

  private static final List<String> KEYDOWN_PREFIXES = buildPrefixes(KEYDOWN_EVENT_BASE_PREFIX, false);
  private static final List<String> KEYUP_PREFIXES = buildPrefixes(KEYUP_EVENT_BASE_PREFIX, false);

  private static final List<String> ALL_PREFIXES = concat(buildPrefixes(KEYDOWN_EVENT_BASE_PREFIX, true),
                                                          buildPrefixes(KEYUP_EVENT_BASE_PREFIX, true));

  private static List<String> buildPrefixes(String prefix, boolean includeCanonical) {
    if (includeCanonical) {
      return Arrays.asList("(" + prefix, CANONICAL_PREFIX_BASE + prefix, EVENT_ATTR_PREFIX + prefix, prefix);
    }
    else {
      return Arrays.asList("(" + prefix, EVENT_ATTR_PREFIX + prefix, prefix);
    }
  }

  @NonNls private static final List<String> MODIFIER_KEYS = newArrayList(
    "alt",
    "control",
    "meta",
    "shift"
  );

  @NonNls private static final List<String> SPECIAL_KEY_NAMES = newArrayList(
    "space",
    "dot",
    "escape",
    "enter",
    "tab",
    "arrowDown",
    "arrowLeft",
    "arrowRight",
    "arrowUp",
    "end",
    "home",
    "pageDown",
    "pageUp",
    "backspace",
    "delete",
    "insert",
    "contextMenu",
    "help",
    "printScreen"
  );

  private static final List<String> STD_KEY_NAMES = new ArrayList<>();

  static {
    for (int i = 1; i <= 20; i++) {
      SPECIAL_KEY_NAMES.add("f" + i);
    }
    for (char ch = 'a'; ch <= 'z'; ch++) {
      STD_KEY_NAMES.add(Character.toString(ch));
    }
    for (char ch = '0'; ch <= '9'; ch++) {
      STD_KEY_NAMES.add(Character.toString(ch));
    }
    for (char ch : "`~!@#$%^&*()_+-[]{}|;:,?".toCharArray()) {
      STD_KEY_NAMES.add(Character.toString(ch));
    }
  }

  @Override
  public void contributeCompletionResults(@NotNull CompletionResultsConsumer result,
                                          @NotNull XmlTag tag,
                                          @NotNull String attributeName) {
    String currentPrefix;
    if ((currentPrefix = find(ALL_PREFIXES, attributeName::startsWith)) != null) {
      boolean isCanonical = currentPrefix.startsWith(CANONICAL_PREFIX_BASE);
      List<String> keySpec = split(attributeName.substring(currentPrefix.length()), ".", true, false);
      if (keySpec.size() > 0) {
        keySpec.remove(keySpec.size() - 1);
      }
      boolean keydown = currentPrefix.endsWith(KEYDOWN_EVENT_BASE_PREFIX);
      String itemBase = (keydown ? KEYDOWN_EVENT_BASE_PREFIX : KEYUP_EVENT_BASE_PREFIX)
                        + join(keySpec, ".");
      if (!itemBase.endsWith(".")) {
        itemBase += ".";
      }
      if (!MODIFIER_KEYS.containsAll(keySpec)) {
        return;
      }
      for (String modifier : MODIFIER_KEYS) {
        if (!keySpec.contains(modifier)) {
          String itemWithModifier = itemBase + modifier + ".";
          if (isCanonical) {
            result.addAbbreviation(CANONICAL_PREFIX_BASE + itemWithModifier, HIGH, CANONICAL_PREFIX_BASE + itemBase, null);
          }
          else {
            result.addAbbreviation(buildPrefixes(itemWithModifier, false), HIGH, "(" + itemBase, ")");
          }
        }
      }
      XmlAttributeDescriptor descriptor = getEventDescriptor(tag, keydown);
      for (String keyName : SPECIAL_KEY_NAMES) {
        result.addDescriptor(new Angular2ExtendedKeyEventDescriptor(tag, descriptor, itemBase, keyName, NORMAL, isCanonical));
      }
      for (String keyName : STD_KEY_NAMES) {
        result.addDescriptor(new Angular2ExtendedKeyEventDescriptor(tag, descriptor, itemBase, keyName, LOW, isCanonical));
      }
    }
    else {
      if (attributeName.startsWith(CANONICAL_PREFIX_BASE)) {
        result.addAbbreviation(CANONICAL_PREFIX_BASE + KEYDOWN_EVENT_BASE_PREFIX, LOW, CANONICAL_PREFIX_BASE, null);
        result.addAbbreviation(CANONICAL_PREFIX_BASE + KEYUP_EVENT_BASE_PREFIX, LOW, CANONICAL_PREFIX_BASE, null);
      }
      else {
        result.addAbbreviation(KEYDOWN_PREFIXES, LOW, null, ")");
        result.addAbbreviation(KEYUP_PREFIXES, LOW, null, ")");
      }
    }
  }

  @Override
  public @Nullable Angular2AttributeDescriptor getDescriptor(@NotNull XmlTag tag,
                                                             @NotNull String attributeName,
                                                             @NotNull Angular2AttributeNameParser.AttributeInfo info) {
    if (info.type == EVENT
        && (info.name.startsWith(KEYDOWN_EVENT_BASE_PREFIX)
            || info.name.startsWith(KEYUP_EVENT_BASE_PREFIX))) {
      return new Angular2ExtendedKeyEventDescriptor(tag, getEventDescriptor(
        tag, info.name.startsWith(KEYDOWN_EVENT_BASE_PREFIX)), null, info.name, NORMAL, info.isCanonical);
    }
    return null;
  }

  @Override
  public @NotNull Collection<String> getRelatedAttributes(@NotNull XmlAttributeDescriptor descriptor) {
    return Collections.emptyList();
  }

  private static XmlAttributeDescriptor getEventDescriptor(@NotNull XmlTag tag, boolean keydown) {
    String attrName = EVENT_ATTR_PREFIX + trimEnd(keydown ? KEYDOWN_EVENT_BASE_PREFIX : KEYUP_EVENT_BASE_PREFIX,
                                                  '.');
    return find(Angular2AttributeDescriptorsProvider.getDefaultAttributeDescriptors(tag),
                attr -> attrName.equalsIgnoreCase(attr.getName()));
  }

  private static class Angular2ExtendedKeyEventDescriptor extends Angular2AttributeDescriptor {

    private final String myBaseName;

    protected Angular2ExtendedKeyEventDescriptor(@NotNull XmlTag xmlTag,
                                                 @Nullable XmlAttributeDescriptor originalDescriptor,
                                                 @Nullable String baseName,
                                                 @NotNull String keyName,
                                                 @NotNull AttributePriority priority,
                                                 boolean canonical) {
      super(xmlTag,
            Objects.requireNonNull(EVENT.buildName(notNullize(baseName) + keyName, canonical)),
            priority,
            originalDescriptor == null ? Collections.emptySet() : originalDescriptor.getDeclarations(),
            true);
      myBaseName = baseName;
    }

    @Override
    protected LookupElementInfo buildElementInfo(@NotNull PrefixMatcher prefixMatcher) {
      if (myBaseName != null) {
        return new LookupElementInfo(getName(),
                                     Arrays.asList(pair(CANONICAL_PREFIX_BASE + myBaseName, ""), pair("(" + myBaseName, ")")),
                                     null);
      }
      return super.buildElementInfo(prefixMatcher);
    }

    @Override
    public XmlAttributeDescriptor cloneWithName(String attributeName) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected boolean shouldInsertHandlerRemoveLeftover() {
      return true;
    }
  }
}
