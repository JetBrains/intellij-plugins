package com.jetbrains.actionscript.profiler.calltree;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import com.jetbrains.actionscript.profiler.sampler.FrameUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CallTreeCallerTest extends CallTreeTest {
  @Override
  protected String getBasePath() {
    return super.getBasePath() + File.separator + "caller";
  }

  @Override
  protected Pair<Map<FrameInfo, Long>, Map<FrameInfo, Long>> getMaps(CallTree callTree, XmlTag rootTag) {
    XmlAttribute framesAttribute = rootTag.getAttribute("frames");
    assertNotNull("No frames attribute", framesAttribute);
    return callTree.getCallersTimeMaps(FrameUtil.getInstances(framesAttribute.getValue().split(",")));
  }

  public void testSimple() throws IOException {
    doTest("simple_caller.xml", "simple_caller_results.xml");
  }

  public void testCaller() throws IOException {
    doTest("caller.xml", "caller_results.xml");
  }

  public void testCaller2() throws IOException {
    doTest("caller2.xml", "caller2_results.xml");
  }

  public void testCaller3() throws IOException {
    doTest("caller3.xml", "caller3_results.xml");
  }

  public void testCaller4() throws IOException {
    doTest("caller4.xml", "caller4_results.xml");
  }

  public void testCallerRec() throws IOException {
    doTest("caller_rec.xml", "caller_rec_results.xml");
  }
}
