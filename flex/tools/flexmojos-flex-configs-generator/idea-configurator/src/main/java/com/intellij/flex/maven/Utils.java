package com.intellij.flex.maven;

import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

final class Utils {
  static boolean isFlashProject(MavenProject project) {
   return project.getPackaging().equals("swc") || project.getPackaging().equals("swf");
  }

  static void copyFile(File fromFile, File toFile) throws IOException {
    final FileChannel fromChannel = new FileInputStream(fromFile).getChannel();
    final FileChannel toChannel = new FileOutputStream(toFile).getChannel();
    try {
      fromChannel.transferTo(0, fromFile.length(), toChannel);
      //noinspection ResultOfMethodCallIgnored
      toFile.setLastModified(fromFile.lastModified());
    }
    finally {
      fromChannel.close();
      toChannel.close();
    }
  }
}
