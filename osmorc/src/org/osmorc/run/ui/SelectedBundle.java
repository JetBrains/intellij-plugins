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
package org.osmorc.run.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class representing a bundle that has been selected for running.
 * This can either be some pre-jarred bundle from the classpath, or a module from the project.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public class SelectedBundle {
  public enum BundleType {
    /**
     * The selected bundle is a module of the currently open project.
     */
    Module(true),

    /**
     * The selected bundle is an existing bundle that is part of the OSGi framework (e.g. the Knopflerfish Desktop bundle).
     */
    FrameworkBundle(true),

    /**
     * The selected bundle is a library used in this project, that should be started. This is rarely used, except for
     * libraries that are meant to be used as bundles (such as Spring-DM).
     */
    StartLibrary(true),

    /**
     * The selected bundle is a plain library that should be installed only.
     */
    PlainLibrary(false);

    public final boolean autoStart;

    BundleType(boolean autoStart) {
      this.autoStart = autoStart;
    }
  }

  private final BundleType myBundleType;
  private String myDisplayName;
  private String myBundlePath;
  private int myStartLevel;
  private boolean myStartAfterInstallation;

  public SelectedBundle(@NotNull BundleType bundleType, @NotNull String displayName, @Nullable String path) {
    myBundleType = bundleType;
    myDisplayName = displayName;
    myBundlePath = path;
    myStartLevel = 1;
    myStartAfterInstallation = bundleType.autoStart;
  }

  public BundleType getBundleType() {
    return myBundleType;
  }

  public boolean isModule() {
    return myBundleType == BundleType.Module;
  }

  public @NotNull String getName() {
    return myDisplayName;
  }

  public void setName(@NotNull String displayName) {
    myDisplayName = displayName;
  }

  public @Nullable String getBundlePath() {
    return myBundlePath;
  }

  public void setBundlePath(@Nullable String path) {
    myBundlePath = path;
  }

  /**
   * Returns the start level of this bundle (default is 1).
   */
  public int getStartLevel() {
    return myStartLevel;
  }

  /**
   * Returns true if the start level of this bundle should be the default start level of the run configuration.
   */
  public boolean isDefaultStartLevel() {
    return myStartLevel == 0;
  }

  public void setStartLevel(int startLevel) {
    myStartLevel = startLevel;
  }

  public boolean isStartAfterInstallation() {
    return myStartAfterInstallation;
  }

  public void setStartAfterInstallation(boolean startAfterInstallation) {
    myStartAfterInstallation = startAfterInstallation;
  }

  /**
   * Two selected bundles are equal when they point to the same URL. When the bundles are modules, they do not necessarily
   * have to point to the same URL (as the URL might be null on modules), so in this case the display name decides.
   */
  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }

    if (o instanceof SelectedBundle other) {
      return isModule() ? isEqual(myDisplayName, other.myDisplayName) : isEqual(myBundlePath, other.myBundlePath);
    }

    return false;
  }

  private static boolean isEqual(Object o1, Object o2) {
    return o1 == null && o2 == null || o1 != null && o2 != null && o1.equals(o2) && o2.equals(o1);
  }

  @Override
  public int hashCode() {
    return isModule() ? myDisplayName.hashCode() : (myBundlePath != null ? myBundlePath.hashCode() : 0);
  }

  @Override
  public String toString() {
    return myDisplayName + (myBundlePath != null ? (" (" + myBundlePath.substring(myBundlePath.lastIndexOf("/") + 1) + ")") : "");
  }
}
