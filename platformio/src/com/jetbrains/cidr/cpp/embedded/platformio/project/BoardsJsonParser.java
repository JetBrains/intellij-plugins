package com.jetbrains.cidr.cpp.embedded.platformio.project;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.io.JsonReaderEx;

import java.util.*;

import static com.jetbrains.cidr.cpp.embedded.platformio.project.DeviceTreeNode.TYPE.*;

public class BoardsJsonParser {
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
    DeviceTreeNode rootNode = new DeviceTreeNode(null, ROOT, "");
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
                String frameworkName = jsonReader.nextAsString();
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
        DeviceTreeNode vendorNode =
          boardsByVendor.computeIfAbsent(vendorName, name -> rootNode.add(new DeviceTreeNode(rootNode, VENDOR, name)));
        DeviceTreeNode board = new DeviceTreeNode(vendorNode, BOARD, Objects.requireNonNull(boardName), "--board", boardId);
        vendorNode.add(board);
        if (frameworks.size() > 1) {
          for (String frameworkName : frameworks) {
            DeviceTreeNode framework =
              new DeviceTreeNode(board, FRAMEWORK, frameworkName, "--board", boardId, "-O", "framework=" + frameworkName);
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
