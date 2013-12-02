package org.angularjs.codeInsight;

import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import org.angularjs.codeInsight.attributes.AngularAttributeDescriptor;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.angularjs.codeInsight.AngularAttributesRegistry.createDescriptor;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSAttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {
  private static final String[] DIRECTIVE_NAMES = new String[]{
    "animate",
    "app",
    "bind",
    "bind-html-unsafe",
    "bind-template",
    "change",
    "checked",
    "class",
    "class-even",
    "class-odd",
    "click",
    "cloak",
    "controller",
    "csp",
    "dblclick",
    "disabled",
    "false-value",
    "form",
    "hide",
    "href",
    "if",
    "include",
    "init",
    "keypress",
    "list",
    "minlength",
    "maxlength",
    "model",
    "mousedown",
    "mouseup",
    "mouseover",
    "mouseout",
    "mousemove",
    "mouseenter",
    "mouseleave",
    "multiple",
    "non-bindable",
    "options",
    "pattern",
    "pluralize",
    "readonly",
    "repeat",
    "required",
    "selected",
    "show",
    "src",
    "srcset",
    "submit",
    "style",
    "swipe",
    "switch",
    "switch-when",
    "switch-default",
    "transclude",
    "true-value",
    "value",
    "view"
  };
  private static final XmlAttributeDescriptor[] DESCRIPTORS = new XmlAttributeDescriptor[DIRECTIVE_NAMES.length];
  private static final Map<String, XmlAttributeDescriptor> ATTRIBUTE_BY_NAME = new HashMap<String, XmlAttributeDescriptor>();

  static {
    for (int i = 0; i < DIRECTIVE_NAMES.length; i++) {
      final String directiveName = DIRECTIVE_NAMES[i];
      AngularAttributeDescriptor desc = createDescriptor(null, "ng-" + directiveName, null, -1);
      DESCRIPTORS[i] = desc;
      ATTRIBUTE_BY_NAME.put(desc.getName(), desc);
    }
  }

  @Override
  public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag xmlTag) {
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      final Map<String, XmlAttributeDescriptor> result = new LinkedHashMap<String, XmlAttributeDescriptor>();
      for (AngularIndexUtil.Entry entry : AngularIndexUtil.collect(project, AngularJSIndexingHandler.DIRECTIVE_KEY)) {
        result.put(entry.name, createDescriptor(project, entry.name, entry.file, entry.offset));
      }
      // marker entry: if ng-model is present then angular.js file was indexed and there's no need to add all
      // predefined entries
      if (!result.containsKey("ng-model")) {
        for (String name : DIRECTIVE_NAMES) {
          result.put(name, createDescriptor(project, name, null, -1));
        }
      }
      return result.values().toArray(new XmlAttributeDescriptor[result.size()]);
    }
    return DESCRIPTORS;
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(final String attrName, XmlTag xmlTag) {
    final String attributeName = normalizeAttributeName(attrName);
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      final AngularIndexUtil.Entry resolve = AngularIndexUtil.resolve(project, AngularJSIndexingHandler.DIRECTIVE_KEY, attributeName);
      if (resolve != null) {
        return createDescriptor(project, attributeName, resolve.file, resolve.offset);
      }
      // fallback for predefined entries
      if (ATTRIBUTE_BY_NAME.containsKey(attributeName)) {
        return createDescriptor(project, attributeName, null, -1);
      }
    }
    return ATTRIBUTE_BY_NAME.get(attributeName);
  }

  private static String normalizeAttributeName(String name) {
    if (name == null) return null;
    if (name.startsWith("data-")) {
      name = name.substring(5);
    } else if (name.startsWith("x-")) {
      name = name.substring(2);
    }
    name = name.replace(':', '-');
    name = name.replace('_', '-');
    return name;
  }
}
