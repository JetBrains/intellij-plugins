/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.motion;

import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.MemberChooserObjectBase;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.actions.override.RubyOverrideHandler;
import org.jetbrains.plugins.ruby.ruby.codeInsight.OverriddenMethodGenerator;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.sdk.LanguageLevel;

import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionOverrideTest extends RubyMotionLightFixtureTestCase {
  @Override
  protected String getTestDataRelativePath() {
    return "testApp";
  }

  public void testNoParams() {
    myFixture.configureByFiles("app/test_controller.rb", "app/app_delegate.rb", "Rakefile");
    doTest("viewDidLoad", "def viewDidLoad\n" +
                          "  super\n" +
                          "end");
  }

  public void testOneParam() {
    myFixture.configureByFiles("app/test_controller.rb", "app/app_delegate.rb", "Rakefile");
    doTest("addChildViewController:", "def addChildViewController(childController)\n" +
                                      "  super\n" +
                                      "end");
  }

  public void testNamedParam() {
    myFixture.configureByFiles("app/test_controller.rb", "app/app_delegate.rb", "Rakefile");
    doTest("setToolbarItems:animated:", "def setToolbarItems(toolbarItems, animated:animated)\n" +
                                        "  super\n" +
                                        "end");
  }


  private void doTest(@NotNull final String name, @NotNull final String result) {
    final Symbol controller = SymbolUtil.findConstantByFQN(getProject(), Type.CLASS, "TestController", null);
    assertNotNull(controller);
    final List<ClassMember> list = RubyOverrideHandler.createOverrideMembers(controller, myFixture.getFile());
    final StringBuilder namesInClass = new StringBuilder();
    for (ClassMember classMember : list) {
      MemberChooserObjectBase methodMember = (MemberChooserObjectBase)classMember;
      if (name.equals(methodMember.getText())) {
        final PsiElement element = OverriddenMethodGenerator.generate(classMember, LanguageLevel.DEFAULT);
        assertNotNull(element);
        namesInClass.append(element.getText()).append("\n");
      }
    }
    assertEquals(result, namesInClass.toString().trim());
  }
}
