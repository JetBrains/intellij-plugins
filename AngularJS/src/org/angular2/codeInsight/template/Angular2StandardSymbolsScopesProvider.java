// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.template;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitParameterStructure;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.SmartList;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.angular2.lang.html.psi.Angular2HtmlEvent;
import org.angular2.lang.types.Angular2EventType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Angular2StandardSymbolsScopesProvider extends Angular2TemplateScopesProvider {

  @NonNls public static final String $ANY = "$any";
  @NonNls public static final String $EVENT = "$event";

  @Override
  public @NotNull List<? extends Angular2TemplateScope> getScopes(@NotNull PsiElement element, @Nullable PsiElement hostElement) {
    SmartList<Angular2TemplateScope> result = new SmartList<>(new Angular2$AnyScope(element.getContainingFile()));
    if (hostElement != null) {
      PsiElement attribute = hostElement;
      while (attribute != null
             && !(attribute instanceof XmlAttribute)
             && !(attribute instanceof XmlTag)) {
        attribute = attribute.getParent();
      }
      if (attribute instanceof XmlAttribute) {
        Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(
          ((XmlAttribute)attribute).getName(), ((XmlAttribute)attribute).getParent());
        if (info.type == Angular2AttributeType.EVENT) {
          result.add(new Angular2EventScope(((XmlAttribute)attribute)));
        }
      }
    }
    else if (element instanceof JSElement
             || element.getParent() instanceof JSElement) {
      PsiElement attribute = element;
      while (attribute != null
             && !(attribute instanceof XmlAttribute)
             && !(attribute instanceof XmlTag)) {
        attribute = attribute.getParent();
      }
      if (attribute instanceof Angular2HtmlEvent) {
        result.add(new Angular2EventScope((Angular2HtmlEvent)attribute));
      }
    }
    return result;
  }

  @Override
  public boolean isImplicitReferenceExpression(JSReferenceExpression expression) {
    var value = expression.getText();
    return $ANY.equals(value) || $EVENT.equals(value);
  }

  private static final class Angular2$AnyScope extends Angular2TemplateScope {

    private final JSImplicitElement $any;

    private Angular2$AnyScope(@NotNull PsiElement context) {
      super(null);
      $any = new JSImplicitElementImpl.Builder($ANY, context)
        .setJSType(JSAnyType.get(context, true))
        .setParameters(Collections.singletonList(
          new JSImplicitParameterStructure("arg", "*", false, false, true)
        ))
        .setType(JSImplicitElement.Type.Function)
        .toImplicitElement();
    }

    @Override
    public void resolve(@NotNull Consumer<? super ResolveResult> consumer) {
      consumer.accept(new JSResolveResult($any));
    }
  }

  private static final class Angular2EventScope extends Angular2TemplateScope {

    private final XmlAttribute myEvent;

    private Angular2EventScope(@NotNull Angular2HtmlEvent event) {
      super(null);
      myEvent = event;
    }

    private Angular2EventScope(@NotNull XmlAttribute event) {
      super(null);
      myEvent = event;
    }

    @Override
    public void resolve(@NotNull Consumer<? super ResolveResult> consumer) {
      consumer.accept(new JSResolveResult(new Angular2EventImplicitElement(myEvent)));
    }
  }

  private static final class Angular2EventImplicitElement extends JSLocalImplicitElementImpl {
    private final @Nullable Collection<PsiElement> myDeclarations;

    private Angular2EventImplicitElement(@NotNull XmlAttribute attribute) {
      super($EVENT, new Angular2EventType(attribute), attribute, JSImplicitElement.Type.Variable);
      XmlAttributeDescriptor descriptor = attribute.getDescriptor();
      myDeclarations = descriptor != null ? descriptor.getDeclarations() : Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Angular2EventImplicitElement element = (Angular2EventImplicitElement)o;
      if (!myName.equals(element.myName)) return false;
      if (!Objects.equals(myDeclarations, element.myDeclarations)) return false;
      if (!Objects.equals(myProvider, element.myProvider)) return false;
      if (myKind != element.myKind) return false;
      return true;
    }

    @Override
    public int hashCode() {
      return Objects.hash(getClass(), myDeclarations, myName, myProvider, myKind);
    }
  }
}
