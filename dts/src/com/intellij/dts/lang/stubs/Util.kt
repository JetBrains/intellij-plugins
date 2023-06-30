package com.intellij.dts.lang.stubs

import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream

internal fun StubOutputStream.writeUTFList(list: List<String>) {
    writeInt(list.size)

    for (item in list) {
        writeUTF(item)
    }
}

internal fun StubInputStream.readUTFList(): List<String> {
    val size = readInt()
    return List(size) { readUTF() }
}