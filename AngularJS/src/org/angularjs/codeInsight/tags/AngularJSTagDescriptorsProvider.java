package org.angularjs.codeInsight.tags;

import com.intellij.codeInsight.completion.XmlTagInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlTagNameProvider;
import org.angularjs.codeInsight.attributes.AngularJSAttributeDescriptorsProvider;
import org.angularjs.index.AngularDirectivesDocIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSTagDescriptorsProvider implements XmlElementDescriptorProvider, XmlTagNameProvider {
  @Override
  public void addTagNameVariants(List<LookupElement> elements, @NotNull XmlTag xmlTag, String prefix) {
    final Project project = xmlTag.getProject();
    final Collection<String> docDirectives = AngularIndexUtil.getAllKeys(AngularDirectivesDocIndex.INDEX_ID, project);
    for (String directiveName : docDirectives) {
      final JSNamedElementProxy directive = getTagDirective(project, directiveName);
      if (directive != null) {
        elements.add(LookupElementBuilder.create(directive).withInsertHandler(XmlTagInsertHandler.INSTANCE));
      }
    }
  }

  @Nullable
  @Override
  public XmlElementDescriptor getDescriptor(XmlTag xmlTag) {
    final String attributeName = AngularJSAttributeDescriptorsProvider.normalizeAttributeName(xmlTag.getName());
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      final JSNamedElementProxy directive = getTagDirective(project, attributeName);

      return directive != null ? new AngularJSTagDescriptor(attributeName, directive) : null;
    }
    return null;
  }

  private static JSNamedElementProxy getTagDirective(Project project, String directiveName) {
    final JSNamedElementProxy directive = AngularIndexUtil.resolve(project, AngularDirectivesDocIndex.INDEX_ID, directiveName);
    final String restrictions = directive != null ? directive.getIndexItem().getTypeString() : null;
    if (restrictions != null) {
      final String[] split = restrictions.split(";", -1);
      final String restrict = split[0];
      if (!StringUtil.isEmpty(restrict) && StringUtil.containsIgnoreCase(restrict, "E")) {
        return directive;
      }
    }
    return null;
  }
}
