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

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionParamdefsTest extends RubyMotionLightFixtureTestCase {
  @Override
  protected String getTestDataRelativePath() {
    return "testApp";
  }

  public void testFirstArg() {
    assertResolveToMethod("Paramdefs.foo", "app/paramdefs.rb", "'f<caret>oo'", "app/paramdefs.rb", "app/app_delegate.rb", "Rakefile");
  }

  public void testSecondArg() throws Throwable {
    myFixture.configureByFiles("app/paramdefs.rb", "app/app_delegate.rb", "Rakefile");
    assertInCompletionList("<caret>test", "withObject:", "onThread:");
  }

  public void testSecondArgResolve() {
    assertResolveToMethod("Paramdefs.foo", "app/paramdefs.rb", "action: 'f<caret>oo'", "app/paramdefs.rb", "app/app_delegate.rb", "Rakefile");
  }

  public void testThirdArg() throws Throwable {
    myFixture.configureByFiles("app/paramdefs.rb", "app/app_delegate.rb", "Rakefile");
    assertInCompletionList("<caret>test2", "withObject:");
  }
}
