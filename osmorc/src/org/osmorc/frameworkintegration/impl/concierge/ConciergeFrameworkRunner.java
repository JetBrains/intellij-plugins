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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.CachingBundleInfoProvider;
import org.osmorc.frameworkintegration.impl.AbstractSimpleFrameworkRunner;
import org.osmorc.run.ui.SelectedBundle;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Concierge specific implementation of {@link org.osmorc.frameworkintegration.impl.AbstractSimpleFrameworkRunner}.
 *
 * @author <a href="mailto:al@chilibi.org">Alain Greppin</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ConciergeFrameworkRunner extends AbstractSimpleFrameworkRunner<ConciergeRunProperties> {
    @NotNull
    protected String[] getCommandlineParameters(@NotNull SelectedBundle[] bundlesToInstall,
                                                @NotNull ConciergeRunProperties runProperties) {
        return new String[]{};
    }

    @NotNull
    protected Map<String, String> getSystemProperties(@NotNull SelectedBundle[] bundlesToInstall,
                                                      @NotNull ConciergeRunProperties runProperties) {
        Map<String, String> props = new HashMap<String, String>();
        int level = 0;
        for (SelectedBundle bundle : bundlesToInstall) {
            int startLevel = bundle.getStartLevel();
            level = Math.max(level, startLevel);
            String installBundles = props.get("osgi.auto.install");
            installBundles = installBundles != null ? installBundles + " " + bundle.getBundleUrl() : bundle.getBundleUrl();

            String startBundles = props.get("osgi.auto.start");
            if (bundle.isStartAfterInstallation() && !CachingBundleInfoProvider.isFragmentBundle(bundle.getBundleUrl())) {
                startBundles = startBundles != null ? startBundles + " " + bundle.getBundleUrl() : bundle.getBundleUrl();
            }
            if (installBundles != null) {
                props.put("osgi.auto.install", installBundles);
            }
            if (startBundles != null) {
                props.put("osgi.auto.start", startBundles);
            }
        }

        props.put("osgi.startlevel.framework", String.valueOf(level));
        // http://concierge.sourceforge.net/properties.html
        props.put("osgi.init", "true");

        // setup the framework storage directory.
        props.put("ch.ethz.iks.concierge.storage", getFrameworkDirCanonicalPath());

        // show errors, otherwise the user will be going crazy...
        if (runProperties.isDebugMode()) {
            props.put("ch.ethz.iks.concierge.debug", "true");
        }

        return props;
    }

    protected void runCustomInstallationSteps(@NotNull SelectedBundle[] bundlesToInstall,
                                              @NotNull ConciergeRunProperties runProperties) {
    }


    @NotNull
    @NonNls
    public String getMainClass() {
        return "ch.ethz.iks.concierge.framework.Framework";
    }

    @NotNull
    protected ConciergeRunProperties convertProperties(Map<String, String> properties) {
        return new ConciergeRunProperties(properties);
    }

    protected Pattern getFrameworkStarterClasspathPattern() {
        return FRAMEWORK_STARTER_JAR_PATTERN;
    }

    private static final Pattern FRAMEWORK_STARTER_JAR_PATTERN = Pattern.compile("^concierge-.*.jar");
}
