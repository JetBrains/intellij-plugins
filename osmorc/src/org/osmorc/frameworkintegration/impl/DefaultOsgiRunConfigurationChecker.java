/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package org.osmorc.frameworkintegration.impl;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.OsgiRunConfigurationChecker;

import java.io.File;

/**
 * Default implementation for the run configuration checker.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thomä</a>
 */
public class DefaultOsgiRunConfigurationChecker implements OsgiRunConfigurationChecker {
  @Override
  public final void checkConfiguration(@NotNull OsgiRunConfiguration runConfiguration) throws RuntimeConfigurationException {
    // make sure that if the user wants to re-use a runtime directory that it exists
    if (!runConfiguration.isGenerateWorkingDir()) {
      if (StringUtil.isEmptyOrSpaces(runConfiguration.getWorkingDir())) {
        throw new RuntimeConfigurationError(OsmorcBundle.message("run.configuration.working.dir.set"));
      }

      File dir = new File(runConfiguration.getWorkingDir());
      if (!dir.isDirectory()) {
        if (!dir.mkdirs()) {
          throw new RuntimeConfigurationError(OsmorcBundle.message("run.configuration.working.dir.create"));
        }
        FileUtil.delete(dir);
      }
    }

    checkFrameworkSpecifics(runConfiguration);
  }

  /**
   * Method which can be overridden by subclasses to do framework-specific configuration checks.
   * Subclasses overriding this method do not need to call the implementation of the superclass.
   */
  protected void checkFrameworkSpecifics(@NotNull OsgiRunConfiguration runConfiguration) throws RuntimeConfigurationException { }
}
