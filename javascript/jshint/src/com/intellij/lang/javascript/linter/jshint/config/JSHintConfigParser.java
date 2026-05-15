package com.intellij.lang.javascript.linter.jshint.config;

import com.google.gson.stream.JsonReader;
import com.intellij.lang.javascript.linter.jshint.JSHintBundle;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.jshint.JSHintOption;
import com.intellij.lang.javascript.linter.jshint.JSHintOptionsState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class JSHintConfigParser {

  private static final Logger LOG = Logger.getInstance(JSHintConfigParser.class);

  private final Set<VirtualFile> myParsedFiles = new HashSet<>();

  private JSHintConfigParser() {
  }

  private @NotNull JSHintConfigLookupResult doParse(@NotNull VirtualFile config) {
    if (myParsedFiles.contains(config)) {
      return JSHintConfigLookupResult.createErrorResult(config, JSHintBundle.message("jshint.config.extends.cyclically"));
    }
    myParsedFiles.add(config);
    final JSHintOptionsState optionsState;
    try {
      String text = JSLinterConfigFileUtil.loadActualText(config);
      optionsState = parseOptionsState(text);
    }
    catch (IOException e) {
      LOG.info("Cannot parse jshint config at " + config.getPath(), e);
      return JSHintConfigLookupResult.createErrorResult(config, JSHintBundle.message("jshint.config.failed.to.read"));
    }
    Object extendsObj = optionsState.getValue(JSHintConfigFileUtil.EXTENDS_KEY);
    if (extendsObj == null) {
      return JSHintConfigLookupResult.createSuccessfulResult(config, optionsState);
    }
    JSHintConfigLookupResult extendedResult = doParseExtended(config, extendsObj);
    if (extendedResult.getErrorMessage() != null) {
      return extendedResult;
    }
    JSHintOptionsState extendedOptionsState = extendedResult.getOptionsState();
    if (extendedOptionsState == null) {
      LOG.warn("JSHint extended options state is null unexpectedly");
      return JSHintConfigLookupResult.createSuccessfulResult(config, optionsState);
    }
    JSHintOptionsState mergedOptionsState = merge(optionsState, extendedOptionsState);
    return JSHintConfigLookupResult.createSuccessfulResult(config, mergedOptionsState);
  }

  private static @NotNull JSHintOptionsState merge(@NotNull JSHintOptionsState optionsState,
                                                   @NotNull JSHintOptionsState extendedOptionsState) {
    JSHintOptionsState.Builder builder = new JSHintOptionsState.Builder();
    for (String key : extendedOptionsState.getOptionKeys()) {
      if (!JSHintConfigFileUtil.EXTENDS_KEY.equals(key)) {
        builder.put(key, extendedOptionsState.getValue(key));
      }
    }
    for (String key : optionsState.getOptionKeys()) {
      if (!JSHintConfigFileUtil.EXTENDS_KEY.equals(key)) {
        builder.put(key, optionsState.getValue(key));
      }
    }
    Map<String, Boolean> mergedGlobals = mergeGlobals(extendedOptionsState.getValue(JSHintOption.PREDEF),
                                                      optionsState.getValue(JSHintOption.PREDEF));
    if (mergedGlobals != null) {
      builder.put(JSHintOption.PREDEF, mergedGlobals);
    }
    return builder.build();
  }

  private static @Nullable Map<String, Boolean> mergeGlobals(@Nullable Object extendedGlobals, @Nullable Object globals) {
    if (extendedGlobals == null || globals == null) {
      return null;
    }
    Map<String, Boolean> result = new HashMap<>();
    overwriteGlobals(result, extendedGlobals);
    overwriteGlobals(result, globals);
    return result;
  }

  private static void overwriteGlobals(@NotNull Map<String, Boolean> result, @Nullable Object globals) {
    if (globals instanceof List) {
      @SuppressWarnings("unchecked")
      List<Object> list = (List<Object>) globals;
      for (Object o : list) {
        if (o instanceof String) {
          result.put((String) o, false);
        }
      }
    }
    else if (globals instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<Object, Object> map = (Map<Object, Object>) globals;
      for (Map.Entry<Object, Object> entry : map.entrySet()) {
        if (entry.getKey() instanceof String && entry.getValue() instanceof Boolean) {
          result.put((String) entry.getKey(), (Boolean) entry.getValue());
        }
      }
    }
  }

  private @NotNull JSHintConfigLookupResult doParseExtended(@NotNull VirtualFile config, @NotNull Object extendsObj) {
    if (extendsObj instanceof String extendsPath) {
      VirtualFile configDir = config.getParent();
      if (configDir == null) {
        return JSHintConfigLookupResult.createErrorResult(config, JSHintBundle.message("jshint.config.error.cannot.locate.ext.config"));
      }
      VirtualFile extendedFile = configDir.findFileByRelativePath(extendsPath);
      if (extendedFile != null && extendedFile.isValid() && !extendedFile.isDirectory()) {
        return doParse(extendedFile);
      }
    }
    return JSHintConfigLookupResult.createErrorResult(config, JSHintBundle.message("jshint.config.error.cannot.parse.ext.config"));
  }

  private static @NotNull JSHintOptionsState parseOptionsState(@NotNull String text) throws IOException {
    JsonReader reader = new JsonReader(new StringReader(text));
    // allow comments in .jshintrc
    reader.setLenient(true);
    return JSHintConfigFileUtil.parseOptionsState(reader);
  }

  public static @NotNull JSHintConfigLookupResult parse(@NotNull VirtualFile config) {
    return new JSHintConfigParser().doParse(config);
  }
}
