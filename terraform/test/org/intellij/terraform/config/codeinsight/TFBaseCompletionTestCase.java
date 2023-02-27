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
