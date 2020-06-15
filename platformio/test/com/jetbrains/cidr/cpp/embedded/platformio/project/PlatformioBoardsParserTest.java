package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.testFramework.UsefulTestCase;
import com.intellij.util.ResourceUtil;

import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Stream;

import static com.jetbrains.cidr.cpp.embedded.platformio.project.DeviceTreeNode.TYPE.*;

public class PlatformioBoardsParserTest extends UsefulTestCase {

  private String myJson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myJson = ResourceUtil.loadText(PlatformioBoardsParserTest.class.getResourceAsStream(this.getClass().getSimpleName() + ".json"));
  }

  public void testParse() {
    DeviceTreeNode root = BoardsJsonParser.parse(myJson);
    assertEquals(190, root.getChildCount());

    DeviceTreeNode board = childrenStream(root)
      .filter(node -> node.hasSameValues("ST", VENDOR))
      .flatMap(node -> childrenStream(node))
      .filter(node -> node.hasSameValues("ST 32F3348DISCOVERY", BOARD, "--board", "disco_f334c8"))
      .findAny()
      .orElseThrow(AssertionError::new);
    assertEquals(2, board.getChildCount());

    assertTrue(board.getChildAt(0).hasSameValues("mbed", FRAMEWORK,
                                                 "--board", "disco_f334c8", "-O", "framework=mbed"));
    assertTrue(board.getChildAt(1).hasSameValues("stm32cube", FRAMEWORK,
                                                 "--board", "disco_f334c8", "-O", "framework=stm32cube"));
  }

  @SuppressWarnings({"unchecked", "RedundantCast"})
  private static Stream<DeviceTreeNode> childrenStream(TreeNode treeNode) {
    return Collections.list((Enumeration)treeNode.children()).stream();
  }
}