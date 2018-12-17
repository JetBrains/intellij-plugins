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
package org.osmorc.frameworkintegration.impl.knopflerfish;

import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.impl.AbstractFrameworkRunner;
import org.osmorc.frameworkintegration.impl.GenericRunProperties;

import java.util.List;
import java.util.Map;

/**
 * Knopflerfish specific implementation of {@link org.osmorc.frameworkintegration.FrameworkRunner}.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class KnopflerfishRunner extends AbstractFrameworkRunner {
  static final String MAIN_CLASS = "org.knopflerfish.framework.Main";

  /**
   * See <a href="http://www.knopflerfish.org/releases/current/docs/running.html">Running Knopflerfish</a>.
   */
  @Override
  protected void setupParameters(@NotNull JavaParameters parameters) {
    ParametersList vmParameters = parameters.getVMParametersList();
    ParametersList programParameters = parameters.getProgramParametersList();

    // framework-specific options

    vmParameters.addProperty("org.knopflerfish.framework.debug.errors", "true");
    if (GenericRunProperties.isDebugMode(myAdditionalProperties)) {
      // todo: more detailed settings in the dialog (?)
      vmParameters.addProperty("org.knopflerfish.verbosity", "10");
      vmParameters.addProperty("org.knopflerfish.framework.debug.startlevel", "true");
      vmParameters.addProperty("org.knopflerfish.framework.debug.classloader", "true");
    }

    parameters.setMainClass(MAIN_CLASS);

    programParameters.add("-init");
    programParameters.add("-launch");

    // bundles and start levels

    Map<Integer, Pair<List<String>, List<String>>> bundles = collectBundles();
    for (Integer startLevel : bundles.keySet()) {
      programParameters.addAll("-initlevel", String.valueOf(startLevel));
      Pair<List<String>, List<String>> lists = bundles.get(startLevel);
      for (String bundle : lists.first) programParameters.addAll("-install", bundle);
      for (String bundle : lists.second) programParameters.addAll("-install", bundle);
    }

    for (Integer startLevel : bundles.keySet()) {
      for (String bundle : bundles.get(startLevel).first) programParameters.addAll("-start", bundle);
    }

    int frameworkStartLevel = getFrameworkStartLevel();
    programParameters.addAll("-startlevel", String.valueOf(frameworkStartLevel));
  }
}