package org.jetbrains.plugins.ruby.motion;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionParamdefsTest extends RubyMotionLightFixtureTestCase {
  @Override
  protected String getTestDataRelativePath() {
    return "testApp";
  }

  public void testFirstArg() throws Throwable {
    assertResolveToMethod("Paramdefs.foo", "app/paramdefs.rb", "'f<caret>oo'", "app/paramdefs.rb", "app/app_delegate.rb", "Rakefile");
  }

  public void testSecondArg() throws Throwable {
    myFixture.configureByFiles("app/paramdefs.rb", "app/app_delegate.rb", "Rakefile");
    assertInCompletionList("<caret>test", "withObject:", "onThread:");
  }

  public void testSecondArgResolve() throws Throwable {
    assertResolveToMethod("Paramdefs.foo", "app/paramdefs.rb", "action: 'f<caret>oo'", "app/paramdefs.rb", "app/app_delegate.rb", "Rakefile");
  }

  public void testThirdArg() throws Throwable {
    myFixture.configureByFiles("app/paramdefs.rb", "app/app_delegate.rb", "Rakefile");
    assertInCompletionList("<caret>test2", "withObject:");
  }
}
