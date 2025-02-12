// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.ide.actions.CopyReferenceAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightPlatformTestCase;
import org.intellij.terraform.hcl.psi.*;
import org.intellij.terraform.config.psi.TfElementGenerator;

import java.util.List;

public class CopyReferenceTest extends LightPlatformTestCase {
  private HCLElementGenerator myElementGenerator;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myElementGenerator = new TfElementGenerator(getProject());
  }

  public void testResourceDefinitionBlock() throws Exception {
    HCLBlock block = (HCLBlock) myElementGenerator.createDummyFile("resource 'A' 'B' {}").getFirstChild();
    checkFQN("A.B", block);
  }

  public void testResourceUsageInDependsOn() throws Exception {
    PsiFile file = myElementGenerator.createDummyFile("resource x y {}\nresource a b {depends_on=['x.y']}");
    HCLBlock[] blocks = PsiTreeUtil.getChildrenOfType(file, HCLBlock.class);
    assertNotNull(blocks);
    HCLObject object = blocks[1].getObject();
    assertNotNull(object);
    HCLProperty property = object.findProperty("depends_on");
    assertNotNull(property);
    HCLArray array = (HCLArray) (property.getValue());
    assertNotNull(array);
    List<HCLExpression> valueList = array.getElements();
    assertNotNull(valueList);
    HCLExpression value = valueList.get(0);
    assertNotNull(value);
    checkFQN("x.y", value);
  }

  public void testDataSourceUsageInDependsOn() throws Exception {
    PsiFile file = myElementGenerator.createDummyFile("data x y {}\nresource a b {depends_on=['data.x.y']}");
    HCLBlock[] blocks = PsiTreeUtil.getChildrenOfType(file, HCLBlock.class);
    assertNotNull(blocks);
    HCLObject object = blocks[1].getObject();
    assertNotNull(object);
    HCLProperty property = object.findProperty("depends_on");
    assertNotNull(property);
    HCLArray array = (HCLArray) (property.getValue());
    assertNotNull(array);
    List<HCLExpression> valueList = array.getElements();
    assertNotNull(valueList);
    HCLExpression value = valueList.get(0);
    assertNotNull(value);
    checkFQN("data.x.y", value);
  }

  private void checkFQN(String expected, PsiElement element) {
    String fqn = CopyReferenceAction.elementToFqn(element);
    // Emulate logic from CopyReferenceAction which uses Editor internally
    if (fqn == null) {
      PsiReference[] references = element.getReferences();
      for (PsiReference reference : references) {
        PsiElement resolve = reference.resolve();
        if (resolve != null) {
          fqn = CopyReferenceAction.elementToFqn(resolve);
          if (fqn != null) break;
        }
      }
    }
    assertEquals(expected, fqn);
  }
}
