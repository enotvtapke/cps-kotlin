package cps.parser

import cps.Result
import cps.failure
import cps.success

//typealias Parser <T> = (String) -> Result<T>

fun interface Parser <T> {
  operator fun invoke(s: CharSequence): Result<Pair<CharSequence, T>>


//  infix fun <U> map(f: (Pair<String, T>) -> Pair<String, U>): Parser<U> = Parser { s -> this(s).map(f)}
//  infix fun <U> map(f: (T) -> U): Parser<U> = map { pair: Pair<String, T> -> Pair(pair.first, f(pair.second)) }
  fun <U> map(f: (T) -> U): Parser<U> = Parser { s1 -> this(s1).map { (s2, t) -> Pair(s2, f(t)) } }

  infix fun <U> bind(f: (T) -> Parser<U>) = Parser { s1 -> this(s1).flatMap { (s2, t) -> f(t)(s2) } }

  infix fun <U> bind(p: Parser<U>) = Parser { s1 -> this(s1).flatMap { (s2, _) -> p(s2) } }

  infix fun alt(p: Parser<T>) = Parser { s -> this(s).orElse { p(s) }}

  companion object {
    fun <T> ret(t: T): Parser<T> = Parser { s -> success(Pair(s, t)) }
    fun <T> zero(): Parser<T> = Parser { failure() }
  }
}

fun term(term: CharSequence): Parser<CharSequence> = Parser { s -> if (s.startsWith(term)) success(Pair(s.removePrefix(term), term)) else failure() }

fun term(term: Regex): Parser<CharSequence> = Parser { s ->
  val matcher = term.toPattern().matcher(s)
  if (matcher.useAnchoringBounds(false).useTransparentBounds(true).region(0, s.length).lookingAt())
    success(Pair(s.subSequence(matcher.end(), s.length), matcher.toMatchResult().group()))
  else failure()
}

fun eps(): Parser<String> = Parser { s -> success(Pair(s, "")) }

//fun seq(vararg ps: Parser<String>): Parser<String> =
//  ps.reduce { acc, p ->
//    Parser { i -> acc(i).flatMap(p::invoke) }
//  }
//
//fun rule(vararg alts: Parser<String>): Parser<String> = memo(
//  alts.reduce { acc, p ->
//    Parser { i -> acc(i).orElse { p(i) } }
//  }
//)
