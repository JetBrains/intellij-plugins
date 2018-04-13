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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.PathUtil;
import org.jetbrains.plugins.ruby.RubyVMOptions;
import org.jetbrains.plugins.ruby.motion.facet.RubyMotionFacet;
import org.jetbrains.plugins.ruby.motion.facet.RubyMotionFacetType;
import org.jetbrains.plugins.ruby.rails.RubyLightProjectDescriptorBase;
import org.jetbrains.plugins.ruby.ruby.sdk.LanguageLevel;
import org.jetbrains.plugins.ruby.ruby.testCases.RubyFixtureTestCase;
import org.jetbrains.plugins.ruby.ruby.testCases.RubyLightFixtureTestCase;

/**
 * @author Dennis.Ushakov
 */
public abstract class RubyMotionLightFixtureTestCase extends RubyLightFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    RubyVMOptions.getInstance().forceLanguageLevel(LanguageLevel.RUBY19);
  }

  @Override
  protected void tearDown() throws Exception {
    RubyVMOptions.resetForcedLanguageLevel();
    super.tearDown();
  }

  @Override
  protected String getTestDataPath() {
    return PathUtil.getDataPath(getClass()) + "/" + getTestDataRelativePath();
  }

  @Nullable
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return new RubyMotionLightFixtureDescriptor();
  }

  protected PsiFile defaultConfigure() {
    return myFixture.configureByFiles("app/app_delegate.rb", "Rakefile")[0];
  }

  public static class RubyMotionLightFixtureDescriptor extends RubyLightProjectDescriptorBase {
    @Override
    public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
      prepareSourceRoots(contentEntry);

      final RubyMotionFacetType facetType = RubyMotionFacetType.getInstance();
      RubyFixtureTestCase.addFacetToModule(module, facetType, facetType.createDefaultConfiguration(), "RubyMotion");
      RubyMotionFacet.updateMotionLibrary(model);
    }
  }
}
