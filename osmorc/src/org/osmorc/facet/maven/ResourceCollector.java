package org.osmorc.facet.maven;

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Constants;
import com.intellij.openapi.ui.MessageType;
import com.intellij.util.ArrayUtil;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenResource;
import org.jetbrains.idea.maven.project.MavenProject;
import org.osmorc.OsmorcProjectComponent;

import java.io.File;
import java.util.*;

/**
 * Class which collects resources from a maven projects and modifies the imported bnd instructions to incorporate them.
 */
public class ResourceCollector {
  private static final String MAVEN_RESOURCES = "{maven-resources}";
  private static final String[] DEFAULT_INCLUDES = {"**/**"};


  public static void includeMavenResources(@NotNull MavenProject currentProject, @NotNull Analyzer analyzer) {
    // pass maven resource paths onto BND analyzer
    final String mavenResourcePaths = getMavenResourcePaths(currentProject);
    final String includeResource = (String)analyzer.getProperty(Constants.INCLUDE_RESOURCE);
    if (includeResource != null) {
      if (includeResource.contains(MAVEN_RESOURCES)) {
        // if there is no maven resource path, we do a special treatment and replace
        // every occurance of MAVEN_RESOURCES and a following comma with an empty string
        if (mavenResourcePaths.length() == 0) {
          String cleanedResource = ImporterUtil.removeTagFromInstruction(includeResource, MAVEN_RESOURCES);
          if (cleanedResource.length() > 0) {
            analyzer.setProperty(Constants.INCLUDE_RESOURCE, cleanedResource);
          }
          else {
            analyzer.unsetProperty(Constants.INCLUDE_RESOURCE);
          }
        }
        else {
          String combinedResource = StringUtils
            .replace(includeResource, MAVEN_RESOURCES, mavenResourcePaths);
          analyzer.setProperty(Constants.INCLUDE_RESOURCE, combinedResource);
        }
      }
      else if (mavenResourcePaths.length() > 0) {
        OsmorcProjectComponent.IMPORTANT_ERROR_NOTIFICATION
          .createNotification(Constants.INCLUDE_RESOURCE + ": overriding " + mavenResourcePaths + " with " + includeResource
                              + " (add " + MAVEN_RESOURCES + " if you want to include the maven resources)", MessageType.WARNING)
          .notify(null);
      }
    }
    else if (mavenResourcePaths.length() > 0) {
      analyzer.setProperty(Constants.INCLUDE_RESOURCE, mavenResourcePaths);
    }
  }


  private static String getMavenResourcePaths(@NotNull MavenProject currentProject) {
    final String basePath = currentProject.getDirectory();

    Set<String> pathSet = new LinkedHashSet<String>();
    for (MavenResource resource : getMavenResources(currentProject)) {
      final String sourcePath = resource.getDirectory();
      final String targetPath = resource.getTargetPath();

      // ignore empty or non-local resources
      if (new File(sourcePath).exists() && ((targetPath == null) || (!targetPath.contains("..")))) {
        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir(sourcePath);
        if (resource.getIncludes() != null && !resource.getIncludes().isEmpty()) {
          scanner.setIncludes(ArrayUtil.toStringArray(resource.getIncludes()));
        }
        else {
          scanner.setIncludes(DEFAULT_INCLUDES);
        }

        if (resource.getExcludes() != null && !resource.getExcludes().isEmpty()) {
          scanner.setExcludes(ArrayUtil.toStringArray(resource.getExcludes()));
        }

        scanner.addDefaultExcludes();
        scanner.scan();

        List<String> includedFiles = Arrays.asList(scanner.getIncludedFiles());

        for (Object includedFile : includedFiles) {
          String name = (String)includedFile;
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
    List<MavenResource> resources = new ArrayList<MavenResource>(currentProject.getResources());

    // also scan for any "packageinfo" files lurking in the source folders
    List<String> packageInfoIncludes = Collections.singletonList("**/packageinfo");

    for (String sourceRoot : currentProject.getSources()) {
      MavenResource packageInfoResource = new MavenResource(sourceRoot, false, null, packageInfoIncludes, null);
      resources.add(packageInfoResource);
    }

    return resources;
  }
}
