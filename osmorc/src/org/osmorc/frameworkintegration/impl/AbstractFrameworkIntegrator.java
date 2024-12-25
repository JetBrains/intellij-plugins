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
package org.osmorc.frameworkintegration.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.FrameworkInstanceManager;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.run.OsgiRunConfigurationChecker;
import org.osmorc.run.OsgiRunConfigurationCheckerProvider;
import org.osmorc.run.ui.FrameworkRunPropertiesEditor;

/**
 * Abstract base class for framework integrators, for avoiding duplicated code.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public abstract class AbstractFrameworkIntegrator implements FrameworkIntegrator, OsgiRunConfigurationCheckerProvider {
  private final FrameworkInstanceManager myInstanceManager;
  private final OsgiRunConfigurationChecker myRunConfigurationChecker;

  protected AbstractFrameworkIntegrator(@NotNull FrameworkInstanceManager manager) {
    this(manager, new DefaultOsgiRunConfigurationChecker());
  }

  protected AbstractFrameworkIntegrator(@NotNull FrameworkInstanceManager manager, @NotNull OsgiRunConfigurationChecker checker) {
    myInstanceManager = manager;
    myRunConfigurationChecker = checker;
  }

  @Override
  public @NotNull FrameworkInstanceManager getFrameworkInstanceManager() {
    return myInstanceManager;
  }

  @Override
  public @Nullable FrameworkRunPropertiesEditor createRunPropertiesEditor() {
    return null;
  }

  @Override
  public OsgiRunConfigurationChecker getOsgiRunConfigurationChecker() {
    return myRunConfigurationChecker;
  }

  @Override
  public String toString() {
    return getDisplayName();
  }
}
