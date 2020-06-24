// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiPresentableMetaData;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.ArrayUtilRt;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import icons.AngularJSIcons;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Dennis.Ushakov
 */
public class AngularAttributeDescriptor extends BasicXmlAttributeDescriptor implements XmlAttributeDescriptorEx, PsiPresentableMetaData {
  protected final Project myProject;
  protected final PsiElement myElement;
  private final String myAttributeName;
  private final StubIndexKey<String, JSImplicitElementProvider> myIndex;


  public AngularAttributeDescriptor(@Nullable Project project,
                                    @NotNull String attributeName,
                                    @Nullable StubIndexKey<String, JSImplicitElementProvider> index,
                                    @Nullable PsiElement element) {
    myProject = project;
    myAttributeName = attributeName;
    myIndex = index;
    myElement = element;
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
    if (myProject == null || myIndex == null) return ArrayUtilRt.EMPTY_STRING_ARRAY;
    return ArrayUtilRt.toStringArray(AngularIndexUtil.getAllKeys(myIndex, myProject));
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
    return myElement;
  }

  @Override
  public @Nullable String handleTargetRename(@NotNull @NonNls String newTargetName) {
    return newTargetName;
  }

  @Override
  public String getTypeName() {
    return null;
  }

  @Override
  public @Nullable Icon getIcon() {
    return AngularJSIcons.Angular2;
  }
}
