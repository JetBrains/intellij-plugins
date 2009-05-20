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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.CachingBundleInfoProvider;
import org.osmorc.frameworkintegration.impl.AbstractSimpleFrameworkRunner;
import org.osmorc.run.ui.SelectedBundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Knopflerfish specific implementation of {@link org.osmorc.frameworkintegration.FrameworkRunner}.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 * @version $Id$
 */
public class KnopflerfishFrameworkRunner extends AbstractSimpleFrameworkRunner<KnopflerfishRunProperties> {
    @NotNull
    protected String[] getCommandlineParameters(@NotNull SelectedBundle[] bundlesToInstall,
                                                @NotNull KnopflerfishRunProperties runProperties) {
        List<String> params = new ArrayList<String>();

        params.add("-init");
        params.add("-launch");

        int level = 1;
        for (SelectedBundle bundle : bundlesToInstall) {
            int startLevel = bundle.getStartLevel();
            if (startLevel > level) {
                level = startLevel;
                params.add("-initlevel");
                params.add(String.valueOf(level));
            }
            params.add("-install");
            params.add(bundle.getBundleUrl());
            // one cannot start fragment bundles, so we have to make sure they are only installed
            if (bundle.isStartAfterInstallation() && !CachingBundleInfoProvider.isFragmentBundle(bundle.getBundleUrl())) {

                params.add("-start");
                params.add(bundle.getBundleUrl());
            }
        }
        params.add("-startlevel");
        params.add(String.valueOf(level));
        return params.toArray(new String[params.size()]);
    }

    @NotNull
    protected Map<String, String> getSystemProperties(@NotNull SelectedBundle[] urlsOfBundlesToInstall,
                                                      @NotNull KnopflerfishRunProperties runProperties) {
        Map<String, String> result = new HashMap<String, String>();
        // setup the framework storage directory.
        result.put("org.osgi.framework.dir", getFrameworkDirCanonicalPath());

        result.put("org.knopflerfish.framework.debug.errors", "true");

        // debugging (TODO: more detailed settings in the dialog)
        if (runProperties.isDebugMode()) {
            // result.put("org.knopflerfish.framework.debug.packages", "true");
            result.put("org.knopflerfish.framework.debug.startlevel", "true");
            result.put("org.knopflerfish.verbosity", "10");
            result.put("org.knopflerfish.framework.debug.classloader", "true");
        }
        result.put("org.knopflerfish.framework.system.export.all_15", "true");
        String systemPackages = runProperties.getSystemPackages();
        if (systemPackages != null && !(systemPackages.trim().length() == 0)) {
            result.put("org.osgi.framework.system.packages", systemPackages);
        }

        String bootDelegation = runProperties.getBootDelegation();
        if (bootDelegation != null && !(bootDelegation.trim().length() == 0)) {
            result.put("org.osgi.framework.bootdelegation", bootDelegation);
        }

        return result;
    }

    protected void runCustomInstallationSteps(@NotNull SelectedBundle[] urlsOfBundlesToInstall,
                                              @NotNull KnopflerfishRunProperties runProperties) {
    }


    @NotNull
    @NonNls
    public String getMainClass() {
        return "org.knopflerfish.framework.Main";
    }

    @NotNull
    protected KnopflerfishRunProperties convertProperties(Map<String, String> properties) {
        return new KnopflerfishRunProperties(properties);
    }

    protected Pattern getFrameworkStarterClasspathPattern() {
        return FRAMEWORK_STARTER_JAR_PATTERN;
    }

    private static final Pattern FRAMEWORK_STARTER_JAR_PATTERN = Pattern.compile("^framework.jar");
}
