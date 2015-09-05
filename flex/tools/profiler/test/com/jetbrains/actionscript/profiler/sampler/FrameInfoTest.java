package com.jetbrains.actionscript.profiler.sampler;

import com.intellij.lang.javascript.psi.JSFunction;
import junit.framework.TestCase;
import org.jetbrains.annotations.Nullable;

public class FrameInfoTest extends TestCase {

  public static void check(String data,
                           @Nullable String fileDirectory,
                           @Nullable String fileName,
                           int fileLine,
                           @Nullable String packageName,
                           String qName,
                           @Nullable String methodName,
                           @Nullable JSFunction.FunctionKind kind,
                           @Nullable String namespace) {
    FrameInfoBuilder frameInfoBuilder = new FrameInfoBuilder();
    checkFrameInfo(frameInfoBuilder.buildInstance(data),
                   fileDirectory,
                   fileName,
                   fileLine,
                   packageName,
                   qName,
                   methodName,
                   kind,
                   namespace);
  }

  private static void checkFrameInfo(FrameInfo frameInfo,
                                     @Nullable String fileDirectory,
                                     @Nullable String fileName,
                                     int fileLine,
                                     @Nullable String packageName,
                                     String qName,
                                     @Nullable String methodName,
                                     @Nullable JSFunction.FunctionKind kind,
                                     @Nullable String namespace) {
    assertEquals(fileDirectory, frameInfo.getFileDirectory());
    assertEquals(fileName, frameInfo.getFileName());
    assertEquals(fileLine, frameInfo.getFileLine());
    assertEquals(packageName, frameInfo.getPackageName());
    assertEquals(qName, frameInfo.getQName());
    assertEquals(methodName, frameInfo.getMethodName());
    assertEquals(kind, frameInfo.getKind());
    assertEquals(namespace, frameInfo.getNamespace());
  }

  public void testSimple() {
    check("com.test::Foo/bar()[C:\\workspace\\astella\\test\\src;com\\test;Foo.as:90]",
          "C:\\workspace\\astella\\test\\src",
          "Foo.as",
          90,
          "com.test",
          "com.test.Foo",
          "bar",
          null,
          null);
  }

  public void testSimpleGet() {
    check("com.test::Foo/get bar()[C:\\workspace\\astella\\test\\src;com\\test;Foo.as:90]",
          "C:\\workspace\\astella\\test\\src",
          "Foo.as",
          90,
          "com.test",
          "com.test.Foo",
          "bar",
          JSFunction.FunctionKind.GETTER,
          null);
  }

  public void testSimpleSet() {
    check("com.test::Foo/set bar()[C:\\workspace\\astella\\test\\src;com\\test;Foo.as:90]",
          "C:\\workspace\\astella\\test\\src",
          "Foo.as",
          90,
          "com.test",
          "com.test.Foo",
          "bar",
          JSFunction.FunctionKind.SETTER,
          null);
  }

  public void testConstructor() {
    check("com.test::Foo()[C:\\workspace\\astella\\test\\src;com\\test;Foo.as:90]",
          "C:\\workspace\\astella\\test\\src",
          "Foo.as",
          90,
          "com.test",
          "com.test.Foo",
          "Foo",
          null,
          null);
  }

  public void testSystem() {
    final String render = "[render]()";
    check(render,
          null,
          null,
          -1,
          null,
          "",
          "render",
          null,
          null);
    FrameInfoBuilder frameInfoBuilder = new FrameInfoBuilder();
    assertTrue(frameInfoBuilder.buildInstance(render).isSystem());
  }

  public void testNamespace() {
    check("Array/http://adobe.com/AS3/2006/builtin::push()",
          null,
          null,
          -1,
          null,
          "Array",
          "push",
          null,
          "http://adobe.com/AS3/2006/builtin");
  }
}
