package com.intellij.flex.maven;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

public interface FlexConfigGenerator {
  void generate(Mojo configuration, File sourceFile) throws Exception;

  void generate(Mojo configuration) throws Exception;

  void preGenerate(MavenProject project, String classifier, AdditionalSourcePathProvider additionalSourcePathProvider) throws IOException;

  String postGenerate(MavenProject project) throws IOException;
}
