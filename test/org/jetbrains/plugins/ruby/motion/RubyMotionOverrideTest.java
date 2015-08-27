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

  public void testNoParams() throws Exception {
    myFixture.configureByFiles("app/test_controller.rb", "app/app_delegate.rb", "Rakefile");
    doTest("viewDidLoad", "def viewDidLoad\n" +
                          "  super\n" +
                          "end");
  }

  public void testOneParam() throws Exception {
    myFixture.configureByFiles("app/test_controller.rb", "app/app_delegate.rb", "Rakefile");
    doTest("addChildViewController:", "def addChildViewController(childController)\n" +
                                      "  super\n" +
                                      "end");
  }

  public void testNamedParam() throws Exception {
    myFixture.configureByFiles("app/test_controller.rb", "app/app_delegate.rb", "Rakefile");
    doTest("setToolbarItems:animated:", "def setToolbarItems(toolbarItems, animated:animated)\n" +
                                        "  super\n" +
                                        "end");
  }


  private void doTest(@NotNull final String name, @NotNull final String result) throws Exception {
    final Symbol controller = SymbolUtil.findSymbol(getProject(), Type.CLASS, "TestController", null);
    assertNotNull(controller);
    final List<ClassMember> list = RubyOverrideHandler.createOverrideMembers(controller, myFixture.getFile());
    final StringBuilder namesInClass = new StringBuilder();
    for (ClassMember classMember : list) {
      MemberChooserObjectBase methodMember = (MemberChooserObjectBase)classMember;
      if (name.equals(methodMember.getText())) {
        final PsiElement element = OverriddenMethodGenerator.generate(classMember, LanguageLevel.RUBY19);
        assertNotNull(element);
        namesInClass.append(element.getText()).append("\n");
      }
    }
    assertEquals(result, namesInClass.toString().trim());
  }
}
