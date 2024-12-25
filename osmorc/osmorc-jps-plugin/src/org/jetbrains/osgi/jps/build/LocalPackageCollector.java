/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Modified for usage within IntelliJ IDEA.
 */
package org.jetbrains.osgi.jps.build;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Constants;
import com.intellij.openapi.util.text.StringUtil;
import org.codehaus.plexus.util.DirectoryScanner;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Helper class which collects local packages from the output path. This is copied code from the
 * felix bnd maven plugin.
 */
public final class LocalPackageCollector {
  private static final String LOCAL_PACKAGES = "{local-packages}";

  /**
   * Adds the local packages to the headers in the given manifest.
   * @param outputDirectory the output directory where the compiled classes are located
   * @param currentManifest the currently calculated manifest contents.
   */
  public static void addLocalPackages(File outputDirectory, Map<String, String> currentManifest) {
    Analyzer fakeAnalyzer = new FakeAnalyzer(currentManifest);
    addLocalPackages(outputDirectory, fakeAnalyzer);
  }

  private static void addLocalPackages(File outputDirectory, Analyzer analyzer) {
    Collection<String> packages = new LinkedHashSet<>();

    if (outputDirectory != null && outputDirectory.isDirectory()) {
      // scan classes directory for potential packages
      DirectoryScanner scanner = new DirectoryScanner();
      scanner.setBasedir(outputDirectory);
      scanner.setIncludes(new String[] {"**/*.class"});
      scanner.addDefaultExcludes();
      scanner.scan();

      String[] paths = scanner.getIncludedFiles();
      for (String path : paths) {
        packages.add(getPackageName(path));
      }
    }

    StringBuilder exportedPackages = new StringBuilder();
    StringBuilder privatePackages = new StringBuilder();

    boolean noPrivatePackages = "!*".equals(analyzer.getProperty(Constants.PRIVATE_PACKAGE));

    for (String pkg : packages) {
      // mark all source packages as private by default (can be overridden by export list)
      if (privatePackages.length() > 0) {
        privatePackages.append(';');
      }
      privatePackages.append(pkg);

      // we can't export the default package (".") and we shouldn't export internal packages
      if (noPrivatePackages || !(".".equals(pkg) || pkg.contains(".internal") || pkg.contains(".impl"))) {
        if (exportedPackages.length() > 0) {
          exportedPackages.append(';');
        }
        exportedPackages.append(pkg);
      }
    }

    if (analyzer.getProperty(Constants.EXPORT_PACKAGE) == null) {
      if (analyzer.getProperty(Constants.EXPORT_CONTENTS) == null) {
        // no -exportcontents overriding the exports, so use our computed list
        analyzer.setProperty(Constants.EXPORT_PACKAGE, exportedPackages + ";-split-package:=merge-first");
      }
      else {
        // leave Export-Package empty (but non-null) as we have -exportcontents
        analyzer.setProperty(Constants.EXPORT_PACKAGE, "");
      }
    }
    else {
      String exported = analyzer.getProperty(Constants.EXPORT_PACKAGE);
      if (exported.contains(LOCAL_PACKAGES)) {
        String newExported = StringUtil.replace(exported, LOCAL_PACKAGES, exportedPackages.toString());
        analyzer.setProperty(Constants.EXPORT_PACKAGE, newExported);
      }
    }

    String internal = analyzer.getProperty(Constants.PRIVATE_PACKAGE);
    if (internal == null) {
      if (privatePackages.length() > 0) {
        analyzer.setProperty(Constants.PRIVATE_PACKAGE, privatePackages + ";-split-package:=merge-first");
      }
      else {
        // if there are really no private packages then use "!*" as this will keep the Bnd Tool happy
        analyzer.setProperty(Constants.PRIVATE_PACKAGE, "!*");
      }
    }
    else if (internal.contains(LOCAL_PACKAGES)) {
      String newInternal = StringUtil.replace(internal, LOCAL_PACKAGES, privatePackages.toString());
      analyzer.setProperty(Constants.PRIVATE_PACKAGE, newInternal);
    }
  }

  /**
   * Returns the package name of the given file name.
   * @param filename the filename
   * @return the package name.
   */
  private static @NotNull String getPackageName(@NotNull String filename) {
    int n = filename.lastIndexOf(File.separatorChar);
    return n < 0 ? "." : filename.substring(0, n).replace(File.separatorChar, '.');
  }
}
