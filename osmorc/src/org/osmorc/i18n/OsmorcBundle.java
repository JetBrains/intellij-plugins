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
package org.osmorc.i18n;

import com.intellij.CommonBundle;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import javax.swing.*;
import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

/**
 * Internationalization bundle for Osmorc.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id$
 */
public class OsmorcBundle {

    private static Reference<ResourceBundle> _ourBundle;
    private static String infoHtml;
    private static Map<String, Icon> _iconCache = new WeakHashMap<String, Icon>();

    @NonNls
    private static final String BUNDLE = "org.osmorc.i18n.OsmorcBundle";

    private OsmorcBundle() {
    }

    /**
     * Translates the given message.
     *
     * @param key    the key to be used for translation
     * @param params the parameters for the translation
     * @return the translated message.
     * @deprecated Translation isn't used consistently so this function is rather useless.
     */
    @Deprecated
    public static String getTranslation(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return CommonBundle.message(getBundle(), key, params);
    }

    /**
     * Returns the resource bundle. In case there is a memory shortage the resource bundle is garbage collected. This
     * method will provide a new resource bundle in case the previous one got garbage collected.
     *
     * @return the resoruce bundle.
     */
    private static ResourceBundle getBundle() {
        ResourceBundle bundle = null;
        if (_ourBundle != null) {
            bundle = _ourBundle.get();
        }
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE);
            _ourBundle = new SoftReference<ResourceBundle>(bundle);
        }
        return bundle;
    }

    private static Icon getCachedIcon(@PropertyKey(resourceBundle = BUNDLE) String property) {
        Icon result = _iconCache.get(property);
        if (result == null) {
            result = IconLoader.getIcon(getTranslation(property));
            _iconCache.put(property, result);
        }
        return result;
    }

    /**
     * @return a small icon for Osmorc
     */
    public static Icon getSmallIcon() {
        return getCachedIcon("runconfiguration.icon");
    }


    /**
     * @return a big icon for Osmorc
     */
    public static Icon getBigIcon() {
        return getCachedIcon("projectconfiguration.icon");
    }

    public static Icon getLogo() {
        return getCachedIcon("logo.icon");
    }

    public static String getInfo() {
        if (infoHtml == null) {
            StringBuilder builder = new StringBuilder();
            InputStream stream = null;
            InputStreamReader streamReader =  null;
            BufferedReader bReader = null;

            try {
                String infoFileName = getTranslation("info.file");
                stream = OsmorcBundle.class.getResourceAsStream(infoFileName);
                streamReader = new InputStreamReader(stream);
                bReader = new BufferedReader(streamReader);
                while (bReader.ready()) {
                    builder.append(bReader.readLine());
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            finally {
                try {
                    if (bReader != null) {
                        bReader.close();
                    }
                    if (streamReader != null) {
                        streamReader.close();
                    }
                    if (stream != null) {
                        stream.close();
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            infoHtml = builder.toString();

        }

        return infoHtml;
    }
}
