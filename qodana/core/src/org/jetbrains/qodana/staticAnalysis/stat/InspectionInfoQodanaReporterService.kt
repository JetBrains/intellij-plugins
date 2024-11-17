package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.codeInspection.ex.InspectListener
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.inspectionProfile.makeCategoryId
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

private const val FILE_LOG_INSPECTIONS_SUMMARY = "qodana_inspections_summary.csv"
private const val FILE_LOG_DIRECTORIES_INSPECTION_GROUPS = "qodana_directories_inspection_groups.csv"

private const val CSV_SEPARATOR = ";"

private const val ALL_INSPECTIONS_GROUP_NAME = "all"

/**
 * Service collects information about finished inspections
 *
 * - Information about each inspection is collected in inspectionsSummaryInfo
 * - Information about all inspectionGroups is collected for each inspected project directory
 *   this info is stored in directoriesInspectionGroupsInfos
 *
 * inspectionGroups is list of InspectionGroup. Using this mechanics you can combine inspections into custom "groups"
 */
@Service(Service.Level.PROJECT)
class InspectionInfoQodanaReporterService(val project: Project) {
  companion object {
    private val _inspectionGroups = mutableListOf(InspectionGroup(ALL_INSPECTIONS_GROUP_NAME) { true })
    val inspectionGroups = _inspectionGroups as List<InspectionGroup>

    fun getInstance(project: Project): InspectionInfoQodanaReporterService = project.service()

    /** Add new inspection group, name of new group != ALL_INSPECTIONS_GROUP_NAME  */
    fun addInspectionGroup(inspectionGroup: InspectionGroup) {
      assert(inspectionGroup.groupName != ALL_INSPECTIONS_GROUP_NAME)

      _inspectionGroups.removeAll { it.groupName == inspectionGroup.groupName }
      _inspectionGroups.add(inspectionGroup)
      updateMapInspectionGroupNameToIdx()
    }

    /** Remove inspection group by its name, name of group to be removed != ALL_INSPECTIONS_GROUP_NAME */
    fun removeInspectionGroupByName(groupName: String) {
      assert(groupName != ALL_INSPECTIONS_GROUP_NAME)

      _inspectionGroups.removeAll { it.groupName == groupName }
      updateMapInspectionGroupNameToIdx()
    }

    private var mapInspectionGroupNameToIdx = (inspectionGroups.map { it.groupName } zip inspectionGroups.indices).toMap()

    private fun updateMapInspectionGroupNameToIdx() {
      mapInspectionGroupNameToIdx = (inspectionGroups.map { it.groupName } zip inspectionGroups.indices).toMap()
    }
  }

  /**
   * Group of inspections
   * @property containsInspection function to determine if given inspection belongs to this group
   */
  class InspectionGroup(val groupName: String, val containsInspection: (FinishedInspectionDescriptor) -> Boolean) {
    operator fun contains(finishedInspectionDescriptor: FinishedInspectionDescriptor) = containsInspection(finishedInspectionDescriptor)
  }

  class FinishedInspectionDescriptor(
    val duration: Long,
    val problemsCount: Int,
    val kind: InspectListener.InspectionKind,
    val tool: InspectionToolWrapper<*, *>
  ) {
    val inspectionKey: InspectionKey = tool.id
  }

  private class InspectionInfo(
    var duration: Long,
    var problemsCount: Int,
    var performedTimesCount: Int,
    val id: String,
    val displayName: String,
    val groupPath: String,
    val language: String,
    val kind: InspectListener.InspectionKind,
  ) {
    constructor(descriptor: FinishedInspectionDescriptor) :
      this(
        descriptor.duration,
        descriptor.problemsCount,
        1,
        descriptor.tool.id,
        descriptor.tool.displayName,
        descriptor.tool.groupPath.makeCategoryId(),
        descriptor.tool.language ?: "none",
        descriptor.kind,
      )

    fun addFinishedInspection(descriptor: FinishedInspectionDescriptor) {
      assert(id == descriptor.tool.id)

      duration += descriptor.duration
      problemsCount += descriptor.problemsCount
      performedTimesCount += 1
    }
  }

  private val inspectionsSummaryInfo = ConcurrentHashMap<InspectionKey, InspectionInfo>()

  /** Information about each group in inspectionGroups (finished in some directory) */
  @JvmInline
  private value class InspectionGroupsInfosCollection(
    private val arrInspectionGroupsInfos: Array<InspectionGroupInfo> =
      Array(inspectionGroups.size) { i -> InspectionGroupInfo(inspectionGroups[i]) }
  ) {

    class InspectionGroupInfo(
      val inspectionGroup: InspectionGroup,
      var inspectionsDuration: Long = 0L,
      var problemsCount: Int = 0,
      var performedTimesCount: Int = 0
    )

    fun addFinishedInspection(finishedInspection: FinishedInspectionDescriptor) =
      arrInspectionGroupsInfos
        .filter { finishedInspection in it.inspectionGroup }
        .forEach {
          it.inspectionsDuration += finishedInspection.duration
          it.problemsCount += finishedInspection.problemsCount
          it.performedTimesCount += 1
        }

    fun add(other: InspectionGroupsInfosCollection) =
      (this.arrInspectionGroupsInfos zip other.arrInspectionGroupsInfos).forEach { (thisInfo, otherInfo) ->
        thisInfo.inspectionsDuration += otherInfo.inspectionsDuration
        thisInfo.problemsCount += otherInfo.problemsCount
        thisInfo.performedTimesCount += otherInfo.performedTimesCount
      }

    /** Get info about inspectionGroup by its group name */
    operator fun get(groupName: String): InspectionGroupInfo {
      assert(groupName in mapInspectionGroupNameToIdx)
      return arrInspectionGroupsInfos[mapInspectionGroupNameToIdx[groupName]!!]
    }
  }

  /**
   * associate directories with infos about inspection groups in this directory
   * here we store info only about project directory and directories in which files were inspected, without their parent directories
   * (i.e. we do not store all directories up to project root)
   */
  private val directoriesInspectionGroupsInfos = ConcurrentHashMap<VirtualFile, InspectionGroupsInfosCollection>()

  private val timeFirstInspectionStarted: AtomicLong = AtomicLong(-1L)
  private var timeLastInspectionFinished = 0L
  private val sumOfAllInspectionDurations: AtomicLong = AtomicLong(0L)

  private val projectRoot = project.guessProjectDir() // TODO: change this to path passed to Qodana

  fun addInspectionFinishedEvent(duration: Long, problemsCount: Int, tool: InspectionToolWrapper<*, *>,
                                 kind: InspectListener.InspectionKind, file: VirtualFile?) {
    val curTime = System.currentTimeMillis()
    timeFirstInspectionStarted.compareAndSet(-1L, curTime - duration)
    timeLastInspectionFinished = curTime
    sumOfAllInspectionDurations.addAndGet(duration)

    val finishedInspection = FinishedInspectionDescriptor(duration, problemsCount, kind, tool)
    inspectionsSummaryInfo.compute(finishedInspection.inspectionKey) { _, v ->
      if (v == null) {
        InspectionInfo(finishedInspection)
      }
      else {
        v.addFinishedInspection(finishedInspection)
        v
      }
    }

    val inspectedDirectory = file?.parent ?: projectRoot
    inspectedDirectory?.let {
      directoriesInspectionGroupsInfos.compute(it) { _, v ->
        val inspectionGroupsInfos = v ?: InspectionGroupsInfosCollection()
        inspectionGroupsInfos.addFinishedInspection(finishedInspection)
        inspectionGroupsInfos
      }
    }
  }

  suspend fun logInspectionsSummaryInfo(): Unit = runInterruptible(StaticAnalysisDispatchers.IO) {
    val headerCSV = arrayOf(
      "Inspection ID", "Inspection Group", "Inspection Name", "Inspection Language", "Inspection Type",
      "Absolute Time spent on Inspection, s", "Percent of Time spent on Inspection",
      "Problems Count", "Times Inspection was Performed").toCSVLine()

    val logFile = Paths.get(PathManager.getLogPath(), FILE_LOG_INSPECTIONS_SUMMARY).toFile()
    if (logFile.isFile) logFile.delete()

    logFile.bufferedWriter().use { bw ->
      bw.write(headerCSV)
      inspectionsSummaryInfo.values
        .sortedByDescending { it.duration }
        .forEach { inspectionInfo ->
          bw.newLine()
          bw.write(arrayOf(
            inspectionInfo.id,
            inspectionInfo.groupPath,
            inspectionInfo.displayName,
            inspectionInfo.language,
            inspectionInfo.kind,
            "%.2f".format(inspectionAbsoluteTime(inspectionInfo.duration) / 1000.0),
            "%.2f%%".format(inspectionTimeProportionFromTotal(inspectionInfo.duration) * 100),
            inspectionInfo.problemsCount,
            inspectionInfo.performedTimesCount
          ).toCSVLine())
        }
    }
  }

  suspend fun logDirectoriesInspectionsInfo(): Unit = runInterruptible(StaticAnalysisDispatchers.IO) {
    if (projectRoot == null) return@runInterruptible

    val valuesToPrintBuffer = mutableListOf<Any>()

    // parts of csv header for each inspected group
    inspectionGroups.forEach {
      valuesToPrintBuffer.add("Absolute Time spent on Inspection Group '${it.groupName}', s")
      valuesToPrintBuffer.add("Percent of Time spent on Inspection Group '${it.groupName}'")
      valuesToPrintBuffer.add("Inspections Problem Count in Group '${it.groupName}'")
      valuesToPrintBuffer.add("Times Inspection from Group '${it.groupName}' was Performed")
    }
    val headerCSV = arrayOf("Directory", valuesToPrintBuffer.toCSVLine()).toCSVLine()

    fun inspectionGroupsInfosToCSVLine(inspectionGroupsInfos: InspectionGroupsInfosCollection): String {
      valuesToPrintBuffer.clear()
      inspectionGroups
        .map { inspectionGroupsInfos[it.groupName] }
        .forEach { inspectionGroupInfo ->
          valuesToPrintBuffer.add("%.2f".format(inspectionAbsoluteTime(inspectionGroupInfo.inspectionsDuration) / 1000.0))
          valuesToPrintBuffer.add("%.2f%%".format(inspectionTimeProportionFromTotal(inspectionGroupInfo.inspectionsDuration) * 100))
          valuesToPrintBuffer.add(inspectionGroupInfo.problemsCount)
          valuesToPrintBuffer.add(inspectionGroupInfo.performedTimesCount)
        }
      return valuesToPrintBuffer.toCSVLine()
    }

    val logFile = Paths.get(PathManager.getLogPath(), FILE_LOG_DIRECTORIES_INSPECTION_GROUPS).toFile()
    if (logFile.isFile) logFile.delete()

    logFile.bufferedWriter().use { bw ->
      bw.write(headerCSV)

      val infosAllInspectedDirectories = inspectionGroupsInfosAllProjectDirectories()

      // traverse project tree with DFS using stack
      val directoriesStackDFS = ArrayDeque<VirtualFile>()
      directoriesStackDFS.add(projectRoot)

      while (!directoriesStackDFS.isEmpty()) {
        val curDirectory = directoriesStackDFS.removeLast()

        val inspectedSubDirectories = curDirectory.children
          .filter { it in infosAllInspectedDirectories }
          .sortedBy { infosAllInspectedDirectories[it]!![ALL_INSPECTIONS_GROUP_NAME].inspectionsDuration }

        val directoryContainsInspectedFiles = directoriesInspectionGroupsInfos.containsKey(curDirectory)
        if (inspectedSubDirectories.size > 1 || directoryContainsInspectedFiles) {
          bw.newLine()
          bw.write(
            arrayOf(
              projectRoot.parent?.let { VfsUtil.getRelativePath(it, curDirectory) } ?: curDirectory.path,
              inspectionGroupsInfosToCSVLine(infosAllInspectedDirectories[curDirectory]!!)
            ).toCSVLine()
          )
        }
        directoriesStackDFS.addAll(inspectedSubDirectories)
      }
    }
  }

  /**
   * Compute info about all inspected project directories
   * (directories from directoriesInspectionGroupsInfos and their parent directories)
   */
  private fun inspectionGroupsInfosAllProjectDirectories(): Map<VirtualFile, InspectionGroupsInfosCollection> {
    val infosAllDirectories = mutableMapOf<VirtualFile, InspectionGroupsInfosCollection>()

    directoriesInspectionGroupsInfos.entries.forEach { (directory, inspectionGroupsInfos) ->
      directory.directoriesToProjectRoot().forEach {
        infosAllDirectories.compute(it) { _, v ->
          val groupInfos = v ?: InspectionGroupsInfosCollection()
          groupInfos.add(inspectionGroupsInfos)
          groupInfos
        }
      }
    }
    return infosAllDirectories
  }

  /** Get list of directories up to project root from file */
  private fun VirtualFile.directoriesToProjectRoot(): List<VirtualFile> {
    val directories = mutableListOf<VirtualFile>()

    if (projectRoot == null) return directories

    var directory: VirtualFile? = this
    while (directory != null && directory in projectRoot) {
      directories.add(directory)
      directory = directory.parent
    }
    directories.add(projectRoot)

    return directories
  }

  private fun inspectionTimeProportionFromTotal(inspectionsDuration: Long): Double =
    inspectionsDuration.toDouble() / sumOfAllInspectionDurations.get()

  private fun inspectionAbsoluteTime(inspectionsDuration: Long): Long =
    (inspectionTimeProportionFromTotal(inspectionsDuration) * (timeLastInspectionFinished - timeFirstInspectionStarted.get())).toLong()

  private operator fun VirtualFile.contains(other: VirtualFile) = VfsUtil.isAncestor(this, other, true)

  private fun <T> Array<T>.toCSVLine() = joinToString(CSV_SEPARATOR)
  private fun <T> List<T>.toCSVLine() = joinToString(CSV_SEPARATOR)
}

private typealias InspectionKey = String
