// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.SchemaPrefix;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.URLUtil;
import com.intellij.xml.HtmlXmlExtension;
import com.intellij.xml.util.XmlUtil;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.html.Angular2HtmlFileType;
import org.angular2.lang.html.psi.Angular2HtmlBananaBoxBinding;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.angular2.lang.svg.Angular2SvgLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.List;

public class Angular2HtmlExtension extends HtmlXmlExtension {

  private static final NotNullLazyValue<String> NG_ENT_LOCATION = AtomicNotNullLazyValue.createValue(() -> {
    URL url = Angular2HtmlExtension.class.getResource("/dtd/ngChars.ent");
    return VfsUtilCore.urlToPath(VfsUtilCore.fixURLforIDEA(
      URLUtil.unescapePercentSequences(url.toExternalForm())));
  });

  @Override
  public boolean isAvailable(PsiFile file) {
    return file.getFileType() instanceof Angular2HtmlFileType
           && Angular2LangUtil.isAngular2Context(file);
  }

  @Override
  public boolean isSelfClosingTagAllowed(@NotNull XmlTag tag) {
    return tag.getLanguage().is(Angular2SvgLanguage.INSTANCE)
           || super.isSelfClosingTagAllowed(tag);
  }

  @Override
  public boolean isRequiredAttributeImplicitlyPresent(XmlTag tag, String attrName) {
    Ref<Boolean> result = new Ref<>();
    tag.acceptChildren(new Angular2HtmlElementVisitor() {
      @Override
      public void visitPropertyBinding(Angular2HtmlPropertyBinding propertyBinding) {
        checkBinding(propertyBinding.getBindingType(), propertyBinding.getPropertyName());
      }

      @Override
      public void visitBananaBoxBinding(Angular2HtmlBananaBoxBinding bananaBoxBinding) {
        checkBinding(bananaBoxBinding.getBindingType(), bananaBoxBinding.getPropertyName());
      }

      private void checkBinding(PropertyBindingType type,
                                String name) {
        switch (type) {
          case PROPERTY:
          case ATTRIBUTE:
            if (attrName.equals(name)) {
              result.set(Boolean.TRUE);
            }
          default:
        }
      }
    });
    if (!result.isNull()) {
      return result.get();
    }
    return super.isRequiredAttributeImplicitlyPresent(tag, attrName);
  }

  @Override
  public @NotNull List<@NotNull XmlFile> getCharEntitiesDTDs(@NotNull XmlFile file) {
    List<XmlFile> result = new SmartList<>(super.getCharEntitiesDTDs(file));
    ContainerUtil.addAllNotNull(result, XmlUtil.findXmlFile(file, NG_ENT_LOCATION.getValue()));
    return result;
  }

  @Override
  public SchemaPrefix getPrefixDeclaration(XmlTag context, String namespacePrefix) {
    if (namespacePrefix != null && (namespacePrefix.startsWith("(") || namespacePrefix.startsWith("["))) {
      SchemaPrefix attribute = findAttributeSchema(context, namespacePrefix);
      if (attribute != null) return attribute;
    }
    return super.getPrefixDeclaration(context, namespacePrefix);
  }

  private static @Nullable SchemaPrefix findAttributeSchema(XmlTag context, String namespacePrefix) {
    for (XmlAttribute attribute : context.getAttributes()) {
      if (attribute.getName().startsWith(namespacePrefix)) {
        return new SchemaPrefix(attribute, TextRange.create(1, namespacePrefix.length()), namespacePrefix.substring(1));
      }
    }
    return null;
  }
}
