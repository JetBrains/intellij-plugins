package org.angularjs.codeInsight;

import com.intellij.lang.javascript.index.AngularDirectivesIndex;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import org.angularjs.codeInsight.attributes.AngularAttributeDescriptor;
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
      AngularAttributeDescriptor desc = createDescriptor(null, "ng-" + directiveName);
      DESCRIPTORS[i] = desc;
      ATTRIBUTE_BY_NAME.put(desc.getName(), desc);
    }
  }

  @Override
  public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag xmlTag) {
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      final Map<String, XmlAttributeDescriptor> result = new LinkedHashMap<String, XmlAttributeDescriptor>();
      for (String directiveName : FileBasedIndex.getInstance().getAllKeys(AngularDirectivesIndex.INDEX_ID, project)) {
        result.put(directiveName, createDescriptor(project, directiveName));
      }
      // marker entry: if ng-model is present then angular.js file was indexed and there's no need to add all
      // predefined entries
      if (!result.containsKey("ng-model")) {
        for (String name : DIRECTIVE_NAMES) {
          result.put(name, createDescriptor(project, name));
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
      if (FileBasedIndex.getInstance().getAllKeys(AngularDirectivesIndex.INDEX_ID, project).contains(attributeName) ||
          // fallback for predefined entries
          ATTRIBUTE_BY_NAME.containsKey(attributeName)) {
        return createDescriptor(project, attributeName);
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
