package com.intellij.lang.javascript.linter.eslint.service;

import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.LinterLanguageServiceClient;
import com.intellij.lang.javascript.linter.eslint.EslintError;
import com.intellij.lang.javascript.linter.eslint.EslintRequestData;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EslintLanguageServiceClient extends LinterLanguageServiceClient {

  @NotNull
  NodePackage getNodePackage();

  @NotNull
  VirtualFile getWorkingDirectory();

  @Nullable
  CompletableFuture<Response<List<EslintError>>> highlight(@NotNull EslintRequestData requestData, String extraOptions);

  @Nullable
  CompletableFuture<Response<String>> fixFile(@NotNull EslintRequestData requestData, String extraOptions);

  class Response<T> {
    public final @InspectionMessage String globalError;
    public final boolean isNoConfigFile;
    public final T value;
    /** The text that the linter wrote to stderr while it handled the request, or null when it wrote nothing. */
    public final @Nullable @NlsSafe String stderr;

    public Response(@InspectionMessage String globalError, T value, boolean isNoConfigFile) {
      this(globalError, value, isNoConfigFile, null);
    }

    public Response(@InspectionMessage String globalError, T value, boolean isNoConfigFile, @Nullable @NlsSafe String stderr) {
      this.globalError = globalError;
      this.isNoConfigFile = isNoConfigFile;
      this.value = value;
      this.stderr = stderr;
    }

    public static <T> Response<T> error(@InspectionMessage String globalError, boolean isNoConfigFile) {
      return new Response<>(globalError, null, isNoConfigFile);
    }

    public static <T> Response<T> ok(T value) {
      return new Response<>(null, value, false);
    }
  }
}
