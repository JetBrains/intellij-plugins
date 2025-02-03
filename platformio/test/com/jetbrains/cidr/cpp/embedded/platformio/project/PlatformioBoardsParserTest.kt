package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.ResourceUtil
import com.jetbrains.cidr.cpp.embedded.platformio.project.DeviceTreeNode.TYPE.*

class PlatformioBoardsParserTest : UsefulTestCase() {
  private var myJson: String? = null

  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    myJson = ResourceUtil.loadText(
      this.javaClass.getResourceAsStream(this.javaClass.simpleName + ".json")!!)
  }

  private fun boardInfo(sourceTemplate: SourceTemplate, vararg params: String) = BoardInfo(sourceTemplate, listOf(*params))
  fun testParse() {
    val root = BoardsJsonParser.parse(myJson!!)
    assertEquals(190, root.childCount)
    val board = root.children().asSequence()
      .filter { node: DeviceTreeNode -> node.hasSameValues("ST", VENDOR, SourceTemplate.EMPTY_BOARD_INFO) }
      .flatMap { node: DeviceTreeNode -> node.children().asSequence() }
      .single { node: DeviceTreeNode -> node.name.startsWith("ST 32F3348DISCOVERY") }
    assertTrue(
      board.hasSameValues("ST 32F3348DISCOVERY (STM32F334C8T6, 72MHz, ROM: 64K, RAM: 12K)", BOARD,
                          boardInfo(SourceTemplate.GENERIC, "--board", "disco_f334c8")))
    assertEquals(2, board.childCount)
    assertTrue(board.getChildAt(0).hasSameValues("mbed", FRAMEWORK,
                                                 boardInfo(SourceTemplate.GENERIC, "--board", "disco_f334c8", "-O",
                                                           "framework=mbed")))
    assertTrue(board.getChildAt(1).hasSameValues("stm32cube", FRAMEWORK,
                                                 boardInfo(SourceTemplate.GENERIC, "--board", "disco_f334c8", "-O",
                                                           "framework=stm32cube")))
    val arduinoBoard = root.children().asSequence()
      .filter { node: DeviceTreeNode -> node.hasSameValues("Armed", VENDOR, SourceTemplate.EMPTY_BOARD_INFO) }
      .flatMap { node: DeviceTreeNode -> node.children().asSequence() }
      .single { node: DeviceTreeNode -> node.name.startsWith("3D Printer Controller") }
    arduinoBoard.hasSameValues("3D Printer Controller (STM32F407VET6, 168MHz, ROM: 512K, RAM: 192K)", BOARD,
                               boardInfo(SourceTemplate.ARDUINO, "--board", "armed_v1"))
    assertTrue(arduinoBoard.getChildAt(0).hasSameValues("arduino", FRAMEWORK,
                                                        boardInfo(SourceTemplate.ARDUINO, "--board", "armed_v1", "-O",
                                                                  "framework=arduino")))
  }

}