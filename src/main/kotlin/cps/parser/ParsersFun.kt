package cps.parser

import cps.Result
import cps.failure
import cps.success

typealias Parser <T> = (String) -> Result<T>

fun term(term: String): Parser<String> = { s -> if (s.startsWith(term)) success(s.removePrefix(term)) else failure() }

fun eps(): Parser<String> = { s -> success(s) }

fun seq(vararg ps: Parser<String>): Parser<String> =
  ps.reduce { acc, p ->
    { i -> acc(i).flatMap(p) }
  }

fun rule(vararg alts: Parser<String>): Parser<String> = memo(
  alts.reduce { acc, p ->
    { i -> acc(i).orElse { p(i) } }
  }
)
