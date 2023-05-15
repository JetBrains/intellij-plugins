// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring;

import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.intellij.terraform.hcl.psi.HCLBlock;
import org.intellij.terraform.config.psi.TerraformElementGenerator;
import org.intellij.terraform.hil.psi.ILElementGenerator;

public class TerraformRenameRefactoringTest extends BasePlatformTestCase {

  public void testNewIdentifierNames() throws Exception {
    final TerraformElementGenerator generator = new TerraformElementGenerator(getProject());
    final PsiElement element = generator.createIdentifier("origin");

    doTestNameValidness(element, "origin", true);
    doTestNameValidness(element, "with-hyphen", true);
    doTestNameValidness(element, "with_underscore", true);
    doTestNameValidness(element, "_with_leading_underscore", true);

    doTestNameValidness(element, "with.dot", false);
    doTestNameValidness(element, "with*star", false);
    doTestNameValidness(element, "0a", false);
    doTestNameValidness(element, "*", false);
  }

  public void testNewStringLiteralsNames() throws Exception {
    final TerraformElementGenerator generator = new TerraformElementGenerator(getProject());
    final PsiElement element = generator.createStringLiteral("origin", '\"');

    doTestNameValidness(element, "origin", true);
    doTestNameValidness(element, "with-hyphen", true);
    doTestNameValidness(element, "with*star", true);
    doTestNameValidness(element, "with.dot", true);
    doTestNameValidness(element, "with_underscore", true);
    doTestNameValidness(element, "_with_leading_underscore", true);

    doTestNameValidness(element, "0a", true);
    doTestNameValidness(element, "*", true);

    doTestNameValidness(element, "with\"quote", false);
    doTestNameValidness(element, "with'different'quote", true);
  }

  public void testNewILVariableNames() throws Exception {
    final ILElementGenerator generator = new ILElementGenerator(getProject());
    final PsiElement element = generator.createILVariable("origin");

    doTestNameValidness(element, "origin", true);
    doTestNameValidness(element, "with-hyphen", true);
    doTestNameValidness(element, "with*star", true);
    doTestNameValidness(element, "with_underscore", true);
    doTestNameValidness(element, "_with_leading_underscore", true);
    doTestNameValidness(element, "0a", true);

    doTestNameValidness(element, "with.dot", false);
    doTestNameValidness(element, "*", false);
  }

  public void testNewTerraformResourceNames() throws Exception {
    final TerraformElementGenerator generator = new TerraformElementGenerator(getProject());
    com.intellij.psi.PsiFile file = generator.createDummyFile("resource null_resource \"name\" {}");
    HCLBlock element = (HCLBlock) file.getFirstChild();

    doTestNameValidness(element, "origin", true);
    doTestNameValidness(element, "with-hyphen", true);
    doTestNameValidness(element, "with_underscore", true);
    doTestNameValidness(element, "_with_leading_underscore", true);

    doTestNameValidness(element, "0a", false);
    doTestNameValidness(element, "-with-leading-hyphen", false);
    doTestNameValidness(element, "with*star", false);
    doTestNameValidness(element, "with\"quote", false);
    doTestNameValidness(element, "with'quote", false);
    doTestNameValidness(element, "with.dot", false);
    doTestNameValidness(element, "*", false);
  }

  private static void doTestNameValidness(PsiElement element, String newName, boolean valid) {
    final boolean actual = RenameUtil.isValidName(element.getProject(), element, newName);
    if (actual != valid) {
      if (valid) {
        fail("Name \"" + newName + "\" expected to be valid");
      } else {
        fail("Name \"" + newName + "\" expected to be invalid");
      }
    }
  }
}
