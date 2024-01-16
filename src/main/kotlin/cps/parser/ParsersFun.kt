package cps.parser

import cps.Result
import cps.failure
import cps.success

typealias Parser <T> = ParserM<T, CharSequence>

fun interface ParserM <T, S> {
  operator fun invoke(s: S): Result<Pair<S, T>>


//  infix fun <U> map(f: (Pair<String, T>) -> Pair<String, U>): Parser<U> = Parser { s -> this(s).map(f)}
//  infix fun <U> map(f: (T) -> U): Parser<U> = map { pair: Pair<String, T> -> Pair(pair.first, f(pair.second)) }
  fun <U> map(f: (T) -> U): ParserM<U, S> = ParserM { s1 -> this(s1).map { (s2, t) -> Pair(s2, f(t)) } }

  infix fun <U> bind(f: (T) -> ParserM<U, S>): ParserM<U, S> = ParserM { s1 -> this(s1).flatMap { (s2, t) -> f(t)(s2) } }

  infix fun <U> bind(p: ParserM<U, S>): ParserM<U, S> = ParserM { s1 ->
    this(s1).flatMap { (s2, _) -> p(s2) }
  }

  infix fun alt(p: ParserM<T, S>): ParserM<T, S> = ParserM { s -> this(s).orElse { p(s) }}

  infix fun modify(f: (S) -> (S)): ParserM<T, S> = this.bind { t ->
    update(f).map { t }
  }

  companion object {
    fun <S> update(f: (S) -> (S)): ParserM<S, S> = ParserM { s -> success(Pair(f(s), s)) }
    fun <S> fetch(): ParserM<S, S> = update { it }

    fun <T, S> ret(t: T): ParserM<T, S> = ParserM { s -> success(Pair(s, t)) }
    fun <T, S> zero(): ParserM<T, S> = ParserM { failure() }
  }
}

fun term(term: CharSequence): ParserM<CharSequence, CharSequence> = ParserM { s -> if (s.startsWith(term)) success(Pair(s.removePrefix(term), term)) else failure() }

fun term(term: Regex): ParserM<CharSequence, CharSequence> = ParserM { s ->
  val matcher = term.toPattern().matcher(s)
  if (matcher.useAnchoringBounds(false).useTransparentBounds(true).region(0, s.length).lookingAt())
    success(Pair(s.subSequence(matcher.end(), s.length), matcher.toMatchResult().group()))
  else failure()
}

fun eps(): ParserM<CharSequence, CharSequence> = ParserM { s -> success(Pair(s, "")) }

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
