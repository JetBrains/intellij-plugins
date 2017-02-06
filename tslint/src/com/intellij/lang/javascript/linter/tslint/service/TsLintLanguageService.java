package com.intellij.lang.javascript.linter.tslint.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Future;


public final class TsLintLanguageService extends JSLanguageServiceBase {

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


  public final Future<List<TsLinterError>> highlight(@Nullable VirtualFile virtualFile,
                                                     @Nullable VirtualFile config,
                                                     @Nullable String content) {
    JSLanguageServiceQueue process = getProcess();
    if (process == null || virtualFile == null) {
      return null;
    }


    String configPath = config == null ? null : JSLanguageServiceUtil.normalizeNameAndPath(config);
    if (configPath == null) {
      return null;
    }

    String path = JSLanguageServiceUtil.normalizeNameAndPath(virtualFile);
    if (path == null) {
      return null;
    }


    TsLintGetErrorsCommand command = new TsLintGetErrorsCommand(path, configPath, StringUtil.notNullize(content));
    return process.execute(command, createHighlightProcessor(path));
  }

  public final Future<List<TsLinterError>> highlightAndFix(@Nullable VirtualFile virtualFile, @NotNull TsLintState state) {

    JSLanguageServiceQueue process = getProcess();
    if (process == null || virtualFile == null) {
      return null;
    }

    VirtualFile config = myConfigFileSearcher.getConfig(state, virtualFile);


    String configPath = config == null ? null : JSLanguageServiceUtil.normalizeNameAndPath(config);
    if (configPath == null) {
      return null;
    }

    String path = JSLanguageServiceUtil.normalizeNameAndPath(virtualFile);
    if (path == null) {
      return null;
    }

    //doesn't pass content (file should be saved before)
    TsLintFixErrorsCommand command = new TsLintFixErrorsCommand(path, configPath);
    return process.execute(command, createHighlightProcessor(path));
  }

  @NotNull
  private static JSLanguageServiceCommandProcessor<List<TsLinterError>> createHighlightProcessor(@NotNull String path) {
    return (object, answer) -> parseResults(answer, path);
  }


  @Nullable
  private static List<TsLinterError> parseResults(@NotNull JSLanguageServiceAnswer answer, @NotNull String path) {
    JsonObject element = answer.getElement();
    JsonElement body = element.get("body");
    if (body == null) {
      return null;
    }

    String version = element.get("version").getAsString();
    SemVer tsLintVersion = SemVer.parseFromText(version);
    boolean isZeroBased = TsLintOutputJsonParser.isVersionZeroBased(tsLintVersion);
    TsLintOutputJsonParser parser = new TsLintOutputJsonParser(path, body, isZeroBased);
    return ContainerUtil.newArrayList(parser.getErrors());
  }

  @Nullable
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
