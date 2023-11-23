package com.intellij.dts.lang

import com.intellij.openapi.util.NlsSafe

enum class DtsPropertyType(val typeName: @NlsSafe kotlin.String) {
  /**
   * Exactly one string.
   * - Zephyr type: string
   * - Example: status = "disabled";
   */
  String("string"),

  /**
   * Exactly one 32-bit value (cell).
   * Zephyr type: int
   * Example: current-speed = <115200>;
   */
  Int("int"),

  /**
   * Exactly one phandle value (cell).
   * Zephyr type: phandle
   * Example: interrupt-parent = <&gic>;
   */
  PHandle("phandle"),

  /**
   * Flags that don’t take a value when true, and are absent if false.
   * Zephyr type: boolean
   * Example: hw-flow-control;
   */
  Boolean("boolean"),

  /**
   * Zero or more 32-bit values (cells), can be split into several <> blocks.
   * Zephyr type: array
   * Example: offsets = <0x100 0x200 0x300>;
   */
  Ints("array"),

  /**
   * Zero or more bytes (byte).
   * Zephyr type: uint8-array
   * Example: local-mac-address = [de ad be ef 12 34];
   */
  Bytes("uint8-array"),

  /**
   * Zero or more phandles (cell), can be split into several <> blocks.
   * Zephyr type: phandles
   * Example: pinctrl-0 = <&usart2_tx_pd5 &usart2_rx_pd6>;
   */
  PHandles("phandles"),

  /**
   * A list of strings.
   * Zephyr type: string-array
   * Example: dma-names = "tx", "rx";
   */
  StringList("string-array"),

  /**
   * A list of phandles and 32-bit cells (usually specifiers), can be split
   * into several <> blocks.
   * Zephyr type: phandle-array
   * Example: dmas = <&dma0 2>, <&dma0 3>;
   */
  PHandleList("phandle-array"),

  /**
   * A path to a node as a phandle path reference or path string.
   * Zephyr type: path
   * Example: zephyr,bt-c2h-uart = &uart0; or foo = "/path/to/some/node";
   */
  Path("path"),

  /**
   * A catch-all for more complex types.
   * Zephyr type: compound
   * Example: foo = <&label>, [01 02];
   */
  Compound("compound");

  companion object {
    fun fromZephyr(type: kotlin.String?): DtsPropertyType {
      for (entry in entries) {
        if (entry.typeName == type) return entry
      }

      return Compound
    }
  }
}