package com.intellij.lang.javascript.flex.debug;

import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nik
 */
public class FlexSuspendContext extends XSuspendContext {
  private final FlexExecutionStack myFlexExecutionStack;
  private static final Pattern STACK_FRAMES_DELIMITER = Pattern.compile(".(\\r?\\n)#\\d+ ");
  private static final String AT_MARKER = "at ";

  public FlexSuspendContext(FlexStackFrame stackFrame) {
    myFlexExecutionStack = new FlexExecutionStack(stackFrame);
  }

  public XExecutionStack getActiveExecutionStack() {
    return myFlexExecutionStack;
  }

  private static class FlexExecutionStack extends XExecutionStack {
    private final FlexStackFrame myTopFrame;

    private FlexExecutionStack(final FlexStackFrame stackFrame) {
      super("");

      myTopFrame = stackFrame;
    }

    public XStackFrame getTopFrame() {
      return myTopFrame;
    }

    public void computeStackFrames(final int frameIndex, final XStackFrameContainer container) {
      myTopFrame.getDebugProcess().sendCommand(new DebuggerCommand("bt", CommandOutputProcessingType.SPECIAL_PROCESSING) {
        @Override
        CommandOutputProcessingMode onTextAvailable(@NonNls final String s) {
          if (container.isObsolete()) return CommandOutputProcessingMode.DONE;
          processFrames(s, container, frameIndex);
          return CommandOutputProcessingMode.DONE;
        }
      });
    }

    private void processFrames(String s, XStackFrameContainer container, int frameIndex) {
      String[] frames = splitStackFrames(s);
      final XStackFrame[] allFrames = new XStackFrame[frames.length];
      int i = 0;

      final FlexDebugProcess flexDebugProcess = myTopFrame.getDebugProcess();

      if (frames.length == 0) {
        container.addStackFrames(Collections.<XStackFrame>emptyList(), true); // empty value
        return;
      }
      String stackFrame = frames[i];
      myTopFrame.setScope(extractScope(stackFrame));
      myTopFrame.setFrameIndex(0);
      allFrames[i++] = myTopFrame;

      while (i < frames.length) {
        stackFrame = frames[i];
        VirtualFile file = null;

        final Trinity<String, String, Integer> fileNameAndIndexAndLine = getFileNameAndIndexAndLine(frames[i]);
        final String fileName = fileNameAndIndexAndLine.first;
        final String fileId = fileNameAndIndexAndLine.second;
        int line = fileNameAndIndexAndLine.third;

        if (!StringUtil.isEmpty(fileName)) {
          final String packageName;
          final int classMarkerIndex = stackFrame.indexOf(FlexStackFrame.CLASS_MARKER);
          final int packageEndIndex = stackFrame.indexOf("::", classMarkerIndex);
          final int classEndIndex = stackFrame.indexOf("'", classMarkerIndex);

          if (classMarkerIndex > 0 && packageEndIndex > classMarkerIndex && packageEndIndex < classEndIndex) {
            packageName = stackFrame.substring(classMarkerIndex + FlexStackFrame.CLASS_MARKER.length(), packageEndIndex);
          }
          else {
            packageName = "";
          }

          file = flexDebugProcess.findFileByNameOrId(fileName, packageName, fileId);

          if (file == null) {
            // todo find position in decompiled code
          }
        }

        final FlexStackFrame flexStackFrame =
          new FlexStackFrame(flexDebugProcess,
                             file != null ? XDebuggerUtil.getInstance().createPosition(file, line > 0 ? line - 1 : line) : null);
        allFrames[i] = flexStackFrame;
        flexStackFrame.setScope(extractScope(stackFrame));
        flexStackFrame.setFrameIndex(i);
        i++;
      }

      container.addStackFrames(Arrays.asList(allFrames).subList(frameIndex, allFrames.length), true);
    }
  }

  static String[] splitStackFrames(String s) {
    Matcher m = STACK_FRAMES_DELIMITER.matcher(s);
    ArrayList<String> result = new ArrayList<String>();
    int prev = 0;
    while (m.find()) {
      result.add(s.substring(prev, m.start(1)));
      prev = m.end(1);
    }
    result.add(s.substring(prev));
    return ArrayUtil.toStringArray(result);
  }

  static String extractScope(final String stackFrame) {
    // #0   this = [Object 65683745, class='c::Bar$'].Bar$/get text() at Bar.as#23:7
    // #0   this = [Object 403095729, class='DisplayShelf'].DisplayShelf() at DisplayShelf.as:156
    // #2   apply() at <null>:65535
    // #3   EventDispatcher/dispatchEventFunction() at <null>:0
    int startIndex = stackFrame.indexOf(']');
    if (startIndex == -1) {
      startIndex = stackFrame.indexOf(' ');
      while (startIndex != -1 && stackFrame.length() > startIndex + 1 && stackFrame.charAt(startIndex + 1) == ' ') {
        startIndex++;
      }
    }
    final int dotIndex = Math.max(startIndex, stackFrame.indexOf('.', startIndex + 1));
    int lparIndex = stackFrame.indexOf('(', dotIndex);
    final String clsMethodText = stackFrame.substring(dotIndex + 1, lparIndex != -1 ? lparIndex : stackFrame.length());
    int methodStart = clsMethodText.indexOf('/');

    return methodStart == -1 ? clsMethodText : clsMethodText.substring(methodStart + 1) + ": " + clsMethodText.substring(0, methodStart);
  }

  @Nullable
  static FlexStackFrame getStackFrame(final FlexDebugProcess flexDebugProcess,
                                      final String frameText) {
    // #0   global$init() at Some4Class.as#42:6
    // #0   FlexSprite() at FlexSprite.as:59
    // #2   Some4Class() at Singleton.as#16:0
    // #2   UIComponent() at <null>:0\r\nNo active session
    // #0   global/publicFun() at publicFun.as#41:5
    // #0   HelloFlex4/button1_clickHandler(event=[Object 181591585, class='flash.events::MouseEvent']) at HelloFlex4.mxml#40:11
    // #0   NameUtil$/createUniqueName(object=[Object 172624937, class='mx.controls::TextInput']) at NameUtil.as#29:65
    // #0   EventDispatcher/dispatchEvent(_arg1=null) at <null>:0\r\nNo active session
    // #0   FlexEvent(type="valueCommit", bubbles=false, cancelable=false) at FlexEvent.as#18:1178
    // #0   HelloFlex4/get abc() at HelloFlex4.mxml#40:18
    // #0   HelloFlex4/set abc(value="Asd") at HelloFlex4.mxml#40:22

    VirtualFile file = null;

    final Trinity<String, String, Integer> fileNameAndIndexAndLine = getFileNameAndIndexAndLine(frameText);
    final String fileName = fileNameAndIndexAndLine.first;
    final String fileId = fileNameAndIndexAndLine.second;
    int line = fileNameAndIndexAndLine.third;

    if (fileName != null) {
      file = flexDebugProcess.findFileByNameOrId(fileName, null, fileId);

      if (file == null) {
        // todo find position in decompiled code
        final boolean noname = StringUtil.isEmpty(fileName) || fileName.equals("<null>");
        final String text = noname ? "// File name is not available" : "// No source code for " + fileName + ":" + line;
        file = new LightVirtualFile(noname ? "noname.as" : fileName, text);
        ((LightVirtualFile)file).setWritable(false);
        line = 1;
      }
    }

    return new FlexStackFrame(flexDebugProcess,
                              file != null ? XDebuggerUtil.getInstance().createPosition(file, line > 0 ? line - 1 : line) : null);
  }

  private static Trinity<String, String, Integer> getFileNameAndIndexAndLine(final String text) {
    final int atPos = text.lastIndexOf(AT_MARKER);
    if (atPos == -1) return Trinity.create(null, null, 0);

    final String fileName;
    String fileId = null;
    int line = 0;

    final int eolIndex = Math.min(text.indexOf("\r", atPos), text.indexOf("\n", atPos));
    final String fileNameAndFurther = text.substring(atPos + AT_MARKER.length(), eolIndex > atPos ? eolIndex : text.length());
    final int hashIndex = fileNameAndFurther.indexOf('#');
    final int colonIndex = fileNameAndFurther.indexOf(':', hashIndex);

    if (hashIndex != -1) {
      fileName = fileNameAndFurther.substring(0, hashIndex);
      if (colonIndex > hashIndex) {
        fileId = fileNameAndFurther.substring(hashIndex + 1, colonIndex);
        try {
          line = Integer.parseInt(fileNameAndFurther.substring(colonIndex + 1));
        }
        catch (NumberFormatException e) {/*ignore*/}
      }
    }
    else {
      if (colonIndex != -1) {
        fileName = fileNameAndFurther.substring(0, colonIndex);
        try {
          line = Integer.parseInt(fileNameAndFurther.substring(colonIndex + 1));
        }
        catch (NumberFormatException e) {/*ignore*/}
      }
      else {
        fileName = fileNameAndFurther;
      }
    }

    return Trinity.create(fileName, fileId, line);
  }
}
