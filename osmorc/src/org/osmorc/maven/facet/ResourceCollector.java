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
import com.intellij.util.ArrayUtilRt;
import org.codehaus.plexus.util.DirectoryScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenResource;
import org.jetbrains.idea.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Class which collects resources from a maven projects and modifies the imported bnd instructions to incorporate them.
 */
public final class ResourceCollector {
  private static final String MAVEN_RESOURCES = "{maven-resources}";
  private static final String[] DEFAULT_INCLUDES = {"**/**"};

  public static void includeMavenResources(@NotNull MavenProject mavenProject, @NotNull Analyzer analyzer) {
    // pass maven resource paths onto BND analyzer
    String includeResource = analyzer.getProperty(Constants.INCLUDE_RESOURCE);
    if (includeResource != null) {
      if (includeResource.contains(MAVEN_RESOURCES)) {
        // if there is no maven resource path, we do a special treatment and replace
        // every occurrence of MAVEN_RESOURCES and a following comma with an empty string
        String mavenResourcePaths = getMavenResourcePaths(mavenProject);
        if (mavenResourcePaths.isEmpty()) {
          String cleanedResource = ImporterUtil.removeTagFromInstruction(includeResource, MAVEN_RESOURCES);
          if (!cleanedResource.isEmpty()) {
            analyzer.setProperty(Constants.INCLUDE_RESOURCE, cleanedResource);
          }
          else {
            analyzer.unsetProperty(Constants.INCLUDE_RESOURCE);
          }
        }
        else {
          String combinedResource = StringUtil.replace(includeResource, MAVEN_RESOURCES, mavenResourcePaths);
          analyzer.setProperty(Constants.INCLUDE_RESOURCE, combinedResource);
        }
      }
    }
    else {
      String mavenResourcePaths = getMavenResourcePaths(mavenProject);
      if (!mavenResourcePaths.isEmpty()) {
        analyzer.setProperty(Constants.INCLUDE_RESOURCE, mavenResourcePaths);
      }
    }
  }

  private static String getMavenResourcePaths(@NotNull MavenProject currentProject) {
    Set<String> pathSet = new LinkedHashSet<>();
    for (MavenResource resource : getMavenResources(currentProject)) {
      final String sourcePath = resource.getDirectory();
      final String targetPath = resource.getTargetPath();

      // ignore empty or non-local resources
      if (new File(sourcePath).exists() && ((targetPath == null) || (!targetPath.contains("..")))) {
        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir(sourcePath);
        if (resource.getIncludes() != null && !resource.getIncludes().isEmpty()) {
          scanner.setIncludes(ArrayUtilRt.toStringArray(resource.getIncludes()));
        }
        else {
          scanner.setIncludes(DEFAULT_INCLUDES);
        }

        if (resource.getExcludes() != null && !resource.getExcludes().isEmpty()) {
          scanner.setExcludes(ArrayUtilRt.toStringArray(resource.getExcludes()));
        }

        scanner.addDefaultExcludes();
        scanner.scan();

        String[] includedFiles = scanner.getIncludedFiles();

        for (String includedFile : includedFiles) {
          String name = includedFile;
          String path = sourcePath + '/' + name;


          // make relative to project
          //if (path.startsWith(basePath)) {
          //  if (path.length() == basePath.length()) {
          //    path = ".";
          //  }
          //  else {
          //    path = path.substring(basePath.length() + 1);
          //  }
          //}

          // replace windows backslash with a slash
          // this is a workaround for a problem with bnd 0.0.189
          if (File.separatorChar != '/') {
            name = name.replace(File.separatorChar, '/');
            path = path.replace(File.separatorChar, '/');
          }

          // copy to correct place
          path = name + '=' + path;
          if (targetPath != null) {
            path = targetPath + '/' + path;
          }

          // use Bnd filtering?
          if (resource.isFiltered()) {
            path = '{' + path + '}';
          }

          pathSet.add(path);
        }
      }
    }

    StringBuilder resourcePaths = new StringBuilder();
    for (Iterator<String> i = pathSet.iterator(); i.hasNext(); ) {
      resourcePaths.append(i.next());
      if (i.hasNext()) {
        resourcePaths.append(',');
      }
    }

    return resourcePaths.toString();
  }

  private static List<MavenResource> getMavenResources(MavenProject currentProject) {
    List<MavenResource> resources = new ArrayList<>(currentProject.getResources());

    // also scan for any "packageinfo" files lurking in the source folders
    List<String> packageInfoIncludes = Collections.singletonList("**/packageinfo");

    for (String sourceRoot : currentProject.getSources()) {
      MavenResource packageInfoResource = new MavenResource(sourceRoot, false, null, packageInfoIncludes, null);
      resources.add(packageInfoResource);
    }

    return resources;
  }
}
