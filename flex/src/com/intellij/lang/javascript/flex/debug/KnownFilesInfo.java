package com.intellij.lang.javascript.flex.debug;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.containers.BidirectionalMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class KnownFilesInfo {

  private final FlexDebugProcess myFlexDebugProcess;

  private boolean myUpToDate = false;

  private final Int2ObjectMap<BidirectionalMap<String, String>> myWorkerToFilePathToIdMap = new Int2ObjectOpenHashMap<>();

  private final Int2ObjectMap<Map<String, Collection<String>>> myWorkerToFileNameToPathsMap = new Int2ObjectOpenHashMap<>();

  public KnownFilesInfo(final FlexDebugProcess flexDebugProcess) {
    myFlexDebugProcess = flexDebugProcess;
  }

  public void setUpToDate(final boolean upToDate) {
    myUpToDate = upToDate;
  }

  @Nullable
  public String getFilePathById(final int worker, final String id) {
    ensureUpToDate();

    final BidirectionalMap<String, String> filePathToId = myWorkerToFilePathToIdMap.get(worker);
    final List<String> paths = filePathToId == null ? null : filePathToId.getKeysByValue(id);

    return paths != null && paths.size() > 0 ? paths.get(0) : null;
  }

  @Nullable
  public String getIdByFilePath(final String filePath) {
    ensureUpToDate();

    final int worker = 0; // todo calculate correct worker

    final BidirectionalMap<String, String> filePathToId = myWorkerToFilePathToIdMap.get(worker);
    return filePathToId == null ? null : filePathToId.get(filePath);
  }

  @Nullable
  public String getIdByFilePathNoUpdate(final String filePath) {
    final int worker = 0; // todo calculate correct worker

    final BidirectionalMap<String, String> filePathToId = myWorkerToFilePathToIdMap.get(worker);
    return filePathToId == null ? null : filePathToId.get(filePath);
  }

  @Nullable
  public Collection<String> getPathsByName(final int worker, final String fileName) {
    ensureUpToDate();

    final Map<String, Collection<String>> fileNameToPaths = myWorkerToFileNameToPathsMap.get(worker);
    return fileNameToPaths == null ? null : fileNameToPaths.get(fileName);
  }

  private void ensureUpToDate() {
    if (myUpToDate) return;
    myFlexDebugProcess.sendAndProcessOneCommand(
      new DebuggerCommand("show files", CommandOutputProcessingType.SPECIAL_PROCESSING, VMState.SUSPENDED, VMState.SUSPENDED) {
        @Override
        CommandOutputProcessingMode onTextAvailable(@NonNls String s) {
          processShowFilesResult(new StringTokenizer(s, "\r\n"));
          return CommandOutputProcessingMode.DONE;
        }
      }, null);
    myUpToDate = true;
  }

  private void processShowFilesResult(StringTokenizer tokenizer) {
    //2 C:\work\flex_projects\MP3Worker\src\Workers.as, Workers.as
    //2 C:\work\flex_projects\MP3Worker\src\Workers.as, Workers.as (Main Thread)
    //1 C:\work\flex_projects\MP3Worker\src\BackWorker.as, BackWorker.as (Worker 1)

    while (tokenizer.hasMoreTokens()) {
      final String line = tokenizer.nextToken().trim();
      final int spaceIndex = line.indexOf(' ');
      final int commaIndex = line.indexOf(", ");

      if (spaceIndex == -1 || commaIndex == -1) {
        FlexDebugProcess.log("Unexpected string format:" + line);
        continue;
      }

      final String id = line.substring(0, spaceIndex);
      String fullPath = FileUtil.toSystemIndependentName(line.substring(spaceIndex + 1, commaIndex));

      int markerIndex = fullPath.indexOf("/frameworks/projects/");
      if (markerIndex != -1 && fullPath.indexOf("/src/", markerIndex) > 0) {
        fullPath = myFlexDebugProcess.getAppSdkHome() + fullPath.substring(markerIndex);
      }

      final int nextSpaceIndex = line.indexOf(' ', commaIndex + 2);
      final String shortName = nextSpaceIndex > 0 ? line.substring(commaIndex + 2, nextSpaceIndex) : line.substring(commaIndex);

      int worker = 0;

      if (nextSpaceIndex > 0) {
        if ("(Main Thread)".equals(line.substring(nextSpaceIndex + 1))) {
          worker = 0;
        }
        else if (line.substring(nextSpaceIndex + 1).startsWith("(Worker ") && line.endsWith(")")) {
          try {
            worker = Integer.parseInt(line.substring(nextSpaceIndex + 1 + "(Worker ".length(), line.length() - 1));
          }
          catch (NumberFormatException e) {
            FlexDebugProcess.log("Unexpected string format:" + line);
          }
        }
        else {
          FlexDebugProcess.log("Unexpected string format:" + line);
        }
      }

      BidirectionalMap<String, String> filePathToIdMap = myWorkerToFilePathToIdMap.get(worker);
      if (filePathToIdMap == null) {
        filePathToIdMap = new BidirectionalMap<>();
        myWorkerToFilePathToIdMap.put(worker, filePathToIdMap);
      }
      filePathToIdMap.put(fullPath, id);

      Map<String, Collection<String>> fileNameToPaths = myWorkerToFileNameToPathsMap.get(worker);
      if (fileNameToPaths == null) {
        fileNameToPaths = new HashMap<>();
        myWorkerToFileNameToPathsMap.put(worker, fileNameToPaths);
      }
      addToMap(fileNameToPaths, shortName, fullPath);
    }
  }

  private static <K, T> void addToMap(final Map<K, Collection<T>> map, final K key, final T valueCollectionElement) {
    Collection<T> valueCollection = map.get(key);
    if (valueCollection == null) {
      valueCollection = new ArrayList<>(1);
      map.put(key, valueCollection);
    }

    if (!valueCollection.contains(valueCollectionElement)) {
      valueCollection.add(valueCollectionElement);
    }
  }
}
