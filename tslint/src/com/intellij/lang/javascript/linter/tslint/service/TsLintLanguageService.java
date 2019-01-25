package com.intellij.lang.javascript.linter.tslint.service;

import com.google.gson.*;
import com.intellij.idea.RareLogger;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.linter.AutodetectLinterPackage;
import com.intellij.lang.javascript.linter.ExtendedLinterState;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.execution.TsLintOutputJsonParser;
import com.intellij.lang.javascript.linter.tslint.execution.TsLinterError;
import com.intellij.lang.javascript.service.*;
import com.intellij.lang.javascript.service.protocol.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.concurrency.FixedFuture;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;


public final class TsLintLanguageService extends JSLanguageServiceBase {
  @NotNull private final static Logger LOG = RareLogger.wrap(Logger.getInstance("#com.intellij.lang.javascript.linter.tslint.service.TsLintLanguageService"), false);
  @NotNull private final VirtualFile myWorkingDirectory;
  @NotNull private final NodePackage myNodePackage;

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
  public final Future<List<TsLinterError>> highlight(@NotNull VirtualFile virtualFile,
                                                     @Nullable VirtualFile config,
                                                     @Nullable String content,
                                                     @NotNull TsLintState state) {
    boolean isJSFile = DialectDetector.JAVASCRIPT_FILE_TYPES.contains(virtualFile.getFileType());
    String configFilePath = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(config);
    if (configFilePath == null) {
      if (state.getNodePackageRef() == AutodetectLinterPackage.INSTANCE || isJSFile) {
        return new FixedFuture<>(ContainerUtil.emptyList());
      }
      return new FixedFuture<>(Collections.singletonList(TsLinterError.createGlobalError("Config file was not found.")));
    }
    String path = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(virtualFile);
    if (path == null) {
      return null;
    }

    final JSLanguageServiceQueue process = getProcess();
    if (process == null) {
      return new FixedFuture<>(Collections.singletonList(
        TsLinterError.createGlobalError(JSLanguageServiceUtil.getLanguageServiceCreationError(this))));
    }
    GetErrorsCommand command = new GetErrorsCommand(LocalFilePath.create(path),
                                                    LocalFilePath.create(configFilePath),
                                                    StringUtil.notNullize(content), 
                                                    isJSFile);
    return process.execute(command, createHighlightProcessor(path));
  }

  @Nullable
  public final Future<List<TsLinterError>> highlightAndFix(@NotNull VirtualFile virtualFile, @NotNull TsLintState state) {
    boolean isJSFile = DialectDetector.JAVASCRIPT_FILE_TYPES.contains(virtualFile.getFileType());
    VirtualFile config = TslintUtil.getConfig(state, virtualFile);
    String configFilePath = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(config);
    if (configFilePath == null) {
      if (state.getNodePackageRef() == AutodetectLinterPackage.INSTANCE || isJSFile) {
        return new FixedFuture<>(ContainerUtil.emptyList());
      }
      return new FixedFuture<>(Collections.singletonList(TsLinterError.createGlobalError("Config file was not found.")));
    }
    String path = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(virtualFile);
    if (path == null) {
      return null;
    }

    final JSLanguageServiceQueue process = getProcess();
    if (process == null) {
      return new FixedFuture<>(Collections.singletonList(
        TsLinterError.createGlobalError(JSLanguageServiceUtil.getLanguageServiceCreationError(this))));
    }

    //doesn't pass content (file should be saved before)
    FixErrorsCommand command = new FixErrorsCommand(LocalFilePath.create(path),
                                                    LocalFilePath.create(configFilePath),
                                                    isJSFile);
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
      return Collections.singletonList(TsLinterError.createGlobalError(error.getAsString()));
    }
    final JsonElement body = parseBody(element);
    if (body == null) return null;
    final String version = element.get("version").getAsString();
    final SemVer tsLintVersion = SemVer.parseFromText(version);
    final boolean isZeroBased = TsLintOutputJsonParser.isVersionZeroBased(tsLintVersion);
    final TsLintOutputJsonParser parser = new TsLintOutputJsonParser(path, body, isZeroBased, gson);
    return ContainerUtil.newArrayList(parser.getErrors());
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
            return new JsonParser().parse(bodyContent);
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
  protected final JSLanguageServiceQueue createLanguageServiceQueue() {
    return new JSLanguageServiceQueueImpl(myProject, new Protocol(myNodePackage, myWorkingDirectory, myProject), myProcessConnector,
                                          myDefaultReporter,
                                          new JSLanguageServiceDefaultCacheData());
  }

  @Override
  protected final boolean needInitToolWindow() {
    return false;
  }

  private static abstract class BaseCommand implements JSLanguageServiceCommand, JSLanguageServiceSimpleCommand, JSLanguageServiceObject {
    public LocalFilePath filePath;
    @Nullable
    public LocalFilePath configPath;
    public boolean isJSFile;

    protected BaseCommand(LocalFilePath filePath, @Nullable LocalFilePath configPath, boolean isJSFile) {
      this.filePath = filePath;
      this.configPath = configPath;
      this.isJSFile = isJSFile;
    }

    @NotNull
    @Override
    public JSLanguageServiceObject toSerializableObject() {
      return this;
    }
  }

  private static class GetErrorsCommand extends BaseCommand{
    public String content;
    private GetErrorsCommand(LocalFilePath filePath, @Nullable LocalFilePath configPath, 
                             String content, boolean isJSFile) {
      super(filePath, configPath, isJSFile);
      this.content = content;
    }

    @NotNull
    @Override
    public String getCommand() {
      return "GetErrors";
    }
  }

  private static class FixErrorsCommand extends BaseCommand{
    private FixErrorsCommand(LocalFilePath filePath, @Nullable LocalFilePath configPath, boolean isJSFile) {
      super(filePath, configPath, isJSFile);
    }

    @NotNull
    @Override
    public String getCommand() {
      return "FixErrors";
    }
  }

  private static class Protocol extends JSLanguageServiceNodeStdProtocolBase {
    private final NodePackage myNodePackage;
    private final VirtualFile myWorkingDirectory;

    private Protocol(@NotNull NodePackage nodePackage, @NotNull VirtualFile workingDirectory, @NotNull Project project) {
      super("tslint", project, Consumer.EMPTY_CONSUMER);
      myNodePackage = nodePackage;
      myWorkingDirectory = workingDirectory;
    }

    @Nullable
    @Override
    protected NodeJsInterpreter getInterpreter() {
      ExtendedLinterState<TsLintState> state = TsLintConfiguration.getInstance(myProject).getExtendedState();
      return JSLanguageServiceUtil.getInterpreterIfValid(state.getState().getInterpreterRef().resolve(myProject));
    }

    @Override
    protected String getWorkingDirectory() {
      return JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(myWorkingDirectory);
    }

    @Override
    protected JSLanguageServiceInitialState createState() {
      InitialState result = new InitialState();
      ExtendedLinterState<TsLintState> extendedState = TsLintConfiguration.getInstance(myProject).getExtendedState();
      result.tslintPackagePath = LocalFilePath.create(myNodePackage.getSystemDependentPath());
      result.additionalRootDirectory = LocalFilePath.create(extendedState.getState().getRulesDirectory());
      result.pluginName = "tslint";
      result.pluginPath = LocalFilePath.create(
        JSLanguageServiceUtil.getPluginDirectory(getClass(), "js/languageService/tslint-plugin-provider.js").getAbsolutePath());
      return result;
    }

    @Override
    public void dispose() {
    }
  }

  private static class InitialState extends JSLanguageServiceInitialState {
    LocalFilePath tslintPackagePath;
    LocalFilePath additionalRootDirectory;
  }
}
