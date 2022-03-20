package com.jetbrains.cidr.cpp.embedded.platformio.project;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.io.JsonReaderEx;

import java.util.*;

import static com.jetbrains.cidr.cpp.embedded.platformio.project.BoardInfo.EMPTY;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.DeviceTreeNode.TYPE.*;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.SourceTemplate.getByFrameworkName;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Objects.requireNonNull;

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
    final var boardsByVendor = new TreeMap<String, DeviceTreeNode>(CASE_INSENSITIVE_ORDER);
    final var rootNode = new DeviceTreeNode(null, ROOT, "", EMPTY);
    try (final JsonReaderEx jsonReader = new JsonReaderEx(text)) {
      for (jsonReader.beginArray(); jsonReader.hasNext(); ) {
        jsonReader.beginObject();
        String vendorName = "Generic";
        String boardId = "";
        String boardName = "";
        final var frameworks = new TreeSet<String>(CASE_INSENSITIVE_ORDER);
        while (jsonReader.hasNext()) {
          String name = jsonReader.nextName();
          switch (name) {
            case "id":
              boardId = jsonReader.nextAsString();
              break;
            case "name":
              boardName = jsonReader.nextAsString();
              break;
            case "vendor":
              vendorName = Objects.toString(jsonReader.nextAsString(), vendorName);
              break;
            case "frameworks":
              jsonReader.beginArray();
              while (jsonReader.hasNext()) {
                final var frameworkName = jsonReader.nextAsString();
                frameworks.add(frameworkName);
              }
              jsonReader.endArray();
              break;
            default:
              jsonReader.skipValue();
          }
        }
        jsonReader.endObject();

        if (boardId == null) {
          continue;
        }
        final var vendorNode =
          boardsByVendor.computeIfAbsent(vendorName, name -> rootNode.add(new DeviceTreeNode(rootNode, VENDOR, name, EMPTY)));
        final var boardInfo = new BoardInfo(getByFrameworkName(frameworks), "--board", boardId);
        final var board = new DeviceTreeNode(vendorNode, BOARD, requireNonNull(boardName), boardInfo);
        vendorNode.add(board);
        if (frameworks.size() > 1) {
          for (final String frameworkName : frameworks) {
            final var frameworkBoardInfo = new BoardInfo(getByFrameworkName(frameworkName),
              "--board", boardId, "-O", "framework=" + frameworkName);
            final var framework = new DeviceTreeNode(board, FRAMEWORK, frameworkName, frameworkBoardInfo);
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
