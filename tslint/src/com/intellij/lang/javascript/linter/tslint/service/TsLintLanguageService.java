package com.intellij.lang.javascript.linter.tslint.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.lang.javascript.integration.JSAnnotationError;
import com.intellij.lang.javascript.linter.tslint.execution.TsLintOutputJsonParser;
import com.intellij.lang.javascript.linter.tslint.service.commands.TsLintGetErrorsCommand;
import com.intellij.lang.javascript.linter.tslint.service.protocol.TsLintLanguageServiceProtocol;
import com.intellij.lang.javascript.service.*;
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceAnswer;
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject;
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


  public static TsLintLanguageService getService(@NotNull Project project) {
    return ServiceManager.getService(project, TsLintLanguageService.class);
  }


  public TsLintLanguageService(@NotNull Project project) {
    super(project);
  }


  public final Future<List<JSAnnotationError>> highlight(@Nullable VirtualFile virtualFile,
                                                         @Nullable VirtualFile config,
                                                         @Nullable String content) {
    JSLanguageServiceQueue process = getProcess();
    if (process == null) {
      return null;
    }


    String configPath = config == null ? null : JSLanguageServiceUtil.normalizeNameAndPath(config);
    if (configPath == null) {
      return null;
    }

    String path = virtualFile == null ? null : JSLanguageServiceUtil.normalizeNameAndPath(virtualFile);
    if (path == null) {
      return null;
    }


    TsLintGetErrorsCommand command = new TsLintGetErrorsCommand(path, configPath, StringUtil.notNullize(content));
    return process.execute(command, createProcessor(path));
  }

  @NotNull
  private static JSLanguageServiceCommandProcessor<List<JSAnnotationError>> createProcessor(@NotNull String path) {
    return new JSLanguageServiceCommandProcessor<List<JSAnnotationError>>() {
      @Nullable
      @Override
      public List<JSAnnotationError> process(@NotNull JSLanguageServiceObject serviceObject,
                                             @NotNull JSLanguageServiceAnswer answer) {
        JsonObject element = answer.getElement();
        JsonElement body = element.get("body");
        if (body == null) {
          return null;
        }

        String version = element.get("version").getAsString();
        boolean isZeroBased = TsLintOutputJsonParser.isVersionZeroBased(SemVer.parseFromText(version));
        TsLintOutputJsonParser parser = new TsLintOutputJsonParser(path, body, isZeroBased);
        return ContainerUtil.newArrayList(parser.getErrors());
      }
    };
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
