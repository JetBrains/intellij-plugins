package org.osmorc.facet.maven;

import aQute.lib.osgi.Analyzer;
import com.intellij.notification.NotificationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;
import org.osmorc.OsmorcProjectComponent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for importing settings from the maven bundle plugin.
 */
public class ImporterUtil {

  private static final Pattern FUZZY_VERSION = Pattern.compile("(\\d+)(\\.(\\d+)(\\.(\\d+))?)?([^a-zA-Z0-9](.*))?",
                                                               Pattern.DOTALL);

  private ImporterUtil() {
  }

  /**
   * Removes a tag (like {maven-resources}) from the given instruction string.
   * @param instruction the instruction string.
   * @param tag the tag  to remove
   * @return the string wihtout the tag.
   */
  @NotNull
  static String removeTagFromInstruction(@NotNull String instruction,@NotNull String tag) {
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

  /**
   * Collects the dependencies for the given project. Takes into account any transitive embedding instructions.
   *
   * @param props   the properties with additional instructions
   * @param project the maven project
   * @return a collection of dependencies.
   */
  @NotNull
  static Collection<MavenArtifact> collectDependencies(@NotNull Map<String, String> props, @NotNull MavenProject project) {
    Collection<MavenArtifact> dependencies;
    if (Boolean.parseBoolean(props.get(DependencyEmbedder.EMBED_TRANSITIVE))) {
      Set<MavenArtifact> processed = new HashSet<MavenArtifact>();

      // flatten the tree while taking care of endless recursions
      LinkedList<MavenArtifactNode> nodes = new LinkedList<MavenArtifactNode>(project.getDependencyTree());
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
   *
   * @param VERSION_STRING
   * @return
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
   *
   * @param props   the properties
   * @param project the maven project.
   */
  static void postprocessAdditionalProperties(final Map<String, String> props, MavenProject project) {
    Analyzer myFakeAnalyzer = new Analyzer() {
      @Override
      public String getProperty(String key) {
        return props.get(key);
      }

      @Override
      public String getProperty(String key, String deflt) {
        if (props.containsKey(key)) {
          return key;
        }
        else {
          return deflt;
        }
      }

      @Override
      public void setProperty(String key, String value) {
        props.put(key, value);
      }
    };

    Collection<MavenArtifact> dependencies = collectDependencies(props, project);

    DependencyEmbedder embedder = new DependencyEmbedder(dependencies);
    try {
      embedder.processHeaders(myFakeAnalyzer);
    }
    catch (DependencyEmbedderException e) {
      OsmorcProjectComponent.IMPORTANT_ERROR_NOTIFICATION
        .createNotification("Error when processing Embed-Dependency directive in " + project.getPath() + ": " + e.getMessage(),
                            NotificationType.ERROR).notify(null);
    }
    ResourceCollector.includeMavenResources(project, myFakeAnalyzer);
  }
}
