package com.intellij.lang.javascript.linter.tslint.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.lang.javascript.integration.JSAnnotationError;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.execution.TsLintConfigFileSearcher;
import com.intellij.lang.javascript.linter.tslint.execution.TsLintOutputJsonParser;
import com.intellij.lang.javascript.linter.tslint.service.commands.TsLintGetErrorsCommand;
import com.intellij.lang.javascript.linter.tslint.service.protocol.TsLintLanguageServiceProtocol;
import com.intellij.lang.javascript.service.*;
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceAnswer;
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject;
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Future;


public final class TsLintLanguageService extends JSLanguageServiceBase implements JSLanguageService {

  private final TsLintConfiguration mySettings;

  public static TsLintLanguageService getService(@NotNull Project project) {
    return ServiceManager.getService(project, TsLintLanguageService.class);
  }


  private final TsLintConfigFileSearcher myConfigFileSearcher;

  public TsLintLanguageService(@NotNull Project project) {
    super(project);
    myConfigFileSearcher = new TsLintConfigFileSearcher();
    mySettings = TsLintConfiguration.getInstance(myProject);
  }

  @Nullable
  @Override
  public final Future<List<JSAnnotationError>> highlight(@NotNull PsiFile file, @NotNull JSFileHighlightingInfo info) {
    JSLanguageServiceQueue process = getProcess();
    if (process == null) {
      return null;
    }

    VirtualFile virtualFile = file.getVirtualFile();
    String path = virtualFile == null ? null : JSLanguageServiceUtil.normalizeNameAndPath(virtualFile);
    if (path == null) {
      return null;
    }

    VirtualFile config = myConfigFileSearcher.getConfig(mySettings.getExtendedState().getState(), file);
    String configPath = config == null ? null : JSLanguageServiceUtil.normalizeNameAndPath(config);
    if (configPath == null) {
      return null;
    }

    Document document = info.updateContext.getOpenContents().get(virtualFile);

    TsLintGetErrorsCommand command = new TsLintGetErrorsCommand(path, configPath, document.getText());
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

  @Override
  public final boolean canHighlight(@NotNull PsiFile file) {
    return TsLintConfiguration.getInstance(myProject).getExtendedState().isEnabled();
  }

  @NotNull
  @Override
  public final Condition<VirtualFile> getAcceptableFilesFilter() {
    return TypeScriptLanguageServiceUtil.FILES_TO_PROCESS;
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
