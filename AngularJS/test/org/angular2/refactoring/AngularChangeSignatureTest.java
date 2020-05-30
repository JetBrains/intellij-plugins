// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring;

import com.intellij.lang.javascript.JSChangeSignatureTestBase;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.refactoring.changeSignature.JSParameterInfo;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.refactoring.changeSignature.ParameterInfo.NEW_PARAMETER;

public class AngularChangeSignatureTest extends JSChangeSignatureTestBase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  @Override
  protected String[] getActiveFileNames() {
    return new String[]{"component.ts"};
  }

  @NotNull
  @Override
  protected String getTestRoot() {
    return "changeSignature/";
  }


  public void testAddParamAndRenameAndSetReturnType() {
    doTest("bar", JSAttributeList.AccessType.PUBLIC, "number",
           new JSParameterInfo("stringParam", "string", "", "\"def\"", NEW_PARAMETER));
  }
}
