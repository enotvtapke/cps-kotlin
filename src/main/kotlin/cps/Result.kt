package cps

import cps.Result.Companion.result

interface Result<T> {
  operator fun invoke(k: (T) -> Unit): Unit

  // >>
  fun <U> map(f: (T) -> U): Result<U> = result { k -> this { t -> k(f(t)) } }

  // >>=
  fun <U> flatMap(f: (T) -> Result<U>): Result<U> = result { k -> this { t -> f(t)(k) } }

  // ++
  fun orElse(r: () -> Result<T>): Result<T> = result { k -> this(k); r()(k) }

  companion object {
    fun <T> result(f: ((T) -> Unit) -> Unit): Result<T> =
      object : Result<T> {
        override fun invoke(k: (T) -> Unit) = f(k)
      }
  }
}

// return
fun <T> success(t: T): Result<T> = result { k -> k(t) }

// zero
fun <T> failure(): Result<T> = result { _ -> {} }
