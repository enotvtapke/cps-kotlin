package cps.parser

import cps.Result
import cps.Result.Companion.result
import java.util.HashMap

fun <T, S> memom(p: ParserM<T, S>): ParserM<T, S> {
  val table = hashMapOf<S, Result<Pair<S, T>>>()
  return ParserM { s ->
    table.getOrPut(s) { memoResult { p(s) } }
  }
}

val t: HashMap<ParserM<*, CharSequence>, Map<CharSequence, Result<Pair<CharSequence, *>>>> = hashMapOf()

fun <T> memo(p: Parser<T>): Parser<T> {
  val table = t.getOrPut(p) { hashMapOf<CharSequence, Result<Pair<CharSequence, T>>>() as Map<CharSequence, Result<Pair<CharSequence, *>>> } as HashMap<CharSequence, Result<Pair<CharSequence, T>>>
//  val table = hashMapOf<CharSequence, Result<Pair<CharSequence, T>>>()
  return Parser { s ->
    table.getOrPut(s) { memoResult { p(s) } }
  }
}

fun <T> memoResult(res: () -> Result<T>): Result<T> {
  val rs = mutableSetOf<T>()
  val ks = mutableListOf<(T) -> Unit>()

  return result { k ->
    if (ks.isEmpty()) {
      ks += k
      (res()) { t ->
        if (!rs.contains(t)) {
          rs += t
          var i = 0
          val size = ks.size
          while (i < size) {
            ks[i](t)
            i++
          }
//          for (i in 0..<ks.size) ks[i](t)
//          ks.forEach { it(t) }
        }
      }
    } else {
      ks += k
      var i = 0
      val size = rs.size
//      while (i < size) {
//        k(rs[i])
//        i++
//      }
      rs.forEach { k(it) }
    }
  }
}
