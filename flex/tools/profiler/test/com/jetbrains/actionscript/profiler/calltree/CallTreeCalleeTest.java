package com.jetbrains.actionscript.profiler.calltree;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import com.jetbrains.actionscript.profiler.sampler.FrameUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CallTreeCalleeTest extends CallTreeTest {
  @Override
  protected String getBasePath() {
    return super.getBasePath() + File.separator + "callee";
  }

  @Override
  protected Pair<Map<FrameInfo, Long>, Map<FrameInfo, Long>> getMaps(CallTree callTree, XmlTag rootTag) {
    XmlAttribute framesAttribute = rootTag.getAttribute("frames");
    assertNotNull("No frames attribute", framesAttribute);
    return callTree.getCalleesTimeMaps(FrameUtil.getInstances(ArrayUtil.reverseArray(framesAttribute.getValue().split(","))));
  }

  public void testCallee() throws IOException {
    doTest("callee.xml", "callee_results.xml");
  }

  public void testCallee2() throws IOException {
    doTest("callee2.xml", "callee2_results.xml");
  }

  public void testCallee3() throws IOException {
    doTest("callee3.xml", "callee3_results.xml");
  }
}
