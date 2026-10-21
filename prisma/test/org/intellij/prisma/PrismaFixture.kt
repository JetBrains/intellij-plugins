package org.intellij.prisma

import com.intellij.javascript.testFramework.web.WebFrameworkFixture
import com.intellij.polySymbols.testFramework.HybridFixtureContext
import com.intellij.polySymbols.testFramework.HybridTestMode
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import org.intellij.prisma.lang.PrismaFileType
import org.jetbrains.annotations.TestOnly

/**
 * Creates a [PrismaFixture] on the light project. [testCasePath] is the test data directory under the Prisma test data root.
 * Declare the result as an instance property.
 */
@TestOnly
fun prismaFixture(testCasePath: String): TestFixture<PrismaFixture> =
  hybridFixture({ HybridTestMode.BasePlatform }) { PrismaFixture(it, testCasePath) }

/**
 * A [WebFrameworkFixture] with the Prisma test data root and the `prisma` extension.
 * Call a member from a background thread.
 */
open class PrismaFixture(context: HybridFixtureContext, testCasePath: String) : WebFrameworkFixture(
  context,
  testDataRoot = getPrismaTestDataPath(),
  testCasePath = testCasePath,
  defaultExtension = PrismaFileType.defaultExtension,
  defaultDependencies = emptyMap(),
) {

  /**
   * Returns the test file name: the test name with the first letter in lowercase, and the extension [ext].
   */
  fun getTestFileName(ext: String = "prisma"): String = "${getTestName(true)}.$ext"
}
