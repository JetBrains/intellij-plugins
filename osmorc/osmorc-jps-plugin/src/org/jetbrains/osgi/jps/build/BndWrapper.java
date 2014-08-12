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

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Builder;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Verifier;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.osgi.jps.model.JpsOsmorcExtensionService;
import org.jetbrains.osgi.jps.model.LibraryBundlificationRule;
import org.jetbrains.osgi.jps.util.OrderedProperties;
import org.osgi.framework.Constants;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which wraps bnd and integrates it into IntelliJ.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class BndWrapper {
  private final Reporter myReporter;

  public BndWrapper(Reporter reporter) {
    myReporter = reporter;
  }

  /**
   * Wraps an existing .jar file using Bnd analyzer.
   * Uses bundlification rules for the library that have been set in Settings/OSGi/Library Bundling.
   */
  @Nullable
  public File wrapLibrary(@NotNull File sourceFile, @NotNull File outputDir) throws OsgiBuildException {
    if (!sourceFile.isFile()) {
      throw new OsgiBuildException("The library '" + sourceFile + "' does not exist - please check module dependencies.");
    }

    File targetFile = new File(outputDir, sourceFile.getName());
    Map<String, String> additionalProperties = ContainerUtil.newHashMap();

    long lastModified = Long.MIN_VALUE;
    for (LibraryBundlificationRule bundlificationRule : JpsOsmorcExtensionService.getInstance().getLibraryBundlificationRules()) {
      if (bundlificationRule.appliesTo(sourceFile.getName())) {
        if (bundlificationRule.isDoNotBundle()) {
          return null;
        }
        additionalProperties.putAll(bundlificationRule.getAdditionalPropertiesMap());
        lastModified = Math.max(lastModified, bundlificationRule.getLastModified());
        if (bundlificationRule.isStopAfterThisRule()) {
          break;
        }
      }
    }

    if (targetFile.exists() && targetFile.lastModified() >= sourceFile.lastModified() && targetFile.lastModified() >= lastModified) {
      return targetFile;
    }

    doWrap(sourceFile, targetFile, additionalProperties);
    return targetFile;
  }

  /**
   * Internal function which does the actual wrapping. 90% borrowed from the Bnd source code.
   */
  private void doWrap(@NotNull File inputJar, @NotNull File outputJar, @NotNull Map<String, String> properties) throws OsgiBuildException {
    if (!FileUtil.delete(outputJar)) {
      throw new OsgiBuildException("Can't delete outdated bundle '" + outputJar + "'");
    }
    if (!FileUtil.createParentDirs(outputJar)) {
      throw new OsgiBuildException("Can't create output directory for '" + outputJar + "'");
    }

    try {
      Analyzer analyzer = new ReportingAnalyzer(myReporter);
      analyzer.setPedantic(false);
      analyzer.setJar(inputJar);
      analyzer.putAll(properties, false);

      if (analyzer.getProperty(Constants.IMPORT_PACKAGE) == null) {
        analyzer.setProperty(Constants.IMPORT_PACKAGE, "*;resolution:=optional");
      }

      if (analyzer.getProperty(Constants.BUNDLE_SYMBOLICNAME) == null) {
        Pattern p = Pattern.compile("(" + Verifier.SYMBOLICNAME.pattern() + ")(-[0-9])?.*\\.jar");
        Matcher m = p.matcher(inputJar.getName());
        if (!m.matches()) {
          throw new OsgiBuildException("Can't calculate output bundle name for '" + inputJar + "' - rename file or use -properties");
        }
        analyzer.setProperty(Constants.BUNDLE_SYMBOLICNAME, m.group(1));
      }

      if (analyzer.getProperty(Constants.EXPORT_PACKAGE) == null) {
        analyzer.setProperty(Constants.EXPORT_PACKAGE, "*");
      }

      JarFile jarFile = new JarFile(inputJar);
      try {
        analyzer.mergeManifest(jarFile.getManifest());
      }
      finally {
        jarFile.close();
      }

      String version = analyzer.getProperty(Constants.BUNDLE_VERSION);
      if (version != null) {
        version = Analyzer.cleanupVersion(version);
        analyzer.setProperty(Constants.BUNDLE_VERSION, version);
      }

      analyzer.calcManifest();

      Jar jar = analyzer.getJar();
      jar.write(outputJar);
      jar.close();
      analyzer.close();
    }
    catch (OsgiBuildException e) {
      throw e;
    }
    catch (Exception e) {
      throw new OsgiBuildException("There was an unexpected problem when trying to bundlify", e, null);
    }
  }

  /**
   * Generates temporary Bnd file from the given content.
   */
  @NotNull
  public File makeBndFile(@NotNull Map<String, String> contents, @NotNull String comment, @NotNull File outputDir) throws OsgiBuildException {
    try {
      File file = FileUtil.createTempFile(outputDir, "osgi.", ".bnd", true, true);
      OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
      try {
        OrderedProperties.fromMap(contents).store(stream, comment);
      }
      finally {
        stream.close();
      }
      return file;
    }
    catch (IOException e) {
      throw new OsgiBuildException("Problem when generating .bnd file", e, null);
    }
  }

  /**
   * Builds the .jar file for the given module.
   */
  public void build(@NotNull File bndFile, @NotNull File moduleOutput, @NotNull File outputFile) throws OsgiBuildException {
    try {
      Builder builder = new ReportingBuilder(myReporter);
      builder.setPedantic(false);
      builder.setProperties(bndFile);
      builder.setClasspath(new File[]{moduleOutput});

      // check if the manifest version is missing (IDEADEV-41174)
      String manifest = builder.getProperty(aQute.bnd.osgi.Constants.MANIFEST);
      if (manifest != null) {
        File manifestFile = builder.getFile(manifest);
        if (manifestFile != null) {
          try {
            FileInputStream stream = new FileInputStream(manifestFile);
            try {
              Properties p = new Properties();
              p.load(stream);
              String value = p.getProperty(Attributes.Name.MANIFEST_VERSION.toString());
              if (StringUtil.isEmptyOrSpaces(value)) {
                String message = "Manifest misses a Manifest-Version entry. This may produce an empty manifest in the resulting bundle.";
                myReporter.warning(message, null, manifest);
              }
            }
            finally {
              stream.close();
            }
          }
          catch (Exception e) {
            myReporter.warning("Can't read manifest: " + e.getMessage(), e, manifest);
          }
        }
      }

      Jar jar = builder.build();
      jar.setName(outputFile.getName());
      jar.write(outputFile);
      builder.close();
    }
    catch (Exception e) {
      throw new OsgiBuildException("Unexpected build error", e, null);
    }
  }

  /**
   * Creates an output dir relative to a module's one.
   */
  @NotNull
  public static File getOutputDir(@NotNull File moduleOutputDir) throws OsgiBuildException {
    File outputDir = new File(moduleOutputDir.getParent(), "bundles");
    if (!outputDir.exists() && !outputDir.mkdirs()) {
      throw new OsgiBuildException("Can't create output directory '" + outputDir + "'. Please check file permissions.");
    }
    return outputDir;
  }
}
