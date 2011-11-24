package com.jetbrains.actionscript.profiler.sampler;

import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim
 * Date: 15.11.10
 * Time: 10:06
 */
public class SampleLocationResolver implements Navigatable {
  private static final int UNKNOWN_LINE = -1;
  private @Nullable VirtualFile resolvedFile;
  private int resolvedLine = UNKNOWN_LINE;
  private @Nullable PsiElement resolvedElement;
  private final GlobalSearchScope scope;

  public SampleLocationResolver(String path, GlobalSearchScope scope) {
    this.scope = scope;
    String s = path;
    boolean resolved = false;
    int line = UNKNOWN_LINE;
    final int i = s.indexOf('[');
    final int i2 = s.indexOf(']', i);

    if (i != -1 && i2 != -1) { // try navigate from debug info [path;package path;file:line] , btw it can be invalid for SDK
      s = s.substring(i + 1, i2);
      int i3 = s.indexOf(';');
      int i4 = s.indexOf(';', i3 + 1);

      if (i3 == -1) return;
      String dir = s.substring(0, i3);
      String packageName = s.substring(i3 + 1, i4);
      String filename = s.substring(i4 + 1);

      if (packageName.length() > 0) {
        dir += File.separatorChar + packageName.replace('.', File.separatorChar);
      }
      dir += File.separatorChar;
      final int endIndex = filename.indexOf(':');
      dir += filename.substring(0, endIndex != -1 ? endIndex : filename.length());
      VirtualFile relativeFile = VfsUtil.findRelativeFile(dir, null);

      if (relativeFile == null) {
        String className = packageName.replace('\\', '.').replace('/', '.');
        if (className.length() > 0) className += ".";
        className += filename.substring(0, filename.indexOf('.'));

        PsiElement element = JSResolveUtil.findClassByQName(className, scope);
        if (element != null) element = element.getNavigationElement();
        if (element != null && element.isWritable()) {
          relativeFile = element.getContainingFile().getVirtualFile();
        }
      }

      line = endIndex != -1 ? Integer.parseInt(filename.substring(endIndex + 1)) - 1 : 0;

      if (relativeFile != null) {
        resolvedFile = relativeFile;
        this.resolvedLine = line;
        resolved = true;
      }
    }

    if (!resolved) { // resolve via qname package::Class/((get|set) )?method name()
      LocationInfo locationInfo = buildMethodInfo(path);

      PsiElement element = JSResolveUtil.findClassByQName(locationInfo.clazz, scope);
      if (element instanceof XmlBackedJSClassImpl) {
        final PsiElement candidateElement = locationInfo.findFunctionOrField((XmlBackedJSClassImpl)element);
        if (candidateElement != null) element = candidateElement;
      }
      else {
        if (element != null) element = element.getNavigationElement();
        if (element instanceof JSClass) {
          final PsiElement candidateElement = locationInfo.findFunctionOrField((JSClass)element);
          if (candidateElement != null) element = candidateElement;
        }
      }

      if (element instanceof Navigatable) {
        if (line != UNKNOWN_LINE) {
          this.resolvedLine = line;
          resolvedFile = element.getContainingFile().getVirtualFile();
        }
        else {
          resolvedElement = element;
        }
      }
    }
  }

  private final static String[] suffixes = {"$", "$cinit()"};

  @Override
  public void navigate(boolean requestFocus) {
    if (resolvedElement != null) {
      ((Navigatable)resolvedElement).navigate(true);
    }
    else if (resolvedFile != null) {
      new OpenFileDescriptor(scope.getProject(), resolvedFile, resolvedLine, 0).navigate(true);
    }
  }

  @Override
  public boolean canNavigate() {
    return canNavigateToSource();
  }

  @Override
  public boolean canNavigateToSource() {
    return resolvedElement != null || resolvedFile != null;
  }

  public static LocationInfo buildMethodInfo(String s) {
    int i = s.indexOf('[');
    if (i != -1) s = s.substring(0, i);
    final int packageEnd = s.indexOf("::");
    int methodNameStart = s.indexOf('/', packageEnd + 1);
    String qName = methodNameStart != -1 ? s.substring(0, methodNameStart) : s;
    qName = StringUtil.replace(qName, "::", ".");

    for (String suffix : suffixes) {
      if (qName.endsWith(suffix)) {
        qName = qName.substring(0, qName.length() - suffix.length());
        break;
      }
    }

    if (methodNameStart != -1) {
      ++methodNameStart;
      int methodNameEnd = s.indexOf('(', methodNameStart);
      if (methodNameEnd == -1) methodNameEnd = s.length();
      int possibleGetSet = s.indexOf(' ', methodNameStart);
      final int oldMethodStart = methodNameStart;
      if (possibleGetSet != -1) methodNameStart = possibleGetSet + 1;
      String methodName = s.substring(methodNameStart, methodNameEnd);
      final int nsStart = methodName.indexOf("::");
      String namespace = null;
      if (nsStart != -1) {
        namespace = methodName.substring(0, nsStart); // todo use namespace
        methodName = methodName.substring(nsStart + "::".length());
      }
      JSFunction.FunctionKind kind = null;

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

      return new LocationInfo(qName, methodName, kind, namespace);
    }
    return new LocationInfo(qName, null, null, null);
  }

  public static class LocationInfo {
    final String clazz;
    final String name;
    final JSFunction.FunctionKind kind;
    final String namespace;

    public LocationInfo(String qName, String _methodName, JSFunction.FunctionKind _kind, String _namespace) {
      clazz = qName;
      name = _methodName;
      kind = _kind;
      namespace = _namespace;
    }

    public String getClazz() {
      return clazz;
    }

    public String getName() {
      return name;
    }

    @Nullable
    public PsiElement findFunctionOrField(JSClass clazz) {
      PsiElement result = kind != null ? clazz.findFunctionByNameAndKind(name, kind) : clazz.findFunctionByName(name);
      if (result == null && (kind == JSFunction.FunctionKind.GETTER || kind == JSFunction.FunctionKind.SETTER)) {
        //generated getter/setter in case of binding
        result = clazz.findFieldByName(name);
      }
      if (result == null && name != null && name.startsWith("__")) {
        //generated handler
        int i = name.lastIndexOf('_');
        if (i > 2) {
          result = clazz.findFieldByName(name.substring(2, i));
        }
      }
      return result;
    }
  }
}
