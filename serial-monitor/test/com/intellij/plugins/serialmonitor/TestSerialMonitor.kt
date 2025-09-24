package com.intellij.plugins.serialmonitor

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.util.Disposer
import com.intellij.plugins.serialmonitor.service.PortStatus
import com.intellij.plugins.serialmonitor.service.SerialPortProvider
import com.intellij.plugins.serialmonitor.service.SerialPortService
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.TestDisposable
import com.intellij.testFramework.replaceService
import com.intellij.util.application
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.function.Consumer
import kotlin.String

@TestApplication
class TestSerialMonitor {

  private lateinit var provider: MockSerialPortProvider

  @TestDisposable
  private lateinit var disposable: Disposable

  suspend fun simpleTestCase(vararg ports: String) {
    provider = MockSerialPortProvider(*ports)
    application.replaceService(SerialPortProvider::class.java, provider, disposable)
    provider.awaitScan()
  }

  suspend fun failingConnectTestCase(vararg ports: String) {
    simpleTestCase(*ports)
    provider.failCreateFor.addAll(ports)
  }

  suspend fun createConnection(portName: String): SerialPortService.SerialConnection =
    serviceAsync<SerialPortService>().newConnection(portName).also { Disposer.register(disposable, it) }

  suspend fun serialConnectionTestCase(portName: String, profile: SerialPortProfile): SerialPortService.SerialConnection {
    simpleTestCase(portName)

    val conn = createConnection(portName)
    conn.connect(profile)
    return conn
  }

  suspend fun serialConnectionTestCase(portName: String, localEcho: Boolean = false): SerialPortService.SerialConnection =
    serialConnectionTestCase(portName, SerialPortProfile(portName = portName, localEcho = localEcho))

  @Test
  fun `can list expected port`() = timeoutRunBlocking {
    simpleTestCase("/dev/ttyUSB0")

    val portService = serviceAsync<SerialPortService>()
    val ports = portService.getPortsNames()
    assertEquals(setOf("/dev/ttyUSB0"), ports)
  }

  @Test
  fun `port descriptive name comes from provider`() = timeoutRunBlocking {
    simpleTestCase("/dev/ttyUSB0")

    val portService = serviceAsync<SerialPortService>()
    assertEquals("USB Device /dev/ttyUSB0", portService.portDescriptiveName("/dev/ttyUSB0"))
  }

  @Test
  fun `connect sets connected status and write without echo`() = timeoutRunBlocking {
    simpleTestCase("/dev/ttyUSB0")

    val conn = createConnection("/dev/ttyUSB0")
    assertEquals(PortStatus.DISCONNECTED, conn.getStatus())

    val profile = SerialPortProfile("/dev/ttyUSB0", baudRate = 9600, localEcho = false)
    conn.connect(profile)
    assertEquals(PortStatus.CONNECTED, conn.getStatus())

    val data = "hello".toByteArray()

    // No data echoed from write
    suspendCancellableCoroutine { cont ->
      conn.dataListener = Consumer {
        cont.resumeWith(Result.failure(AssertionError("dataListener should not be called when localEcho is false")))
      }
      conn.write(data)
      launch {
        delay(1000)
        cont.resumeWith(Result.success(Unit))
      }
    }

    // but underlying port receives write
    val port = provider.createPort("/dev/ttyUSB0")
    assertArrayEquals(data, port.lastWritten)
  }

  @Test
  fun `local echo echoes written data back to listener`() = timeoutRunBlocking {
    val conn = serialConnectionTestCase("/dev/ttyUSB0", localEcho = true)

    val data = "abc".toByteArray()

    val echoed = suspendCancellableCoroutine { cont ->
      conn.dataListener = Consumer {
        cont.resumeWith(Result.success(it))
      }
      conn.write(data)
    }

    assertArrayEquals(data, echoed)
  }

  @Test
  fun `incoming data from port is forwarded to listener`() = timeoutRunBlocking {
    val conn = serialConnectionTestCase("/dev/ttyUSB0")

    val payload = "foobar".toByteArray()

    val received = suspendCancellableCoroutine { cont ->
      conn.dataListener = Consumer { cont.resumeWith(Result.success(it)) }
      provider.createPort("/dev/ttyUSB0").fireData(payload)
    }

    assertArrayEquals(payload, received)
  }

  @Test
  fun `cts and dsr are updated from events`() = timeoutRunBlocking {
    val conn = serialConnectionTestCase("/dev/ttyUSB0")

    val port = provider.createPort("/dev/ttyUSB0")

    val cts = suspendCancellableCoroutine { cont ->
      conn.ctsListener = { cont.resumeWith(Result.success(it)) }
      port.fireCTS(true)
    }
    val dsr = suspendCancellableCoroutine { cont ->
      conn.dsrListener = { cont.resumeWith(Result.success(it)) }
      port.fireDSR(true)
    }

    assertTrue(cts)
    assertTrue(dsr)
  }

  @Test
  fun `rts dtr settings are applied before and after connect`() = timeoutRunBlocking {
    val conn = serialConnectionTestCase("/dev/ttyUSB0")

    // Change before connect
    conn.rts = false
    conn.dtr = false

    val profile = SerialPortProfile("/dev/ttyUSB0", baudRate = 9600)
    conn.connect(profile)

    val port = provider.createPort("/dev/ttyUSB0")
    assertFalse(port.lastRTS!!)
    assertFalse(port.lastDTR!!)

    // Change after connect -> should call port.setRTS/DTR
    conn.rts = true
    conn.dtr = true
    assertTrue(port.lastRTS!!)
    assertTrue(port.lastDTR!!)
  }

  @Test
  fun `close transitions to disconnected and disconnects port`() = timeoutRunBlocking {
    val conn = serialConnectionTestCase("/dev/ttyUSB0")

    val port = provider.createPort("/dev/ttyUSB0")
    assertTrue(port.connected)

    conn.close(true)
    assertEquals(PortStatus.DISCONNECTED, conn.getStatus())
    assertFalse(port.connected)
  }

  @Test
  fun `disappearing port results in unavailable disconnected status`() = timeoutRunBlocking {
    val conn = serialConnectionTestCase("/dev/ttyUSB0")

    // port disappears
    provider.changePortsAndAwaitScan {
      clear()
    }

    assertEquals(PortStatus.UNAVAILABLE_DISCONNECTED, conn.getStatus())
    assertFalse(provider.createPort("/dev/ttyUSB0").connected)
  }

  @Test
  fun `portStatus for unknown port without connection is UNAVAILABLE, with connection is UNAVAILABLE_DISCONNECTED`() = timeoutRunBlocking {
    val portService = serviceAsync<SerialPortService>()

    assertEquals(PortStatus.UNAVAILABLE, portService.portStatus("/dev/does-not-exist"))

    val conn = createConnection("/dev/does-not-exist")
    assertEquals(PortStatus.UNAVAILABLE_DISCONNECTED, portService.portStatus(conn.portName))
  }

  @Test
  fun `ports are sorted numerically by suffix`() = timeoutRunBlocking {
    simpleTestCase("/dev/ttyUSB0", "/dev/ttyUSB2", "/dev/ttyUSB1", "/dev/ttyUSB10", "/dev/ttyUSB")

    val portService = serviceAsync<SerialPortService>()
    val sorted = portService.getPortsNames().toList()
    assertEquals(listOf("/dev/ttyUSB", "/dev/ttyUSB0", "/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB10"), sorted)
  }

  @Test
  fun `port names are sorted case-insensitively`() = timeoutRunBlocking {
    simpleTestCase("/dev/ttyacm3", "/dev/ttyACM1", "/dev/TTYacm2")
    val portService = serviceAsync<SerialPortService>()
    val sorted = portService.getPortsNames().toList()
    assertEquals(listOf("/dev/ttyACM1", "/dev/TTYacm2", "/dev/ttyacm3"), sorted)
  }

  @Test
  fun `port descriptive name returns null when provider createPort fails`() = timeoutRunBlocking {
    failingConnectTestCase("/dev/fail")

    val portService = serviceAsync<SerialPortService>()

    assertNull(portService.portDescriptiveName("/dev/fail"))
  }

  @Test
  fun `rts and dtr setter failures are wrapped into SerialMonitorException`(): Unit = timeoutRunBlocking {
    val conn = serialConnectionTestCase("/dev/ttyUSB0")
    val port = provider.createPort("/dev/ttyUSB0")

    port.failOnRTS = true
    port.failOnDTR = true

    assertThrows(SerialMonitorException::class.java) {
      conn.rts = false
    }

    assertThrows(SerialMonitorException::class.java) {
      conn.dtr = false
    }
  }

  @Test
  fun `cts and dsr properties are false when not connected`() = timeoutRunBlocking {
    val conn = createConnection("/dev/ttyUSB0")
    assertFalse(conn.cts)
    assertFalse(conn.dsr)
  }

  @Test
  fun `cts and dsr properties reflect underlying port state after connect`() = timeoutRunBlocking {
    val conn = serialConnectionTestCase("/dev/ttyUSB0")
    val port = provider.createPort("/dev/ttyUSB0")

    port.cts = true
    port.dsr = true
    assertTrue(conn.cts)
    assertTrue(conn.dsr)

    port.cts = false
    port.dsr = false
    assertFalse(conn.cts)
    assertFalse(conn.dsr)
  }

  @Test
  fun `connect failure throws Serial Monitor Exception`(): Unit = timeoutRunBlocking {
    simpleTestCase("/dev/ttyUSB0")

    val conn = createConnection("/dev/ttyUSB0")
    val port = provider.createPort("/dev/ttyUSB0")
    port.failOnConnect = true
    assertThrows(SerialMonitorException::class.java) {
      conn.connect(SerialPortProfile(portName = "/dev/ttyUSB0", baudRate = 9600))
    }
  }

  @Test
  fun `portStatus READY when port exists and no connection`() = timeoutRunBlocking {
    simpleTestCase("/dev/uniqueREADY")

    val portService = serviceAsync<SerialPortService>()

    assertEquals(PortStatus.READY, portService.portStatus("/dev/uniqueREADY"))
  }

  @Test
  fun `local echo remains active after disconnect`() = timeoutRunBlocking {
    val conn = serialConnectionTestCase("/dev/ttyUSB0", localEcho = true)
    conn.close(true)
    // even after disconnect, write should echo to dataListener because localEcho remains set
    val data = "after".toByteArray()
    val echoed = suspendCancellableCoroutine { cont ->
      conn.dataListener = Consumer { cont.resumeWith(Result.success(it)) }
      conn.write(data)
    }
    assertArrayEquals(data, echoed)
  }

  @Test
  fun `reappearing port restores DISCONNECTED status after unavailable`() = timeoutRunBlocking {
    val conn = serialConnectionTestCase("/dev/ttyUSB0")

    // Disappear then rescan
    provider.changePortsAndAwaitScan {
      clear()
    }

    assertEquals(PortStatus.UNAVAILABLE_DISCONNECTED, conn.getStatus())

    // Re-appear then rescan
    provider.changePortsAndAwaitScan {
      add("/dev/ttyUSB0")
    }

    // Should go back to DISCONNECTED (not CONNECTED)
    assertEquals(PortStatus.DISCONNECTED, conn.getStatus())
  }

  @Test
  fun `disposing connection disconnects port and removes connection from portStatus`() = timeoutRunBlocking {
    val conn = serialConnectionTestCase("/dev/ttyUSB0")
    val portService = serviceAsync<SerialPortService>()
    val port = provider.createPort("/dev/ttyUSB0")
    assertTrue(port.connected)

    // Dispose connection, simulating the tab closing
    Disposer.dispose(conn)

    // Underlying mock port disconnected and service reports READY (port exists, no connection)
    assertFalse(port.connected)
    assertEquals(PortStatus.READY, portService.portStatus("/dev/ttyUSB0"))
  }

  @Test
  fun `newConnection replaces and disposes previous connection for same port`() = timeoutRunBlocking {
    simpleTestCase("/dev/ttyUSB0")

    val first = createConnection("/dev/ttyUSB0")
    first.connect(SerialPortProfile(portName = "/dev/ttyUSB0", baudRate = 9600))
    val port = provider.createPort("/dev/ttyUSB0")
    assertTrue(port.connected)

    // Creating a second connection should dispose the first and disconnect the port
    val second = createConnection("/dev/ttyUSB0")
    assertFalse(port.connected)

    // Now connect with the second connection to make sure it functions
    second.connect(SerialPortProfile(portName = "/dev/ttyUSB0", baudRate = 9600))
    assertTrue(port.connected)
  }

  @Test
  fun `cts dsr getters return false when underlying port getter throws`() = timeoutRunBlocking {
    val conn = serialConnectionTestCase("/dev/ttyUSB0")
    val port = provider.createPort("/dev/ttyUSB0")

    port.failOnGetCTS = true
    port.failOnGetDSR = true

    assertDoesNotThrow { conn.cts }
    assertFalse(conn.cts)

    assertDoesNotThrow { conn.dsr }
    assertFalse(conn.dsr)
  }

  @Test
  fun `close failure is wrapped and status updated`() = timeoutRunBlocking {
    val conn = serialConnectionTestCase("/dev/ttyUSB0")
    val port = provider.createPort("/dev/ttyUSB0")
    port.failOnDisconnect = true
    assertThrows(SerialMonitorException::class.java) {
      conn.close(true)
    }

    // Despite failure, final state should be updated to DISCONNECTED
    assertEquals(PortStatus.DISCONNECTED, conn.getStatus())
  }

  @Test
  fun `dispose swallows disconnect failure and updates status`() = timeoutRunBlocking {
    val conn = serialConnectionTestCase("/dev/ttyUSB0")
    val port = provider.createPort("/dev/ttyUSB0")
    port.failOnDisconnect = true
    assertDoesNotThrow { Disposer.dispose(conn) }
    assertEquals(PortStatus.DISCONNECTED, conn.getStatus())
    assertTrue(port.connected) // Verifies the disconnect failed
  }

  @Test
  fun `write before connect does not echo and does not write`() = timeoutRunBlocking {
    simpleTestCase("/dev/ttyUSB0")
    val conn = createConnection("/dev/ttyUSB0")

    val data = "pre-connect".toByteArray()
    val port = provider.createPort("/dev/ttyUSB0")

    // No echo should occur because not connected and localEcho is false by default
    suspendCancellableCoroutine { cont ->
      launch {
        delay(500)
        cont.resumeWith(Result.success(Unit))
      }
      conn.dataListener = Consumer {
        cont.resumeWith(Result.failure(AssertionError("dataListener should not be called before connect")))
      }
      conn.write(data)
      assertNull(port.lastWritten)
    }
  }

  @Test
  fun `profile parameters are propagated to mock port on connect`() = timeoutRunBlocking {
    val profile = SerialPortProfile(
      portName = "/dev/ttyUSB0",
      baudRate = 230400,
      bits = 7,
      stopBits = StopBits.BITS_1_5,
      parity = Parity.ODD,
      newLine = SerialProfileService.NewLine.LF,
      encoding = "UTF-8",
      localEcho = true,
      showHardwareControls = true
    )

    serialConnectionTestCase(profile.portName, profile.copy())

    val port = provider.createPort(profile.portName)
    assertEquals(profile, port.connectedProfile)
  }
}
