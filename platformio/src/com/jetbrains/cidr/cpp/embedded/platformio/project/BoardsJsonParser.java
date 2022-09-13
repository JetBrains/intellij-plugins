package com.jetbrains.cidr.cpp.embedded.platformio.project;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.io.JsonReaderEx;

import java.util.*;

import static com.jetbrains.cidr.cpp.embedded.platformio.project.DeviceTreeNode.TYPE.*;

public final class BoardsJsonParser {

  public static final String ARDUINO_ID = "arduino";

  private BoardsJsonParser() {
  }

  /**
   * Parses boards list into vendor->board->framework(optional) structure.
   *
   * @return virtual root of parsed tree
   */
  @NotNull
  public static DeviceTreeNode parse(@NotNull CharSequence text) {
    SortedMap<String, DeviceTreeNode> boardsByVendor = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    DeviceTreeNode rootNode = new DeviceTreeNode(null, ROOT, "", BoardInfo.EMPTY);
    try (JsonReaderEx jsonReader = new JsonReaderEx(text)) {
      for (jsonReader.beginArray(); jsonReader.hasNext(); ) {
        jsonReader.beginObject();
        String vendorName = "Generic";
        String boardId = "";
        String boardName = "";
        SortedSet<String> frameworks = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        while (jsonReader.hasNext()) {
          String name = jsonReader.nextName();
          switch (name) {
            case "id" -> boardId = jsonReader.nextAsString();
            case "name" -> boardName = jsonReader.nextAsString();
            case "vendor" -> vendorName = Objects.toString(jsonReader.nextAsString(), vendorName);
            case "frameworks" -> {
              jsonReader.beginArray();
              while (jsonReader.hasNext()) {
                String frameworkName = jsonReader.nextAsString();
                frameworks.add(frameworkName);
              }
              jsonReader.endArray();
            }
            default -> jsonReader.skipValue();
          }
        }
        jsonReader.endObject();

        if (boardId == null) {
          continue;
        }
        DeviceTreeNode vendorNode =
          boardsByVendor.computeIfAbsent(vendorName, name -> rootNode.add(new DeviceTreeNode(rootNode, VENDOR, name, BoardInfo.EMPTY)));
        BoardInfo boardInfo = new BoardInfo(SourceTemplate.getByFrameworkName(frameworks), "--board", boardId);

        DeviceTreeNode board = new DeviceTreeNode(vendorNode, BOARD, Objects.requireNonNull(boardName), boardInfo);
        vendorNode.add(board);
        if (frameworks.size() > 1) {
          for (String frameworkName : frameworks) {
            BoardInfo frameworkBoardInfo = new BoardInfo(
              SourceTemplate.getByFrameworkName(frameworkName),
              "--board", boardId, "-O", "framework=" + frameworkName);
            DeviceTreeNode framework =
              new DeviceTreeNode(board, FRAMEWORK, frameworkName, frameworkBoardInfo);
            board.add(framework);
          }
        }
      }
      jsonReader.endArray();
    }
    rootNode.setChildren(new ArrayList<>(boardsByVendor.values()));
    return rootNode;
  }
}
