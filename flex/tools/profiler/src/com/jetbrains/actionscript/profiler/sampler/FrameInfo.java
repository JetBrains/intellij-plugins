package com.jetbrains.actionscript.profiler.sampler;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.actionscript.profiler.base.QNameProducer;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author: Fedor.Korotkov
 */
public class FrameInfo implements Comparable<FrameInfo>, QNameProducer {
  public static final FrameInfo[] EMPTY_FRAME_INFO_ARRAY = new FrameInfo[0];

  private int fileLine;
  private final String qName;

  @Nullable
  private final String fileDirectory;
  @Nullable
  private final String fileName;
  @Nullable
  private final String packageName;
  @Nullable
  private final String methodName;
  @Nullable
  private final JSFunction.FunctionKind kind;
  @Nullable
  private final String namespace;

  protected FrameInfo(@Nullable String fileDirectory,
                      @Nullable String fileName,
                      int fileLine,
                      @Nullable String packageName,
                      String qName,
                      @Nullable String methodName,
                      @Nullable JSFunction.FunctionKind kind,
                      @Nullable String namespace) {
    this.fileDirectory = fileDirectory;
    this.fileName = fileName;
    this.fileLine = fileLine;
    this.packageName = packageName;
    this.qName = qName;
    this.methodName = methodName;
    this.kind = kind;
    this.namespace = namespace;
  }

  @Nullable
  public String getFilePath() {
    if (getFileDirectory() == null) {
      return getFileName();
    }
    final StringBuilder pathBuilder = new StringBuilder();
    pathBuilder.append(getFileDirectory());
    if (packageName != null) {
      pathBuilder.append(File.separator);
      pathBuilder.append(packageName.replace('.', File.separatorChar));
    }
    pathBuilder.append(File.separator);
    pathBuilder.append(getFileName());
    return pathBuilder.toString();
  }

  @Nullable
  public String getFileDirectory() {
    return fileDirectory;
  }

  @Nullable
  public String getFileName() {
    return fileName;
  }

  public int getFileLine() {
    return fileLine;
  }

  public void setFileLine(int fileLine) {
    this.fileLine = fileLine;
  }

  @Nullable
  public String getPackageName() {
    return packageName;
  }

  @Override
  public String getQName() {
    return qName;
  }

  @Nullable
  public String getMethodName() {
    return methodName;
  }

  @Nullable
  public JSFunction.FunctionKind getKind() {
    return kind;
  }

  @Nullable
  public String getNamespace() {
    return namespace;
  }

  public boolean isSystem() {
    return qName.length() == 0;
  }

  public boolean isAnonymous() {
    return "Function".equals(getQName()) && "<anonymous>".equals(getMethodName());
  }

  public boolean isInnerClass() {
    return !getClassName().equals(getClassNameByFileName());
  }

  public String getClassName() {
    String className = getQName();
    int i = className.lastIndexOf('.');
    if (i != -1 && className.charAt(i + 1) == '<') {
      final String temp = className.substring(0, i);
      i = temp.lastIndexOf('.');
    }
    if (i != -1) {
      className = className.substring(i + 1);
    }
    return className;
  }

  public String toSimpleString() {
    return getBuilderWithBasicInfo().toString();
  }

  @Override
  public String toString() {
    StringBuilder builder = getBuilderWithBasicInfo();
    if (getPackageName() != null) {
      builder.insert(0, getPackageName());
    }
    if (fileName != null) {
      builder.append("[");
      builder.append(fileName);
      builder.append(":");
      builder.append(fileLine);
      builder.append("]");
    }
    return builder.toString();
  }

  private StringBuilder getBuilderWithBasicInfo() {
    StringBuilder builder = new StringBuilder();
    builder.append(getClassName());
    if (getClassName().length() > 0 && methodName != null) {
      builder.append(".");
    }
    if (kind != null) {
      builder.append(kind == JSFunction.FunctionKind.GETTER ? "get" : "set");
      builder.append(" ");
    }
    if (methodName != null) {
      builder.append(methodName);
    }
    return builder;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FrameInfo)) {
      return false;
    }
    FrameInfo other = (FrameInfo)obj;
    boolean result = getClassName().equals(other.getClassName());
    result = result && StringUtil.equals(getPackageName(), other.getPackageName());
    result = result && StringUtil.equals(methodName, other.getMethodName());
    result = result && kind == other.getKind();
    if (isAnonymous() ^ other.isAnonymous()) {
      return false;
    }
    else if (isAnonymous() && other.isAnonymous()) {
      result = result && StringUtil.equals(fileName, other.getFileName());
      result = result && fileLine == other.getFileLine();
    }
    return result;
  }

  @Override
  public int hashCode() {
    int result = getClassName().hashCode();
    if (packageName != null) {
      result = 31 * result + packageName.hashCode();
    }
    if (methodName != null) {
      result = 31 * result + methodName.hashCode();
    }
    if (kind != null) {
      result = 31 * result + kind.hashCode();
    }
    if (isAnonymous() && fileName != null) {
      result = 31 * result + fileName.hashCode();
    }
    if (isAnonymous()) {
      result += fileLine;
    }
    return result;
  }

  @Override
  public int compareTo(FrameInfo o) {
    int tmp = StringUtil.compare(getClassName(), o.getClassName(), false);
    if (tmp != 0) return tmp;
    tmp = StringUtil.compare(getMethodName(), o.getMethodName(), false);
    if (tmp != 0) return tmp;
    tmp = StringUtil.compare(getPackageName(), o.getPackageName(), false);
    if (tmp != 0) return tmp;
    if (kind != null) {
      tmp = kind.compareTo(o.getKind());
    }
    if (!isAnonymous()) {
      return tmp;
    }
    tmp = StringUtil.compare(getPackageName(), o.getPackageName(), false);
    if (tmp != 0) return tmp;
    tmp = StringUtil.compare(getFileName(), o.getFileName(), false);
    if (tmp != 0) return tmp;
    return fileLine - o.getFileLine();
  }

  @Nullable
  public String getClassNameByFileName() {
    String result = getFileName();
    if (result == null) {
      return null;
    }
    int i = result.lastIndexOf('.');
    if (i != -1) {
      return result.substring(0, i);
    }
    return result;
  }

  @Nullable
  public String getQNameByFile() {
    String qName = getPackageName();
    qName = qName != null ? qName.replace(File.separatorChar, '.') + '.' : "";
    String className = getClassNameByFileName();
    if (className == null) {
      return null;
    }
    return qName + className;
  }

  @Nullable
  public PsiElement findFunctionOrField(JSClass clazz) {
    PsiElement result = kind != null ? clazz.findFunctionByNameAndKind(methodName, kind) : clazz.findFunctionByName(methodName);
    if (result == null && (kind == JSFunction.FunctionKind.GETTER || kind == JSFunction.FunctionKind.SETTER)) {
      //generated getter/setter in case of binding
      result = clazz.findFieldByName(methodName);
    }
    if (result == null && methodName != null && methodName.startsWith("__")) {
      //generated handler
      int i = methodName.lastIndexOf('_');
      if (i > 2) {
        result = clazz.findFieldByName(methodName.substring(2, i));
      }
    }
    return result;
  }
}
