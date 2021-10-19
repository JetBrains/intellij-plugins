// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.javascript.web.WebFramework;
import com.intellij.javascript.web.codeInsight.html.WebSymbolsXmlExtension;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.URLUtil;
import com.intellij.xml.util.XmlUtil;
import org.angular2.Angular2Framework;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.html.psi.Angular2HtmlBananaBoxBinding;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.angular2.lang.svg.Angular2SvgLanguage;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;

public final class Angular2HtmlExtension extends WebSymbolsXmlExtension {
  private static final NotNullLazyValue<String> NG_ENT_LOCATION = NotNullLazyValue.lazy(() -> {
    URL url = Angular2HtmlExtension.class.getResource("/dtd/ngChars.ent");
    return VfsUtilCore.urlToPath(VfsUtilCore.fixURLforIDEA(
      URLUtil.unescapePercentSequences(url.toExternalForm())));
  });

  @Override
  public boolean isAvailable(PsiFile file) {
    return file != null
           && WebFramework.forFileType(file.getFileType()) == Angular2Framework.getInstance()
           && Angular2LangUtil.isAngular2Context(file);
  }

  @Override
  public boolean isSelfClosingTagAllowed(@NotNull XmlTag tag) {
    return tag.getLanguage().is(Angular2SvgLanguage.INSTANCE)
           || super.isSelfClosingTagAllowed(tag);
  }

  @Override
  public boolean isRequiredAttributeImplicitlyPresent(XmlTag tag, String attrName) {
    if (tag == null || attrName == null) return false;
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
}
