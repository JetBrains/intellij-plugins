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
package org.osmorc.maven.facet;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Constants;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.idea.maven.model.MavenArtifact;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Add BND directives to embed selected dependencies inside a bundle
 *
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public final class DependencyEmbedder extends AbstractDependencyFilter {
  public static final String EMBED_DEPENDENCY = "Embed-Dependency";
  public static final String EMBED_DIRECTORY = "Embed-Directory";
  public static final String EMBED_STRIP_GROUP = "Embed-StripGroup";
  public static final String EMBED_STRIP_VERSION = "Embed-StripVersion";
  public static final String EMBED_TRANSITIVE = "Embed-Transitive";
  public static final String EMBEDDED_ARTIFACTS = "Embedded-Artifacts";
  public static final String MAVEN_DEPENDENCIES = "{maven-dependencies}";

  private String myEmbedDirectory;
  private String myEmbedStripGroup;
  private String myEmbedStripVersion;
  private final LinkedHashSet<String> myInlinePaths;
  private final Collection<MavenArtifact> myEmbeddedArtifacts;

  public DependencyEmbedder(Collection<MavenArtifact> dependencyArtifacts) {
    super(dependencyArtifacts);
    myInlinePaths = new LinkedHashSet<>();
    myEmbeddedArtifacts = new LinkedHashSet<>();
  }

  public void processHeaders(Analyzer analyzer) throws DependencyEmbedderException {
    StringBuilder includeResource = new StringBuilder();
    StringBuilder bundleClassPath = new StringBuilder();
    StringBuilder embeddedArtifacts = new StringBuilder();

    myInlinePaths.clear();
    myEmbeddedArtifacts.clear();

    String embedDependencyHeader = analyzer.getProperty(EMBED_DEPENDENCY);
    if (StringUtil.isNotEmpty(embedDependencyHeader)) {
      myEmbedDirectory = analyzer.getProperty(EMBED_DIRECTORY);
      myEmbedStripGroup = analyzer.getProperty(EMBED_STRIP_GROUP, "true");
      myEmbedStripVersion = analyzer.getProperty(EMBED_STRIP_VERSION);

      processInstructions(embedDependencyHeader);

      for (String inlinePath : myInlinePaths) {
        inlineDependency(inlinePath, includeResource);
      }
      for (MavenArtifact embeddedArtifact : myEmbeddedArtifacts) {
        embedDependency(embeddedArtifact, includeResource, bundleClassPath, embeddedArtifacts);
      }
    }

    if (analyzer.getProperty(Constants.WAB) == null && !bundleClassPath.isEmpty()) {
      // set explicit default before merging dependency classpath
      if (analyzer.getProperty(Constants.BUNDLE_CLASSPATH) == null) {
        analyzer.setProperty(Constants.BUNDLE_CLASSPATH, ".");
      }
    }

    appendDependencies(analyzer, Constants.INCLUDE_RESOURCE, includeResource.toString());
    appendDependencies(analyzer, Constants.BUNDLE_CLASSPATH, bundleClassPath.toString());
    appendDependencies(analyzer, EMBEDDED_ARTIFACTS, embeddedArtifacts.toString());
  }

  @Override
  protected void processDependencies(Collection<MavenArtifact> dependencies, String inline) {
    if (null == inline || "false".equalsIgnoreCase(inline)) {
      myEmbeddedArtifacts.addAll(dependencies);
    }
    else {
      for (MavenArtifact dependency : dependencies) {
        addInlinePaths(dependency, inline, myInlinePaths);
      }
    }
  }

  private static void addInlinePaths(MavenArtifact dependency, String inline, Collection<String> inlinePaths) {
    File path = dependency.getFile();
    if (path.exists()) {
      if ("true".equalsIgnoreCase(inline) || inline.isEmpty()) {
        inlinePaths.add(path.getPath());
      }
      else {
        String[] filters = inline.split("\\|");
        for (String filter : filters) {
          if (!filter.isEmpty()) {
            inlinePaths.add(path + "!/" + filter);
          }
        }
      }
    }
  }

  private void embedDependency(MavenArtifact dependency, StringBuilder includeResource, StringBuilder bundleClassPath,
                               StringBuilder embeddedArtifacts) {
    File sourceFile = dependency.getFile();
    if (sourceFile.exists()) {
      String embedDirectory = myEmbedDirectory;
      if (myEmbedDirectory == null || embedDirectory.isEmpty() || ".".equals(embedDirectory)) {
        embedDirectory = null;
      }

      if (!Boolean.valueOf(myEmbedStripGroup).booleanValue()) {
        embedDirectory = new File(embedDirectory, dependency.getGroupId()).getPath();
      }

      StringBuilder targetFileName = new StringBuilder();
      targetFileName.append(dependency.getArtifactId());
      if (!Boolean.valueOf(myEmbedStripVersion).booleanValue()) {
        targetFileName.append('-').append(dependency.getVersion());
        if (StringUtil.isNotEmpty(dependency.getClassifier())) {
          targetFileName.append('-').append(dependency.getClassifier());
        }
      }
      String extension = dependency.getExtension();
      if (StringUtil.isNotEmpty(extension)) {
        targetFileName.append('.').append(extension);
      }

      File targetFile = new File(embedDirectory, targetFileName.toString());

      String targetFilePath = targetFile.getPath();

      // replace windows backslash with a slash
      if (File.separatorChar != '/') {
        targetFilePath = targetFilePath.replace(File.separatorChar, '/');
      }

      if (!includeResource.isEmpty()) {
        includeResource.append(',');
      }

      includeResource.append(targetFilePath);
      includeResource.append('=');
      includeResource.append(sourceFile);

      if (!bundleClassPath.isEmpty()) {
        bundleClassPath.append(',');
      }

      bundleClassPath.append(targetFilePath);

      if (!embeddedArtifacts.isEmpty()) {
        embeddedArtifacts.append(',');
      }

      embeddedArtifacts.append(targetFilePath).append(';');
      embeddedArtifacts.append("g=\"").append(dependency.getGroupId()).append('"');
      embeddedArtifacts.append(";a=\"").append(dependency.getArtifactId()).append('"');
      embeddedArtifacts.append(";v=\"").append(dependency.getVersion()).append('"');
      if (StringUtil.isNotEmpty(dependency.getClassifier())) {
        embeddedArtifacts.append(";c=\"").append(dependency.getClassifier()).append('"');
      }
    }
  }

  private static void inlineDependency(String path, StringBuilder includeResource) {
    if (!includeResource.isEmpty()) {
      includeResource.append(',');
    }

    includeResource.append('@');
    includeResource.append(path);
  }

  private static void appendDependencies(Analyzer analyzer, String directiveName, String mavenDependencies) {
    // similar algorithm to {maven-resources} but default behaviour here is to append rather than override
    final String instruction = analyzer.getProperty(directiveName);
    if (StringUtil.isNotEmpty(instruction)) {
      if (instruction.contains(MAVEN_DEPENDENCIES)) {
        // if there are no embedded dependencies, we do a special treatment and replace
        // every occurrence of MAVEN_DEPENDENCIES and a following comma with an empty string
        if (mavenDependencies.isEmpty()) {
          String cleanInstruction = ImporterUtil.removeTagFromInstruction(instruction, MAVEN_DEPENDENCIES);
          analyzer.setProperty(directiveName, cleanInstruction);
        }
        else {
          String mergedInstruction = StringUtil.replace(instruction, MAVEN_DEPENDENCIES, mavenDependencies);
          analyzer.setProperty(directiveName, mergedInstruction);
        }
      }
      else if (!mavenDependencies.isEmpty()) {
        if (Constants.INCLUDE_RESOURCE.equalsIgnoreCase(directiveName)) {
          // dependencies should be prepended so they can be overwritten by local resources
          analyzer.setProperty(directiveName, mavenDependencies + ',' + instruction);
        }
        else
        // Analyzer.BUNDLE_CLASSPATH
        {
          // for the classpath we want dependencies to be appended after local entries
          analyzer.setProperty(directiveName, instruction + ',' + mavenDependencies);
        }
      }
      // otherwise leave instruction unchanged
    }
    else if (!mavenDependencies.isEmpty()) {
      analyzer.setProperty(directiveName, mavenDependencies);
    }
    // otherwise leave instruction unchanged
  }
}
