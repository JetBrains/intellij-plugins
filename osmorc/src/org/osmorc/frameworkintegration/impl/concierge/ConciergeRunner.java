/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.frameworkintegration.impl.concierge;

import com.intellij.execution.configurations.JavaParameters;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.impl.AbstractFrameworkRunner;
import org.osmorc.frameworkintegration.impl.GenericRunProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Concierge specific implementation of {@link AbstractFrameworkRunner}.
 *
 * @author <a href="mailto:al@chilibi.org">Alain Greppin</a>
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class ConciergeRunner extends AbstractFrameworkRunner {
  static final String MAIN_CLASS = "org.eclipse.concierge.Concierge";

  private File myArgFile;

  /**
   * @see <a href="https://www.eclipse.org/concierge/documentation.php">Eclipse Concierge Documentation</a>.
   */
  @Override
  protected void setupParameters(@NotNull JavaParameters parameters) {
    myArgFile = new File(myWorkingDir, "concierge" + new Random().nextInt(Integer.MAX_VALUE) + ".xargs");

    try (PrintWriter writer = new PrintWriter(new FileWriter(myArgFile))) {
      // bundles and start levels

      Map<Integer, Pair<List<String>, List<String>>> bundles = collectBundles();
      for (Integer startLevel : bundles.keySet()) {
        writer.println("-initlevel " + startLevel);
        Pair<List<String>, List<String>> lists = bundles.get(startLevel);
        for (String bundle : lists.first) writer.println("-istart " + bundle);
        for (String bundle : lists.second) writer.println("-install " + bundle);
      }

      writer.println("-Dorg.osgi.framework.startlevel.beginning=" + getFrameworkStartLevel());

      // framework-specific options

      writer.println("-Dorg.osgi.framework.storage.clean=onFirstInit");

      if (GenericRunProperties.isDebugMode(myAdditionalProperties)) {
        writer.println("-Dorg.eclipse.concierge.debug=true");
      }
    }
    catch (IOException e) {
      FileUtil.delete(myArgFile);
      throw new ConfigurationException("Cannot create .xargs file " + myArgFile, e);
    }

    parameters.setMainClass(MAIN_CLASS);
    parameters.getProgramParametersList().add(myArgFile.getPath());
  }

  @Override
  public @Nullable File getArgumentFile() {
    return myArgFile;
  }
}