package com.intellij.dts.util

/**
 * Represents values with two possibilities: a value of type Either<L, R> is
 * either Left<L> or Right<R> - a common construct in functional programming.
 *
 * The type is sometimes used to represent a value which is either correct or an
 * error. By convention, Left is used to hold an error value and Right is used
 * to hold a correct value.
 */
sealed class Either<out L, out R> {
  data class Left<out L>(val value: L) : Either<L, Nothing>()
  data class Right<out R>(val value: R) : Either<Nothing, R>()

  fun <S> fold(onLeft: (L) -> S, onRight: (R) -> S): S = when (this) {
    is Left<L> -> onLeft(value)
    is Right<R> -> onRight(value)
  }

  fun <S> mapRight(mapping: (R) -> S): Either<L, S> = when (this) {
    is Left<L> -> this
    is Right<R> -> Right(mapping(value))
  }

  fun <S> mapLeft(mapping: (L) -> S): Either<S, R> = when (this) {
    is Left<L> -> Left(mapping(value))
    is Right<R> -> this
  }
}