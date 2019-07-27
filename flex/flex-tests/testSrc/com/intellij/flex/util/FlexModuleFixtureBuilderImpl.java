package com.intellij.flex.util;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.ModuleFixture;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.testFramework.fixtures.impl.ModuleFixtureBuilderImpl;
import com.intellij.testFramework.fixtures.impl.ModuleFixtureImpl;
import org.jetbrains.annotations.NotNull;

public class FlexModuleFixtureBuilderImpl extends ModuleFixtureBuilderImpl<ModuleFixture> implements FlexModuleFixtureBuilder {

  public FlexModuleFixtureBuilderImpl(TestFixtureBuilder<? extends IdeaProjectTestFixture> fixtureBuilder) {
    super(new FlexModuleType(), fixtureBuilder);
  }

  @NotNull
  @Override
  protected ModuleFixture instantiateFixture() {
    return new ModuleFixtureImpl(this);
  }
}
