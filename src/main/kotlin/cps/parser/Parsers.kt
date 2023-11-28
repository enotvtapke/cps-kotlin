package cps.parser

import cps.Result
import cps.failure
import cps.success

fun interface Parser<T> {
  operator fun invoke(s: String): Result<T>
}

class Term(private val term: String) : Parser<String> {
  override fun invoke(s: String): Result<String> = if (s.startsWith(term)) success(s.removePrefix(term)) else failure()
}

class Seq(private val ps: List<Parser<String>>) : Parser<String> {
  override fun invoke(s: String): Result<String> = ps.reduce { acc, p ->
    Parser { i -> acc(i).flatMap(p::invoke) }
  }(s)
}
