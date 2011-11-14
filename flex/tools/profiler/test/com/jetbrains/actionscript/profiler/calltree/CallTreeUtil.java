package com.jetbrains.actionscript.profiler.calltree;

import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

public class CallTreeUtil {
  private CallTreeUtil() {
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
    CallTreeNode node = new CallTreeNode(rootTag.getName(), count);
    if (rootTag.isEmpty()) {
      return node;
    }
    for (XmlTag childTag : rootTag.getSubTags()) {
      CallTreeNode childNode = getCallTreeNodeFromXmlTag(childTag);
      node.addChildRecursive(childNode);
      Assert.assertTrue("Bad edge " + node.getFrameName() + "->" + childNode.getFrameName(), childNode.calcSelfTiming() <= count);
    }
    return node;
  }
}
