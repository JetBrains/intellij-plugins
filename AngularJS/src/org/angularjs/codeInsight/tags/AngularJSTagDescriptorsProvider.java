package org.angularjs.codeInsight.tags;

import com.intellij.codeInsight.completion.XmlTagInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
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
  private static final String NG_CONTAINER = "ng-container";
  private static final String NG_CONTENT = "ng-content";
  private static final String NG_TEMPLATE = "ng-template";

  @Override
  public void addTagNameVariants(final List<LookupElement> elements, @NotNull XmlTag xmlTag, String prefix) {
    if (!(xmlTag instanceof HtmlTag && AngularIndexUtil.hasAngularJS(xmlTag.getProject()))) return;

    final Project project = xmlTag.getProject();
    Language language = xmlTag.getContainingFile().getLanguage();
    DirectiveUtil.processTagDirectives(project, directive -> {
      addLookupItem(language, elements, directive);
      return true;
    });
    if (AngularIndexUtil.hasAngularJS2(project)) {
      addLookupItem(language, elements, createDirective(xmlTag, NG_CONTAINER));
      addLookupItem(language, elements, createDirective(xmlTag, NG_CONTENT));
      addLookupItem(language, elements, createDirective(xmlTag, NG_TEMPLATE));
    }
  }

  private static void addLookupItem(Language language, List<LookupElement> elements, JSImplicitElement directive) {
    LookupElementBuilder element = LookupElementBuilder.create(directive).
      withIcon(AngularJSIcons.Angular2);
    if (language.isKindOf(XMLLanguage.INSTANCE)) {
      element = element.withInsertHandler(XmlTagInsertHandler.INSTANCE);
    }
    elements.add(element);
  }

  @Nullable
  @Override
  public XmlElementDescriptor getDescriptor(XmlTag xmlTag) {
    final Project project = xmlTag.getProject();
    if (!(xmlTag instanceof HtmlTag && AngularIndexUtil.hasAngularJS(project))) return null;

    final String tagName = xmlTag.getName();
    final String directiveName = DirectiveUtil.normalizeAttributeName(tagName);
    if (XmlUtil.isTagDefinedByNamespace(xmlTag)) return null;
    if ((NG_CONTAINER.equals(directiveName) || NG_CONTENT.equals(directiveName) || NG_TEMPLATE.equals(directiveName)) &&
        AngularIndexUtil.hasAngularJS2(project)) {
      return new AngularJSTagDescriptor(directiveName, createDirective(xmlTag, directiveName));
    }

    JSImplicitElement directive = DirectiveUtil.getTagDirective(directiveName, project);
    if (DirectiveUtil.isAngular2Directive(directive) && !directive.getName().equals(tagName)) {
      // we've found directive via normalized name for Angular, it should not work
      directive = null;
    }
    if (directive == null && !tagName.equals(directiveName) && AngularIndexUtil.hasAngularJS2(project)) {
      directive = DirectiveUtil.getTagDirective(tagName, project);
      if (!DirectiveUtil.isAngular2Directive(directive)) directive = null;
    }

    return directive != null ? new AngularJSTagDescriptor(directiveName, directive) : null;
  }

  @NotNull
  private static JSImplicitElementImpl createDirective(XmlTag xmlTag, String name) {
    return new JSImplicitElementImpl.Builder(name, xmlTag).setTypeString("E;;;").toImplicitElement();
  }
}
