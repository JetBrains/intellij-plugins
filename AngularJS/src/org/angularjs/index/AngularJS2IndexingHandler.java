package org.angularjs.index;

import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES7Decorator;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.Nullable;

public class AngularJS2IndexingHandler extends FrameworkIndexingHandler {
  @Override
  public JSElementIndexingDataImpl processDecorator(ES7Decorator decorator, JSElementIndexingDataImpl outData) {
    final String name = decorator.getName();
    final String restrict = computeRestrictions(name);
    final String selectorName = getSelectorName(decorator);

    if (restrict != null && !StringUtil.isEmpty(selectorName)) {
      if (outData == null) outData = new JSElementIndexingDataImpl();

      JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(selectorName, decorator)
        .setType(JSImplicitElement.Type.Class).setTypeString(restrict + ";;;");
      elementBuilder.setUserString("adi");
      final JSImplicitElementImpl implicitElement = elementBuilder.toImplicitElement();
      outData.addImplicitElement(implicitElement);
    }
    return outData;
  }

  @Nullable
  private static String getSelectorName(ES7Decorator decorator) {
    final JSArgumentList argumentList = PsiTreeUtil.getChildOfType(decorator, JSArgumentList.class);
    final JSExpression[] arguments = argumentList != null ? argumentList.getArguments() : null;
    final JSObjectLiteralExpression descriptor = ObjectUtils.tryCast(arguments != null && arguments.length > 0 ? arguments[0] : null,
                                                                     JSObjectLiteralExpression.class);
    final JSProperty selector = descriptor != null ? descriptor.findProperty("selector") : null;
    final JSExpression value = selector != null ? selector.getValue() : null;
    if (value instanceof JSLiteralExpression && ((JSLiteralExpression)value).isQuotedLiteral()) {
      final String selectorFormat = StringUtil.unquoteString(value.getText());
      final int start = selectorFormat.indexOf('[');
      final int end = selectorFormat.indexOf(']');
      if (start == 0 && end > 0) {
        return selectorFormat.substring(start + 1, end);
      }
    }
    return null;
  }

  @Nullable
  private static String computeRestrictions(String name) {
    return "Directive".equals(name) ? "A" :
           "Component".equals(name) ? "E" :
           null;
  }

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION;
  }
}
