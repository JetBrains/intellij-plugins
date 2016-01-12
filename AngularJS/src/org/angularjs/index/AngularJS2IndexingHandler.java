package org.angularjs.index;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.index.JSCustomIndexer;
import com.intellij.lang.javascript.index.JSImplicitElementsIndex;
import com.intellij.lang.javascript.index.JSIndexContentBuilder;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES7Decorator;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AngularJS2IndexingHandler extends FrameworkIndexingHandler {
  @Override
  public void processCallExpression(JSCallExpression callExpression, @NotNull JSElementIndexingData outData) {
    final JSExpression expression = callExpression.getMethodExpression();
    if (expression instanceof JSReferenceExpression) {
      final String name = ((JSReferenceExpression)expression).getReferenceName();
      if (!isDirective(name)) return;
      addImplicitElement(callExpression, (JSElementIndexingDataImpl)outData, getSelectorName(callExpression));
    }
  }

  @Override
  public boolean shouldCreateStubForCallExpression(ASTNode node) {
    ASTNode ref = node.getFirstChildNode();
    if (ref.getElementType() == JSTokenTypes.NEW_KEYWORD) {
      ref = TreeUtil.findSibling(ref, JSElementTypes.REFERENCE_EXPRESSION);
    }
    if (ref != null){
      final ASTNode name = ref.getLastChildNode();
      if (name != null && name.getElementType() == JSTokenTypes.IDENTIFIER) {
        final String referencedName = name.getText();
        return isDirective(referencedName);
      }
    }
    return false;
  }

  @Override
  public JSElementIndexingDataImpl processDecorator(ES7Decorator decorator, JSElementIndexingDataImpl outData) {
    final String name = decorator.getName();
    if (!isDirective(name)) return outData;
    final String selectorName = getSelectorName(decorator);

    return addImplicitElement(decorator, outData, selectorName);
  }

  private static JSElementIndexingDataImpl addImplicitElement(PsiElement decorator,
                                                              JSElementIndexingDataImpl outData,
                                                              String selector) {
    if (selector == null) return outData;

    final String[] names = selector.split(",");
    for (String selectorName : names) {
      final int not = selectorName.indexOf(":");
      if (not >= 0) {
        selectorName = selectorName.substring(0, not);
      }
      if (!StringUtil.isEmpty(selectorName)) {
        final int start = selectorName.indexOf('[');
        final int end = selectorName.indexOf(']');
        if (start == 0 && end > 0 || start < 0 && end < 0) {
          if (outData == null) outData = new JSElementIndexingDataImpl();
          JSImplicitElementImpl.Builder elementBuilder;
          for (String attr : StringUtil.split(selectorName, "]", false)) {
            final String restrict = selectorName.startsWith("[") ? "A" : "E";
            elementBuilder = new JSImplicitElementImpl.Builder(attr, decorator)
              .setType(JSImplicitElement.Type.Class).setTypeString(restrict + ";template;;");
            elementBuilder.setUserString("adi");
            outData.addImplicitElement(elementBuilder.toImplicitElement());
          }
          if (end > 0) {
            elementBuilder = new JSImplicitElementImpl.Builder("*" + selectorName.substring(1, end), decorator)
              .setType(JSImplicitElement.Type.Class).setTypeString("A;;;");
            elementBuilder.setUserString("adi");
            outData.addImplicitElement(elementBuilder.toImplicitElement());
          }
        }
      }
    }
    return outData;
  }

  @Nullable
  private static String getSelectorName(PsiElement decorator) {
    final JSProperty selector = getSelector(decorator);
    final JSExpression value = selector != null ? selector.getValue() : null;
    if (value instanceof JSLiteralExpression && ((JSLiteralExpression)value).isQuotedLiteral()) {
      return StringUtil.unquoteString(value.getText());
    }
    return null;
  }

  @Nullable
  public static JSProperty getSelector(PsiElement decorator) {
    final JSArgumentList argumentList = PsiTreeUtil.getChildOfType(decorator, JSArgumentList.class);
    final JSExpression[] arguments = argumentList != null ? argumentList.getArguments() : null;
    final JSObjectLiteralExpression descriptor = ObjectUtils.tryCast(arguments != null && arguments.length > 0 ? arguments[0] : null,
                                                                     JSObjectLiteralExpression.class);
    return descriptor != null ? descriptor.findProperty("selector") : null;
  }

  public static boolean isDirective(String name) {
    return "Directive".equals(name) || "DirectiveAnnotation".equals(name) ||
           "Component".equals(name) || "ComponentAnnotation".equals(name);
  }

  @Override
  public boolean processCustomElement(@NotNull PsiElement customElement, @NotNull JSIndexContentBuilder builder) {
    for (XmlAttribute attribute : ((HtmlTag)customElement).getAttributes()) {
      final String name = attribute.getName();
      if (name.startsWith("#")) {
        final JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(name.substring(1), attribute)
          .setType(JSImplicitElement.Type.Variable);

        builder.addImplicitElement(name.substring(1), new JSImplicitElementsIndex.JSElementProxy(elementBuilder, attribute.getTextOffset() + 1));
        JSCustomIndexer.addImplicitElement(attribute, elementBuilder, builder);
      }
    }
    return true;
  }

  @Override
  public boolean canProcessCustomElement(@NotNull PsiElement element) {
    return element instanceof HtmlTag;
  }

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION;
  }
}
