package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.testFramework.UsefulTestCase;
import com.intellij.util.ResourceUtil;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.stream.Stream;

import static com.jetbrains.cidr.cpp.embedded.platformio.project.BoardInfo.EMPTY;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.DeviceTreeNode.TYPE.BOARD;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.DeviceTreeNode.TYPE.FRAMEWORK;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.DeviceTreeNode.TYPE.VENDOR;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.SourceTemplate.ARDUINO;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.SourceTemplate.GENERIC;
import static java.util.Collections.list;

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
      .filter(node -> node.hasSameValues("ST", VENDOR, EMPTY))
      .flatMap(node -> childrenStream(node))
      .filter(node -> node
        .hasSameValues("ST 32F3348DISCOVERY", BOARD, new BoardInfo(GENERIC, "--board", "disco_f334c8")))
      .findAny()
      .orElseThrow(AssertionError::new);
    assertEquals(2, board.getChildCount());

    assertTrue(board.getChildAt(0).hasSameValues("mbed", FRAMEWORK,
                                                 new BoardInfo(GENERIC, "--board", "disco_f334c8", "-O",
                                                               "framework=mbed")));
    assertTrue(board.getChildAt(1).hasSameValues("stm32cube", FRAMEWORK,
                                                 new BoardInfo(GENERIC, "--board", "disco_f334c8", "-O",
                                                               "framework=stm32cube")));
    DeviceTreeNode arduinoBoard = childrenStream(root)
      .filter(node -> node.hasSameValues("Armed", VENDOR, EMPTY))
      .flatMap(node -> childrenStream(node))
      .filter(node -> node
        .hasSameValues("3D Printer Controller", BOARD, new BoardInfo(ARDUINO, "--board", "armed_v1")))
      .findAny()
      .orElseThrow(AssertionError::new);
    assertTrue(arduinoBoard.getChildAt(0).hasSameValues("arduino", FRAMEWORK,
                                                        new BoardInfo(ARDUINO, "--board", "armed_v1", "-O",
                                                                      "framework=arduino")));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Stream<DeviceTreeNode> childrenStream(final TreeNode treeNode) {
    return list((Enumeration)treeNode.children()).stream();
  }
}
