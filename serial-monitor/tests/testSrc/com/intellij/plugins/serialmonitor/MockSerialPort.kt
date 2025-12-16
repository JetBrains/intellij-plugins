package com.intellij.plugins.serialmonitor

import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.util.Disposer
import com.intellij.plugins.serialmonitor.service.SerialPort
import com.intellij.plugins.serialmonitor.service.SerialPortException
import com.intellij.plugins.serialmonitor.service.SerialPortProvider
import com.intellij.plugins.serialmonitor.service.SerialPortService
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import com.intellij.testFramework.replaceService
import com.intellij.util.application
import kotlinx.coroutines.flow.first
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class MockSerialPortProvider : SerialPortProvider {
  private val ports = AtomicReference<List<String>>(emptyList())
  private val created = ConcurrentHashMap<String, MockSerialPort>()
  val failCreateFor: MutableSet<String> = ConcurrentHashMap.newKeySet()

  constructor(vararg portNames: String) {
    ports.store(listOf(*portNames))
  }

  override suspend fun scanAvailablePorts(): List<String> = ports.load()

  override fun createPort(portName: String): MockSerialPort {
    if (failCreateFor.contains(portName)) throw SerialPortException("create failed for $portName")
    return created.getOrPut(portName) { MockSerialPort(portName) }
  }

  /**
   * Suspends until the ports reported by [SerialPortService] match ports of this provider.
   */
  suspend fun awaitScan() {
    val portNames = ports.load()
    serviceAsync<SerialPortService>().portNamesFlow.first {
      it == portNames.toSet()
    }
  }

  suspend fun changePortsAndAwaitScan(action: MutableList<String>.()->Unit) {
    // Compute new ports
    val newPorts = ports.load().toMutableList()
    newPorts.action()

    // Store the new state so the scanner finds it
    ports.store(newPorts)

    // Listen until the names update to our expected state asynchronously
    // State flow also emits the current value to new collectors, then it emits changes; so unless a full update to another value which
    // propagates to the service sneaks in here, we are safe.
    awaitScan()
  }
}

class MockSerialPort(private val name: String) : SerialPort {
  private var portListener: SerialPort.SerialPortListener? = null

  @Volatile
  var connected: Boolean = false
    private set

  @Volatile
  var lastWritten: ByteArray? = null
    private set

  @Volatile
  var lastRTS: Boolean? = null
  @Volatile
  var lastDTR: Boolean? = null

  @Volatile
  var cts: Boolean = false
  @Volatile
  var dsr: Boolean = false

  @Volatile
  var connectedProfile: SerialPortProfile? = null

  @Volatile
  var failOnConnect: Boolean = false
  @Volatile
  var failOnRTS: Boolean = false
  @Volatile
  var failOnDTR: Boolean = false
  @Volatile
  var failOnDisconnect: Boolean = false
  @Volatile
  var failOnGetCTS: Boolean = false
  @Volatile
  var failOnGetDSR: Boolean = false

  override fun getSystemName(): String = name
  override fun getDescriptiveName(): String = "USB Device $name"

  override fun connect(profile: SerialPortProfile, listener: SerialPort.SerialPortListener, rts: Boolean, dtr: Boolean) {
    if (failOnConnect) throw RuntimeException("connect failed for $name")
    connected = true
    connectedProfile = profile
    portListener = listener
    lastRTS = rts
    lastDTR = dtr
  }

  override fun disconnect() {
    if (failOnDisconnect) throw SerialPortException("disconnect failed for $name")
    connected = false
  }

  override fun write(data: ByteArray): Int {
    lastWritten = data
    return data.size
  }

  override fun setRTS(value: Boolean) {
    if (failOnRTS) throw SerialPortException("RTS failure for $name")
    lastRTS = value
  }

  override fun setDTR(value: Boolean) {
    if (failOnDTR) throw SerialPortException("DTR failure for $name")
    lastDTR = value
  }

  override fun getCTS(): Boolean {
    if (failOnGetCTS) throw SerialPortException("CTS failure for $name")
    return cts
  }

  override fun getDSR(): Boolean {
    if (failOnGetDSR) throw SerialPortException("DSR failure for $name")
    return dsr
  }

  // Test helpers
  fun fireData(data: ByteArray) {
    portListener?.onDataReceived(data)
  }

  fun fireCTS(state: Boolean) {
    cts = state
    portListener?.onCTSChanged(state)
  }

  fun fireDSR(state: Boolean) {
    dsr = state
    portListener?.onDSRChanged(state)
  }
}

@TestOnly
fun serialPortProviderFixture(vararg ports: String) = testFixture("Serial Port Provider Fixture") {
  val provider = MockSerialPortProvider(*ports)
  val disposable = Disposer.newDisposable("Serial Port Provider Test Fixture")
  application.replaceService(SerialPortProvider::class.java, provider, disposable)
  provider.awaitScan()

  initialized(provider) {
    Disposer.dispose(disposable)
  }
}

@TestOnly
fun TestFixture<MockSerialPortProvider>.serialPortFixture(portName: String) = testFixture("Serial Port Fixture") {
  val provider = init()
  val port = provider.createPort(portName)
  initialized(port) { }
}

@TestOnly
fun TestFixture<MockSerialPort>.serialConnectionFixture(profile: SerialPortProfile? = null) = testFixture("Serial Connection Fixture") {
  val port = init()
  val usedProfile = profile ?: SerialPortProfile(portName = port.getSystemName())
  val conn = serviceAsync<SerialPortService>().newConnection(port.getSystemName())
  conn.connect(usedProfile)

  initialized(conn) {
    Disposer.dispose(conn)
  }
}