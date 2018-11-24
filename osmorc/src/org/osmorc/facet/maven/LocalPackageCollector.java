package org.osmorc.facet.maven;

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Constants;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Helper class which collects local packages from the output path. This is copied code from the
 * felix bnd maven plugin.
 */
public class LocalPackageCollector {

  private static final String LOCAL_PACKAGES = "{local-packages}";


  /**
   * Adds the local packages to the headers in the given manifest.
   * @param outputDirectory the output directory where the compiled classes are located
   * @param currentManifest the currently calculated manifest contents.
   */
  public static void addLocalPackages(File outputDirectory, Map<String, String> currentManifest) {
    Analyzer fakeAnalyzer = ImporterUtil.makeFakeAnalyzer(currentManifest);
    addLocalPackages(outputDirectory, fakeAnalyzer);
  }

  private static void addLocalPackages(File outputDirectory, Analyzer analyzer) {
    Collection<String> packages = new LinkedHashSet<String>();

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

    StringBuilder exportedPkgs = new StringBuilder();
    StringBuilder privatePkgs = new StringBuilder();

    boolean noprivatePackages = "!*".equals(analyzer.getProperty(Constants.PRIVATE_PACKAGE));

    for (Object aPackage : packages) {
      String pkg = (String)aPackage;

      // mark all source packages as private by default (can be overridden by export list)
      if (privatePkgs.length() > 0) {
        privatePkgs.append(';');
      }
      privatePkgs.append(pkg);

      // we can't export the default package (".") and we shouldn't export internal packages
      if (noprivatePackages || !(".".equals(pkg) || pkg.contains(".internal") || pkg.contains(".impl"))) {
        if (exportedPkgs.length() > 0) {
          exportedPkgs.append(';');
        }
        exportedPkgs.append(pkg);
      }
    }

    if (analyzer.getProperty(Constants.EXPORT_PACKAGE) == null) {
      if (analyzer.getProperty(Constants.EXPORT_CONTENTS) == null) {
        // no -Fexportcontents overriding the exports, so use our computed list
        analyzer.setProperty(Constants.EXPORT_PACKAGE, exportedPkgs + ";-split-package:=merge-first");
      }
      else {
        // leave Export-Package empty (but non-null) as we have -exportcontents
        analyzer.setProperty(Constants.EXPORT_PACKAGE, "");
      }
    }
    else {
      String exported = analyzer.getProperty(Constants.EXPORT_PACKAGE);
      if (exported.contains(LOCAL_PACKAGES)) {
        String newExported = StringUtils.replace(exported, LOCAL_PACKAGES, exportedPkgs.toString());
        analyzer.setProperty(Constants.EXPORT_PACKAGE, newExported);
      }
    }

    String internal = analyzer.getProperty(Constants.PRIVATE_PACKAGE);
    if (internal == null) {
      if (privatePkgs.length() > 0) {
        analyzer.setProperty(Constants.PRIVATE_PACKAGE, privatePkgs + ";-split-package:=merge-first");
      }
      else {
        // if there are really no private packages then use "!*" as this will keep the Bnd Tool happy
        analyzer.setProperty(Constants.PRIVATE_PACKAGE, "!*");
      }
    }
    else if (internal.contains(LOCAL_PACKAGES)) {
      String newInternal = StringUtils.replace(internal, LOCAL_PACKAGES, privatePkgs.toString());
      analyzer.setProperty(Constants.PRIVATE_PACKAGE, newInternal);
    }
  }

  /**
   * Returns the package name of the given file name.
   * @param filename the filename
   * @return the package name.
   */
  @NotNull
  private static String getPackageName(@NotNull String filename) {
    int n = filename.lastIndexOf(File.separatorChar);
    return n < 0 ? "." : filename.substring(0, n).replace(File.separatorChar, '.');
  }
}
