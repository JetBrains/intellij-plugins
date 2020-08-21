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

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.run.ui.FrameworkRunPropertiesEditor;

/**
 * The FrameworkIntegrator is an integrating class that provides access to all framework specific components.
 *
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public interface FrameworkIntegrator {
  ExtensionPointName<FrameworkIntegrator> EP_NAME = ExtensionPointName.create("Osmorc.frameworkIntegrator");

  /**
   * Returns the display name of the framework.
   */
  @NotNull @NlsSafe String getDisplayName();

  /**
   * Creates the {@link FrameworkInstanceManager} that controls data about a specific framework instance.
   */
  @NotNull FrameworkInstanceManager getFrameworkInstanceManager();

  /**
   * Creates the framework runner which is used to get runtime specific information about a framework.
   */
  @NotNull FrameworkRunner createFrameworkRunner();

  /**
   * Creates an editor for framework specific run properties.
   */
  @Nullable FrameworkRunPropertiesEditor createRunPropertiesEditor();
}
