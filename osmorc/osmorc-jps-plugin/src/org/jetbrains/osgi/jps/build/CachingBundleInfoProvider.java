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
package org.jetbrains.osgi.jps.build;

import aQute.bnd.osgi.Constants;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * This is a helper class which helps providing information about bundles (that do not necessarily belong to the project).
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public final class CachingBundleInfoProvider {
  private static final Map<String, Pair<Long, Manifest>> ourCache = new WeakHashMap<>();

  /**
   * True for .jar files or exploded directories with Bundle-SymbolicName in their manifests.
   */
  public static boolean isBundle(@NotNull String path) {
    return getBundleAttribute(path, Constants.BUNDLE_SYMBOLICNAME) != null;
  }

  /**
   * True for .jar files which are not already bundles.
   */
  public static boolean canBeBundlified(@NotNull String path) {
    return path.endsWith(".jar") && new File(path).isFile() && !isBundle(path);
  }

  @Nullable
  public static String getBundleSymbolicName(@NotNull String path) {
    String symbolicName = getBundleAttribute(path, Constants.BUNDLE_SYMBOLICNAME);
    if (symbolicName != null) {
      int p = symbolicName.indexOf(';');
      if (p > 0) symbolicName = symbolicName.substring(0, p);  // take the name and leave out any parameters
    }
    return symbolicName;
  }

  @Nullable
  public static String getBundleVersion(@NotNull String path) {
    return getBundleAttribute(path, Constants.BUNDLE_VERSION);
  }

  /**
   * True if the bundle exists and is a fragment one.
   */
  public static boolean isFragmentBundle(@NotNull String path) {
    return getBundleAttribute(path, Constants.FRAGMENT_HOST) != null;
  }

  @Nullable
  public synchronized static String getBundleAttribute(@NotNull String path, @NotNull String attribute) {
    Pair<Long, Manifest> pair = ourCache.remove(path);

    try {
      File bundleFile = new File(path);
      if (bundleFile.isDirectory()) {
        File manifestFile = new File(bundleFile, JarFile.MANIFEST_NAME);
        if (pair == null || pair.first != manifestFile.lastModified()) {
          pair = null;
          FileInputStream stream = new FileInputStream(manifestFile);
          try {
            Manifest manifest = new Manifest(stream);
            pair = Pair.create(manifestFile.lastModified(), manifest);
          }
          finally {
            stream.close();
          }
        }
      }
      else if (bundleFile.isFile()) {
        if (pair == null || pair.first != bundleFile.lastModified()) {
          pair = null;
          JarFile jar = new JarFile(bundleFile);
          try {
            Manifest manifest = jar.getManifest();
            if (manifest != null) {
              pair = Pair.create(bundleFile.lastModified(), manifest);
            }
          }
          finally {
            jar.close();
          }
        }
      }
    }
    catch (IOException e) {
      Logger.getInstance(CachingBundleInfoProvider.class).debug(e);
    }

    if (pair != null) {
      ourCache.put(path, pair);
      return pair.second.getMainAttributes().getValue(attribute);
    }
    else {
      return null;
    }
  }
}
