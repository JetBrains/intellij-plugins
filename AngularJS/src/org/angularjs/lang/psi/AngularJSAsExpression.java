package org.angularjs.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.impl.JSBinaryExpressionImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import org.angularjs.codeInsight.attributes.AngularAttributesRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSAsExpression extends JSBinaryExpressionImpl {
  public AngularJSAsExpression(ASTNode node) {
    super(node);
  }

  @Nullable
  public JSDefinitionExpression getDefinition() {
    return PsiTreeUtil.getChildOfType(this, JSDefinitionExpression.class);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AngularJSElementVisitor) {
      ((AngularJSElementVisitor)visitor).visitAngularJSAsExpression(this);
    } else {
      super.accept(visitor);
    }
  }

  public static boolean isAsControllerRef(PsiReference ref, PsiElement parent) {
    if (parent instanceof AngularJSAsExpression && ref == parent.getFirstChild()) {
      return true;
    }
    final InjectedLanguageManager injector = InjectedLanguageManager.getInstance(parent.getProject());
    final PsiLanguageInjectionHost host = injector.getInjectionHost(parent);
    final PsiElement hostParent = host instanceof XmlAttributeValueImpl ? host.getParent() : null;
    return hostParent instanceof XmlAttribute &&
           AngularAttributesRegistry.isAngularAttribute((XmlAttribute)hostParent, "ng-controller");
  }
}
