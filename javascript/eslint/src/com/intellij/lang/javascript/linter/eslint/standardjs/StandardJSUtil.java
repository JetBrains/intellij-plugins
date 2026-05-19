package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class StandardJSUtil {
  public static final String PACKAGE_NAME = "standard";
  public static final String CONFIG_SECTION_NAME = "standard";
  public static final Key<ParameterizedCachedValue<ConfigData, PsiFile>> CACHE_KEY = new Key<>(StandardJSUtil.class.getName() + ".config");
  public static final Logger LOG = Logger.getInstance(StandardJSUtil.class);
  public static final SemVer MIN_VERSION = new SemVer("10.0.0", 10, 0, 0);

  private StandardJSUtil() {
  }

  @Contract("_, null -> null")
  public static @Nullable ConfigData getPackageJsonConfigData(@NotNull Project project, @Nullable VirtualFile virtualFile) {
    final PsiManager psiManager = PsiManager.getInstance(project);
    if (virtualFile == null) {
      return null;
    }
    return ReadAction.compute(() -> {
      PsiFile psiFile = psiManager.findFile(virtualFile);
      if (psiFile == null) {
        return null;
      }

      return CachedValuesManager.getManager(project).getParameterizedCachedValue(psiFile, CACHE_KEY, (theFile) -> {
        VirtualFile theVirtualFile = theFile.getVirtualFile();
        if (theVirtualFile == null) {
          return CachedValueProvider.Result.create(null, psiFile);
        }
        try {
          String text = VfsUtilCore.loadText(theVirtualFile);
          return CachedValueProvider.Result.create(parseSectionData(text), psiFile);
        }
        catch (Exception e) {
          LOG.info(String.format("Could not read config data from file [%s]", theVirtualFile.getCanonicalPath()), e);
        }
        return CachedValueProvider.Result.create(null, psiFile);
      }, false, psiFile);
    });
  }

  private static @Nullable ConfigData parseSectionData(@Nullable String text) {
    JsonObject jsonObject = JsonUtil.tryParseJsonObject(text);
    if (jsonObject == null) {
      return null;
    }

    JsonObject sectionObject = JsonUtil.getChildAsObject(jsonObject, CONFIG_SECTION_NAME);
    if (sectionObject == null) {
      return null;
    }
    List<String> plugins = getChildAsSingleStringOrList(sectionObject, "plugins", "plugin");
    List<String> globals = getChildAsSingleStringOrList(sectionObject, "globals", "global");
    List<String> ignored = getChildAsSingleStringOrList(sectionObject, "ignore");
    String parser = JsonUtil.getChildAsString(sectionObject, "parser");
    List<String> environment = getEnvironment(sectionObject);
    return new ConfigData(plugins, ignored, globals, environment, parser);
  }

  private static @NotNull List<String> getEnvironment(@NotNull JsonObject sectionObject) {
    JsonElement child = JsonUtil.findChild(sectionObject, "env", "envs");
    JsonObject object = JsonUtil.getAsObject(child);
    return object != null ? JsonUtil.keys(object) : getAsSingleStringOrList(child);
  }

  private static @NotNull List<String> getChildAsSingleStringOrList(@NotNull JsonObject obj, String... names) {
    JsonElement child = JsonUtil.findChild(obj, names);
    return child == null ? ContainerUtil.emptyList() : getAsSingleStringOrList(child);
  }

  private static @NotNull List<String> getAsSingleStringOrList(@Nullable JsonElement element) {
    if (element == null) {
      return ContainerUtil.emptyList();
    }
    String string = JsonUtil.getString(element);
    return string != null ? Collections.singletonList(string) : JsonUtil.getAsStringList(element);
  }

  public static class ConfigData {

    public ConfigData(@NotNull List<String> plugins,
                      @NotNull List<String> ignored,
                      @NotNull List<String> globals,
                      @NotNull List<String> env,
                      String parser) {
      this.plugins = plugins;
      this.ignored = ignored;
      this.globals = globals;
      this.env = env;
      this.parser = parser;
    }

    public final List<String> plugins;
    public final List<String> ignored;
    public final List<String> globals;
    public final List<String> env;
    public final String parser;

    @Override
    public String toString() {
      return "ConfigData{" +
             "plugins=" + plugins +
             ", ignored=" + ignored +
             ", globals=" + globals +
             ", env=" + env +
             ", parser='" + parser + '\'' +
             '}';
    }
  }
}
