// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight;

import com.google.common.collect.Collections2;
import com.intellij.lang.Language;
import org.intellij.terraform.config.CompletionTestCase;
import org.intellij.terraform.config.TerraformLanguage;
import org.intellij.terraform.config.model.PropertyOrBlockType;
import org.intellij.terraform.config.model.TypeModel;

import java.util.Collection;
import java.util.TreeSet;

public abstract class TFBaseCompletionTestCase extends CompletionTestCase {
  public static final Collection<String> COMMON_RESOURCE_PROPERTIES = new TreeSet<>(Collections2.transform(Collections2.filter(TypeModel.AbstractResource.getProperties().values(), PropertyOrBlockType::getConfigurable), PropertyOrBlockType::getName));
  public static final Collection<String> COMMON_DATA_SOURCE_PROPERTIES = new TreeSet<>(Collections2.transform(Collections2.filter(TypeModel.AbstractDataSource.getProperties().values(), PropertyOrBlockType::getConfigurable), PropertyOrBlockType::getName));

  @Override
  protected String getTestDataPath() {
    return "tests/data";
  }

  @Override
  protected String getFileName() {
    return "a.tf";
  }

  @Override
  protected Language getExpectedLanguage() {
    return TerraformLanguage.INSTANCE;
  }
}
