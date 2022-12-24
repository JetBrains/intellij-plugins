package com.jetbrains.cidr.cpp.embedded.platformio.project

import org.jetbrains.io.JsonReaderEx
import java.util.*

object BoardsJsonParser {
  /**
   * Parses boards list into vendor->board->framework(optional) structure.
   *
   * @return virtual root of parsed tree
   */
  fun parse(text: CharSequence): DeviceTreeNode {
    val boardsByVendor: SortedMap<String, DeviceTreeNode> = TreeMap(java.lang.String.CASE_INSENSITIVE_ORDER)
    val rootNode = DeviceTreeNode(null, DeviceTreeNode.TYPE.ROOT, "", SourceTemplate.EMPTY_BOARD_INFO)
    JsonReaderEx(text).use { jsonReader ->
      jsonReader.beginArray()
      while (jsonReader.hasNext()) {
        jsonReader.beginObject()
        var vendorName = "Generic"
        var boardId: String? = ""
        var boardName = ""
        var mcuName: String? = null
        var ram: String? = null
        var rom: String? = null
        var fcpu: String? = null

        val frameworks: SortedSet<String> = TreeSet(java.lang.String.CASE_INSENSITIVE_ORDER)
        while (jsonReader.hasNext()) {
          when (jsonReader.nextName()) {
            "id" -> boardId = jsonReader.nextAsString()
            "name" -> boardName = jsonReader.nextAsString()
            "vendor" -> vendorName = Objects.toString(jsonReader.nextAsString(), vendorName)
            "mcu" -> mcuName = jsonReader.nextAsString()
            "ram" -> ram = readNumber(jsonReader) { if (it < 2096 * 1024) "RAM: ${it / 1024}K" else "RAM: ${it / 1024 / 1024}M" }
            "rom" -> rom = readNumber(jsonReader) { if (it < 2096 * 1024) "ROM: ${it / 1024}K" else "ROM: ${it / 1024 / 1024}M" }
            "fcpu" -> fcpu = readNumber(jsonReader) { "${it / 1000000}MHz" }
            "frameworks" -> {
              jsonReader.beginArray()
              while (jsonReader.hasNext()) {
                val frameworkName = jsonReader.nextAsString()
                frameworks.add(frameworkName)
              }
              jsonReader.endArray()
            }
            else -> jsonReader.skipValue()
          }
        }
        jsonReader.endObject()
        if (boardId == null) {
          continue
        }
        val vendorNode = boardsByVendor.computeIfAbsent(vendorName) { name: String? ->
          DeviceTreeNode(rootNode, DeviceTreeNode.TYPE.VENDOR, name!!, SourceTemplate.EMPTY_BOARD_INFO)
        }
        val boardInfo = BoardInfo(SourceTemplate.byFrameworkName(frameworks), listOf("--board", boardId))
        val description = listOfNotNull(mcuName, fcpu, rom, ram).joinToString(", ")
        if (!description.isEmpty()) {
          boardName = "$boardName ($description)"
        }
        val board = DeviceTreeNode(vendorNode, DeviceTreeNode.TYPE.BOARD, boardName, boardInfo)
        vendorNode.add(board)
        if (frameworks.size > 1) {
          for (frameworkName in frameworks) {
            val frameworkBoardInfo = BoardInfo(
              SourceTemplate.byFrameworkName(frameworkName), listOf(
              "--board", boardId, "-O", "framework=$frameworkName"))
            val framework = DeviceTreeNode(board, DeviceTreeNode.TYPE.FRAMEWORK, frameworkName, frameworkBoardInfo)
            board.add(framework)
          }
        }
      }
      jsonReader.endArray()
    }
    rootNode.children.addAll(boardsByVendor.values)
    return rootNode
  }

  private fun readNumber(jsonReader: JsonReaderEx, convert: (Long) -> String): String? {
    try {
      val string = jsonReader.nextString(true)
      return convert.invoke(string.toLong())
    }
    catch (_: NumberFormatException) {
      return null
    }
  }
}
