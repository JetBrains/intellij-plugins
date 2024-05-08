package com.intellij.javascript.bower.browsePackages;

import com.google.gson.stream.JsonWriter;
import com.intellij.javascript.bower.BowerPackageBasicInfo;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class BowerPopularPackagesGenerator {
  public static void main(String[] args) throws IOException {
    List<BowerPackageBasicInfo> infos = BowerPackageSearcher.doFindPackagesByPrefix(null, "", 30);
    File output = FileUtil.createTempFile("bower-popular-packages-", ".json", false);
    writePackages(infos, output);
  }

  private static void writePackages(@NotNull List<BowerPackageBasicInfo> names, @NotNull File outputFile) throws IOException {
    try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
      writer.setIndent(" ");
      writer.beginArray();
      for (BowerPackageBasicInfo info : names) {
        writer.beginObject();
        writer.setIndent("");
        writer.name(BowerPackageSearcher.NAME_PROP);
        writer.value(info.getName());
        if (info.getDescription() != null) {
          writer.name(BowerPackageSearcher.DESCRIPTION_PROP);
          writer.value(info.getDescription());
        }
        writer.endObject();
        writer.setIndent(" ");
      }
      writer.endArray();
    }
    System.out.println(names.size() + " packages are written to " + outputFile.getAbsolutePath());
  }
}
