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
package org.osmorc.frameworkintegration.impl.felix;

import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.CachingBundleInfoProvider;
import org.osmorc.frameworkintegration.impl.AbstractSimpleFrameworkRunner;
import org.osmorc.run.ui.SelectedBundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Framework runner for the felix osgi container.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 * @version $Id$
 */
public class FelixFrameworkRunner extends AbstractSimpleFrameworkRunner<FelixRunProperties> {

    @NotNull
    protected String[] getCommandlineParameters(@NotNull SelectedBundle[] urlsOfBundlesToInstall,
                                                @NotNull FelixRunProperties runProperties) {
        return new String[]{};
    }

    @NotNull
    protected Map<String, String> getSystemProperties(@NotNull SelectedBundle[] urlsOfBundlesToInstall,
                                                      @NotNull FelixRunProperties runProperties) {
        Map<String, String> result = new HashMap<String, String>();

        result.put("felix.cache.dir",getFrameworkDirCanonicalPath());
        result.put("felix.cache.profile", "osmorcProfile");
        result.put("felix.cache.rootdir",getFrameworkDirCanonicalPath());
        try {
            result.put("felix.config.properties", getPropertiesFile().toURI().toURL().toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private File getPropertiesFile() {
        return new File(getFrameworkDir(), "osmorc_felix.properties");
    }

    protected void runCustomInstallationSteps(@NotNull SelectedBundle[] bundlesToInstall,
                                              @NotNull FelixRunProperties runProperties) {
        Properties props = new Properties();
        try {
            String resourceName = "/" + getClass().getPackage().getName().replace('.', '/') + "/config.properties";
            props.load(getClass().getResourceAsStream(resourceName));
            // enrich with bundles to install
            int level = 0;
            for (SelectedBundle bundle : bundlesToInstall) {
                int startLevel = bundle.getStartLevel();
                level = Math.max(level, startLevel);
                // XXX: this is not exactly resource conserving...
                String installBundles = (String) props.get("felix.auto.install." + startLevel);
                installBundles = installBundles != null ? installBundles + " " + bundle.getBundleUrl() : bundle.getBundleUrl();

                String startBundles = (String) props.get("felix.auto.start." + startLevel);
                if (bundle.isStartAfterInstallation() && !CachingBundleInfoProvider.isFragmentBundle(bundle.getBundleUrl())) {
                    startBundles = startBundles != null ? startBundles + " " + bundle.getBundleUrl() : bundle.getBundleUrl();
                }

                if (installBundles != null) {
                    props.put("felix.auto.install." + startLevel, installBundles);
                }
                if (startBundles != null) {
                    props.put("felix.auto.start." + startLevel, startBundles);
                }
            }
            if (runProperties.isDebugMode()) {
                props.put("felix.log.level", "4");
            } else {
                props.put("felix.log.level", "1");
            }

            String systemPackages = runProperties.getSystemPackages();
            if (systemPackages != null && !(systemPackages.trim().length() == 0)) {
                String pkg = props.getProperty("org.osgi.framework.system.packages") + "," + systemPackages;
                props.setProperty("org.osgi.framework.system.packages", pkg);
            }
            String bootDelegation = runProperties.getBootDelegation();
            if (bootDelegation != null && !(bootDelegation.trim().length() == 0)) {
                props.setProperty("org.osgi.framework.bootdelegation", bootDelegation);
            }

            props.put("felix.startlevel.framework", String.valueOf(level));
            props.store(new FileOutputStream(getPropertiesFile()), "");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public String getMainClass() {
        return "org.apache.felix.main.Main";
    }

    @NotNull
    protected FelixRunProperties convertProperties(Map<String, String> properties) {
        return new FelixRunProperties(properties);
    }

    protected Pattern getFrameworkStarterClasspathPattern() {
        return FRAMEWORK_STARTER_JAR_PATTERN;
    }

    private static final Pattern FRAMEWORK_STARTER_JAR_PATTERN = Pattern.compile("^felix.jar");
}
