package org.angularjs.codeInsight;

import com.intellij.lang.javascript.index.AngularJSIndex;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import gnu.trove.TObjectIntHashMap;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by denofevil on 26/11/13.
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
      AngularAttributeDescriptor desc = new AngularAttributeDescriptor("ng-" + directiveName);
      DESCRIPTORS[i] = desc;
      ATTRIBUTE_BY_NAME.put(desc.getName(), desc);
    }
  }

  @Override
  public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag xmlTag) {
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      final Map<String, XmlAttributeDescriptor> result = new LinkedHashMap<String, XmlAttributeDescriptor>();
      FileBasedIndex.getInstance().processValues(AngularJSIndex.INDEX_ID, AngularJSIndexingHandler.DIRECTIVE, null,
                                                 new FileBasedIndex.ValueProcessor<TObjectIntHashMap<String>>() {
                                                   @Override
                                                   public boolean process(VirtualFile file, TObjectIntHashMap<String> descriptorNames) {
                                                     for (Object o : descriptorNames.keys()) {
                                                       AngularAttributeDescriptor descriptor =
                                                         new AngularAttributeDescriptor(project, (String)o, file,
                                                                                        descriptorNames.get((String)o));
                                                       result.put(descriptor.getName(), descriptor);
                                                     }
                                                     return true;
                                                   }
                                                 }, GlobalSearchScope.allScope(project)
      );
      // marker entry: if ng-model is present then angular.js file was indexed and there's no need to add all
      // predefined entries
      if (!result.containsKey("ng-model")) {
        result.putAll(ATTRIBUTE_BY_NAME);
      }
      return result.values().toArray(new XmlAttributeDescriptor[result.size()]);
    }
    return DESCRIPTORS;
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(final String attrName, XmlTag xmlTag) {
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      final Ref<XmlAttributeDescriptor> result = new Ref<XmlAttributeDescriptor>();
      FileBasedIndex.getInstance().processValues(AngularJSIndex.INDEX_ID, AngularJSIndexingHandler.DIRECTIVE, null,
                                                 new FileBasedIndex.ValueProcessor<TObjectIntHashMap<String>>() {
                                                   @Override
                                                   public boolean process(VirtualFile file, TObjectIntHashMap<String> descriptorNames) {
                                                     for (Object o : descriptorNames.keys()) {
                                                       if (attrName.equals(o)) {
                                                         AngularAttributeDescriptor descriptor =
                                                           new AngularAttributeDescriptor(project, (String)o, file,
                                                                                          descriptorNames.get((String)o));
                                                         result.set(descriptor);
                                                         break;
                                                       }
                                                     }
                                                     return result.get() == null;
                                                   }
                                                 }, GlobalSearchScope.allScope(project)
      );
      if (result.get() != null) {
        return result.get();
      }
    }
    return ATTRIBUTE_BY_NAME.get(attrName);
  }
}
