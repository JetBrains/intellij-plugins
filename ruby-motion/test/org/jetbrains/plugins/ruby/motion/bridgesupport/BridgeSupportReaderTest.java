/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.motion.bridgesupport;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.Pair;
import com.intellij.testFramework.UsefulTestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class BridgeSupportReaderTest extends UsefulTestCase {
  public void testReadClassListShort() throws Exception {
    final Framework framework = createFramework("RubyMotion");
    assertNotNull(framework.getClass("NSObject"));
  }

  public void testReadClassListLong() throws Exception {
    final Framework framework = createFramework("QuartzCore");
    assertSize(21, framework.getClasses());
    assertNotNull(framework.getClass("CADisplayLink"));
  }

  public void testReadClassContents() throws Exception {
    final Framework framework = createFramework("RubyMotion");
    final Class object = framework.getClass("NSObject");
    assertNotNull(object);
    final Function function = object.getFunction("dispatch_object");
    assertNotNull(function);
    assertEquals("void*", function.getReturnValue());
  }

  public void testReadInformalProtocolListLong() throws Exception {
    final Framework framework = createFramework("QuartzCore");
    assertSize(4, framework.getProtocols());
    assertNotNull(framework.getProtocol("CAAnimationDelegate"));
  }

  public void testReadInformalProtocolContents() throws Exception {
    final Framework framework = createFramework("QuartzCore");
    final Class protocol = framework.getProtocol("CAAnimationDelegate");
    assertNotNull(protocol);
    final Function function = protocol.getFunction("animationDidStop:finished:");
    assertNotNull(function);
    assertSize(2, function.getArguments());
  }

  public void testReadConstants() throws Exception {
    final Framework framework = createFramework("QuartzCore");
    assertSize(101, framework.getConstants());
    final Constant constant = framework.getConstant("kCAEmitterLayerCircle");
    assertNotNull(constant);
    assertEquals("NSString*", constant.getDeclaredType());
  }

  public void testReadStructs() throws Exception {
    final Framework framework = createFramework("MapKit");
    assertSize(5, framework.getStructs());
    final Struct struct = framework.getStruct("MKCoordinateSpan");
    assertNotNull(struct);
    assertSize(2, struct.getFields());
    assertEquals("CLLocationDegrees", struct.getFieldType("latitudeDelta"));
  }

  public void testReadStringConstants() throws Exception {
    final Framework framework = createFramework("SystemConfiguration");
    final Constant constant = framework.getConstant("kSCNetworkConnectionPacketsIn");
    assertInstanceOf(constant, StringConstant.class);
    final StringConstant stringConstant = (StringConstant)constant;
    assertEquals("NSString*", stringConstant.getDeclaredType());
    assertEquals("PacketsIn", stringConstant.getValue());
    assertTrue(stringConstant.isNsString());
  }

  public void testReadEnums() throws Exception {
    final Framework framework = createFramework("OpenGLES");
    final Constant constant = framework.getConstant("GL_ARRAY_BUFFER");
    assertInstanceOf(constant, Enum.class);
    final Enum stringConstant = (Enum)constant;
    assertEquals("int", stringConstant.getDeclaredType());
    assertEquals("34962", stringConstant.getValue());
  }

  public void testReadStringConstantsDefault() throws Exception {
    final Framework framework = createFramework("AudioToolbox");
    final Constant constant = framework.getConstant("kAFInfoDictionary_Lyricist");
    assertInstanceOf(constant, StringConstant.class);
    final StringConstant stringConstant = (StringConstant)constant;
    assertEquals("NSString*", stringConstant.getDeclaredType());
    assertEquals("lyricist", stringConstant.getValue());
    assertFalse(stringConstant.isNsString());
  }

  public void testReadFunction() throws Exception {
    final Framework framework = createFramework("AudioToolbox");
    assertSize(203, framework.getFunctions());
    final Function function = framework.getFunction("AUGraphConnectNodeInput");
    assertNotNull(function);
    assertEquals("OSStatus", function.getReturnValue());
    final List<Pair<String, String>> arguments = function.getArguments();
    assertEquals(5, arguments.size());
    assertEquals(Pair.create("inDestNode", "AUNode"), arguments.get(3));
    assertFalse(function.isVariadic());
  }

  public void testReadFunctionVariadic() throws Exception {
    final Framework framework = createFramework("CoreFoundation");
    final Function function = framework.getFunction("CFStringAppendFormat");
    assertNotNull(function);
    assertTrue(function.isVariadic());
  }

  public void testReadClassMethod() throws Exception {
    final Framework framework = createFramework("AVFoundation");
    final Class clazz = framework.getClass("AVAssetReaderAudioMixOutput");
    assertNotNull(clazz);
    final Function function = clazz.getFunction("assetReaderAudioMixOutputWithAudioTracks:audioSettings:");
    assertNotNull(function);
    assertTrue(function.isClassMethod());
  }

  public void testReadFunctionAlias() throws Exception {
    final Framework framework = createFramework("CoreGraphics");
    assertSize(5, framework.getFunctionAliases().keySet());
    assertEquals("__CGSizeApplyAffineTransform", framework.getOriginalFunctionName("CGSizeApplyAffineTransform"));
  }

  public void testReadAndroidClass() throws Exception {
    final Framework framework = createFramework("android");
    assertNotNull(framework.getClass("Android"));
    assertNotNull(framework.getClass("Android::App"));
    assertNotNull(framework.getClass("Android::App::Activity"));
  }

  public void testReadAndroidInterface() throws Exception {
    final Framework framework = createFramework("android");
    assertNotNull(framework.getClass("Android::Database::Cursor"));
  }

  public void testReadAndroidClassContents() throws Exception {
    final Framework framework = createFramework("android");
    final Class activity = framework.getClass("Android::App::Activity");
    assertNotNull(activity);

    Function function = activity.getFunction("managedQuery");
    assertNotNull(function);
    assertEquals("Android::Database::Cursor", function.getReturnValue());
    final List<Pair<String, String>> arguments = function.getArguments();
    assertEquals(5, arguments.size());
    assertEquals("Android::Net::Uri", arguments.get(0).second);
    assertEquals("Array<Java::Lang::String>", arguments.get(1).second);
    assertEquals("Java::Lang::String", arguments.get(2).second);
    assertEquals("Array<Java::Lang::String>", arguments.get(3).second);
    assertEquals("Java::Lang::String", arguments.get(4).second);

    function = activity.getFunction("isTaskRoot");
    assertNotNull(function);
    assertEquals("bool", function.getReturnValue());
  }

  private static Framework createFramework(final String motion) throws Exception {
    return BridgeSupportReader.read(motion, "666", getTestText(motion), false);
  }

  private static InputStream getTestText(final String filename) throws Exception {
    final String path = PathManager.getHomePath() + "/contrib/ruby-motion/test/org/jetbrains/plugins/ruby/motion/bridgesupport/data/" + filename + ".bridgesupport";
    final File file = new File(path);
    assertTrue(path, file.exists());
    return new FileInputStream(file);
  }
}
