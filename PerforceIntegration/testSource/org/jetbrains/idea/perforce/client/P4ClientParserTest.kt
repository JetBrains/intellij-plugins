package org.jetbrains.idea.perforce.client

import com.intellij.vcs.test.VcsPlatformTest
import org.jetbrains.idea.perforce.perforce.connections.P4ClientParser
import org.jetbrains.idea.perforce.perforce.connections.P4ConnectionParameters

internal class P4ClientParserTest : VcsPlatformTest() {

  private val parser = P4ClientParser()
  private val dummyParameters = P4ConnectionParameters()

  fun `test parse client with description`() {
    val line = "Client my-workspace 2025/10/07 root /Users/john/projects/myproject 'My workspace description'"

    val client = parser.parseClientLine(line, dummyParameters)

    assertNotNull(client)
    assertEquals("my-workspace", client!!.clientName)
    assertEquals("2025/10/07", client.lastUpdate)
    assertEquals("/Users/john/projects/myproject", client.workspaceRootPath)
    assertEquals("My workspace description", client.description)
  }

  fun `test parse client with empty description`() {
    val line = "Client john_ws 2025/10/07 root /Users/user/perforce-project ''"

    val client = parser.parseClientLine(line, dummyParameters)

    assertNotNull(client)
    assertEquals("john_ws", client!!.clientName)
    assertEquals("2025/10/07", client.lastUpdate)
    assertEquals("/Users/user/perforce-project", client.workspaceRootPath)
    assertEquals("", client.description)
  }

  fun `test parse client with Windows path`() {
    val line = "Client dev-workspace 2024/05/15 root C:\\Users\\Developer\\Projects\\app 'Development workspace'"

    val client = parser.parseClientLine(line, dummyParameters)

    assertNotNull(client)
    assertEquals("dev-workspace", client!!.clientName)
    assertEquals("2024/05/15", client.lastUpdate)
    assertEquals("C:\\Users\\Developer\\Projects\\app", client.workspaceRootPath)
    assertEquals("Development workspace", client.description)
  }

  fun `test parse client with special characters in description`() {
    val line = "Client test-ws 2023/12/01 root /home/user/test 'Test workspace (v2) - updated!'"

    val client = parser.parseClientLine(line, dummyParameters)

    assertNotNull(client)
    assertEquals("test-ws", client!!.clientName)
    assertEquals("2023/12/01", client.lastUpdate)
    assertEquals("/home/user/test", client.workspaceRootPath)
    assertEquals("Test workspace (v2) - updated!", client.description)
  }

  fun `test parse client with underscores and numbers in name`() {
    val line = "Client user_workspace_123 2025/01/20 root /var/perforce/ws 'Workspace 123'"

    val client = parser.parseClientLine(line, dummyParameters)

    assertNotNull(client)
    assertEquals("user_workspace_123", client!!.clientName)
    assertEquals("2025/01/20", client.lastUpdate)
    assertEquals("/var/perforce/ws", client.workspaceRootPath)
    assertEquals("Workspace 123", client.description)
  }

  fun `test parse client with multiple spaces in description`() {
    val line = "Client ws 2025/06/10 root /path/to/workspace 'Description   with   multiple   spaces'"

    val client = parser.parseClientLine(line, dummyParameters)

    assertNotNull(client)
    assertEquals("ws", client!!.clientName)
    assertEquals("Description   with   multiple   spaces", client.description)
  }

  fun `test invalid line returns null`() {
    val invalidLines = listOf(
      "",
      "Invalid line",
      "Client",
      "Client name-only",
      "Client name 2025/10/07",
      "Client name 2025/10/07 root",
      "Client name 2025/10/07 root /path",
      "Client name invalid-date root /path 'desc'",
    )

    for (line in invalidLines) {
      val client = parser.parseClientLine(line, dummyParameters)
      assertNull("Expected null for line: $line", client)
    }
  }

  fun `test parameters are preserved`() {
    val parameters = P4ConnectionParameters().apply {
      server = "perforce.example.com:1666"
      user = "testuser"
      client = "testclient"
    }
    val line = "Client my-ws 2025/10/07 root /home/user/workspace 'Test'"

    val client = parser.parseClientLine(line, parameters)

    assertNotNull(client)
    assertSame(parameters, client!!.parameters)
  }
}
