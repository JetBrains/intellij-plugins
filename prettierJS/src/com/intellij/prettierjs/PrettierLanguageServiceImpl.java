package com.intellij.prettierjs;

import com.google.gson.JsonObject;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.service.*;
import com.intellij.lang.javascript.service.protocol.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.Consumer;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.intellij.lang.javascript.service.JSLanguageServiceQueue.LOGGER;

public class PrettierLanguageServiceImpl extends JSLanguageServiceBase implements PrettierLanguageService {
  private volatile boolean myFlushConfigCache;

  public PrettierLanguageServiceImpl(@NotNull Project project) {
    super(project);
    project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
      @Override
      public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent event : events) {
          if (!(event instanceof VFileContentChangeEvent) || PrettierUtil.isConfigFileOrPackageJson(event.getFile())) {
            myFlushConfigCache = true;
            break;
          }
        }
      }
    });
  }

  @Nullable
  @Override
  public CompletableFuture<FormatResult> format(@NotNull String filePath,
                                                @Nullable String ignoreFilePath,
                                                @NotNull String text,
                                                @NotNull NodePackage prettierPackage,
                                                @Nullable TextRange range) {
    String prettierPackagePath = JSLanguageServiceUtil.normalizeNameAndPath(prettierPackage.getSystemDependentPath());
    ignoreFilePath = JSLanguageServiceUtil.normalizeNameAndPath(ignoreFilePath);
    JSLanguageServiceQueue process = getProcess();
    if (process == null || !process.isValid()) {
      return CompletableFuture.completedFuture(FormatResult.error(PrettierBundle.message("service.not.started.message")));
    }
    ReformatFileCommand command =
      new ReformatFileCommand(filePath, prettierPackagePath, ignoreFilePath, text, range, myFlushConfigCache);
    return process.execute(command, (ignored, response) -> {
      myFlushConfigCache = false;
      return parseReformatResponse(response);
    });
  }


  @NotNull
  private static FormatResult parseReformatResponse(JSLanguageServiceAnswer response) {
    JsonObject jsonObject = response.getElement();
    final String error = JsonUtil.getChildAsString(jsonObject, "error");
    if (!StringUtil.isEmpty(error)) {
      return FormatResult.error(error);
    }
    if (JsonUtil.getChildAsBoolean(jsonObject, "ignored", false)) {
      return FormatResult.IGNORED;
    }
    if (JsonUtil.getChildAsBoolean(jsonObject, "unsupported", false)) {
      return FormatResult.UNSUPPORTED;
    }
    String formattedResult = JsonUtil.getChildAsString(jsonObject, "formatted");
    return FormatResult.formatted(formattedResult);
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
    Protocol(@NotNull Project project, @NotNull Consumer<?> readyConsumer) {
      super("prettier", project, readyConsumer);
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
      state.pluginPath = LocalFilePath.create(service.getAbsolutePath());
      return state;
    }

    @Override
    public void dispose() {

    }

    @Nullable
    @Override
    protected NodeJsInterpreter getInterpreter() {
      return JSLanguageServiceUtil.getInterpreterIfValid(
        PrettierConfiguration.getInstance(myProject)
                             .getInterpreterRef()
                             .resolve(myProject));
    }
  }

  private static class ReformatFileCommand implements JSLanguageServiceObject, JSLanguageServiceSimpleCommand {
    public final LocalFilePath path;
    public final LocalFilePath prettierPath;
    @Nullable public final String ignoreFilePath;
    public final String content;
    public Integer start;
    public Integer end;
    public final boolean flushConfigCache;

    ReformatFileCommand(@NotNull String filePath,
                        @NotNull String prettierPath,
                        @Nullable String ignoreFilePath,
                        @NotNull String content,
                        @Nullable TextRange range,
                        boolean flushConfigCache) {
      this.path = LocalFilePath.create(filePath);
      this.prettierPath = LocalFilePath.create(prettierPath);
      this.ignoreFilePath = ignoreFilePath;
      this.content = content;
      this.flushConfigCache = flushConfigCache;
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
