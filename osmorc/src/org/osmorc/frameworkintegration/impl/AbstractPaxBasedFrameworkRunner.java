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

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.CachingBundleInfoProvider;
import org.osmorc.frameworkintegration.impl.felix.FelixRunProperties;
import org.osmorc.frameworkintegration.impl.knopflerfish.KnopflerfishRunProperties;
import org.osmorc.frameworkintegration.util.PropertiesWrapper;
import org.osmorc.run.ui.SelectedBundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Framework runner implementation for using the PAX runner. This is an abstract base class that can be extended for the
 * various frameworks.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public abstract class AbstractPaxBasedFrameworkRunner<P extends GenericRunProperties>
        extends AbstractSimpleFrameworkRunner<P> {

    @NotNull
    @Override
    public final List<VirtualFile> getFrameworkStarterLibraries() {
        // pax does it's own magic, so the only lib we need, is the pax lib.
        // XXX: ask anton if there is some better way to do this..
        @SuppressWarnings({"ConstantConditions"}) final String paxLib =
                PluginManager.getPlugin(PluginId.getId("Osmorc")).getPath().getPath() + "/lib/pax-runner-1.3.0.jar";
        List<VirtualFile> libs = new ArrayList<VirtualFile>(1);
        libs.add(LocalFileSystem.getInstance().findFileByPath(paxLib));
        return libs;
    }

    @NotNull
    protected String[] getCommandlineParameters(@NotNull SelectedBundle[] bundlesToInstall,
                                                @NotNull P runProperties) {
        List<String> params = new ArrayList<String>();

        params.add("--p="+getOsgiFrameworkName().toLowerCase());

        for (SelectedBundle bundle : bundlesToInstall) {
            if (bundle.isStartAfterInstallation() &&
                    !CachingBundleInfoProvider.isFragmentBundle(bundle.getBundleUrl())) {
                params.add(bundle.getBundleUrl() + "@" + bundle.getStartLevel());
            } else {
                params.add(bundle.getBundleUrl());
            }
        }

        String bootDelegation = runProperties.getBootDelegation();
        if (bootDelegation != null && !(bootDelegation.trim().length() == 0)) {
            params.add("--bd");
            params.add(bootDelegation);
        }

        String systemPackages = runProperties.getSystemPackages();
        if ( systemPackages != null && !(systemPackages.trim().length() == 0)) {
            params.add("--sp");
            params.add(systemPackages);
        }

        if (runProperties.isDebugMode()) {
            params.add("--log=DEBUG");
        }

        if (runProperties.isStartConsole()) {
            params.add("--console");
        }
        else {
            params.add("--noConsole");
        }

        return ArrayUtil.toStringArray(params);
    }

    /**
     * Needs to be implemented by subclasses.
     *
     * @return the name of the osgi framework that the PAX runner should run.
     */
    @NotNull
    protected abstract String getOsgiFrameworkName();


    @NotNull
    protected Map<String, String> getSystemProperties(@NotNull SelectedBundle[] urlsOfBundlesToInstall,
                                                      @NotNull P runProperties) {
        return new HashMap<String, String>();
    }


    @NotNull
    @NonNls
    public final String getMainClass() {
        return "org.ops4j.pax.runner.Run";
    }



    protected final Pattern getFrameworkStarterClasspathPattern() {
        return null;
    }

    protected void runCustomInstallationSteps(@NotNull SelectedBundle[] bundlesToInstall,
                                              @NotNull P additionalProperties) {

    }
}
