// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package tools

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.intellij.javascript.nodejs.npm.registry.NpmRegistryService
import com.intellij.javascript.nodejs.npm.registry.NpmRegistryService.Companion.getInstance
import com.intellij.javascript.nodejs.packageJson.NodePackageBasicInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.impl.ApplicationImpl
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.testFramework.HeavyPlatformTestCase
import com.intellij.util.io.HttpRequests
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.angular2.cli.AngularCliSchematicsRegistryServiceImpl.Companion.hasNgAddSchematic
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

/**
 * Execute to generate in the console JSON object with list of ng-add supported packages.
 * The result has to be manually merged into `resources/org/angular2/cli/ng-packages.json`.
 */
class GenerateNgAddCompatibleList : HeavyPlatformTestCase() {
  @Throws(Exception::class)
  fun testGenerate() {
    val app = ApplicationManager.getApplication() as ApplicationImpl?
    val f = ApplicationImpl::class.java.getDeclaredField("myTestModeFlag")
    f.setAccessible(true)
    f.setBoolean(app, false)
    try {

      val angularPkgs: MutableMap<String, NodePackageBasicInfo> = ConcurrentHashMap()
      val service = getInstance(project)

      val addPkg = Consumer { pkg: NodePackageBasicInfo ->
        angularPkgs.merge(pkg.name, pkg) { p1: NodePackageBasicInfo, p2: NodePackageBasicInfo ->
          if (!StringUtil.equals(p1.description, p2.description)) {
            System.err.println(
              "Different descriptions for " +
              p1.name +
              " (taking the new one - second):\n- " +
              p1.description +
              "\n- " +
              p2.description
            )
          }
          p2
        }
      }
      println("Current directory: " + File(".").getCanonicalPath())
      println("Reading existing list of packages")

      Files.newBufferedReader(
        Path.of("contrib/Angular/angular-backend/resources/org/angular2/cli/ng-packages.json"),
        StandardCharsets.UTF_8
      ).use { reader ->
        val root = JsonParser.parseReader(reader) as JsonObject
        if (root.get("ng-add") != null) {
          (root.get("ng-add") as JsonObject).entrySet()
            .forEach(Consumer { e: MutableMap.MutableEntry<String?, JsonElement?>? ->
              addPkg.accept(
                NodePackageBasicInfo(
                  e!!.key!!,
                  e.value!!.asString
                )
              )
            })
        }
      }
      val fromFile = angularPkgs.size
      println("Read $fromFile packages.")

      println("Loading list of Angular packages through search.")
      runBlocking(Dispatchers.IO) {
        for (search in listOf("angular", "angular components", "angular schematics")) {
          service.findPackages(
            NpmRegistryService.fullTextSearch(search),
            100000,
            null,
            Condition { _ -> true },
            addPkg
          )
          delay(500)
        }
      }

      println("\nFound additional " + (angularPkgs.size - fromFile) + " Angular packages.")

      val progress = AtomicInteger()

      val schematicsPkgs: MutableList<Pair<NodePackageBasicInfo, JsonObject>> =
        angularPkgs.values.stream().parallel().map<Pair<NodePackageBasicInfo, JsonObject>> { info: NodePackageBasicInfo ->
          try {
            val obj = service.fetchPackageJsonFuture(info.name, "latest", null).get(30, TimeUnit.SECONDS)
            if (obj != null && obj.has("schematics")) {
              return@map Pair.create<NodePackageBasicInfo, JsonObject>(info, obj)
            }
          }
          catch (t: Throwable) {
            println("Error for " + info.name + ", " + t.message)
          }
          finally {
            print(".")
            if (progress.incrementAndGet() % 100 == 0) {
              println(" " + ((progress.get() * 100) / angularPkgs.size) + "%\n")
            }
          }
          null
        }.filter { r -> r != null }.toList()

      println("\nFound " + schematicsPkgs.size + " packages which support schematics.")
      progress.set(0)

      var ngAddPkgs = schematicsPkgs.stream().parallel().map<NodePackageBasicInfo> { p ->
        val info = p.getFirst()
        val pkgJson = p.getSecond()
        val url = pkgJson.get("dist").getAsJsonObject().get("tarball").asString
        var schematicsFile = pkgJson.get("schematics").asString
        schematicsFile = schematicsFile.removePrefix("./")
        try {
          val contents = HttpRequests.request(url).readBytes(null)
          val bi: InputStream = BufferedInputStream(ByteArrayInputStream(contents))
          val gzi: InputStream = GzipCompressorInputStream(bi)
          val input: ArchiveInputStream<*> = TarArchiveInputStream(gzi)
          var e: ArchiveEntry?
          while ((input.getNextEntry().also { e = it }) != null) {
            if (e!!.name.endsWith(schematicsFile)) {
              if (input.canReadEntryData(e)) {
                val schematicsCollection = FileUtil.loadTextAndClose(input)
                if (hasNgAddSchematic(schematicsCollection)) {
                  return@map info
                }
                else {
                  return@map null
                }
              }
            }
          }
        }
        catch (t: Throwable) {
          println("Error for " + info.name + ": " + t.message)
          t.printStackTrace()
        }
        finally {
          print(".")
          if (progress.incrementAndGet() % 20 == 0) {
            println(" " + ((progress.get() * 100) / schematicsPkgs.size) + "%\n")
          }
        }
        null
      }.filter { info -> info != null }.collect(
        Collectors.toMap(
          Function { obj -> obj.name },
          Function { info -> info.description ?: "" })
      )

      ngAddPkgs = TreeMap(ngAddPkgs)

      println("\nFound " + ngAddPkgs.size + " packages which support ng-add:")
      println(GsonBuilder().setPrettyPrinting().create().toJson(ngAddPkgs))
    }
    finally {
      f.setBoolean(app, true)
    }
  }

  companion object {
    @Throws(IOException::class)
    private fun hasNgAddSchematic(schematicsCollection: String): Boolean {
      JsonReader(StringReader(schematicsCollection)).use { reader ->
        return hasNgAddSchematic(reader)
      }
    }
  }
}
