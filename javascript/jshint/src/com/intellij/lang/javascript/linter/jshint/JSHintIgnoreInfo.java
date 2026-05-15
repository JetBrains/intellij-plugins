package com.intellij.lang.javascript.linter.jshint;

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSHintIgnoreInfo {

  private static final char SEPARATOR_CHAR = '/';
  private static final Pattern[] TRUNCATING_PATTERNS = new Pattern[] {
    // /path/to/{,model/}*.js',
    Pattern.compile("/[^/]*\\{.*\\}.*$"),

    // /path/to/!(...)
    Pattern.compile("/[^/]*!\\(.*$"),

    // /path/to/+(...)
    Pattern.compile("/[^/]*\\+\\(.*$"),

    // /path/to/(...)?
    Pattern.compile("/[^/]*\\)\\?.*$"),
  };

  private final List<VirtualFile> myIgnoreRoots;
  private final List<Pattern> myIgnorePatterns;
  private final VirtualFile myIgnoreDir;

  public JSHintIgnoreInfo(@NotNull VirtualFile jshintIgnore, @NotNull List<String> patterns) {
    myIgnoreRoots = new ArrayList<>();
    myIgnorePatterns = new ArrayList<>();
    myIgnoreDir = jshintIgnore.getParent();
    if (myIgnoreDir != null && myIgnoreDir.isDirectory()) {
      for (String pattern : patterns) {
        String refinedPattern = pattern.replace('\\', SEPARATOR_CHAR);
        refinedPattern = findPrefix(refinedPattern);
        VirtualFile file = myIgnoreDir.findFileByRelativePath(refinedPattern);
        if (file != null && file.isValid()) {
          myIgnoreRoots.add(file);
        }
        else {
          Pattern p = buildPattern(refinedPattern);
          myIgnorePatterns.add(p);
        }
      }
    }
  }

  private static @NotNull Pattern buildPattern(@NotNull String pattern) {
    int i = 0;
    StringBuilder res = new StringBuilder("^");
    while (i < pattern.length()) {
      int singleInd = pattern.indexOf("*", i);
      if (singleInd == -1) {
        break;
      }
      addQuotePattern(res, pattern, i, singleInd);
      if (singleInd + 2 == pattern.length() && pattern.startsWith("**", singleInd)) {
        res.append(".*");
        i = singleInd + 2;
      }
      else if (pattern.startsWith("**/", singleInd)) {
        res.append(".*");
        i = singleInd + 3;
      }
      else {
        res.append("[^/]*");
        i = singleInd + 1;
      }
    }
    addQuotePattern(res, pattern, i, pattern.length());
    res.append("$");
    return Pattern.compile(res.toString());
  }

  private static void addQuotePattern(@NotNull StringBuilder buf, @NotNull String pattern, int fromInc, int toExc) {
    if (fromInc < toExc) {
      buf.append(Pattern.quote(pattern.substring(fromInc, toExc)));
    }
  }

  public static @NotNull String findPrefix(@NotNull String refinedPattern) {
    for (Pattern pattern : TRUNCATING_PATTERNS) {
      refinedPattern = pattern.matcher(refinedPattern).replaceFirst("");
    }
    return refinedPattern;
  }

  public boolean isIgnore(@NotNull VirtualFile file) {
    for (VirtualFile root : myIgnoreRoots) {
      if (VfsUtilCore.isAncestor(root, file, false)) {
        return true;
      }
    }
    if (myIgnoreDir != null && !myIgnorePatterns.isEmpty() && VfsUtilCore.isAncestor(myIgnoreDir, file, true)) {
      String relativePath = VfsUtilCore.getRelativePath(file, myIgnoreDir, SEPARATOR_CHAR);
      if (relativePath == null) {
        return false;
      }
      for (Pattern pattern : myIgnorePatterns) {
        Matcher matcher = pattern.matcher(relativePath);
        if (matcher.matches()) {
          return true;
        }
      }
    }
    return false;
  }
}
