// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.xml;

import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.ArrayUtilRt;
import com.intellij.xml.XmlAttributeDescriptor;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartHtmlAttributeDescriptor extends DartHtmlDescriptorBase implements XmlAttributeDescriptor {

  public DartHtmlAttributeDescriptor(@NotNull Project project,
                                     @NotNull String name,
                                     @NotNull DartServerData.DartNavigationTarget target) {
    super(project, name, target);
  }

  @Override
  public boolean isRequired() {
    return false;
  }

  @Override
  public boolean isFixed() {
    return false;
  }

  @Override
  public boolean hasIdType() {
    return false;
  }

  @Override
  public boolean hasIdRefType() {
    return false;
  }

  @Nullable
  @Override
  public String getDefaultValue() {
    return null;
  }

  @Override
  public boolean isEnumerated() {
    return false;
  }

  @Nullable
  @Override
  public String[] getEnumeratedValues() {
    return ArrayUtilRt.EMPTY_STRING_ARRAY;
  }

  @Nullable
  @Override
  public String validateValue(XmlElement context, String value) {
    return null;
  }
}
