package com.intellij.lang.javascript.linter.jshint;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.intellij.lang.javascript.linter.jshint.JSHintBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.NlsContexts.HintText;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.JavaXmlDocumentKt;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sergey Simonchik
 */
public final class JSHintDocumentation {

  private static final Logger LOG = Logger.getInstance(JSHintDocumentation.class);

  private final List<JSHintOptionGroup> myGroups;
  private final Map<JSHintOption, @HintText String> myNonGroupOptions;
  private final ImmutableMap<JSHintOption, @HintText String> myDescriptionByOption;

  private JSHintDocumentation(@NotNull List<JSHintOptionGroup> groups) {
    myGroups = groups;
    myNonGroupOptions = new HashMap<>();
    myNonGroupOptions.put(JSHintOption.PREDEF, JSHintBundle.message("jshint.option.predef.details"));
    Map<JSHintOption, @HintText String> builder = Maps.newHashMap();
    for (JSHintOptionGroup group : groups) {
      for (JSHintOption option : group.getOptions()) {
        builder.put(option, group.getHtmlDescriptionByOption(option));
      }
    }
    builder.putAll(myNonGroupOptions);
    myDescriptionByOption = ImmutableMap.copyOf(builder);
  }

  public @NotNull List<JSHintOptionGroup> getGroups() {
    return myGroups;
  }

  public @Nullable @HintText String getHtmlDescriptionForNonGroupOption(@NotNull JSHintOption option) {
    return myNonGroupOptions.get(option);
  }

  public @Nullable @HintText String getHtmlDescriptionByOption(@NotNull JSHintOption option) {
    return myDescriptionByOption.get(option);
  }

  public static @NotNull JSHintDocumentation getInstance() {
    return Holder.INSTANCE;
  }

  private static @NotNull JSHintDocumentation parseFromXml() {
    try {
      String content = loadXmlContent();
      InputSource source = new InputSource(new StringReader(content));
      Document document = JavaXmlDocumentKt.createDocumentBuilder().parse(source);
      return fromDocument(document);
    }
    catch (Exception e) {
      LOG.error("Can't parse jshint documentation :(", e);
      return new JSHintDocumentation(Collections.emptyList());
    }
  }

  private static @NotNull JSHintDocumentation fromDocument(@NotNull Document document) {
    List<JSHintOptionGroup> groups = new ArrayList<>();
    Element groupsElement = getTheOnlyChildElementWithTagName(document, "groups");
    List<Element> groupChildren = getChildElementsByTagName(groupsElement, "group");
    for (Element groupElement : groupChildren) {
      groups.add(createGroup(groupElement));
    }
    return new JSHintDocumentation(groups);
  }

  private static @NotNull Element getTheOnlyChildElementWithTagName(@NotNull Node parentNode, @NotNull String childTagName) {
    List<Element> children = getChildElementsByTagName(parentNode, childTagName);
    if (children.size() != 1) {
      throw new RuntimeException("The only child is expected, but found: " + children.size());
    }
    return children.get(0);
  }

  private static @NotNull List<Element> getChildElementsByTagName(@NotNull Node parentNode, @NotNull String childTagName) {
    List<Element> children = new ArrayList<>();
    NodeList nodeList = parentNode.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Element childElement = ObjectUtils.tryCast(nodeList.item(i), Element.class);
      if (childElement != null && childTagName.equals(childElement.getTagName())) {
        children.add(childElement);
      }
    }
    return children;
  }

  private static @NotNull JSHintOptionGroup createGroup(@NotNull Element groupElement) {
    String title = getMandatoryAttribute(groupElement, "title"); //NON-NLS
    Element descriptionElement = getTheOnlyChildElementWithTagName(groupElement, "description");
    String description = descriptionElement.getTextContent(); //NON-NLS
    Element optionsElement = getTheOnlyChildElementWithTagName(groupElement, "options");
    List<Element> optionElements = getChildElementsByTagName(optionsElement, "option");
    JSHintOptionGroup group = new JSHintOptionGroup(title, description);
    for (Element optionElement : optionElements) {
      String optionKey = getMandatoryAttribute(optionElement, "key");
      Element optionDescriptionElement = getTheOnlyChildElementWithTagName(optionElement, "description");
      @HintText String optionDescription = optionDescriptionElement.getTextContent(); //NON-NLS
      JSHintOption option = JSHintOption.findByName(optionKey);
      if (option == null) {
        throw new RuntimeException("Option not found: " + optionKey);
      }
      group.add(option, optionDescription);
    }
    return group;
  }

  private static String getMandatoryAttribute(@NotNull Element element, @NotNull String attrName) {
    String value = element.getAttribute(attrName);
    if (StringUtil.isEmpty(value)) {
      throw new RuntimeException("No '" + attrName + "' attribute!");
    }
    return value;
  }

  private static @NotNull String loadXmlContent() throws IOException {
    InputStream in = JSHintDocumentation.class.getResourceAsStream("jshint-documentation.xml");
    Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
    return FileUtil.loadTextAndClose(reader);
  }

  @Override
  public String toString() {
    return myGroups.toString();
  }

  private static class Holder {
    private static final JSHintDocumentation INSTANCE = parseFromXml();
  }
}
