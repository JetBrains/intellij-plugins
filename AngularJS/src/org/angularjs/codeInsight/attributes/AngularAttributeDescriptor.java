package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiPresentableMetaData;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.ArrayUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import icons.AngularJSIcons;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularAttributeDescriptor extends BasicXmlAttributeDescriptor implements XmlAttributeDescriptorEx, PsiPresentableMetaData {
  protected final Project myProject;
  protected final PsiElement[] myElements;
  private final String myAttributeName;
  private final StubIndexKey<String, JSImplicitElementProvider> myIndex;

  /**
   * NativeScript compatibility
   *
   * @deprecated to be removed in 2017.3
   */
  @Deprecated
  public AngularAttributeDescriptor(@Nullable Project project,
                                    @NotNull String attributeName,
                                    @Nullable StubIndexKey<String, JSImplicitElementProvider> index) {
    this(project, attributeName, index, Collections.emptyList());
  }

  public AngularAttributeDescriptor(@Nullable Project project,
                                    @NotNull String attributeName,
                                    @Nullable StubIndexKey<String, JSImplicitElementProvider> index,
                                    @NotNull List<PsiElement> elements) {
    myProject = project;
    myAttributeName = attributeName;
    myIndex = index;
    myElements = elements.toArray(PsiElement.EMPTY_ARRAY);
  }

  /**
   * Kept for compatibility with NativeScript.
   */
  @Deprecated
  @NotNull
  public static XmlAttributeDescriptor[] getFieldBasedDescriptors(JSImplicitElement declaration) {
    return Angular2AttributeDescriptor.getDescriptors(declaration).toArray(XmlAttributeDescriptor.EMPTY);
  }

  @Override
  public String getName() {
    return myAttributeName;
  }

  @Override
  public void init(PsiElement element) {}

  @Override
  public boolean isRequired() {
    return false;
  }

  @Override
  public boolean hasIdType() {
    return "id".equals(myAttributeName);
  }

  @Override
  public boolean hasIdRefType() {
    return false;
  }

  @Override
  public boolean isEnumerated() {
    return myIndex != null;
  }

  @Override
  public boolean isFixed() {
    return false;
  }

  @Override
  public String getDefaultValue() {
    return null;
  }

  @Override
  public String[] getEnumeratedValues() {
    if (myProject == null || myIndex == null) return ArrayUtil.EMPTY_STRING_ARRAY;
    return ArrayUtil.toStringArray(AngularIndexUtil.getAllKeys(myIndex, myProject));
  }

  @Override
  protected PsiElement getEnumeratedValueDeclaration(XmlElement xmlElement, String value) {
    if (myIndex != null) {
      return AngularIndexUtil.resolve(xmlElement.getProject(), myIndex, value);
    }
    return xmlElement;
  }

  @Override
  public PsiElement getDeclaration() {
    return ArrayUtil.getFirstElement(myElements);
  }

  @Nullable
  @Override
  public String handleTargetRename(@NotNull @NonNls String newTargetName) {
    return newTargetName;
  }

  @Override
  public String getTypeName() {
    return null;
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return AngularJSIcons.Angular2;
  }
}
