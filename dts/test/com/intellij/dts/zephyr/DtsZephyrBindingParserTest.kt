package com.intellij.dts.zephyr

import com.intellij.dts.DtsTestBase

class DtsZephyrBindingParserTest : DtsTestBase() {
    override fun setUp() {
        super.setUp()
        addZephyr()
    }

    private fun buildBinding(): DtsZephyrBinding {
        val provider = DtsZephyrBindingProvider.of(project)
        val binding = provider.buildBinding("espressif,esp32-pinctrl")

        assertNotNull(binding)
        return binding!!
    }

    fun `test child includes default properties`() {
        val binding = buildBinding()
        val childProperties = binding.child!!.properties.keys

        assertContainsElements(childProperties, "device_type", "interrupt-parent")
    }

    fun `test nested child includes default properties`() {
        val binding = buildBinding()
        val childProperties = binding.child!!.child!!.properties.keys

        assertContainsElements(childProperties, "device_type", "interrupt-parent")
    }

    fun `test property-allowlist filter`() {
        val binding = buildBinding()
        val childProperties = binding.child!!.child!!.properties.keys

        assertContainsElements(childProperties, "bias-disable", "output-low")
        assertDoesntContain(childProperties, "bias-bus-hold", "drive-strength")
    }
}