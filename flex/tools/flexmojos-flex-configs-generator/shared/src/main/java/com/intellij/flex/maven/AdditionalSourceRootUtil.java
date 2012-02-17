package com.intellij.flex.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.logging.Logger;

import java.io.File;

public final class AdditionalSourceRootUtil {
  public static void addByUnknownGeneratorMojo(MavenProject project) {
    // IDEA-58453
    File generatedSources = new File(project.getBuild().getDirectory(), "/generated-sources");
    if (generatedSources.isDirectory()) {
      File[] files = generatedSources.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isDirectory() && !file.isHidden()) {
            addCompilerSourceRoot(project, file);
          }
        }
      }
    }
  }

  private static void addCompilerSourceRoot(MavenProject project, File file) {
    String filename = file.getAbsolutePath();
    if (!project.getCompileSourceRoots().contains(filename)) {
      project.addCompileSourceRoot(filename);
    }
  }

  private static Object evaluate(ExpressionEvaluator expressionEvaluator, String value, Logger logger) {
    try {
      return expressionEvaluator.evaluate(value);
    }
    catch (ExpressionEvaluationException e) {
      logger.error("Can't evaluate " + value, e);
      return null;
    }
  }

  public static void addByBuildHelper(MojoExecution mojoExecution, MavenSession session, MavenProject project, Logger logger) {
    final PlexusConfiguration parentConfiguration = new XmlPlexusConfiguration(mojoExecution.getConfiguration());
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
      addFile(evaluate(expressionEvaluator, source.getValue(), logger), project, expressionEvaluator);
    }
  }

  public static void addByGeneratorMojo(MojoExecution mojoExecution, MavenSession session, MavenProject project, Logger logger) {
    final PluginParameterExpressionEvaluator expressionEvaluator = new PluginParameterExpressionEvaluator(session, mojoExecution);
    final PlexusConfiguration configuration = new XmlPlexusConfiguration(mojoExecution.getConfiguration());
    for (String parameterName : new String[]{"baseOutputDirectory", "outputDirectory"}) {
      collectGeneratedSource(configuration, parameterName, project, expressionEvaluator, logger);
    }
  }

  private static void collectGeneratedSource(PlexusConfiguration parentConfiguration, String parameterName, MavenProject project, PluginParameterExpressionEvaluator expressionEvaluator, Logger logger) {
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

      filepath = (String)evaluate(expressionEvaluator, defaultValue, logger);
    }

    if (filepath != null) {
      addFile(filepath, project, expressionEvaluator);
    }
  }

  private static void addFile(Object path, MavenProject project, ExpressionEvaluator expressionEvaluator) {
    if (path == null) {
      return;
    }

    addCompilerSourceRoot(project, expressionEvaluator.alignToBaseDirectory(new File((String)path)));
  }
}