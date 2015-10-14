package com.intellij.lang.javascript.flex;

import com.intellij.lang.ant.config.execution.AntBuildMessageView;
import com.intellij.lang.ant.config.execution.AntMessage;
import com.intellij.lang.ant.config.execution.AntMessageCustomizer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.ant.config.execution.AntBuildMessageView.MessageType;

public class FlexAntMessageCustomizer extends AntMessageCustomizer {

  private static final String ERROR_MARKER = " Error: ";
  private static final String WARNING_MARKER = " Warning: ";
  public static final String COL_MARKER = "col: ";

  @Nullable
  public AntMessage createCustomizedMessage(final String text, final int priority) {
    // Searching for the same pattern as in FlexCompilerHandler.errorPattern, but avoid regexp for the sake of performance
    // Pattern.compile("(.*?)(\\(\\D.*\\))?(?:\\((-?\\d+)\\))?: ?(?:col: (-?\\d+):?)? (Warning|Error): (.*)");
    // C:\work\flex_projects\ant\src\com\flexTasks\LabelBuilder.as(21): col: 29 Error: Syntax error: leftparen before l.

    boolean isWarning = false;
    int errorOrWarningIndex = text.indexOf(ERROR_MARKER);
    if (errorOrWarningIndex == -1) {
      errorOrWarningIndex = text.indexOf(WARNING_MARKER);
      isWarning = true;
    }

    if (errorOrWarningIndex > 0) {
      final String pathAndInfoAndPosition = text.substring(0, errorOrWarningIndex);
      final int braceIndex = pathAndInfoAndPosition.indexOf('(');
      if (braceIndex > 0) {
        final String potentialPath = pathAndInfoAndPosition.substring(0, braceIndex);
        final String lowercasedPath = potentialPath.toLowerCase();
        if (lowercasedPath.endsWith(".as") || lowercasedPath.endsWith(".mxml") ||
            lowercasedPath.endsWith(".fxg") || lowercasedPath.endsWith(".css")) {
          final String infoAndPosition = pathAndInfoAndPosition.substring(lowercasedPath.length());

          final int lineOpenBraceIndex = infoAndPosition.lastIndexOf("(");
          final int lineCloseBraceIndex = lineOpenBraceIndex < 0 ? -1 : infoAndPosition.indexOf("):", lineOpenBraceIndex);
          if (lineOpenBraceIndex >= 0 && lineCloseBraceIndex > lineOpenBraceIndex + 1) {
            final String lineString = infoAndPosition.substring(lineOpenBraceIndex + 1, lineCloseBraceIndex);
            try {
              final int line = Integer.parseInt(lineString);

              int column = 1;
              final int colIndex = infoAndPosition.indexOf(COL_MARKER, lineCloseBraceIndex);
              if (colIndex > 0 && colIndex < infoAndPosition.length() - COL_MARKER.length() - 1) {
                final String colString = infoAndPosition.substring(colIndex + COL_MARKER.length(), infoAndPosition.length());
                column = Integer.parseInt(colString);
              }

              final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(potentialPath);
              if (file != null) {
                final String textWithoutPosition = potentialPath + infoAndPosition.substring(0, lineOpenBraceIndex) +
                                                   ": " + text.substring(errorOrWarningIndex + ERROR_MARKER.length());
                return new AntMessage(isWarning ? MessageType.MESSAGE : MessageType.ERROR,
                                      isWarning ? AntBuildMessageView.PRIORITY_WARN : AntBuildMessageView.PRIORITY_ERR,
                                      textWithoutPosition, file, line, column);
              }
            }
            catch (NumberFormatException ignore) {/*ignore*/}
          }
        }
      }
    }

    return null;
  }
}
