package cps

import cps.Result.Companion.result

interface Result<T> {
  operator fun invoke(k: (T) -> Unit): Unit

  // >>=
  fun <U> flatMap(f: (T) -> Result<U>): Result<U> = result { k -> this { t -> f(t)(k) } }

  // ++
  fun orElse(r: () -> Result<T>): Result<T> = result { k -> this(k); r()(k) }

  companion object {
    // return
    fun <T> result(f: ((T) -> Unit) -> Unit): Result<T> =
      object : Result<T> {
        override fun invoke(k: (T) -> Unit) = f(k)
      }
  }
}

fun <T> success(t: T): Result<T> = result { k -> k(t) }

// zero
fun <T> failure(): Result<T> = result { _ -> {} }
