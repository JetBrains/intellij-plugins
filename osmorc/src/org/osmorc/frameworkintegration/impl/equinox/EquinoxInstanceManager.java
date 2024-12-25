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
package org.osmorc.frameworkintegration.impl.equinox;

import com.intellij.util.text.VersionComparatorUtil;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.impl.AbstractFrameworkInstanceManager;
import org.osmorc.run.ui.SelectedBundle;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class EquinoxInstanceManager extends AbstractFrameworkInstanceManager {
  private static final String[] BUNDLE_DIRS = {"plugins", "bundles"};
  private static final Pattern SYSTEM_BUNDLE = Pattern.compile("org.eclipse.osgi_.*\\.jar");
  private static final Pattern SHELL_BUNDLES = Pattern.compile(".*\\.gogo\\.(command|runtime|shell)_.*\\.jar");

  @Override
  public @NotNull Collection<SelectedBundle> getFrameworkBundles(@NotNull FrameworkInstanceDefinition instance, @NotNull FrameworkBundleType type) {
    if (type == FrameworkBundleType.SHELL && VersionComparatorUtil.compare(instance.getVersion(), "3.8") < 0) {
      return Collections.singleton(new SelectedBundle(SelectedBundle.BundleType.FrameworkBundle, "Equinox built-in console", null));
    }
    else {
      return collectBundles(instance, type, BUNDLE_DIRS, SYSTEM_BUNDLE, EquinoxRunner.MAIN_CLASS, 1, SHELL_BUNDLES, null, 3);
    }
  }
}