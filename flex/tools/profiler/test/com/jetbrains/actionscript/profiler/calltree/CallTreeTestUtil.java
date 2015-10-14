package com.jetbrains.actionscript.profiler.calltree;

import com.intellij.psi.xml.XmlTag;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import com.jetbrains.actionscript.profiler.sampler.FrameUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

public class CallTreeTestUtil {
  private CallTreeTestUtil() {
  }

  public static CallTree getCallTreeFromXmlFile(@NotNull XmlTag rootTag) {
    CallTreeNode root = getCallTreeNodeFromXmlTag(rootTag);
    return new CallTree(root);
  }

  private static CallTreeNode getCallTreeNodeFromXmlTag(XmlTag rootTag) {
    int count = Integer.MAX_VALUE;
    String countStr = rootTag.getAttributeValue("count");
    if (countStr != null) {
      count = Integer.parseInt(countStr);
    }
    FrameInfo frameInfo = FrameUtil.getFrameInfo(rootTag.getName());
    CallTreeNode node = new CallTreeNode(frameInfo, count);
    if (rootTag.isEmpty()) {
      return node;
    }
    for (XmlTag childTag : rootTag.getSubTags()) {
      CallTreeNode childNode = getCallTreeNodeFromXmlTag(childTag);
      node.addChildRecursive(childNode);
      Assert.assertTrue("Bad edge " + node.getFrameInfo() + "->" + childNode.getFrameInfo(), childNode.calcSelfTiming() <= count);
    }
    return node;
  }
}
