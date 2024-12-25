package com.intellij.javascript.bower;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

public class BowerPackageInfo {

  private static final Logger LOG = Logger.getInstance(BowerPackageInfo.class);

  private final List<String> myVersions;
  private final JsonObject myLatestContent;

  public BowerPackageInfo(@NotNull List<String> versions, @Nullable JsonObject latestContent) {
    myVersions = ImmutableList.copyOf(versions);
    myLatestContent = latestContent;
  }

  public @NotNull List<String> getVersions() {
    return myVersions;
  }

  public @NotNull @Nls String formatHtmlDescription() {
    String jsonContent = getJsonContent();
    return HtmlChunk.html().children(
      HtmlChunk.head().child(HtmlChunk.styleTag(".line {padding-left:5px}")),
      HtmlChunk.body().attr("class", "line").child(HtmlChunk.tag("pre").addRaw(jsonContent))
    ).toString();
  }

  private @NlsSafe String getJsonContent() {
    if (myLatestContent != null) {
      StringWriter stringWriter = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(stringWriter);
      jsonWriter.setIndent("  ");
      jsonWriter.setLenient(true);
      try {
        Streams.write(myLatestContent, jsonWriter);
        return stringWriter.toString();
      }
      catch (IOException e) {
        LOG.warn("Can't stringify json", e);
      }
    }
    return BowerBundle.message("bower.no_description_available.text");
  }
}
