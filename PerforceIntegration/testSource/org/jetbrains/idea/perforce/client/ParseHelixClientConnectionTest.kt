package org.jetbrains.idea.perforce.client

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.jetbrains.idea.perforce.P4TestUtil
import org.jetbrains.idea.perforce.perforce.connections.HelixClientConnectionParametersProvider
import org.jetbrains.idea.perforce.perforce.connections.HelixClientConnectionParametersProvider.ConfigFiles
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito

@TestApplication
class ParseHelixClientConnectionTest {
  companion object {
    private val projectFixture = projectFixture()
  }

  private val project get() = projectFixture.get()

  @Test
  fun `test parse single client connection`() {
    val configFiles = Mockito.mock(ConfigFiles::class.java)
    val connectionMapFile = P4TestUtil.getResource("connectionmap-single.xml")
    assertTrue(connectionMapFile.exists())

    Mockito.`when`(configFiles.getClientConfig()).thenReturn(connectionMapFile)
    Mockito.`when`(configFiles.getAdminConfig()).thenReturn(connectionMapFile)

    val connectionParameters = HelixClientConnectionParametersProvider(configFiles).getConnectionParameters(project)
    assertEquals(1, connectionParameters.size)

    val singleConnectionParameters = connectionParameters.single()
    assertEquals("SomeUser", singleConnectionParameters.user)
    assertEquals("ssl:server-host:1666", singleConnectionParameters.server)
  }

  @Test
  fun `test parse multi client connection`() {
    val configFiles = Mockito.mock(ConfigFiles::class.java)
    val connectionMapFile = P4TestUtil.getResource("connectionmap-multi.xml")
    assertTrue(connectionMapFile.exists())

    Mockito.`when`(configFiles.getClientConfig()).thenReturn(connectionMapFile)
    Mockito.`when`(configFiles.getAdminConfig()).thenReturn(connectionMapFile)

    val connectionParameters = HelixClientConnectionParametersProvider(configFiles).getConnectionParameters(project)
    assertEquals(2, connectionParameters.size)

    val firstConnectionParameters = connectionParameters.first()

    assertEquals("SomeFirstUser", firstConnectionParameters.user)
    assertEquals("ssl:server-host-1:1666", firstConnectionParameters.server)

    val secondConnectionParameters = connectionParameters.last()
    assertEquals("SomeSecondUser", secondConnectionParameters.user)
    assertEquals("ssl:server-host-2:1666", secondConnectionParameters.server)
  }
}
