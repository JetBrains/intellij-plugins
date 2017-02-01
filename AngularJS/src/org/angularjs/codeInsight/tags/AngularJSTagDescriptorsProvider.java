package org.angularjs.codeInsight.tags;

import com.intellij.codeInsight.completion.XmlTagInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.openapi.project.Project;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.XmlTagNameProvider;
import com.intellij.xml.impl.schema.AnyXmlElementDescriptor;
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

  @Override
  public void addTagNameVariants(final List<LookupElement> elements, @NotNull XmlTag xmlTag, String prefix) {
    if (!(xmlTag instanceof HtmlTag && AngularIndexUtil.hasAngularJS(xmlTag.getProject()))) return;

    final Project project = xmlTag.getProject();
    DirectiveUtil.processTagDirectives(project, directive -> {
      addLookupItem(elements, directive);
      return true;
    });
    if (AngularIndexUtil.hasAngularJS2(project)) {
      addLookupItem(elements, createContainerDirective(xmlTag));
    }
  }

  private static void addLookupItem(List<LookupElement> elements, JSImplicitElement directive) {
    elements.add(LookupElementBuilder.create(directive).
      withInsertHandler(XmlTagInsertHandler.INSTANCE).
      withIcon(AngularJSIcons.Angular2));
  }

  @Nullable
  @Override
  public XmlElementDescriptor getDescriptor(XmlTag xmlTag) {
    final Project project = xmlTag.getProject();
    if (!(xmlTag instanceof HtmlTag && AngularIndexUtil.hasAngularJS(project))) return null;

    final String directiveName = DirectiveUtil.normalizeAttributeName(xmlTag.getName());
    final XmlNSDescriptor nsDescriptor = xmlTag.getNSDescriptor(xmlTag.getNamespace(), false);
    final XmlElementDescriptor descriptor = nsDescriptor != null ? nsDescriptor.getElementDescriptor(xmlTag) : null;
    if (descriptor != null && !(descriptor instanceof AnyXmlElementDescriptor)) {
      return null;
    }
    if (NG_CONTAINER.equals(directiveName) && AngularIndexUtil.hasAngularJS2(project)) {
      return new AngularJSTagDescriptor(NG_CONTAINER, createContainerDirective(xmlTag));
    }

    final JSImplicitElement directive = DirectiveUtil.getTagDirective(directiveName, project);

    return directive != null ? new AngularJSTagDescriptor(directiveName, directive) : null;
  }

  @NotNull
  private static JSImplicitElementImpl createContainerDirective(XmlTag xmlTag) {
    return new JSImplicitElementImpl.Builder(NG_CONTAINER, xmlTag).setTypeString("E;;;").toImplicitElement();
  }
}
