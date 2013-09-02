package com.intellij.flex.maven;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ClassGenerator {
  @SuppressWarnings({"ResultOfMethodCallIgnored", "DynamicRegexReplaceableByCompiledPattern"})
  public static void main(String[] ags) throws IOException {
    File dir = new File("generator-server-31/src/com/intellij/flex/maven");
    for (File file : dir.listFiles()) {
      file.delete();
    }

    List<String> classNames = new ArrayList<String>();
    List<Path> newFiles = new ArrayList<Path>();
    for (File file : new File("generator-server/src/com/intellij/flex/maven").listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".java");
      }
    })) {
      String data = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
      String newData = data;
      newData = newData.replaceAll("org.sonatype.aether.util.", "org.eclipse.aether.");
      newData = newData.replaceAll("org.sonatype.aether.impl.internal.", "org.eclipse.aether.internal.impl.");
      newData = newData.replaceAll("org.sonatype.aether.", "org.eclipse.aether.");
      Path path = FileSystems.getDefault().getPath(file.getPath().replace("generator-server", "generator-server-31"));
      if (!data.equals(newData)) {
        Files.write(path, newData.getBytes(StandardCharsets.UTF_8));
        //classNames.add(file.getName().substring(0, file.getName().length() - ".java".length()));
        newFiles.add(path);
      }
      else {
        Files.copy(file.toPath(), path);
      }
    }

    for (Path path : newFiles) {
      String data = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
      String newData = data;
      if (!data.equals(newData)) {
        Files.write(path, newData.getBytes(StandardCharsets.UTF_8));
      }
    }
  }
}