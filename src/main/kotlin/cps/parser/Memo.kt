package cps.parser

import cps.Result
import cps.Result.Companion.result

fun <T> memo(p: Parser<T>): Parser<T> {
  val table = hashMapOf<String, Result<T>>()
  return { s ->
    table.getOrPut(s) { memoResult { p(s) } }
  }
}

fun <T> memoResult(res: () -> Result<T>): Result<T> {
  val rs = mutableListOf<T>()
  val ks = mutableListOf<(T) -> Unit>()

  return result { k ->
    if (ks.isEmpty()) {
      ks += k
      (res()) { t ->
        if (!rs.contains(t)) {
          rs += t
          ks.forEach { it(t) }
        }
      }
    } else {
      ks += k
      rs.forEach { k(it) }
    }
  }
}
