package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.service.*;
import com.intellij.lang.javascript.service.protocol.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.util.Consumer;
import com.intellij.util.concurrency.FixedFuture;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.Future;

import static com.intellij.lang.javascript.service.JSLanguageServiceQueue.LOGGER;

public class PrettierLanguageServiceImpl extends JSLanguageServiceBase implements PrettierLanguageService {
  public PrettierLanguageServiceImpl(@NotNull Project project) {
    super(project);
  }

  @NotNull
  @Override
  public Future<FormatResult> format(@NotNull PsiFile file, @NotNull NodePackage prettierPackage, @Nullable TextRange range) {
    String prettierPackagePath = JSLanguageServiceUtil.normalizeNameAndPath(prettierPackage.getSystemDependentPath());
    JSLanguageServiceQueue process = getProcess();
    if (process == null || !process.isValid()) {
      return new FixedFuture<>(FormatResult.error(PrettierBundle.message("service.not.started.message")));
    }
    return
      process.execute(new ReformatFileCommand(file.getVirtualFile().getPath(), prettierPackagePath, file.getText(), range),
                      (ignored, response) -> parseResponse(response));
  }

  @NotNull
  private static FormatResult parseResponse(JSLanguageServiceAnswer response) {
    final String error = JsonUtil.getChildAsString(response.getElement(), "error");
    if (!StringUtil.isEmpty(error)) {
      return FormatResult.error(error);
    }
    return FormatResult.formatted(JsonUtil.getChildAsString(response.getElement(), "formatted"));
  }

  @Nullable
  @Override
  protected JSLanguageServiceQueue createLanguageServiceQueue() {
    return new JSLanguageServiceQueueImpl(myProject, new Protocol(myProject, Consumer.EMPTY_CONSUMER),
                                          myProcessConnector, myDefaultReporter, new JSLanguageServiceDefaultCacheData());
  }

  @Override
  protected boolean needInitToolWindow() {
    return false;
  }

  private static class Protocol extends JSLanguageServiceNodeStdProtocolBase {
    public Protocol(@NotNull Project project, @NotNull Consumer<?> readyConsumer) {
      super(project, readyConsumer);
    }

    @Override
    protected JSLanguageServiceInitialState createState() {
      JSLanguageServiceInitialState state = new JSLanguageServiceInitialState();
      final File service = new File(JSLanguageServiceUtil.getPluginDirectory(this.getClass(), "prettierLanguageService"),
                                    "prettier-plugin-provider.js");
      if (!service.exists()) {
        LOGGER.error("prettier language service plugin not found");
      }
      state.pluginName = "prettier";
      state.pluginPath = service.getAbsolutePath();
      return state;
    }

    @Override
    public void dispose() {

    }

    @Nullable
    @Override
    protected String getNodeInterpreter() {
      return JSLanguageServiceUtil.getInterpreterPathIfValid(
        PrettierConfiguration.getInstance(myProject)
                             .getOrDetectInterpreterRef()
                             .resolve(myProject));
    }
  }

  public static class ReformatFileCommand implements JSLanguageServiceObject, JSLanguageServiceSimpleCommand {
    public final String path;
    public final String prettierPath;
    public final String content;
    public Integer start;
    public Integer end;

    public ReformatFileCommand(@NotNull String filePath,
                               @NotNull String prettierPath,
                               @NotNull String content,
                               @Nullable TextRange range) {
      path = filePath;
      this.prettierPath = prettierPath;
      this.content = content;
      if (range != null) {
        start = range.getStartOffset();
        end = range.getEndOffset();
      }
    }

    @NotNull
    @Override
    public JSLanguageServiceObject toSerializableObject() {
      return this;
    }

    @NotNull
    @Override
    public String getCommand() {
      return "reformat";
    }

    @Nullable
    @Override
    public String getPresentableText(@NotNull Project project) {
      return "reformat";
    }
  }
}
