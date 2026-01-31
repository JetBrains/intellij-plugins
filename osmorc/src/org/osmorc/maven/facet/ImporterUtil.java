// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.osmorc.maven.facet;

import aQute.bnd.header.OSGiHeader;
import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Constants;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.osgi.jps.build.FakeAnalyzer;
import org.osmorc.i18n.OsmorcBundle;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for importing settings from the maven bundle plugin.
 */
public final class ImporterUtil {
  private static final Pattern FUZZY_VERSION = Pattern.compile("(\\d+)(\\.(\\d+)(\\.(\\d+))?)?([^a-zA-Z0-9](.*))?", Pattern.DOTALL);

  private ImporterUtil() { }

  /**
   * Removes a tag (like {maven-resources}) from the given instruction string.
   *
   * @param instruction the instruction string.
   * @param tag         the tag  to remove
   * @return the string without the tag.
   */
  static @NotNull String removeTagFromInstruction(@NotNull String instruction, @NotNull String tag) {
    StringBuilder buf = new StringBuilder();

    String[] clauses = instruction.split(",");
    for (String clause1 : clauses) {
      String clause = clause1.trim();
      if (!tag.equals(clause)) {
        if (!buf.isEmpty()) {
          buf.append(',');
        }
        buf.append(clause);
      }
    }

    return buf.toString();
  }

  /**
   * Collects the dependencies for the given project. Takes into account any transitive embedding instructions.
   *
   * @param props   the properties with additional instructions
   * @param project the maven project
   * @return a collection of dependencies.
   */
  static @NotNull Collection<MavenArtifact> collectDependencies(@NotNull Map<String, String> props, @NotNull MavenProject project) {
    Collection<MavenArtifact> dependencies;
    if (Boolean.parseBoolean(props.get(DependencyEmbedder.EMBED_TRANSITIVE))) {
      Set<MavenArtifact> processed = new HashSet<>();

      // flatten the tree while taking care of endless recursions
      LinkedList<MavenArtifactNode> nodes = new LinkedList<>(project.getDependencyTree());
      while (!nodes.isEmpty()) {
        MavenArtifactNode node = nodes.pop();
        MavenArtifact artifact = node.getArtifact();
        if (!processed.contains(artifact)) {
          processed.add(artifact);
          nodes.addAll(node.getDependencies());
        }
      }
      dependencies = processed;
    }
    else {
      dependencies = project.getDependencies();
    }
    return dependencies;
  }

  /**
   * Clean up version parameters. Other builders use more fuzzy definitions of
   * the version syntax. This method cleans up such a version to match an OSGi
   * version.
   */
  static String cleanupVersion(String version) {
    StringBuffer result = new StringBuffer();
    Matcher m = FUZZY_VERSION.matcher(version);
    if (m.matches()) {
      String major = m.group(1);
      String minor = m.group(3);
      String micro = m.group(5);
      String qualifier = m.group(7);

      if (major != null) {
        result.append(major);
        if (minor != null) {
          result.append(".");
          result.append(minor);
          if (micro != null) {
            result.append(".");
            result.append(micro);
            if (qualifier != null) {
              result.append(".");
              cleanupModifier(result, qualifier);
            }
          }
          else if (qualifier != null) {
            result.append(".0.");
            cleanupModifier(result, qualifier);
          }
          else {
            result.append(".0");
          }
        }
        else if (qualifier != null) {
          result.append(".0.0.");
          cleanupModifier(result, qualifier);
        }
        else {
          result.append(".0.0");
        }
      }
    }
    else {
      result.append("0.0.0.");
      cleanupModifier(result, version);
    }
    return result.toString();
  }

  private static void cleanupModifier(StringBuffer result, String modifier) {
    for (int i = 0; i < modifier.length(); i++) {
      char c = modifier.charAt(i);
      if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
          || c == '-') {
        result.append(c);
      }
      else {
        result.append('_');
      }
    }
  }

  /**
   * Postprocessing step which handles Embed-Dependency and replaces placeholders on Import-Resources etc.
   */
  static void postProcessAdditionalProperties(@NotNull Map<String, String> props,
                                              @NotNull MavenProject mavenProject,
                                              @NotNull Project project) {
    Analyzer analyzer = new FakeAnalyzer(props);

    Collection<MavenArtifact> dependencies = collectDependencies(props, mavenProject);
    DependencyEmbedder embedder = new DependencyEmbedder(dependencies);
    try {
      embedder.processHeaders(analyzer);
    }
    catch (DependencyEmbedderException e) {
      String message = OsmorcBundle.message("maven.import.embed.error", mavenProject.getPath(), e.getMessage());
      OsmorcBundle.important("", message, NotificationType.ERROR).notify(project);
    }

    ResourceCollector.includeMavenResources(mavenProject, analyzer);

    // finally post-process the Include-Resources header to account for backslashes and relative paths
    sanitizeIncludedResources(props, mavenProject);
  }

  /**
   * Sanitizes the Include-Resource header by resolving relative paths to the maven project's root and converting backslashes to slashes.
   */
  private static void sanitizeIncludedResources(@NotNull Map<String, String> props, @NotNull MavenProject project) {
    String includeResourceHeader = props.get(Constants.INCLUDE_RESOURCE);
    if (StringUtil.isEmpty(includeResourceHeader)) {
      return;
    }

    Parameters parameters = OSGiHeader.parseHeader(includeResourceHeader);
    StringBuilder sanitizedHeader = new StringBuilder();
    for (Iterator<String> iterator = parameters.keySet().iterator(); iterator.hasNext(); ) {
      String name = iterator.next();
      String prefix = "";
      String suffix = "";
      if (StringUtil.startsWithChar(name, '{') && name.endsWith("}")) {
        name = name.substring(1, name.length() - 1).trim();
        prefix = "{" + prefix;
        suffix += "}";
      }

      String[] parts = name.split("\\s*=\\s*");
      String source = parts[0];
      String target = "";
      if (parts.length == 2) {
        source = parts[1];
        target = parts[0];
      }


      if (StringUtil.startsWithChar(source, '@')) {
        source = source.substring(1);
        prefix += "@";
      }

      String sanitizedSource = source.replace('\\', '/');
      String sanitizedTarget = target.replace('\\', '/');
      // check if it's relative
      VirtualFile relativeFile = VfsUtil.findRelativeFile(project.getDirectoryFile(), sanitizedSource.split("/"));
      if (relativeFile != null) {
        sanitizedSource = relativeFile.getPath();
      }
      sanitizedHeader.append(prefix);
      if (!StringUtil.isEmpty(sanitizedTarget)) {
        sanitizedHeader.append(sanitizedTarget).append("=");
      }
      sanitizedHeader.append(sanitizedSource);
      sanitizedHeader.append(suffix);
      if (iterator.hasNext()) {
        sanitizedHeader.append(",");
      }
    }
    props.put(Constants.INCLUDE_RESOURCE, sanitizedHeader.toString());
  }
}
