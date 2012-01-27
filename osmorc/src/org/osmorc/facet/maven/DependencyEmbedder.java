package org.osmorc.facet.maven;
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
 */

/**
 * Modified for usage within IntelliJ IDEA.
 */

import aQute.lib.osgi.Analyzer;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.idea.maven.model.MavenArtifact;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;


/**
 * Add BND directives to embed selected dependencies inside a bundle
 *
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public final class DependencyEmbedder extends AbstractDependencyFilter {
  public static final String EMBED_DEPENDENCY = "Embed-Dependency";
  public static final String EMBED_DIRECTORY = "Embed-Directory";
  public static final String EMBED_STRIP_GROUP = "Embed-StripGroup";
  public static final String EMBED_STRIP_VERSION = "Embed-StripVersion";
  public static final String EMBED_TRANSITIVE = "Embed-Transitive";

  public static final String EMBEDDED_ARTIFACTS = "Embedded-Artifacts";

  private static final String MAVEN_DEPENDENCIES = "{maven-dependencies}";

  private String myEmbedDirectory;
  private String myEmbedStripGroup;
  private String myEmbedStripVersion;


  /**
   * Inlined paths.
   */
  private final LinkedHashSet<String> myInlinedPaths;

  /**
   * Embedded artifacts.
   */
  private final Collection<MavenArtifact> myEmbeddedArtifacts;


  public DependencyEmbedder(Collection<MavenArtifact> dependencyArtifacts) {
    super(dependencyArtifacts);


    myInlinedPaths = new LinkedHashSet<String>();
    myEmbeddedArtifacts = new LinkedHashSet<MavenArtifact>();
  }


  public void processHeaders(Analyzer analyzer) throws DependencyEmbedderException {
    StringBuffer includeResource = new StringBuffer();
    StringBuffer bundleClassPath = new StringBuffer();
    StringBuffer embeddedArtifacts = new StringBuffer();

    myInlinedPaths.clear();
    myEmbeddedArtifacts.clear();

    String embedDependencyHeader = analyzer.getProperty(EMBED_DEPENDENCY);
    if (StringUtils.isNotEmpty(embedDependencyHeader)) {
      myEmbedDirectory = analyzer.getProperty(EMBED_DIRECTORY);
      myEmbedStripGroup = analyzer.getProperty(EMBED_STRIP_GROUP, "true");
      myEmbedStripVersion = analyzer.getProperty(EMBED_STRIP_VERSION);

      processInstructions(embedDependencyHeader);

      for (String myInlinedPath : myInlinedPaths) {
        inlineDependency(myInlinedPath, includeResource);
      }
      for (MavenArtifact m_embeddedArtifact : myEmbeddedArtifacts) {
        embedDependency(m_embeddedArtifact, includeResource, bundleClassPath, embeddedArtifacts);
      }
    }

    if (analyzer.getProperty(Analyzer.WAB) == null && bundleClassPath.length() > 0) {
      // set explicit default before merging dependency classpath
      if (analyzer.getProperty(Analyzer.BUNDLE_CLASSPATH) == null) {
        analyzer.setProperty(Analyzer.BUNDLE_CLASSPATH, ".");
      }
    }

    appendDependencies(analyzer, Analyzer.INCLUDE_RESOURCE, includeResource.toString());
    appendDependencies(analyzer, Analyzer.BUNDLE_CLASSPATH, bundleClassPath.toString());
    appendDependencies(analyzer, EMBEDDED_ARTIFACTS, embeddedArtifacts.toString());
  }


  @Override
  protected void processDependencies(Collection<MavenArtifact> dependencies, String inline) {
    if (null == inline || "false".equalsIgnoreCase(inline)) {
      myEmbeddedArtifacts.addAll(dependencies);
    }
    else {
      for (Object dependency : dependencies) {
        addInlinedPaths((MavenArtifact)dependency, inline, myInlinedPaths);
      }
    }
  }


  private static void addInlinedPaths(MavenArtifact dependency, String inline, Collection<String> inlinedPaths) {
    File path = dependency.getFile();
    if (path.exists()) {
      if ("true".equalsIgnoreCase(inline) || inline.length() == 0) {
        inlinedPaths.add(path.getPath());
      }
      else {
        String[] filters = inline.split("\\|");
        for (String filter : filters) {
          if (filter.length() > 0) {
            inlinedPaths.add(path + "!/" + filter);
          }
        }
      }
    }
  }


  private void embedDependency(MavenArtifact dependency, StringBuffer includeResource, StringBuffer bundleClassPath,
                               StringBuffer embeddedArtifacts) {
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
        if (StringUtils.isNotEmpty(dependency.getClassifier())) {
          targetFileName.append('-').append(dependency.getClassifier());
        }
      }
      String extension = dependency.getExtension();
      if (StringUtils.isNotEmpty(extension)) {
        targetFileName.append('.').append(extension);
      }

      File targetFile = new File(embedDirectory, targetFileName.toString());

      String targetFilePath = targetFile.getPath();

      // replace windows backslash with a slash
      if (File.separatorChar != '/') {
        targetFilePath = targetFilePath.replace(File.separatorChar, '/');
      }

      if (includeResource.length() > 0) {
        includeResource.append(',');
      }

      includeResource.append(targetFilePath);
      includeResource.append('=');
      includeResource.append(sourceFile);

      if (bundleClassPath.length() > 0) {
        bundleClassPath.append(',');
      }

      bundleClassPath.append(targetFilePath);

      if (embeddedArtifacts.length() > 0) {
        embeddedArtifacts.append(',');
      }

      embeddedArtifacts.append(targetFilePath).append(';');
      embeddedArtifacts.append("g=\"").append(dependency.getGroupId()).append('"');
      embeddedArtifacts.append(";a=\"").append(dependency.getArtifactId()).append('"');
      embeddedArtifacts.append(";v=\"").append(dependency.getVersion()).append('"');
      if (StringUtils.isNotEmpty(dependency.getClassifier())) {
        embeddedArtifacts.append(";c=\"").append(dependency.getClassifier()).append('"');
      }
    }
  }


  private static void inlineDependency(String path, StringBuffer includeResource) {
    if (includeResource.length() > 0) {
      includeResource.append(',');
    }

    includeResource.append('@');
    includeResource.append(path);
  }


  public Collection<String> getInlinedPaths() {
    return myInlinedPaths;
  }


  public Collection<MavenArtifact> getEmbeddedArtifacts() {
    return myEmbeddedArtifacts;
  }


  private static void appendDependencies(Analyzer analyzer, String directiveName, String mavenDependencies) {
    /*
    * similar algorithm to {maven-resources} but default behaviour here is to append rather than override
    */
    final String instruction = analyzer.getProperty(directiveName);
    if (StringUtils.isNotEmpty(instruction)) {
      if (instruction.contains(MAVEN_DEPENDENCIES)) {
        // if there are no embeddded dependencies, we do a special treatment and replace
        // every occurance of MAVEN_DEPENDENCIES and a following comma with an empty string
        if (mavenDependencies.length() == 0) {
          String cleanInstruction = removeTagFromInstruction(instruction, MAVEN_DEPENDENCIES);
          analyzer.setProperty(directiveName, cleanInstruction);
        }
        else {
          String mergedInstruction = StringUtils.replace(instruction, MAVEN_DEPENDENCIES, mavenDependencies);
          analyzer.setProperty(directiveName, mergedInstruction);
        }
      }
      else if (mavenDependencies.length() > 0) {
        if (Analyzer.INCLUDE_RESOURCE.equalsIgnoreCase(directiveName)) {
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
    else if (mavenDependencies.length() > 0) {
      analyzer.setProperty(directiveName, mavenDependencies);
    }
    // otherwise leave instruction unchanged
  }


  static String removeTagFromInstruction(String instruction, String tag) {
    StringBuilder buf = new StringBuilder();

    String[] clauses = instruction.split(",");
    for (String clause1 : clauses) {
      String clause = clause1.trim();
      if (!tag.equals(clause)) {
        if (buf.length() > 0) {
          buf.append(',');
        }
        buf.append(clause);
      }
    }

    return buf.toString();
  }
}
