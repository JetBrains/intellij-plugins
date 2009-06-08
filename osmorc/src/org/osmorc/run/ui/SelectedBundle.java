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
 * Class representing a bundle that has been selected for running. This can either be some pre-jarred bundle from the
 * classpath or a module from this project.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class SelectedBundle {
    public SelectedBundle(@NotNull String displayName, @Nullable String url, @NotNull BundleType bundleType) {
        this.displayName = displayName;
        bundleUrl = url;
        this.bundleType = bundleType;
        startAfterInstallation = bundleType.isDefaultStartAfterInstallation();
        startLevel = 1;
    }

    @NotNull
    public String getName() {
        return displayName;
    }

    public void setName(@NotNull String displayName) {
        this.displayName = displayName;
    }

    @Nullable
    public String getBundleUrl() {
        return bundleUrl;
    }


    public String toString() {
        return displayName + (bundleUrl != null ? (" (" + bundleUrl.substring(bundleUrl.lastIndexOf("/") + 1) + ")") : "");
    }

    /**
     * Two selected bundles equal, when they point to the same URL. When the bundles are modules, they do not necessarily
     * have to point to the same URL, as the URL might be null on modules, so in this case the display name decides.
     *
     * @param o the object to check for equality
     * @return true if the given object represents the same bundle as this bundle.
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof SelectedBundle) {
            SelectedBundle other = (SelectedBundle) o;

            return isModule() ? isEqual(displayName, other.displayName) : isEqual(bundleUrl, other.bundleUrl);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return isModule() ? displayName.hashCode() : (bundleUrl != null ? bundleUrl.hashCode() : 0);
    }

    private boolean isEqual(Object o1, Object o2) {
        return o1 == null && o2 == null || o1 != null && o2 != null && o1.equals(o2) && o2.equals(o1);
    }

    /**
     * @return the start level of this bundle. Unless set to something else, this is 1.
     */
    public int getStartLevel() {
        return startLevel;
    }

    public void setStartLevel(int startLevel) {
        this.startLevel = startLevel;
    }

    public void setBundleUrl(@Nullable String bundleUrl) {
        this.bundleUrl = bundleUrl;
    }

    public BundleType getBundleType() {
        return bundleType;
    }

    public void setBundleType(BundleType bundleType) {
        this.bundleType = bundleType;
    }

    public boolean isModule() {
        return bundleType == BundleType.Module;
    }

    public boolean isStartAfterInstallation() {
        return startAfterInstallation;
    }

    public void setStartAfterInstallation(boolean startAfterInstallation) {
        this.startAfterInstallation = startAfterInstallation;
    }

    private String displayName;
    @Nullable
    private String bundleUrl;
    private int startLevel;
    private boolean startAfterInstallation;
    private BundleType bundleType;

    /**
     * The type of a selected bundle,
     */
    public static enum BundleType {
        /**
         * The selected bundle is a module of the currently open project.
         */
        Module(true),
        /**
         * The selected bundle is an existing bundle that is part of the OSGi framework (e.g. the Knopflerfish Desktop
         * bundle).
         */
        FrameworkBundle(true),
        /**
         * The selected bundle is a library used in this project, that should be started. This is rarely used, except for
         * Libraries that are meant to be used as bundles (such as Spring-DM).
         */
        StartableLibrary(true),

        /**
         * The selected bundle is a plain library that should be installed only.
         */
        PlainLibrary(false);

        BundleType(boolean startAfterInstallation) {
            defaultStartAfterInstallation = startAfterInstallation;
        }

        /**
         * @return true if bundles of this type should by default be started after installation, false otherwise.
         */
        public boolean isDefaultStartAfterInstallation() {
            return defaultStartAfterInstallation;
        }

        private final boolean defaultStartAfterInstallation;
    }
}
