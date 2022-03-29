// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.service;

import com.google.gson.*;
import com.intellij.javascript.nodejs.execution.NodeTargetRun;
import com.intellij.javascript.nodejs.library.yarn.YarnPnpNodePackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.AutodetectLinterPackage;
import com.intellij.lang.javascript.linter.ExtendedLinterState;
import com.intellij.lang.javascript.linter.tslint.TsLintBundle;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.execution.TsLintOutputJsonParser;
import com.intellij.lang.javascript.linter.tslint.execution.TsLinterError;
import com.intellij.lang.javascript.service.*;
import com.intellij.lang.javascript.service.protocol.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EmptyConsumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;


public final class TsLintLanguageService extends JSLanguageServiceBase {
  private final static Logger LOG = Logger.getInstance(TsLintLanguageService.class);

  private final @NotNull VirtualFile myWorkingDirectory;
  private final @NotNull NodePackage myNodePackage;

  public TsLintLanguageService(@NotNull Project project, @NotNull NodePackage nodePackage, @NotNull VirtualFile workingDirectory) {
    super(project);
    myWorkingDirectory = workingDirectory;
    myNodePackage = nodePackage;
  }

  @NotNull
  public NodePackage getNodePackage() {
    return myNodePackage;
  }

  @Nullable
  public CompletableFuture<List<TsLinterError>> highlight(@NotNull VirtualFile virtualFile,
                                                          @Nullable VirtualFile config,
                                                          @Nullable String content,
                                                          @NotNull TsLintState state) {
    return createHighlightFuture(virtualFile, config, state,
                                 (filePath, configPath) -> new GetErrorsCommand(filePath, configPath,StringUtil.notNullize(content)));
  }

  @Nullable
  public CompletableFuture<List<TsLinterError>> highlightAndFix(@NotNull VirtualFile virtualFile, @NotNull TsLintState state) {
    VirtualFile config = TslintUtil.getConfig(state, myProject, virtualFile);
    //doesn't pass content (file should be saved before)
    return createHighlightFuture(virtualFile, config, state, FixErrorsCommand::new);
  }

  private CompletableFuture<List<TsLinterError>> createHighlightFuture(@NotNull VirtualFile virtualFile,
                                                                       @Nullable VirtualFile config,
                                                                       @NotNull TsLintState state,
                                                                       @NotNull BiFunction<LocalFilePath,LocalFilePath, BaseCommand> commandProvider) {
    String configFilePath = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(config);
    if (configFilePath == null) {
      if (state.getNodePackageRef() == AutodetectLinterPackage.INSTANCE) {
        return CompletableFuture.completedFuture(ContainerUtil.emptyList());
      }
      return CompletableFuture.completedFuture(Collections.singletonList(TsLinterError.createGlobalError(
        TsLintBundle.message("tslint.inspection.message.config.file.was.not.found"))));
    }
    String path = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(virtualFile);
    if (path == null) {
      return null;
    }

    final JSLanguageServiceQueue process = getProcess();
    if (process == null) {
      return CompletableFuture.completedFuture(Collections.singletonList(
        TsLinterError.createGlobalError(JSLanguageServiceUtil.getLanguageServiceCreationError(this))));
    }

    //doesn't pass content (file should be saved before)
    BaseCommand command = commandProvider.apply(LocalFilePath.create(path),
                                                    LocalFilePath.create(configFilePath));
    return process.execute(command, createHighlightProcessor(path));
  }

  @NotNull
  private JSLanguageServiceCommandProcessor<List<TsLinterError>> createHighlightProcessor(@NotNull String path) {
    return (object, answer) -> parseResults(answer, path, JSLanguageServiceUtil.getGson(this));
  }

  @Nullable
  private static List<TsLinterError> parseResults(@NotNull JSLanguageServiceAnswer answer, @NotNull String path, @NotNull Gson gson) {
    final JsonObject element = answer.getElement();
    final JsonElement error = element.get("error");
    if (error != null) {
      return Collections.singletonList(TsLinterError.createGlobalError(error.getAsString())); //NON-NLS
    }
    final JsonElement body = parseBody(element);
    if (body == null) return null;
    final String version = element.get("version").getAsString();
    final SemVer tsLintVersion = SemVer.parseFromText(version);
    final boolean isZeroBased = TsLintOutputJsonParser.isVersionZeroBased(tsLintVersion);
    final TsLintOutputJsonParser parser = new TsLintOutputJsonParser(path, body, isZeroBased, gson);
    return new ArrayList<>(parser.getErrors());
  }

  private static JsonElement parseBody(@NotNull JsonObject element) {
    final JsonElement body = element.get("body");
    if (body == null) {
      //we do not currently treat empty body as error in protocol
      return null;
    } else {
      if (body.isJsonPrimitive() && body.getAsJsonPrimitive().isString()) {
        final String bodyContent = StringUtil.unquoteString(body.getAsJsonPrimitive().getAsString());
        if (!StringUtil.isEmptyOrSpaces(bodyContent)) {
          try {
            return JsonParser.parseString(bodyContent);
          } catch (JsonParseException e) {
            LOG.info(String.format("Problem parsing body: '%s'\n%s", body, e.getMessage()), e);
          }
        }
      } else {
        LOG.info(String.format("Error body type, should be a string with json inside. Body:'%s'", body.getAsString()));
      }
    }
    return null;
  }

  @Override
  protected JSLanguageServiceQueue createLanguageServiceQueue() {
    return new JSLanguageServiceQueueImpl(myProject, new Protocol(myNodePackage, myWorkingDirectory, myProject), myProcessConnector,
                                          myDefaultReporter,
                                          new JSLanguageServiceDefaultCacheData());
  }

  @Override
  protected boolean needInitToolWindow() {
    return false;
  }

  private static abstract class BaseCommand implements JSLanguageServiceCommand, JSLanguageServiceSimpleCommand, JSLanguageServiceObject {
    public LocalFilePath filePath;
    @Nullable
    public LocalFilePath configPath;

    protected BaseCommand(LocalFilePath filePath, @Nullable LocalFilePath configPath) {
      this.filePath = filePath;
      this.configPath = configPath;
    }

    @NotNull
    @Override
    public JSLanguageServiceObject toSerializableObject() {
      return this;
    }
  }

  private static final class GetErrorsCommand extends BaseCommand{
    public String content;
    private GetErrorsCommand(LocalFilePath filePath, @Nullable LocalFilePath configPath, String content) {
      super(filePath, configPath);
      this.content = content;
    }

    @NotNull
    @Override
    public String getCommand() {
      return "GetErrors";
    }
  }

  private static final class FixErrorsCommand extends BaseCommand{
    private FixErrorsCommand(LocalFilePath filePath, @Nullable LocalFilePath configPath) {
      super(filePath, configPath);
    }

    @NotNull
    @Override
    public String getCommand() {
      return "FixErrors";
    }
  }

  private static final class Protocol extends JSLanguageServiceNodeStdProtocolBase {
    private final NodePackage myNodePackage;
    private final VirtualFile myWorkingDirectory;

    private Protocol(@NotNull NodePackage nodePackage, @NotNull VirtualFile workingDirectory, @NotNull Project project) {
      super("tslint", project, EmptyConsumer.getInstance());
      myNodePackage = nodePackage;
      myWorkingDirectory = workingDirectory;
    }

    @Override
    protected String getWorkingDirectory() {
      return JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(myWorkingDirectory);
    }

    @Override
    protected JSLanguageServiceInitialState createState() {
      InitialState result = new InitialState();
      ExtendedLinterState<TsLintState> extendedState = TsLintConfiguration.getInstance(myProject).getExtendedState();
      if (myNodePackage instanceof YarnPnpNodePackage) {
        result.tslintPackagePath = LocalFilePath.create(myNodePackage.getName());
        String packageJsonPath = ((YarnPnpNodePackage)myNodePackage).getPackageJsonPath(myProject);
        if (packageJsonPath == null) {
          throw new IllegalStateException("Cannot find package.json path for " + myNodePackage);
        }
        result.packageJsonPath = LocalFilePath.create(FileUtil.toSystemDependentName(packageJsonPath));
      }
      else {
        result.tslintPackagePath = LocalFilePath.create(myNodePackage.getSystemDependentPath());
      }
      result.additionalRootDirectory = LocalFilePath.create(extendedState.getState().getRulesDirectory());
      result.pluginName = "tslint";
      result.pluginPath = LocalFilePath.create(
        JSLanguageServiceUtil.getPluginDirectory(getClass(), "js/languageService/tslint-plugin-provider.js").getAbsolutePath());
      return result;
    }

    @Override
    protected void addNodeProcessAdditionalArguments(@NotNull NodeTargetRun targetRun) {
      super.addNodeProcessAdditionalArguments(targetRun);
      targetRun.path(JSLanguageServiceUtil.getPluginDirectory(getClass(), "js").getAbsolutePath());
    }

    @Override
    protected void addNodeProcessAdditionalArguments(@NotNull JSCommandLineBuilder commandLine) {
      super.addNodeProcessAdditionalArguments(commandLine);
      if (myServiceName != null) {
        JSLanguageServiceUtil.addNodeProcessArgumentsFromRegistry(commandLine, myServiceName,
                                                                  () -> Registry.stringValue("tslint.service.node.arguments"));
      }
    }

    @Override
    public void dispose() {
    }
  }

  private static class InitialState extends JSLanguageServiceInitialState {
    LocalFilePath tslintPackagePath;
    /**
     * Path to package.json declaring tslint dependency.
     * Allows requiring dependencies in proper context. Used by Yarn PnP.
     */
    @Nullable LocalFilePath packageJsonPath;
    LocalFilePath additionalRootDirectory;
  }
}
