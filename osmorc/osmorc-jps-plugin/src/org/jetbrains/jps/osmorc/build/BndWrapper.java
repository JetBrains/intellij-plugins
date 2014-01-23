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

package org.jetbrains.jps.osmorc.build;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Verifier;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.osmorc.model.JpsLibraryBundlificationRule;
import org.jetbrains.jps.osmorc.model.JpsOsmorcExtensionService;
import org.jetbrains.jps.osmorc.util.JpsOrderedProperties;
import org.osgi.framework.Constants;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which wraps bnd and integrates it into IntelliJ.
 * <p/>
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class BndWrapper {

  private final OsmorcBuildSession mySession;

  private final File myOutputDir;

  public BndWrapper(OsmorcBuildSession session) throws OsmorcBuildException {
    mySession = session;

    myOutputDir = new File(session.getModuleOutputDir().getParent(), "bundles");
    if (!FileUtil.createDirectory(myOutputDir)) {
      throw new OsmorcBuildException("Could not create the output directory. Please check file permissions.", myOutputDir);
    }
  }

  /**
   * Wraps an existing jar file using Bnd analyzer. This class will check and use any applying bundlification rules
   * for this library that have been set up in Osmorc library bundlification dialog.
   */
  @Nullable
  public File wrapLibrary(final File sourceFile) {
    if (!sourceFile.exists()) {
      mySession.warn("The library does not exist. Please check your module settings. Ignoring missing library.", sourceFile);
      return null;
    }
    if (sourceFile.isDirectory()) {
      // ok it's an exploded directory, we cannot bundle it.
      return null;
    }

    File targetFile = new File(myOutputDir, sourceFile.getName());
    Map<String, String> additionalProperties = new HashMap<String, String>();

    // okay try to find a rule for this nice package:
    long lastModified = Long.MIN_VALUE;
    for (JpsLibraryBundlificationRule bundlificationRule : JpsOsmorcExtensionService.getInstance().getLibraryBundlificationRules()) {
      if (bundlificationRule.appliesTo(sourceFile.getName())) {
        if (bundlificationRule.isDoNotBundle()) {
          return null; // make it quick in this case
        }
        additionalProperties.putAll(bundlificationRule.getAdditionalPropertiesMap());
        // if a rule applies which has been changed recently we need to re-bundle the file
        lastModified = Math.max(lastModified, bundlificationRule.getLastModified());

        // if stop after this rule is true, we will no longer try to find any more matching rules
        if (bundlificationRule.isStopAfterThisRule()) {
          break;
        }
      }
    }

    if (!targetFile.exists() || targetFile.lastModified() < sourceFile.lastModified() ||
        targetFile.lastModified() < lastModified) {
      try {
        doWrap(sourceFile, targetFile, additionalProperties);
      }
      catch (OsmorcBuildException e) {
        mySession.processException(e);
        return null;
      }
      return targetFile;
    }
    else {
      // Fixes IDEADEV-39099. When the wrapper does not return anything the library is not regarded
      // as a bundle.
      return targetFile;
    }
  }

  /**
   * Internal function which does the actual wrapping. This is 90% borrowed from Bnd source code.
   *
   * @param module
   * @param compileContext the compile context
   * @param inputJar       the input file
   * @param outputJar      the output file
   * @param properties     properties for the manifest. these may contain bnd instructions
   * @return true if the bundling was successful, false otherwise.
   * @throws Exception in case something goes wrong.
   */
  private void doWrap(@NotNull File inputJar,
                      @NotNull final File outputJar,
                      @NotNull Map<String, String> properties) throws OsmorcBuildException {
    try {
      Analyzer analyzer = new ReportingAnalyzer(mySession, inputJar);
      analyzer.setPedantic(false);
      analyzer.setJar(inputJar);
      Jar dot = analyzer.getJar();
      analyzer.putAll(properties, false);
      if (analyzer.getProperty(Constants.IMPORT_PACKAGE) == null) {
        analyzer.setProperty(Constants.IMPORT_PACKAGE, "*;resolution:=optional");
      }
      if (analyzer.getProperty(Constants.BUNDLE_SYMBOLICNAME) == null) {
        Pattern p = Pattern.compile("(" + Verifier.SYMBOLICNAME.pattern() + ")(-[0-9])?.*\\.jar");
        String base = inputJar.getName();
        Matcher m = p.matcher(base);
        if (m.matches()) {
          base = m.group(1);
        }
        else {
          throw new OsmorcBuildException("Can not calculate name of output bundle, rename jar or use -properties", inputJar);
        }

        analyzer.setProperty(Constants.BUNDLE_SYMBOLICNAME, base);
      }
      if (analyzer.getProperty(Constants.EXPORT_PACKAGE) == null) {
        // avoid spurious error messages about string starting with ","
        // String export = analyzer.calculateExportsFromContents(dot).replaceFirst("^\\s*,", "");
        analyzer.setProperty(Constants.EXPORT_PACKAGE, "*");
        //      analyzer.setProperty(Constants.EXPORT_PACKAGE, export);
      }
      analyzer.mergeManifest(dot.getManifest());
      String version = analyzer.getProperty(Constants.BUNDLE_VERSION);
      if (version != null) {
        version = Analyzer.cleanupVersion(version);
        analyzer.setProperty(Constants.BUNDLE_VERSION, version);
      }
      analyzer.calcManifest();
      Jar jar = analyzer.getJar();
      final File f = FileUtil.createTempFile("tmp.bnd.", ".jar");
      jar.write(f);
      jar.close();
      analyzer.close();

      // IDEA-26817 delete the old bundle, so the renameTo later works...

      if (!FileUtil.delete(outputJar)) {
        throw new OsmorcBuildException("Could not delete outdated generated bundle", outputJar);
      }

      if (!FileUtil.createParentDirs(outputJar)) {
        throw new OsmorcBuildException("Cannot create output folder", outputJar);
      }

      FileUtil.rename(f, outputJar);
    }
    catch (OsmorcBuildException e) {
      throw e;
    }
    catch (Exception e) {
      // There is some reported issue where a lot of exceptions have been thrown which caused a ton of popup
      // boxes, so we better put this into the compile context as normal error message. Can't reproduce the issue
      // but i think it's still the better way.
      // IDEA-27101
      // IDEA-69149 - Changed this form ERROR to WARNING, as a non-bundlified library might not be fatal (especially when importing a ton of libs from maven)
      throw new OsmorcBuildException("There was an unexpected problem when trying to bundlify", e, inputJar).setWarning();
    }
  }

  /**
   * Builds the jar file for the given module. This is called inside a compile run.
   */
  public void build(@NotNull File bndFile, @NotNull File outputFile) throws OsmorcBuildException {
    try {
      ReportingBuilder builder = new ReportingBuilder(mySession, bndFile);
      builder.setPedantic(false);
      builder.setProperties(bndFile);

      File[] classPath = {mySession.getModuleOutputDir()};
      builder.setClasspath(classPath);

      // Check if the manifest version is missing (IDEADEV-41174)
      String manifest = builder.getProperty(aQute.bnd.osgi.Constants.MANIFEST);
      if (manifest != null) {
        File manifestFile = builder.getFile(manifest);
        if (manifestFile != null && manifestFile.canRead()) {
          Properties props = new Properties();
          FileInputStream fileInputStream = new FileInputStream(manifestFile);
          try {
            props.load(fileInputStream);
            String value = props.getProperty(Attributes.Name.MANIFEST_VERSION.toString());
            if (StringUtil.isEmptyOrSpaces(value)) {
              String message
                = "Your manifest does not contain a Manifest-Version entry. This may produce an empty manifest in the resulting bundle.";
              mySession.warn(message, manifest);
            }
          }
          catch (Exception ex) {
            mySession.warn("There was a problem reading your manifest.", manifest);
          }
          finally {
            fileInputStream.close();
          }
        }
      }

      Jar jar = builder.build();
      jar.setName(outputFile.getName());
      jar.write(outputFile);
      builder.close();
    }
    catch (Exception e) {
      throw new OsmorcBuildException("Unexpected build error", e);
    }
  }

  /**
   * Generates a bnd file from the given contents map and returns it.
   */
  @NotNull
  public File makeBndFile(@NotNull Map<String, String> contents) throws OsmorcBuildException {
    try {
      File tmpFile = FileUtil.createTempFile(myOutputDir, "osmorc.", ".bnd", true);

      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmpFile));
      try {
        JpsOrderedProperties props = JpsOrderedProperties.loadFromMap(contents);
        JpsModule module = mySession.getModule();
        String comments = "Generated by Osmorc for build of module " + module.getName() + " in project " + module.getProject().getName();
        props.store(bos, comments);
      }
      finally {
        bos.close();
      }

      tmpFile.deleteOnExit();
      return tmpFile;
    }
    catch (IOException e) {
      throw new OsmorcBuildException("Problem when generating bnd file", e);
    }
  }
}
