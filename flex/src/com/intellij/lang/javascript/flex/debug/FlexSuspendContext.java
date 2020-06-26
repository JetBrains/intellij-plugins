// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.debug;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtilRt;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlexSuspendContext extends XSuspendContext {
  private final FlexExecutionStack myFlexExecutionStack;
  private static final Pattern STACK_FRAMES_DELIMITER = Pattern.compile(".(\\r?\\n)#\\d+ ");
  private static final String AT_MARKER = "at ";

  public FlexSuspendContext(final FlexStackFrame topFrame) {
    myFlexExecutionStack = new FlexExecutionStack(topFrame);
  }

  public FlexSuspendContext(final FlexDebugProcess flexDebugProcess, final String[] frames) {
    myFlexExecutionStack = new FlexExecutionStack(createStackFrame(flexDebugProcess, frames[0]));
    myFlexExecutionStack.myAprioriKnownFrames = myFlexExecutionStack.getFrames(frames);
  }

  @Override
  public XExecutionStack getActiveExecutionStack() {
    return myFlexExecutionStack;
  }

  private static final class FlexExecutionStack extends XExecutionStack {
    private final FlexStackFrame myTopFrame;
    private @Nullable List<XStackFrame> myAprioriKnownFrames;

    private FlexExecutionStack(final FlexStackFrame topFrame) {
      super("");

      myTopFrame = topFrame;
    }

    @Override
    public XStackFrame getTopFrame() {
      return myTopFrame;
    }

    @Override
    public void computeStackFrames(final int frameIndex, final XStackFrameContainer container) {
      if (myAprioriKnownFrames != null) {
        container.addStackFrames(myAprioriKnownFrames.subList(frameIndex, myAprioriKnownFrames.size()), true);
      }
      else {
        myTopFrame.getDebugProcess().sendCommand(new DebuggerCommand("bt", CommandOutputProcessingType.SPECIAL_PROCESSING) {
          @Override
          CommandOutputProcessingMode onTextAvailable(@NonNls final String s) {
            if (container.isObsolete()) return CommandOutputProcessingMode.DONE;

            final List<XStackFrame> frames = getFrames(splitStackFrames(s));
            container.addStackFrames(frames.subList(frameIndex, frames.size()), true);
            return CommandOutputProcessingMode.DONE;
          }
        });
      }
    }

    private List<XStackFrame> getFrames(String[] frames) {
      final XStackFrame[] allFrames = new XStackFrame[frames.length];
      int i = 0;

      final FlexDebugProcess flexDebugProcess = myTopFrame.getDebugProcess();

      if (frames.length == 0) {
        return Collections.emptyList();
      }
      String frameText = frames[i];
      myTopFrame.setScope(extractScope(frameText));
      myTopFrame.setFrameIndex(0);
      allFrames[i++] = myTopFrame;

      while (i < frames.length) {
        frameText = frames[i];

        final FlexStackFrame flexStackFrame = createStackFrame(flexDebugProcess, frameText);

        allFrames[i] = flexStackFrame;
        flexStackFrame.setScope(extractScope(frameText));
        flexStackFrame.setFrameIndex(i);
        i++;
      }

      return Arrays.asList(allFrames);
    }
  }

  private static FlexStackFrame createStackFrame(final FlexDebugProcess flexDebugProcess, final String frameText) {
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

    // #0   this = [Object 94314641, class='BackWorker'].BackWorker/onProgress(event=[Object 246336977, class='flash.events::ProgressEvent']) at BackWorker.as:36
    // #1   EventDispatcher/dispatchEventFunction() at <null>:0
    // #2   this = [Object 106360769, class='fr.kikko.lab::ShineMP3Encoder'].EventDispatcher/dispatchEvent(_arg1=[Object 246336977, class='flash.events::ProgressEvent']) at <null>:0
    // #3   this = [Object 106360769, class='fr.kikko.lab::ShineMP3Encoder'].ShineMP3Encoder/update(event=[Object 246068457, class='flash.events::TimerEvent']) at ShineMP3Encoder.as:63
    // #4   Timer/_timerDispatch() at <null>:0
    // #5   this = [Object 152354881, class='flash.utils::Timer'].Timer/tick() at <null>:0


    VirtualFile file = null;

    final Trinity<String, String, Integer> fileNameAndIndexAndLine = getFileNameAndIdAndLine(frameText);
    final String fileName = fileNameAndIndexAndLine.first;
    final String fileId = fileNameAndIndexAndLine.second;
    final int line = fileNameAndIndexAndLine.third;

    if (!StringUtil.isEmpty(fileName)) {
      file = flexDebugProcess.findFileByNameOrId(fileName, getPackageFromFrameText(frameText), fileId);

      if (file == null) {
        // todo find position in decompiled code
      }
    }

    final VirtualFile finalFile = file;
    final XSourcePosition sourcePosition = file == null
                                           ? null
                                           : ReadAction.compute(
                                             () -> XDebuggerUtil.getInstance().createPosition(finalFile, line > 0 ? line - 1 : line));
    return sourcePosition != null ? new FlexStackFrame(flexDebugProcess, sourcePosition)
                                  : new FlexStackFrame(flexDebugProcess, fileName, line);
  }

  private static String getPackageFromFrameText(final String frameText) {
    // #2   this = [Object 106360769, class='fr.kikko.lab::ShineMP3Encoder'].EventDispatcher/dispatchEvent(_arg1=[Object 246336977, class='flash.events::ProgressEvent']) at <null>:0
    String packageName = null;

    int startIndex = frameText.indexOf(' ');
    while (startIndex != -1 && frameText.length() > startIndex && frameText.charAt(startIndex) == ' ') {
      startIndex++;
    }

    if (startIndex > 0 && frameText.substring(startIndex).startsWith("this = [")) {
      final int classMarkerIndex = frameText.indexOf(FlexStackFrame.CLASS_MARKER, startIndex);
      final int packageEndIndex = frameText.indexOf("::", classMarkerIndex + FlexStackFrame.CLASS_MARKER.length());
      final int classEndIndex = frameText.indexOf("']", classMarkerIndex + FlexStackFrame.CLASS_MARKER.length());

      if (classMarkerIndex > 0 && packageEndIndex > classMarkerIndex && packageEndIndex < classEndIndex) {
        packageName = frameText.substring(classMarkerIndex + FlexStackFrame.CLASS_MARKER.length(), packageEndIndex);
      }
      else {
        packageName = "";
      }
    }
    return packageName;
  }

  static String[] splitStackFrames(String s) {
    Matcher m = STACK_FRAMES_DELIMITER.matcher(s);
    ArrayList<String> result = new ArrayList<>();
    int prev = 0;
    while (m.find()) {
      result.add(s.substring(prev, m.start(1)));
      prev = m.end(1);
    }
    result.add(s.substring(prev));
    return ArrayUtilRt.toStringArray(result);
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

  private static Trinity<String, String, Integer> getFileNameAndIdAndLine(final String text) {
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
