package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.util.NotNullFunction;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularEventHandlerDescriptor extends AngularAttributeDescriptor {
  public static final String OUTPUT = "Output";
  public static final NotNullFunction<Pair<PsiElement, String>, XmlAttributeDescriptor> FACTORY = AngularEventHandlerDescriptor::createEventHandler;

  private final PsiElement myElement;

  public AngularEventHandlerDescriptor(PsiElement element,
                                  String attributeName) {
    super(element.getProject(), attributeName, null);
    myElement = element;
  }

  @Override
  public PsiElement getDeclaration() {
    return myElement;
  }

  public static XmlAttributeDescriptor[] getEventHandlerDescriptors(JSImplicitElement declaration) {
    return getFieldBasedDescriptors(declaration, OUTPUT, FACTORY);
  }

  @NotNull
  private static AngularEventHandlerDescriptor createEventHandler(Pair<PsiElement, String> dom) {
    return new AngularEventHandlerDescriptor(dom.first, "(" + dom.second + ")");
  }

  @Nullable
  @Override
  public String handleTargetRename(@NotNull @NonNls String newTargetName) {
    return "(" + newTargetName + ")";
  }
}
