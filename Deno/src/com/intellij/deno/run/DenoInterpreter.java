package com.intellij.deno.run;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterType;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreterType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.util.NullableConsumer;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DenoInterpreter extends NodeJsInterpreter {

  private final String myPath;

  public DenoInterpreter(String path) {
    myPath = path;
  }

  @Override
  public @NotNull NodeJsInterpreterType<? extends NodeJsInterpreter> getType() {
    return NodeJsLocalInterpreterType.getInstance();
  }

  @Override
  public @NotNull String getReferenceName() {
    return myPath;
  }

  @Override
  public @NotNull String getPresentableName() {
    return myPath;
  }

  @Override
  public @Nullable Ref<SemVer> getCachedVersion() {
    return null;
  }

  @Override
  public void fetchVersion(@NotNull NullableConsumer<? super SemVer> consumer) {
    consumer.consume(null);
  }

  @Override
  public @Nullable String validate(@Nullable Project project) {
    return null;
  }
}
