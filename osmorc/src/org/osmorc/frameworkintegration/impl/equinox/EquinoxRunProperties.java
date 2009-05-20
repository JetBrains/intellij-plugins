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

import org.osmorc.frameworkintegration.impl.GenericRunProperties;

import java.util.Map;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class EquinoxRunProperties extends GenericRunProperties {

    public EquinoxRunProperties(Map<String, String> additionalProperties) {
        super(additionalProperties);
    }

    public boolean isStartEquinoxOSGIConsole() {
        return getBooleanProperty(START_EQUINOX_OSGICONSOLE);
    }

    public void setStartEquinoxOSGIConsole(boolean startEquinoxConsole) {
        putBooleanProperty(START_EQUINOX_OSGICONSOLE, startEquinoxConsole);
    }

    public String getEquinoxProduct() {
        return getProperty(EQUINOX_PRODUCT);
    }

    public void setEquinoxProduct(final String product) {
        putProperty(EQUINOX_PRODUCT, product);
    }

    public String getEquinoxApplication() {
        return getProperty(EQUINOX_APPLICATION);
    }

    public void setEquinoxApplication(final String application) {
        putProperty(EQUINOX_APPLICATION, application);
    }

    public boolean isCleanEquinoxCache() {
        return getBooleanProperty(CLEAN_EQUINOX_CACHE);
    }

    public void setCleanEquinoxCache(final boolean cleanEquinoxCache) {
        putBooleanProperty(CLEAN_EQUINOX_CACHE, cleanEquinoxCache);
    }

    public boolean isRecreateEquinoxConfigIni() {
        return getBooleanProperty(RECREATE_CONFIG_INI);
    }

    public void setRecreateEquinoxConfigIni(final boolean recreateConfigIni) {
        putBooleanProperty(RECREATE_CONFIG_INI, recreateConfigIni);
    }

    public static final String START_EQUINOX_OSGICONSOLE = "startEquinoxOSGIConsole";
    public static final String EQUINOX_PRODUCT = "equinoxProduct";
    public static final String EQUINOX_APPLICATION = "equinoxApplication";
    public static final String CLEAN_EQUINOX_CACHE = "cleanEquinoxCache";
    public static final String RECREATE_CONFIG_INI = "recreateEquinoxConfigIni";

}
