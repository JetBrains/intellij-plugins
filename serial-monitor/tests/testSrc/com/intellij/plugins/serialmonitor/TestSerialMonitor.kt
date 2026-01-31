package com.intellij.plugins.serialmonitor

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.util.Disposer
import com.intellij.plugins.serialmonitor.service.PortStatus
import com.intellij.plugins.serialmonitor.service.SerialPortService
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.TestDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.function.Consumer

@TestApplication
class TestSerialMonitor {
  @Nested
  inner class Basic {
    private val providerFixture = serialPortProviderFixture("/dev/ttyUSB0")
    private val port by providerFixture.serialPortFixture("/dev/ttyUSB0")

    @TestDisposable
    private lateinit var disposable: Disposable

    // Create a new SerialConnection in a not-connected state.
    suspend fun createConnection(portName: String): SerialPortService.SerialConnection =
      serviceAsync<SerialPortService>().newConnection(portName).also { Disposer.register(disposable, it) }

    @Test
    fun `can list expected port`() = timeoutRunBlocking {
      val portService = serviceAsync<SerialPortService>()
      val ports = portService.getPortsNames()
      assertEquals(setOf("/dev/ttyUSB0"), ports)
    }

    @Test
    fun `port descriptive name comes from provider`() = timeoutRunBlocking {
      val portService = serviceAsync<SerialPortService>()
      assertEquals("USB Device /dev/ttyUSB0", portService.portDescriptiveName("/dev/ttyUSB0"))
    }

    @Test
    fun `newly created connected should be in DISCONNECTED state`() = timeoutRunBlocking {
      val conn = createConnection("/dev/ttyUSB0")
      assertEquals(PortStatus.DISCONNECTED, conn.getStatus())
    }

    @Test
    fun `portStatus READY when port exists and no connection`() = timeoutRunBlocking {
      val portService = serviceAsync<SerialPortService>()

      assertEquals(PortStatus.READY, portService.portStatus("/dev/ttyUSB0"))
    }

    @Test
    fun `cts and dsr properties are false when not connected`() = timeoutRunBlocking {
      val conn = createConnection("/dev/ttyUSB0")
      assertFalse(conn.cts)
      assertFalse(conn.dsr)
    }


    @Test
    fun `newConnection replaces and disposes previous connection for same port`() = timeoutRunBlocking {
      val first = createConnection("/dev/ttyUSB0")
      first.connect(SerialPortProfile(portName = "/dev/ttyUSB0", baudRate = 9600))
      assertTrue(port.connected)

      // Creating a second connection should dispose the first and disconnect the port
      val second = createConnection("/dev/ttyUSB0")
      assertFalse(port.connected)

      // Now connect with the second connection to make sure it functions
      second.connect(SerialPortProfile(portName = "/dev/ttyUSB0", baudRate = 9600))
      assertTrue(port.connected)
    }

    @Test
    fun `rts dtr settings are applied before and after connect`() = timeoutRunBlocking {
      val conn = createConnection("/dev/ttyUSB0")

      // Change before connect
      conn.rts = false
      conn.dtr = false

      val profile = SerialPortProfile("/dev/ttyUSB0", baudRate = 9600)
      conn.connect(profile)

      assertFalse(port.lastRTS!!)
      assertFalse(port.lastDTR!!)

      // Change after connect -> should call port.setRTS/DTR
      conn.rts = true
      conn.dtr = true

      assertTrue(port.lastRTS!!)
      assertTrue(port.lastDTR!!)
    }

    @Test
    fun `portStatus for unknown port without connection is UNAVAILABLE, with connection is UNAVAILABLE_DISCONNECTED`() = timeoutRunBlocking {
      val portService = serviceAsync<SerialPortService>()

      assertEquals(PortStatus.UNAVAILABLE, portService.portStatus("/dev/does-not-exist"))

      val conn = createConnection("/dev/does-not-exist")
      assertEquals(PortStatus.UNAVAILABLE_DISCONNECTED, portService.portStatus(conn.portName))
    }

    @Test
    fun `connect failure throws Serial Monitor Exception`(): Unit = timeoutRunBlocking {
      val conn = createConnection("/dev/ttyUSB0")
      port.failOnConnect = true
      assertThrows<SerialMonitorException> {
        conn.connect(SerialPortProfile(portName = "/dev/ttyUSB0", baudRate = 9600))
      }
    }

    @Test
    fun `write before connect does not echo and does not write`() = timeoutRunBlocking {
      val conn = createConnection("/dev/ttyUSB0")
      val data = "pre-connect".toByteArray()

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
  }

  @Nested
  inner class Connection {
    private val providerFixture = serialPortProviderFixture("/dev/ttyUSB0")
    private val portFixture = providerFixture.serialPortFixture("/dev/ttyUSB0")
    private val provider by providerFixture
    private val conn by portFixture.serialConnectionFixture()
    private val port by portFixture

    @Test
    fun `connect sets connected status and write without echo`() = timeoutRunBlocking {
      assertEquals(PortStatus.CONNECTED, conn.getStatus())
      assertFalse(port.connectedProfile?.localEcho ?: true)

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
      assertArrayEquals(data, port.lastWritten)
    }

    @Test
    fun `incoming data from port is forwarded to listener`() = timeoutRunBlocking {
      val payload = "foobar".toByteArray()

      val received = suspendCancellableCoroutine { cont ->
        conn.dataListener = Consumer { cont.resumeWith(Result.success(it)) }
        port.fireData(payload)
      }

      assertArrayEquals(payload, received)
    }

    @Test
    fun `cts and dsr are updated from events`() = timeoutRunBlocking {
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
    fun `close transitions to disconnected and disconnects port`() = timeoutRunBlocking {
      assertTrue(port.connected)

      conn.close(true)
      assertEquals(PortStatus.DISCONNECTED, conn.getStatus())
      assertFalse(port.connected)
    }

    @Test
    fun `disappearing port results in unavailable disconnected status`() = timeoutRunBlocking {
      // port disappears
      provider.changePortsAndAwaitCondition(
        action = {
          clear()
        },
        condition = {
          conn.getStatus() == PortStatus.UNAVAILABLE_DISCONNECTED
        }
      )
      assertFalse(port.connected)
    }

    @Test
    fun `rts and dtr setter failures are wrapped into SerialMonitorException`(): Unit = timeoutRunBlocking {
      port.failOnRTS = true
      port.failOnDTR = true

      assertThrows<SerialMonitorException> {
        conn.rts = false
      }

      assertThrows<SerialMonitorException> {
        conn.dtr = false
      }
    }

    @Test
    fun `cts and dsr properties reflect underlying port state after connect`() = timeoutRunBlocking {
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
    fun `cts dsr getters return false when underlying port getter throws`() = timeoutRunBlocking {
      port.failOnGetCTS = true
      port.failOnGetDSR = true

      assertDoesNotThrow { conn.cts }
      assertFalse(conn.cts)

      assertDoesNotThrow { conn.dsr }
      assertFalse(conn.dsr)
    }

    @Test
    fun `reappearing port restores DISCONNECTED status after unavailable`() = timeoutRunBlocking {
      assertEquals(PortStatus.CONNECTED, conn.getStatus())

      provider.changePortsAndAwaitCondition(
        // Remove the port
        action = {
          clear()
        },
        // Should go to UNAVAILABLE_DISCONNECTED
        condition = {
          conn.getStatus() == PortStatus.UNAVAILABLE_DISCONNECTED
        }
      )

      // Double-check with the assert. The status should not change between here and the await conditions
      assertEquals(PortStatus.UNAVAILABLE_DISCONNECTED, conn.getStatus())

      provider.changePortsAndAwaitCondition(
        // Add the port back
        action = {
          add("/dev/ttyUSB0")
        },
        // Should go back to DISCONNECTED (not CONNECTED)
        condition = {
          conn.getStatus() == PortStatus.DISCONNECTED
        }
      )

      assertEquals(PortStatus.DISCONNECTED, conn.getStatus())
    }

    @Test
    fun `disposing connection disconnects port and removes connection from portStatus`() = timeoutRunBlocking {
      val portService = serviceAsync<SerialPortService>()
      assertTrue(port.connected)

      // Dispose connection, simulating the tab closing
      Disposer.dispose(conn)

      // Underlying mock port disconnected and service reports READY (port exists, no connection)
      assertFalse(port.connected)
      assertEquals(PortStatus.READY, portService.portStatus("/dev/ttyUSB0"))
    }


    @Test
    fun `close failure is wrapped and status updated`() = timeoutRunBlocking {
      port.failOnDisconnect = true
      assertThrows<SerialMonitorException> {
        conn.close(true)
      }
      // Despite failure, final state should be updated to DISCONNECTED
      assertEquals(PortStatus.DISCONNECTED, conn.getStatus())
      assertTrue(port.connected) // Verifies the disconnect failed
    }

    @Test
    fun `dispose swallows disconnect failure and updates status`() = timeoutRunBlocking {
      port.failOnDisconnect = true
      assertDoesNotThrow { Disposer.dispose(conn) }
      assertEquals(PortStatus.DISCONNECTED, conn.getStatus())
      assertTrue(port.connected) // Verifies the disconnect failed
    }
  }

  @Nested
  inner class ConnectionWithLocalEcho {
    private val providerFixture = serialPortProviderFixture("/dev/ttyUSB0")
    private val portFixture = providerFixture.serialPortFixture("/dev/ttyUSB0")
    private val conn by portFixture.serialConnectionFixture(SerialPortProfile(portName = "/dev/ttyUSB0", localEcho = true))

    @Test
    fun `local echo echoes written data back to listener`() = timeoutRunBlocking {
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
    fun `local echo remains active after disconnect`() = timeoutRunBlocking {
      conn.close(true)
      // even after disconnect, write should echo to dataListener because localEcho remains set
      val data = "after".toByteArray()
      val echoed = suspendCancellableCoroutine { cont ->
        conn.dataListener = Consumer { cont.resumeWith(Result.success(it)) }
        conn.write(data)
      }
      assertArrayEquals(data, echoed)
    }
  }

  @Nested
  inner class SuffixSort {
    @Suppress("unused")
    private val providerFixture = serialPortProviderFixture("/dev/ttyUSB0", "/dev/ttyUSB2", "/dev/ttyUSB1", "/dev/ttyUSB10", "/dev/ttyUSB")

    @Test
    fun `ports are sorted numerically by suffix`() = timeoutRunBlocking {
      val portService = serviceAsync<SerialPortService>()
      val sorted = portService.getPortsNames().toList()
      assertEquals(listOf("/dev/ttyUSB", "/dev/ttyUSB0", "/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB10"), sorted)
    }
  }

  @Nested
  inner class CaseInsensitiveSort {
    @Suppress("unused")
    private val providerFixture = serialPortProviderFixture("/dev/ttyacm3", "/dev/ttyACM1", "/dev/TTYacm2")
    @Test
    fun `port names are sorted case-insensitively`() = timeoutRunBlocking {
      val portService = serviceAsync<SerialPortService>()
      val sorted = portService.getPortsNames().toList()
      assertEquals(listOf("/dev/ttyACM1", "/dev/TTYacm2", "/dev/ttyacm3"), sorted)
    }
  }

  @Nested
  inner class FailConnect {
    private val provider by serialPortProviderFixture("/dev/fail")

    @Test
    fun `port descriptive name returns null when provider createPort fails`() = timeoutRunBlocking {
      provider.failCreateFor.add("/dev/fail")
      val portService = serviceAsync<SerialPortService>()
      assertNull(portService.portDescriptiveName("/dev/fail"))
    }
  }

  @Nested
  inner class CustomizedProfile {
    private val profile = SerialPortProfile(
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
    private val providerFixture = serialPortProviderFixture("/dev/ttyUSB0")
    private val portFixture = providerFixture.serialPortFixture("/dev/ttyUSB0")
    @Suppress("unused")
    private val connFixture = portFixture.serialConnectionFixture(profile)
    private val port by portFixture

    @Test
    fun `profile parameters are propagated to mock port on connect`() = timeoutRunBlocking {
      assertEquals(profile, port.connectedProfile)
    }
  }
}
