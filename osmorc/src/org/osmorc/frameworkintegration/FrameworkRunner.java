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
package org.osmorc.frameworkintegration;

import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.util.PropertiesWrapper;
import org.osmorc.run.ui.SelectedBundle;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * This interface encapsulates framework-specific runtime configuration.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 * @version $Id$
 */
public interface FrameworkRunner<Props extends PropertiesWrapper> extends Disposable
{


  /**
   * Returns an array of command line parameters that can be used to install and run the specified bundles.
   *
   * @param bundlesToInstall     an array containing the URLs of the bundles to be installed. The bundles must be sorted
   *                             in ascending order by their start level.
   * @param additionalProperties additional runner properties
   * @return a list of command line parameters
   */
  @NotNull
  public String[] getCommandlineParameters(@NotNull SelectedBundle[] bundlesToInstall,
                                           @NotNull Props additionalProperties);


  /**
   * Returns a map of system properties to be set in order to install and run the specified bundles.
   *
   * @param bundlesToInstall     an array containing the URLs of the bundles to be installed. The bundles must be sorted
   *                             in ascending order by their start level.
   * @param additionalProperties additonal runner properties
   * @return a map of system properties
   */
  @NotNull
  public Map<String, String> getSystemProperties(@NotNull SelectedBundle[] bundlesToInstall,
                                                 @NotNull Props additionalProperties);


  /**
   * Instructs the FrameworkRunnner to run any custom installation steps that are required for installing the given
   * bundles.
   *
   * @param bundlesToInstall     an array containing the URLs of the bundles to be installed. The bundles must be sorted
   *                             in ascending order by their start level.
   * @param additionalProperties additional runner properties
   */
  public void runCustomInstallationSteps(@NotNull SelectedBundle[] bundlesToInstall,
                                         @NotNull Props additionalProperties);


  /**
   * Returns true, if the framework supports the notion of exploded (not packaged to a jar file) bundles.
   *
   * @return true if the framework supports exploded bundles, false otherwise.
   */
  public boolean supportsExplodedBundles();


  /**
   * @return the main class of the framework to run.
   */
  @NotNull
  public String getMainClass();

  /**
   * A pattern tested against all framework bundle jars to collect all jars that need to be put into the classpath in order
   * to start a framework.
   *
   * @return The pattern matching all needed jars for running of a framework instance.
   */
  public Pattern getFrameworkStarterClasspathPattern();

  /**
   * @return the working directory in which the framework should be run.
   */
  @NotNull
  public String getWorkingDirectory();

  @NotNull
  public Props convertProperties(Map<String, String> properties);
}
