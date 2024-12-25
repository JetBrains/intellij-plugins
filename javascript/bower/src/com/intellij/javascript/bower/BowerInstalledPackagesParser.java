package com.intellij.javascript.bower;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.intellij.util.ObjectUtils;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BowerInstalledPackagesParser {
  private BowerInstalledPackagesParser() {
  }

  public static @NotNull List<BowerInstalledPackage> parse(@NotNull String jsonContent) throws IOException {
    try (JsonReader jsonReader = new JsonReader(new StringReader(jsonContent))) {
      JsonElement element = JsonParser.parseReader(jsonReader);
      return doParsePackages(element);
    }
  }

  private static @NotNull List<BowerInstalledPackage> doParsePackages(@NotNull JsonElement element) throws IOException {
    if (!element.isJsonObject()) {
      throw new IOException("Unexpected root element");
    }
    JsonObject root = element.getAsJsonObject();
    JsonElement dependenciesElement = root.get("dependencies");
    if (dependenciesElement == null || !dependenciesElement.isJsonObject()) {
      throw new IOException("Top level 'dependencies' object key not found");
    }
    JsonObject dependenciesObject = dependenciesElement.getAsJsonObject();
    List<BowerInstalledPackage> packages = new ArrayList<>();
    for (Map.Entry<String, JsonElement> entry : dependenciesObject.entrySet()) {
      JsonElement packageElement = entry.getValue();
      if (packageElement != null && packageElement.isJsonObject()) {
        BowerInstalledPackage pkg = parsePackage(packageElement.getAsJsonObject());
        if (pkg != null) {
          packages.add(pkg);
        }
      }
    }
    return packages;
  }

  private static @Nullable BowerInstalledPackage parsePackage(@NotNull JsonObject pkgObject) {
    String name = null;
    String _release = null;
    String target = null;
    String latest = null;
    JsonObject endpointObject = JsonUtil.getChildAsObject(pkgObject, "endpoint");
    if (endpointObject != null) {
      name = JsonUtil.getChildAsString(endpointObject, "name");
    }
    JsonObject pkgMetaObject = JsonUtil.getChildAsObject(pkgObject, "pkgMeta");
    if (pkgMetaObject != null) {
      if (name == null) {
        name = JsonUtil.getChildAsString(pkgMetaObject, "name");
      }
      _release = JsonUtil.getChildAsString(pkgMetaObject, "_release");
    }
    JsonElement updateElement = pkgObject.get("update");
    if (updateElement != null && updateElement.isJsonObject()) {
      JsonObject updateObject = updateElement.getAsJsonObject();
      target = JsonUtil.getChildAsString(updateObject, "target");
      latest = JsonUtil.getChildAsString(updateObject, "latest");
    }
    if (name != null && !name.isEmpty()) {
      return new BowerInstalledPackage(name, ObjectUtils.chooseNotNull(target, _release), latest);
    }
    return null;
  }

}
