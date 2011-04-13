package com.intellij.flex.maven;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.compiler.ICommandLineConfiguration;
import org.sonatype.flexmojos.compiler.ICompcConfiguration;
import org.sonatype.flexmojos.compiler.command.Result;
import org.sonatype.flexmojos.plugin.compiler.AbstractFlexCompilerMojo;
import org.sonatype.flexmojos.plugin.compiler.CompcMojo;
import org.sonatype.flexmojos.plugin.utilities.SourceFileResolver;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWF;

/**
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @extendsPlugin flexmojos-maven-plugin
 * @extendsGoal compile-swc
 * @goal generateConfig
 * @requiresDependencyResolution compile
 * @threadSafe
 * @phase compile
 */
public class IdeaConfigurationMojo extends CompcMojo implements ICommandLineConfiguration {
  /**
   * DOCME Again, undocumented by adobe
   * <p>
   * Equivalent to -file-specs§§§§§§§§§
   * </p>
   * Usage:
   * <p/>
   * <pre>
   * &lt;fileSpecs&gt;
   *   &lt;fileSpec&gt;???&lt;/fileSpec&gt;
   *   &lt;fileSpec&gt;???&lt;/fileSpec&gt;
   * &lt;/fileSpecs&gt;
   * </pre>
   *
   * @parameter
   */
  private List<String> fileSpecs;

  /**
   * DOCME Another, undocumented by adobe
   * <p>
   * Equivalent to -projector
   * </p>
   *
   * @parameter expression="${flex.projector}"
   */
  private String projector;

  @Override
  public Result doCompile(ICompcConfiguration cfg, boolean synchronize) throws Exception {
    throw new UnsupportedOperationException("This is not a compilation mojo");
  }

  @SuppressWarnings("unchecked")
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!(packaging.equals(SWC) || packaging.equals(SWF))) {
      return;
    }

    IdeaConfigurator configurator = new IdeaConfigurator();
    try {
      configurator.init(project, classifier);
      if (SWC.equals(getProjectType())) {
        configurator.buildConfiguration(this);
      }
      else {
        configurator.buildConfiguration(this, getSourceFile());
      }
    }
    catch (Exception e) {
      throw new MojoExecutionException("Failed to execute configurator: " + e.getMessage(), e);
    }
  }

  public List<String> getFileSpecs() {
    return fileSpecs;
  }

  public String getProjector() {
    return projector;
  }

  @Override
  public String getProjectType() {
    return packaging;
  }

  /**
   * The file to be compiled. The path must be relative with source folder
   *
   * @parameter expression="${flex.sourceFile}"
   */
  private String sourceFile;

  protected File getSourceFile() {
    return SourceFileResolver.resolveSourceFile(project.getCompileSourceRoots(), sourceFile, project.getGroupId(),
                                                project.getArtifactId());
  }

  /**
   * @parameter default-value="true"
   */
  @SuppressWarnings({"FieldCanBeLocal"}) private boolean useDefaultLocale = true;

  @Override
  public String[] getLocale() {
    if (SWC.equals(getProjectType())) {
      return super.getLocale();
    }

    if (!useDefaultLocale) {
      return new String[]{};
    }

    String[] locales;
    try {
      locales = getLocale2();
    }
    catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    if (locales != null) {
      return locales;
    }

    if ("css".equalsIgnoreCase(FilenameUtils.getExtension(sourceFile))) {
      return new String[]{};
    }

    return new String[]{toolsLocale};
  }

  public String[] getLocale2() throws NoSuchFieldException, IllegalAccessException {
    Field localesCompiledField = AbstractFlexCompilerMojo.class.getDeclaredField("localesCompiled");
    localesCompiledField.setAccessible(true);
    String[] localesCompiled = (String[])localesCompiledField.get(this);
    if (localesCompiled != null) {
      String[] locales = new String[localesCompiled.length];
      for (int i = 0; i < localesCompiled.length; i++) {
        String locale = localesCompiled[i];
        if (locale.contains(",")) {
          locale = locale.split(",")[0];
        }
        locales[i] = locale;
      }
      return locales;
    }

    // if there are runtime locales, no need for compiled locales
    if (getLocalesRuntime() != null) {
      return new String[]{};
    }

    return null;
  }
}
