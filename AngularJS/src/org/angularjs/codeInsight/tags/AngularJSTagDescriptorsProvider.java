package org.angularjs.codeInsight.tags;

import com.intellij.codeInsight.completion.XmlTagInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlTagNameProvider;
import com.intellij.xml.util.XmlUtil;
import icons.AngularJSIcons;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSTagDescriptorsProvider implements XmlElementDescriptorProvider, XmlTagNameProvider {

  @Override
  public void addTagNameVariants(final List<LookupElement> elements, @NotNull XmlTag xmlTag, String prefix) {
    if (!(xmlTag instanceof HtmlTag)
        || !AngularIndexUtil.hasAngularJS(xmlTag.getProject())) {
      return;
    }

    final Project project = xmlTag.getProject();
    Language language = xmlTag.getContainingFile().getLanguage();
    DirectiveUtil.processTagDirectives(project, directive -> {
      addLookupItem(language, elements, directive);
      return true;
    });
  }

  private static void addLookupItem(Language language, List<LookupElement> elements, JSImplicitElement directive) {
    LookupElementBuilder element = LookupElementBuilder.create(directive, DirectiveUtil.getAttributeName(directive.getName()))
      .withIcon(AngularJSIcons.Angular2);
    if (language.isKindOf(XMLLanguage.INSTANCE)) {
      element = element.withInsertHandler(XmlTagInsertHandler.INSTANCE);
    }
    elements.add(element);
  }

  @Override
  public @Nullable XmlElementDescriptor getDescriptor(XmlTag xmlTag) {
    final Project project = xmlTag.getProject();
    if (!(xmlTag instanceof HtmlTag)
        || XmlUtil.isTagDefinedByNamespace(xmlTag)
        || !AngularIndexUtil.hasAngularJS(project)) {
      return null;
    }

    final String tagName = xmlTag.getName();
    String directiveName = DirectiveUtil.normalizeAttributeName(tagName);

    JSImplicitElement directive = DirectiveUtil.getTagDirective(directiveName, project);
    if (DirectiveUtil.isAngular2Directive(directive)) {
      // we've found a directive for Angular 2+
      directive = null;
    }
    return directive != null ? new AngularJSTagDescriptor(tagName, directive) : null;
  }
}
