package com.intellij.lang.javascript.linter.tslint.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.intellij.idea.RareLogger;
import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.execution.TsLintConfigFileSearcher;
import com.intellij.lang.javascript.linter.tslint.execution.TsLintOutputJsonParser;
import com.intellij.lang.javascript.linter.tslint.execution.TsLinterError;
import com.intellij.lang.javascript.linter.tslint.service.commands.TsLintFixErrorsCommand;
import com.intellij.lang.javascript.linter.tslint.service.commands.TsLintGetErrorsCommand;
import com.intellij.lang.javascript.linter.tslint.service.protocol.TsLintLanguageServiceProtocol;
import com.intellij.lang.javascript.service.*;
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceAnswer;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
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
  @NotNull
  private final TsLintConfigFileSearcher myConfigFileSearcher;

  @NotNull
  public static TsLintLanguageService getService(@NotNull Project project) {
    return ServiceManager.getService(project, TsLintLanguageService.class);
  }


  public TsLintLanguageService(@NotNull Project project) {
    super(project);
    myConfigFileSearcher = new TsLintConfigFileSearcher();
  }

  @NotNull
  @Override
  protected String getProcessName() {
    return "TSLint";
  }

  @NotNull
  @Override
  protected JSLanguageServiceQueue.ServiceInfoReporter createDefaultReporter() {
    return new NotificationLanguageServiceReporter(JSLinterUtil.NOTIFICATION_GROUP, super.createDefaultReporter());
  }

  public final Future<List<TsLinterError>> highlight(@Nullable VirtualFile virtualFile,
                                                     @Nullable VirtualFile config,
                                                     @Nullable String content) {
    JSLanguageServiceQueue process = getProcess();
    if (checkParameters(virtualFile, process)) return new FixedFuture<>(Collections.singletonList(new TsLinterError("Path not specified")));

    String configPath = config == null ? null : JSLanguageServiceUtil.normalizeNameAndPath(config);

    String path = JSLanguageServiceUtil.normalizeNameAndPath(virtualFile);
    if (path == null) {
      return new FixedFuture<>(Collections.singletonList(new TsLinterError("Can not work with the path: " + virtualFile.getPath())));
    }

    TsLintGetErrorsCommand command = new TsLintGetErrorsCommand(path, configPath, StringUtil.notNullize(content));
    return process.execute(command, createHighlightProcessor(path));
  }

  public final Future<List<TsLinterError>> highlightAndFix(@Nullable VirtualFile virtualFile, @NotNull TsLintState state) {

    JSLanguageServiceQueue process = getProcess();
    if (checkParameters(virtualFile, process)) return new FixedFuture<>(Collections.singletonList(new TsLinterError("Path not specified")));

    VirtualFile config = myConfigFileSearcher.getConfig(state, virtualFile);

    String configPath = config == null ? null : JSLanguageServiceUtil.normalizeNameAndPath(config);

    String path = JSLanguageServiceUtil.normalizeNameAndPath(virtualFile);
    if (path == null) {
      return null;
    }

    //doesn't pass content (file should be saved before)
    TsLintFixErrorsCommand command = new TsLintFixErrorsCommand(path, configPath);
    return process.execute(command, createHighlightProcessor(path));
  }

  private static boolean checkParameters(@Nullable VirtualFile virtualFile, JSLanguageServiceQueue process) {
    if (process == null || virtualFile == null || !virtualFile.isInLocalFileSystem()) {
      return true;
    }
    return false;
  }

  @NotNull
  private static JSLanguageServiceCommandProcessor<List<TsLinterError>> createHighlightProcessor(@NotNull String path) {
    return (object, answer) -> parseResults(answer, path);
  }


  @Nullable
  private static List<TsLinterError> parseResults(@NotNull JSLanguageServiceAnswer answer, @NotNull String path) {
    final JsonObject element = answer.getElement();
    final JsonElement error = element.get("error");
    if (error != null) {
      return Collections.singletonList(new TsLinterError(error.getAsString()));
    }
    final JsonElement body = parseBody(element);
    if (body == null) return null;
    final String version = element.get("version").getAsString();
    final SemVer tsLintVersion = SemVer.parseFromText(version);
    final boolean isZeroBased = TsLintOutputJsonParser.isVersionZeroBased(tsLintVersion);
    final TsLintOutputJsonParser parser = new TsLintOutputJsonParser(path, body, isZeroBased);
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
    TsLintLanguageServiceProtocol protocol = new TsLintLanguageServiceProtocol(myProject, (el) -> {
    });

    return new JSLanguageServiceQueueImpl(myProject, protocol, myProcessConnector, myDefaultReporter,
                                          new JSLanguageServiceDefaultCacheData());
  }

  @Override
  protected final boolean needInitToolWindow() {
    return false;
  }
}
