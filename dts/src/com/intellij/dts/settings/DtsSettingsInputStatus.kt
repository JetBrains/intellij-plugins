package com.intellij.dts.settings

import com.intellij.dts.DtsBundle
import com.intellij.dts.util.Either
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.SimpleColoredComponent
import com.intellij.util.Alarm
import com.intellij.util.ui.CenteredIcon
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import kotlin.coroutines.cancellation.CancellationException

// from com.jetbrains.cidr.cpp.ui.UIUtil.kt
private val OK_ICON = CenteredIcon(AllIcons.General.InspectionsOK, 16, 16, false)
private val ERROR_ICON = AllIcons.General.Error
private val WARNING_ICON = AllIcons.General.BalloonWarning

typealias Result<T> = Either<@Nls String, T>

abstract class DtsSettingsInputStatus<State, T>(disposable: Disposable?) : SimpleColoredComponent() {
    private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, disposable)

    /**
     * Executed on UI thread.
     *
     * Read the current state of the UI to check in [performCheck].
     */
    protected abstract fun readState(): State

    /**
     * Executed on background.
     *
     * Check if the state from [readState] is valid.
     */
    protected abstract fun performCheck(state: State): Result<T>

    /**
     * Executed on UI thread.
     *
     * Evaluate the result of [performCheck] and update UI.
     */
    protected open fun evaluate(state: State, result: Result<T>) {}

    protected fun success(result: T): Result<T> = Either.Right(result)

    protected fun error(key: @PropertyKey(resourceBundle = DtsBundle.BUNDLE) String): Result<T> =
        Either.Left(DtsBundle.message(key))

    protected fun cancel(key: @PropertyKey(resourceBundle = DtsBundle.BUNDLE) String): Nothing =
        throw CancellationException(DtsBundle.message(key))

    fun check() {
        alarm.cancelAllRequests()

        clear()
        icon = AnimatedIcon.Default()

        val state = readState()

        alarm.addRequest({
            val result = try {
                performCheck(state)
            } catch (e: CancellationException) {
                UIUtil.invokeLaterIfNeeded {
                    icon = WARNING_ICON
                    e.message?.let(::append)
                }

                return@addRequest
            }

            UIUtil.invokeLaterIfNeeded {
                icon = result.fold({ ERROR_ICON }, { OK_ICON })
                append(result.fold({ it }, { DtsBundle.message("settings.valid" )}))

                evaluate(state, result)
            }
        }, 300)
    }
}