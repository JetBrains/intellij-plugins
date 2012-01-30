package com.jetbrains.actionscript.profiler.sampler;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.openapi.util.text.StringUtil;
import gnu.trove.THashMap;

import java.util.Map;

/**
 * @author: Fedor.Korotkov
 */
public class FrameInfoBuilder {
  private final String[] suffixes = {"$", "$cinit()"};
  private final Map<String, FrameInfo> cache = new THashMap<String, FrameInfo>();
  private final Map<FrameInfo, FrameInfo> cacheInstances = new THashMap<FrameInfo, FrameInfo>();
  private final int UNKNOWN_LINE = -1;

  FrameInfo[] buildInstances(String[] frames) {
    FrameInfo[] result = new FrameInfo[frames.length];
    for (int i = 0; i < frames.length; ++i) {
      result[i] = buildInstance(frames[i]);
    }
    return result;
  }

  public FrameInfo buildInstance(String data) {
    FrameInfo result = cache.get(data);
    if (result == null) {
      result = createInstance(data);
      FrameInfo cashedInstance = cacheInstances.get(result);
      //do NOT put anonymous in cache
      if (!result.isAnonymous() && cashedInstance != null) {
        int fileLine = Math.min(cashedInstance.getFileLine(), result.getFileLine());
        if (fileLine != -1) {
          cashedInstance.setFileLine(fileLine);
        }
        result = cashedInstance;
      }
      else if (!result.isAnonymous()) {
        cacheInstances.put(result, result);
      }
      cache.put(data, result);
    }
    return result;
  }

  public FrameInfo createInstance(final String data) {
    int line = UNKNOWN_LINE;
    String dir = null;
    String packageName = null;
    String filename = null;

    final int i = data.indexOf('[');
    final int i2 = data.indexOf(']', i);

    if (i != -1 && i2 != -1) { // try navigate from debug info [path;package path;file:line] , btw it can be invalid for SDK
      String s = data.substring(i + 1, i2);
      int i3 = s.indexOf(';');
      int i4 = s.indexOf(';', i3 + 1);

      if (i3 != -1) {
        dir = s.substring(0, i3);
        packageName = s.substring(i3 + 1, i4);
        filename = s.substring(i4 + 1);

        packageName = packageName.replace('\\', '.').replace('/', '.');
        final int endIndex = filename.indexOf(':');
        line = 0;
        if (endIndex != -1) {
          line = Integer.parseInt(filename.substring(endIndex + 1));
          filename = filename.substring(0, endIndex);
        }

        String className = packageName;
        if (className.length() > 0) className += ".";
        className += filename.substring(0, filename.indexOf('.'));
      }
    }

    String s = data;
    if (i != -1) s = s.substring(0, i);
    int packageEnd = s.indexOf("::");
    int methodNameStart = s.indexOf('/');
    if (methodNameStart < packageEnd) {
      //namespace
      packageEnd = -1;
    }
    String qName = methodNameStart != -1 ? s.substring(0, methodNameStart) : s;
    qName = StringUtil.replace(qName, "::", ".");

    for (String suffix : suffixes) {
      if (qName.endsWith(suffix)) {
        qName = qName.substring(0, qName.length() - suffix.length());
        break;
      }
    }

    String methodName = null;
    String namespace = null;
    JSFunction.FunctionKind kind = null;

    final int constructorStart = qName.indexOf('(');
    if (methodNameStart == -1 && constructorStart != -1) {
      qName = qName.substring(0, constructorStart);
      methodName = qName.substring(qName.lastIndexOf('.') + 1);
    }
    else if (methodNameStart != -1) {
      ++methodNameStart;
      int methodNameEnd = s.indexOf('(', methodNameStart);
      if (methodNameEnd == -1) methodNameEnd = s.length();
      int possibleGetSet = s.indexOf(' ', methodNameStart);
      final int oldMethodStart = methodNameStart;
      if (possibleGetSet != -1) methodNameStart = possibleGetSet + 1;
      methodName = s.substring(methodNameStart, methodNameEnd);
      final int nsStart = methodName.indexOf("::");
      if (nsStart != -1) {
        namespace = methodName.substring(0, nsStart);
        methodName = methodName.substring(nsStart + "::".length());
      }

      if (possibleGetSet != -1) {
        String getOrSet = s.substring(oldMethodStart, possibleGetSet);
        if ("get".equals(getOrSet)) {
          kind = JSFunction.FunctionKind.GETTER;
        }
        else if ("set".equals(getOrSet)) kind = JSFunction.FunctionKind.SETTER;
      }

      if ("ctor".equals(methodName)) {
        methodName = qName.substring(qName.lastIndexOf('.') + 1);
      }
    }

    if (qName.length() == 0 && data.matches("\\[.*\\]\\(\\)")) {
      methodName = data.substring(1, data.length() - 3);
    }

    if (packageName == null && qName.lastIndexOf('.') != -1) {
      packageName = qName.substring(0, qName.lastIndexOf('.'));
    }
    return new FrameInfo(dir, filename, line, packageName, qName, methodName, kind, namespace);
  }
}
