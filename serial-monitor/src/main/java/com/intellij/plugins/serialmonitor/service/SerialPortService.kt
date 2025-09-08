package com.intellij.plugins.serialmonitor.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.UI
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.getOrHandleException
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.SerialMonitorCollector
import com.intellij.plugins.serialmonitor.SerialMonitorException
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.service.SerialPortsListener.Companion.SERIAL_PORTS_TOPIC
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.TestOnly
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

@Service
class SerialPortService(val cs: CoroutineScope) : Disposable.Default {

  @Volatile var serialPortProvider: SerialPort.SerialPortProvider = JsscSerialPort.Provider
    private set
  private val portNames: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
  private val connections: MutableMap<String, SerialConnection> = ConcurrentHashMap()

  init {
    cs.launch(CoroutineName("Serial Port Watcher")) {
      while(true) {
        rescanPorts()
        delay(500)
      }
    }
    cs.launch {
      portNames.collect {
        portMessageTopic().portsStatusChanged()
      }
    }
  }

  private val NAME_COMPARATOR = object : Comparator<String> {

    private fun splitName(s: String): Pair<String, Int> {
      var digitIdx = s.length - 1
      var num = 0
      var mul = 1
      while (digitIdx >= 0 && s[digitIdx].isDigit()) {
        num += mul * (s[digitIdx] - '0')
        mul *= 10
        digitIdx--
      }
      return s.substring(0, digitIdx + 1) to num
    }

    override fun compare(name1: String, name2: String): Int {
      val (base1, idx1) = splitName(name1)
      val (base2, idx2) = splitName(name2)
      val result = String.CASE_INSENSITIVE_ORDER.compare(base1, base2)
      return if (result == 0) idx1 - idx2 else result
    }
  }

  private suspend fun scanPorts(): Set<String> = withContext(Dispatchers.IO) {
    serialPortProvider.scanAvailablePorts().toCollection(TreeSet(NAME_COMPARATOR))
  }

  private suspend fun rescanPorts() {
    val portList = scanPorts()
    for (name in portNames.value) {
      if (!portList.contains(name)) {
        //port disappeared
        connections[name]?.closeSilently(false)
      }
    }
    for (name in portList) {
      if (connections[name]?.getStatus() == PortStatus.UNAVAILABLE_DISCONNECTED) {
        connections[name]?.setStatus(PortStatus.DISCONNECTED)
      }
    }
    portNames.emit(portList)
  }

  private fun portMessageTopic(): SerialPortsListener =
    ApplicationManager.getApplication().messageBus.syncPublisher(SERIAL_PORTS_TOPIC)

  fun getPortsNames(): Set<String> = portNames.value

  fun newConnection(portName: String): SerialConnection {
    connections[portName]?.also { Disposer.dispose(it) }
    val serialConnection = SerialConnection(portName)
    connections[portName] = serialConnection
    return serialConnection
  }

  fun portStatus(portName: String?): PortStatus {
    if (!portNames.value.contains(portName)) {
      return if (connections.containsKey(portName)) PortStatus.UNAVAILABLE_DISCONNECTED else PortStatus.UNAVAILABLE
    }
    return connections[portName]?.getStatus() ?: PortStatus.READY
  }

  fun portDescriptiveName(portName: String): @Nls String? {
    @NlsSafe val description = runCatching {
      serialPortProvider.createPort(portName).getDescriptiveName()
    }.getOrNull()
    return description
  }


  inner class SerialConnection(val portName: @NlsSafe String) : Disposable {

    var dataListener: Consumer<ByteArray>? = null

    var dsrListener: ((Boolean)->Unit)? = null
    var ctsListener: ((Boolean)->Unit)? = null

    @Volatile
    private var port: SerialPort? = null

    @Volatile
    private var status: PortStatus = PortStatus.DISCONNECTED

    private var localEcho: Boolean = false

    var rts: Boolean = true
      @Throws(SerialMonitorException::class)
      set(value) {
        runCatching {
          port?.setRTS(value)
        }.onFailure {
          throw SerialMonitorException(SerialMonitorBundle.message("port.modify.error", portName, it.message))
        }
        field = value
      }

    var dtr: Boolean = true
      @Throws(SerialMonitorException::class)
      set(value) {
        runCatching {
          port?.setDTR(value)
        }.onFailure {
          throw SerialMonitorException(SerialMonitorBundle.message("port.modify.error", portName, it.message))
        }
        field = value
      }

    val cts: Boolean
      get() = runCatching {
        port?.getCTS()
      }.getOrHandleException(thisLogger()::info) ?: false


    val dsr: Boolean
      get() = runCatching {
        port?.getDSR()
      }.getOrHandleException(thisLogger()::info) ?: false

    override fun dispose() {
      closeSilently(true)
      connections.remove(portName, this)
      cs.launch {
        rescanPorts()
      }
    }

    fun getStatus(): PortStatus = status
    internal fun setStatus(value: PortStatus) {
      status = value
    }

    @Throws(SerialMonitorException::class)
    fun close(portAvailable: Boolean) {
      try {
        port?.disconnect()
      }
      catch (e: SerialPortException) {
        throw SerialMonitorException(SerialMonitorBundle.message("port.close.error", portName, e.message))
      }
      finally {
        status = if (portAvailable) PortStatus.DISCONNECTED else PortStatus.UNAVAILABLE_DISCONNECTED
        port = null
        portMessageTopic().portsStatusChanged()
      }
    }

    fun closeSilently(portAvailable: Boolean) {
      try {
        close(portAvailable)
      }
      catch (_: SerialMonitorException) {
      }
    }

    private val listener = object : SerialPort.SerialPortListener {
      override fun onDataReceived(data: ByteArray) {
        dataListener?.accept(data)
      }

      override fun onCTSChanged(state: Boolean) {
        cs.launch(Dispatchers.UI) {
          ctsListener?.invoke(state)
        }
      }

      override fun onDSRChanged(state: Boolean) {
        cs.launch(Dispatchers.UI) {
          dsrListener?.invoke(state)
        }
      }
    }

    @RequiresBackgroundThread
    @Throws(SerialMonitorException::class)
    fun connect(profile: SerialPortProfile) {
      this.status = PortStatus.CONNECTING
      this.localEcho = profile.localEcho

      lateinit var newPort: SerialPort
      try {
        newPort = serialPortProvider.createPort(portName)
        portMessageTopic().portsStatusChanged()

        newPort.connect(profile, listener, rts, dtr)

        port = newPort
        SerialMonitorCollector.logConnect(profile.baudRate, true)
        status = PortStatus.CONNECTED
        portMessageTopic().portsStatusChanged()
      }
      catch (e: Exception) {
        SerialMonitorCollector.logConnect(profile.baudRate, false)

        try {
          newPort.disconnect()
        }
        catch (_: SerialPortException) {
        }

        status = PortStatus.UNAVAILABLE_DISCONNECTED
        portMessageTopic().portsStatusChanged()

        throw SerialMonitorException(SerialMonitorBundle.message("port.connect.error", portName, e.message))
      }
    }

    fun write(data: ByteArray) {
      port?.write(data)
      if (localEcho) {
        dataListener?.accept(data)
      }
    }
  }

  @TestOnly
  internal suspend fun setPortProvider(provider: SerialPort.SerialPortProvider) {
    serialPortProvider = provider
    rescanPorts()
  }
}
