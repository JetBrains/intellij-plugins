package org.jetbrains.idea.perforce.client

import com.intellij.vcs.test.VcsPlatformTest
import org.jetbrains.idea.perforce.perforce.connections.HelixClientConnectionParametersProvider
import org.jetbrains.idea.perforce.perforce.connections.HelixClientConnectionParametersProvider.ConfigFiles
import org.mockito.Mockito
import java.io.File

class ParseHelixClientConnectionTest : VcsPlatformTest() {

  fun `test parse single client connection`() {
    val configFiles = Mockito.mock(ConfigFiles::class.java)
    val connectionMapFile = File("resources/connectionmap-single.xml")
    assertTrue(connectionMapFile.exists())

    Mockito.`when`(configFiles.getClientConfig()).thenReturn(connectionMapFile)
    Mockito.`when`(configFiles.getAdminConfig()).thenReturn(connectionMapFile)

    val connectionParameters = HelixClientConnectionParametersProvider(configFiles).getConnectionParameters(project)
    assertEquals(1, connectionParameters.size)

    val singleConnectionParameters = connectionParameters.single()
    assertEquals("SomeUser", singleConnectionParameters.user)
    assertEquals("ssl:server-host:1666", singleConnectionParameters.server)
  }

  fun `test parse multi client connection`() {
    val configFiles = Mockito.mock(ConfigFiles::class.java)
    val connectionMapFile = File("resources/connectionmap-multi.xml")
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
