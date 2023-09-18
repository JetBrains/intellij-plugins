package com.intellij.dts.settings

import com.intellij.dts.DtsBundle
import com.intellij.dts.util.Either
import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.util.Alarm
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import java.util.concurrent.CancellationException
import javax.swing.JComponent

typealias Result<T> = Either<@Nls String, T>

abstract class DtsSettingsInputStatus<State, T>(private val disposable: Disposable?) {
    private val listeners = mutableListOf<(ValidationInfo?) -> Unit>()
    private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, disposable)

    var isEnabled = true

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
    protected open fun evaluate(state: State, result: Result<T>): ValidationInfo? {
        return result.fold({ left -> ValidationInfo(left) }, { null })
    }

    protected fun success(result: T): Result<T> = Either.Right(result)

    protected fun error(key: @PropertyKey(resourceBundle = DtsBundle.BUNDLE) String): Result<T> =
        Either.Left(DtsBundle.message(key))

    protected fun cancel(): Nothing = throw CancellationException()

    fun check() {
        if (!isEnabled) return

        alarm.cancelAllRequests()

        val state = readState()

        alarm.addRequest({
            val result = try {
                performCheck(state)
            } catch (e: CancellationException) {
                UIUtil.invokeLaterIfNeeded { notify(null) }
                return@addRequest
            }

            UIUtil.invokeLaterIfNeeded { notify(evaluate(state, result)) }
        }, 300)
    }

    private fun notify(info: ValidationInfo?) {
        if (!isEnabled) return

        for (listener in listeners) {
            listener(info)
        }
    }

    fun installOn(component: JComponent) {
        val disposable = this.disposable ?: return

        val validator = ComponentValidator(disposable).installOn(component)
        listeners.add { info ->
            validator.updateInfo(info?.forComponent(component))
        }
    }

    fun enableAndCheck() {
        isEnabled = true
        check()
    }
}