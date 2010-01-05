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

import org.osmorc.frameworkintegration.util.PropertiesWrapper;

import java.util.Map;

/**
 * Run properties which are supported by any OSGi Framework.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class GenericRunProperties extends PropertiesWrapper {
    public static final String DEBUG_MODE = "debugMode";
    public static final String START_CONSOLE = "startConsole";
    public static final String SYSTEM_PACKAGES = "systemPackages";
    public static final String BOOT_DELEGATION = "bootDelegation";

    public GenericRunProperties(Map<String, String> properties) {
        super(properties);
    }

    public String getBootDelegation() {
        return getProperty(BOOT_DELEGATION);
    }

    public String getSystemPackages() {
        return getProperty(SYSTEM_PACKAGES);
    }

    public boolean isDebugMode() {
        return getBooleanProperty(DEBUG_MODE);
    }

    public void setBootDelegation(String value) {
        putProperty(BOOT_DELEGATION, value);
    }

    public void setDebugMode(boolean debugMode) {
        putBooleanProperty(DEBUG_MODE, debugMode);
    }

    public void setSystemPackages(String value) {
        putProperty(SYSTEM_PACKAGES, value);
    }

    public void setStartConsole(boolean value) {
        putBooleanProperty(START_CONSOLE, value);
    }

    public boolean isStartConsole() {
        return getBooleanProperty(START_CONSOLE);
    }
}
