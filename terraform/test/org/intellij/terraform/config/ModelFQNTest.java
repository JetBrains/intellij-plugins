// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightPlatformTestCase;
import org.intellij.terraform.hcl.navigation.HCLQualifiedNameProvider;
import org.intellij.terraform.hcl.psi.HCLBlock;
import org.intellij.terraform.hcl.psi.HCLElementGenerator;
import org.intellij.terraform.hcl.psi.HCLObject;
import org.intellij.terraform.config.psi.TerraformElementGenerator;

public class ModelFQNTest extends LightPlatformTestCase {
  private HCLElementGenerator myElementGenerator;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myElementGenerator = new TerraformElementGenerator(getProject());
  }

  public void testResourceDefinitionBlock() throws Exception {
    HCLBlock block = (HCLBlock) myElementGenerator.createDummyFile("resource 'A' 'B' {}").getFirstChild();
    checkFQN("resource.A", block);
  }

  public void testDataSourceDefinitionBlock() throws Exception {
    HCLBlock block = (HCLBlock) myElementGenerator.createDummyFile("data 'A' 'B' {}").getFirstChild();
    checkFQN("data.A", block);
  }

  public void testProviderDefinitionBlock() throws Exception {
    HCLBlock block = (HCLBlock) myElementGenerator.createDummyFile("provider 'X' {}").getFirstChild();
    checkFQN("provider.X", block);
  }

  public void testDataSourceInnerBlock() throws Exception {
    PsiFile file = myElementGenerator.createDummyFile("""
                                                        data "consul_keys" "demo" {
                                                          var {
                                                          }
                                                        }
                                                        """);
    HCLBlock[] blocks = PsiTreeUtil.getChildrenOfType(file, HCLBlock.class);
    assertNotNull(blocks);
    HCLObject object = blocks[0].getObject();
    assertNotNull(object);
    HCLBlock vars = PsiTreeUtil.getChildOfType(object, HCLBlock.class);
    assertNotNull(vars);
    checkFQN("data.consul_keys.var", vars);
  }

  private void checkFQN(String expected, PsiElement element) {
    String fqn = HCLQualifiedNameProvider.Companion.getQualifiedModelName(element);
    assertEquals(expected, fqn);
  }
}
