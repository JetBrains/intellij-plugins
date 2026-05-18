package com.intellij.lang.javascript.linter.jshint;

import com.intellij.javascript.common.icons.JavascriptCommonIcons;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.linter.JSDumbAwareLinterExternalAnnotator;
import com.intellij.lang.javascript.linter.JSLinterAnnotationResult;
import com.intellij.lang.javascript.linter.JSLinterAnnotationsBuilder;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterEditSettingsAction;
import com.intellij.lang.javascript.linter.JSLinterError;
import com.intellij.lang.javascript.linter.JSLinterFileLevelAnnotation;
import com.intellij.lang.javascript.linter.JSLinterInput;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.lang.javascript.linter.JSLinterStandardFixes;
import com.intellij.lang.javascript.linter.jshint.config.JSHintConfigFileChangeTracker;
import com.intellij.lang.javascript.linter.jshint.config.JSHintConfigFileUtil;
import com.intellij.lang.javascript.linter.jshint.config.JSHintConfigLookupResult;
import com.intellij.lang.javascript.linter.jshint.version.JSHintVersionUtil;
import com.intellij.lang.javascript.linter.jshint.rhino.FunctionWithScope;
import com.intellij.lang.javascript.linter.jshint.rhino.RhinoFunctionManager;
import com.intellij.lang.javascript.linter.jshint.rhino.RhinoUtil;
import com.intellij.lang.javascript.linter.jshint.rhino.WrappedRhinoException;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.TopLevel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;

public class JSHintExternalAnnotator extends JSDumbAwareLinterExternalAnnotator<JSHintState> {

  private static final Logger LOG = Logger.getInstance(JSHintExternalAnnotator.class);
  private static final JSHintExternalAnnotator INSTANCE_FOR_BATCH_INSPECTION = new JSHintExternalAnnotator(false);
  private final Object myLock = new Object();

  private volatile Pair<String, RhinoFunctionManager> myVersionAndFunctionManager;

  public static @NotNull JSHintExternalAnnotator getInstanceForBatchInspection() {
    return INSTANCE_FOR_BATCH_INSPECTION;
  }

  @SuppressWarnings("unused")
  public JSHintExternalAnnotator() {
    this(true);
  }

  public JSHintExternalAnnotator(boolean onTheFly) {
    super(onTheFly);
  }

  @Override
  protected @NotNull String getSettingsConfigurableID() {
    return JSHintConfigurable.ID;
  }

  @Override
  protected Class<? extends JSLinterConfiguration<JSHintState>> getConfigurationClass() {
    return JSHintConfiguration.class;
  }

  @Override
  protected Class<? extends JSLinterInspection> getInspectionClass() {
    return JSHintInspection.class;
  }

  @Override
  protected boolean acceptPsiFile(@NotNull PsiFile file) {
    return file instanceof JSFile && JSUtils.isJavaScriptFile(file);
  }

  @Override
  public JSLinterAnnotationResult annotate(@NotNull JSLinterInput<JSHintState> collectedInfo) {
    final Project project = collectedInfo.getProject();
    if (project.isDisposed()) {
      return null;
    }
    Context cx = Context.enter();
    try {
      JSHintState state = collectedInfo.getState();
      RhinoFunctionManager manager = getFunctionManager(project, state.getVersion());
      if (manager == null) {
        return JSLinterAnnotationResult.create(collectedInfo, new JSLinterFileLevelAnnotation(
          JSHintBundle.message("jshint.inspection.message.not.loaded", state.getVersion())), null);
      }
      VirtualFile file = collectedInfo.getPsiFile().getVirtualFile();
      if (file == null) {
        return null;
      }
      boolean ignored = JSHintConfigFileUtil.isIgnored(project, file);
      if (ignored) {
        return null;
      }
      try {
        final JSHintOptionsState optionsState;
        final VirtualFile configFile;
        if (!state.isConfigFileUsed()) {
          optionsState = state.getOptionsState();
          configFile = null;
        }
        else {
          ReadAction.runBlocking(() -> {
            if (!project.isDisposed()) {
              JSHintConfigFileChangeTracker.getInstance(project).startIfNeeded();
            }
          });
          final JSHintConfigLookupResult configLookupResult;
          if (state.isCustomConfigFileUsed()) {
            configLookupResult = JSHintConfigFileUtil.loadConfigByPath(state.getCustomConfigFilePath());
          }
          else {
            configLookupResult = JSHintConfigFileUtil.lookupConfig(project, file);
          }
          if (configLookupResult == null) {
            return JSLinterAnnotationResult.create(collectedInfo, new JSLinterFileLevelAnnotation(
              JSHintBundle.message("jshint.inspection.message.config.not.found")), null);
          }
          configFile = configLookupResult.getConfigFile();
          optionsState = configLookupResult.getOptionsState();
          if (optionsState == null) {
            String message = StringUtil.notNullize(configLookupResult.getErrorMessage(),
                                                   JSHintBundle.message("jshint.inspection.message.malformed.config"));
            final JSLinterFileLevelAnnotation annotation = new JSLinterFileLevelAnnotation(message);
            return JSLinterAnnotationResult.create(collectedInfo, annotation, configFile);
          }
        }
        List<JSLinterError> errors = doLint(cx, manager, optionsState, collectedInfo.getFileContent());
        if (errors != null) {
          return new JSHintAnnotationResult(collectedInfo.getColorsScheme(), errors, configFile, optionsState);
        }
        return null;
      }
      catch (WrappedRhinoException e) {
        RhinoException rhinoException = e.getRhinoException();
        LOG.info("JSHint " + state.getVersion() + " crashed"
                 + ", JavaScript stacktrace: " + rhinoException.getScriptStackTrace()
                 + ", details: " + rhinoException.details(), rhinoException);
        return null;
      }
      catch (Exception e) {
        throw new RuntimeException("Unhandled exception when running JSHint " + state.getVersion(), e);
      }
    } finally {
      Context.exit();
    }
  }

  private @Nullable RhinoFunctionManager getFunctionManager(final @NotNull Project project, final @NotNull String version) {
    Pair<String, RhinoFunctionManager> pair = getCachedByVersion(version);
    if (pair != null) {
      return pair.getSecond();
    }
    synchronized (myLock) {
      Pair<String, RhinoFunctionManager> local = getCachedByVersion(version);
      if (local != null) {
        return local.getSecond();
      }
      RhinoFunctionManager rfm = createRhinoFunctionManager(project, version);
      myVersionAndFunctionManager = Pair.create(version, rfm);
      return rfm;
    }
  }

  private @Nullable Pair<String, RhinoFunctionManager> getCachedByVersion(@NotNull String version) {
    Pair<String, RhinoFunctionManager> versionAndFunctionManager = myVersionAndFunctionManager;
    if (versionAndFunctionManager != null) {
      String aVersion = versionAndFunctionManager.getFirst();
      if (aVersion.equals(version)) {
        return versionAndFunctionManager;
      }
    }
    return null;
  }

  private static @Nullable RhinoFunctionManager createRhinoFunctionManager(final @NotNull Project project, final @NotNull String version) {
    if (!JSHintVersionUtil.isSourceLocallyAvailable(version)) {
      fetchSourceSync(project, version);
    }
    final String content;
    try {
      content = JSHintVersionUtil.loadSourceContentFromLocalDrive(version);
      if (content == null) {
        LOG.warn("No local source content found for " + version);
        return null;
      }
    } catch (IOException e) {
      LOG.warn("Can't load JSHint " + version, e);
      return null;
    }
    return new RhinoFunctionManager(
      () -> content,
      "JSHINT",
      version
    );
  }

  private static void fetchSourceSync(final @NotNull Project project, final @NotNull String version) {
    Application app = ApplicationManager.getApplication();
    boolean headless = app.isUnitTestMode() || app.isCommandLine() || app.isHeadlessEnvironment();
    // under the normal circumstances this method is called in a JobScheduler thread (not EDT)
    if (headless || app.isDispatchThread()) {
      try {
        JSHintVersionUtil.downloadSourceContent(version);
      }
      catch (IOException e) {
        LOG.warn("Can't download JSHint " + version, e);
      }
    }
    else {
      final Semaphore semaphore = new Semaphore(0, true);
      UIUtil.invokeLaterIfNeeded(() -> JSHintVersionUtil.downloadSourceContentUnderProgress(project, version, () -> semaphore.release()));
      try {
        semaphore.acquire();
      }
      catch (InterruptedException e) {
        LOG.warn("Thread has been interrupted unexpectedly", e);
      }
    }
  }

  private static @Nullable List<JSLinterError> doLint(Context cx,
                                                      @NotNull RhinoFunctionManager manager,
                                                      @NotNull JSHintOptionsState optionsState,
                                                      @NotNull String fileContent) throws WrappedRhinoException {
    NativeObject optionsNativeObject = convertOptionsToRhinoObject(optionsState);
    final Object[] args;
    Object predefObj = optionsState.getValue(JSHintOption.PREDEF);
    if (predefObj != null) {
      Object predefRhinoObject = convertPredefToRhinoMap(predefObj);
      args = new Object[] {fileContent, optionsNativeObject, predefRhinoObject};
    }
    else {
      args = new Object[] {fileContent, optionsNativeObject};
    }
    FunctionWithScope functionWithScope = manager.getFunctionWithScope();
    Function function = functionWithScope.getFunction();
    Scriptable scope = functionWithScope.getScope();
    final Object status;
    try {
      status = function.call(cx, scope, scope, args);
    } catch (JavaScriptException e) {
      NativeObject nativeError = ObjectUtils.tryCast(e.getValue(), NativeObject.class);
      if (nativeError != null) {
        JSLinterError error = toLinterError(nativeError);
        if (error != null) {
          return List.of(error);
        }
      }
      throw new WrappedRhinoException(e);
    }
    Boolean noErrors = (Boolean) Context.jsToJava(status, Boolean.class);
    if (!noErrors) {
      Object errorsObj = function.get("errors", scope);
      if (errorsObj == null || errorsObj == Scriptable.NOT_FOUND) {
        throw new RuntimeException(manager.getFunctionName() + ".errors is " + errorsObj);
      }
      return convertErrors((NativeArray) errorsObj);
    }
    return null;
  }

  private static @NotNull List<JSLinterError> convertErrors(@NotNull NativeArray errorsNativeArray) {
    List<JSLinterError> errors = new ArrayList<>(errorsNativeArray.size());
    for (Object errorObj : errorsNativeArray) {
      if (errorObj instanceof NativeObject) {
        JSLinterError error = toLinterError((NativeObject)errorObj);
        if (error != null) {
          errors.add(error);
        }
      }
    }
    return errors;
  }

  private static @Nullable JSLinterError toLinterError(@NotNull NativeObject nativeError) {
    int line = toInt(nativeError.get("line"));
    int character = toInt(nativeError.get("character"));
    if (line < 0 || character < 0) {
      return null;
    }
    String code = RhinoUtil.getStringKey(nativeError, "code");
    @NlsSafe String reason = RhinoUtil.getStringKey(nativeError, "reason");
    if (reason == null) {
      reason = RhinoUtil.getStringKey(nativeError, "message");
    }
    if (reason != null) {
      return new JSLinterError(line, character, reason, code);
    }
    return null;
  }

  private static int toInt(Object obj) {
    if (obj instanceof Number) {
      return ((Number) obj).intValue();
    }
    return -1;
  }

  private static @NotNull NativeObject convertOptionsToRhinoObject(@NotNull JSHintOptionsState optionsState) {
    Map<String, Object> options = new HashMap<>(optionsState.getValueByOptionMap());
    options.remove(JSHintOption.PREDEF.getKey());
    return RhinoUtil.toRhinoMap(options);
  }

  @Override
  public void apply(@NotNull PsiFile psiFile, JSLinterAnnotationResult annotationResult, @NotNull AnnotationHolder holder) {
    if (annotationResult == null) return;

    JSHintConfigurable configurable = new JSHintConfigurable(psiFile.getProject(), true);
    JSLinterStandardFixes fixes = new JSLinterStandardFixes()
      .setEditSettingsAction(new JSLinterEditSettingsAction(configurable, JavascriptCommonIcons.FileTypes.JsHint))
      .setShowEditSettings(false)
      .setEditConfig(false);
    new JSLinterAnnotationsBuilder(psiFile,
                                   annotationResult,
                                   holder,
                                   configurable,
                                   JSHintBundle.message("jshint.inspection.message.prefix") + " ",
                                   getInspectionClass(),
                                   fixes)
      .setTabSize(getIndent(annotationResult))
      .setDefaultFileLevelErrorIcon(JavascriptCommonIcons.FileTypes.JsHint)
      .setHighlightingGranularity(HighlightingGranularity.element).apply();
  }

  private static int getIndent(@NotNull JSLinterAnnotationResult annotationResult) {
    int indent = 4;
    if (annotationResult instanceof JSHintAnnotationResult) {
      Object obj = ((JSHintAnnotationResult)annotationResult).myOptionsState.getValue(JSHintOption.INDENT);
      if (obj instanceof Number) {
        indent = ((Number)obj).intValue();
      }
    }
    return Math.max(1, indent);
  }

  public static @NotNull NativeObject convertPredefToRhinoMap(@NotNull Object predef) {
    final Map<String, Object> map = new HashMap<>();
    if (predef instanceof String) {
      Map<String, Boolean> boolMap = convertPredefStrToMap((String)predef);
      map.putAll(boolMap);
    }
    else if (predef instanceof List) {
      for (Object o : (List)predef) {
        if (o instanceof String) {
          map.put((String)o, false);
        }
      }
    }
    else if (predef instanceof Map) {
      //noinspection unchecked
      for (Map.Entry<Object, Object> entry : ((Map<Object, Object>)predef).entrySet()) {
        if (entry.getKey() instanceof String) {
          map.put((String)entry.getKey(), entry.getValue());
        }
      }
    }
    return RhinoUtil.toRhinoMap(map);
  }

  public static @NotNull NativeArray convertPredefStrToNativeArray(@NotNull String predef, @NotNull Scriptable scope) {
    Map<String, Boolean> structure = convertPredefStrToMap(predef);
    NativeArray array = RhinoUtil.toRhinoArray(new ArrayList<>(structure.keySet()));
    ScriptRuntime.setBuiltinProtoAndParent(array, scope, TopLevel.Builtins.Array);
    return array;
  }

  private static @NotNull Map<String, Boolean> convertPredefStrToMap(@NotNull String predef) {
    Map<String, Boolean> map = new LinkedHashMap<>();
    StringTokenizer st = new StringTokenizer(predef, ",");
    while (st.hasMoreTokens()) {
      String token = st.nextToken().trim();
      final int ind = token.indexOf(':');
      String name = token;
      boolean readonly = false;
      if (ind >= 0) {
        String value = token.substring(ind + 1).trim();
        if (Boolean.toString(true).equals(value) || Boolean.toString(false).equals(value)) {
          name = token.substring(0, ind).trim();
          readonly = Boolean.parseBoolean(value);
        }
      }
      map.put(name, readonly);
    }
    return map;
  }

  private static class JSHintAnnotationResult extends JSLinterAnnotationResult {
    private final JSHintOptionsState myOptionsState;

    JSHintAnnotationResult(@Nullable EditorColorsScheme colorsScheme,
                           @NotNull List<JSLinterError> errors,
                           @Nullable VirtualFile configFile,
                           @NotNull JSHintOptionsState optionsState) {
      super(colorsScheme, errors, null, configFile);
      myOptionsState = optionsState;
    }
  }
}
