package com.intellij.flex.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdditionalSourcePathProvider {
  private final List<MojoExecution> mojoExecutions;

  private ExpressionEvaluator flexmojosGeneratorExpressionEvaluator;

  public AdditionalSourcePathProvider(List<MojoExecution> sourceProviderMojoExecutions) {
    mojoExecutions = sourceProviderMojoExecutions;
  }

  public List<File> merge(File[] existing, MavenSession session) throws ExpressionEvaluationException {
    final List<File> existingList = new ArrayList<File>();
    Collections.addAll(existingList, existing);

    for (MojoExecution mojoExecution : mojoExecutions) {
      final PlexusConfiguration configuration = new XmlPlexusConfiguration(mojoExecution.getConfiguration());
      if (mojoExecution.getArtifactId().equals("flexmojos-generator-mojo")) {
        for (String parameterName : new String[]{"baseOutputDirectory", "outputDirectory"}) {
          collectGeneratedSource(configuration, parameterName, existingList, session, mojoExecution);
        }
        flexmojosGeneratorExpressionEvaluator = null;
      }
      else {
        collectAddedSource(configuration, existingList, session, mojoExecution);
      }
    }

    return existingList;
  }

  private void collectAddedSource(PlexusConfiguration parentConfiguration, List<File> existingList, MavenSession session, MojoExecution mojoExecution) throws ExpressionEvaluationException {
    final PlexusConfiguration configuration = parentConfiguration.getChild("sources");
    if (configuration == null) {
      return;
    }

    final PlexusConfiguration[] sources = configuration.getChildren();
    if (sources == null) {
      return;
    }

    final ExpressionEvaluator expressionEvaluator = new PluginParameterExpressionEvaluator(session, mojoExecution);
    for (PlexusConfiguration source : sources) {
      addFile(expressionEvaluator.evaluate(source.getValue()), existingList, expressionEvaluator);
    }
  }

  private void collectGeneratedSource(PlexusConfiguration parentConfiguration, String parameterName, List<File> existingList, MavenSession session, MojoExecution mojoExecution) throws ExpressionEvaluationException {
    final PlexusConfiguration configuration = parentConfiguration.getChild(parameterName);
    if (configuration == null) {
      return;
    }

    String filepath = configuration.getValue();
    if (filepath == null) {
      final String defaultValue = configuration.getAttribute("default-value");
      if (defaultValue == null) {
        return;
      }

      if (flexmojosGeneratorExpressionEvaluator == null) {
        flexmojosGeneratorExpressionEvaluator = new PluginParameterExpressionEvaluator(session, mojoExecution);
      }

      filepath = (String)flexmojosGeneratorExpressionEvaluator.evaluate(defaultValue);
    }

    if (filepath != null) {
      addFile(filepath, existingList, flexmojosGeneratorExpressionEvaluator);
    }
  }

  private void addFile(Object path, List<File> existingList, ExpressionEvaluator expressionEvaluator) {
    File file = expressionEvaluator.alignToBaseDirectory(new File((String)path));
    if (!existingList.contains(file)) {
      existingList.add(file);
    }
  }
}