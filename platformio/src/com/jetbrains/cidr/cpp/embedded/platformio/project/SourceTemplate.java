package com.jetbrains.cidr.cpp.embedded.platformio.project;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static java.nio.charset.StandardCharsets.US_ASCII;

public final class SourceTemplate {
  public static final SourceTemplate NONE = new SourceTemplate("", "");
  public static final SourceTemplate GENERIC = new SourceTemplate(
    "main.c",
    "int main() {\n" +
    "// write your code here\n" +
    "}\n");

  public static final SourceTemplate ARDUINO = new SourceTemplate(
    "main.cpp",
    "#include <Arduino.h>\n" +
    "void setup() {\n" +
    "// write your initialization code here\n" +
    "}\n" +
    "\n" +
    "void loop() {\n" +
    "// write your code here\n" +
    "}");
  private final String fileName;
  private final byte[] content;

  private SourceTemplate(final @NotNull String fileName, final @NotNull String content) {
    this.fileName = fileName;
    this.content = content.getBytes(US_ASCII);
  }

  public @NotNull String getFileName() {
    return fileName;
  }

  public @NotNull byte[] getContent() {
    return content;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final SourceTemplate template = (SourceTemplate)o;

    if (!fileName.equals(template.fileName)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return fileName.hashCode();
  }

  public static SourceTemplate getByFrameworkName(final @NotNull String platformName) {
    if ("arduino".equalsIgnoreCase(platformName)) return ARDUINO;
    return GENERIC;
  }

  public static SourceTemplate getByFrameworkName(final @NotNull Collection<String> platformsNames) {
    if (platformsNames.contains("arduino")) return ARDUINO;
    return GENERIC;
  }
}
