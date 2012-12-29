package com.jetbrains.lang.dart.analyzer;

import com.google.dart.compiler.CommandLineOptions;
import com.google.dart.compiler.CompilerConfiguration;

import java.io.File;
import java.util.List;

public class CompilerOptionsWrapper extends CommandLineOptions.CompilerOptions {

  private final CommandLineOptions.CompilerOptions myOptions;

  public CompilerOptionsWrapper(CommandLineOptions.CompilerOptions options) {
    myOptions = options;
  }

  @Override
  public String getJvmMetricOptions() {
    return myOptions.getJvmMetricOptions();
  }

  @Override
  public String getPlatformName() {
    return myOptions.getPlatformName();
  }

  @Override
  public File getPackageRoot() {
    return myOptions.getPackageRoot();
  }

  @Override
  public File getDartSdkPath() {
    return myOptions.getDartSdkPath();
  }

  @Override
  public boolean suppressSdkWarnings() {
    return myOptions.suppressSdkWarnings();
  }

  @Override
  public boolean typeChecksForInferredTypes() {
    return myOptions.typeChecksForInferredTypes();
  }

  @Override
  public boolean reportNoMemberWhenHasInterceptor() {
    return myOptions.reportNoMemberWhenHasInterceptor();
  }

  @Override
  public List<String> getSourceFiles() {
    return myOptions.getSourceFiles();
  }

  @Override
  public File getWorkDirectory() {
    return myOptions.getWorkDirectory();
  }

  @Override
  public boolean ignoreUnrecognizedFlags() {
    return myOptions.ignoreUnrecognizedFlags();
  }

  @Override
  public boolean buildIncrementally() {
    return myOptions.buildIncrementally();
  }

  @Override
  public boolean shouldBatch() {
    return myOptions.shouldBatch();
  }

  @Override
  public boolean resolveDespiteParseErrors() {
    return myOptions.resolveDespiteParseErrors();
  }

  @Override
  public boolean showHelp() {
    return myOptions.showHelp();
  }

  @Override
  public boolean showJvmMetrics() {
    return myOptions.showJvmMetrics();
  }

  @Override
  public boolean showMetrics() {
    return myOptions.showMetrics();
  }

  @Override
  public boolean showVersion() {
    return myOptions.showVersion();
  }

  @Override
  public boolean showSourceFromAst() {
    return myOptions.showSourceFromAst();
  }

  @Override
  public boolean typeErrorsAreFatal() {
    return myOptions.typeErrorsAreFatal();
  }

  @Override
  public boolean warningsAreFatal() {
    return myOptions.warningsAreFatal();
  }

  @Override
  public boolean developerModeChecks() {
    return myOptions.developerModeChecks();
  }

  @Override
  public CompilerConfiguration.ErrorFormat printErrorFormat() {
    return myOptions.printErrorFormat();
  }
}
