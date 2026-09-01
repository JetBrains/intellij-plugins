package com.jetbrains.lang.makefile

import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
internal class MakefileCoroutineScopeProvider(val coroutineScope: CoroutineScope)
