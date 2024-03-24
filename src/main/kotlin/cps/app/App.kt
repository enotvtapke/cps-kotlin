package cps.app
import cps.Result
import cps.app.Parser.Companion.app
import cps.app.Parser.Companion.pure
import cps.failure
import cps.parser.ParserM
import cps.parser.memoResult
import cps.success

data class StaticParser<S>(var eps: Boolean, var first: List<S>)

fun interface DynamicParser<S, A> {
  operator fun invoke(l: List<S>): Result<Pair<List<S>, A>>
}

class Parser<S, A>(val staticParser: StaticParser<S>, val dynamicParser: DynamicParser<S, A>) {
  operator fun invoke(l: List<S>): Result<Pair<List<S>, A>> = dynamicParser.invoke(l)

//  infix fun <U> bind(f: (T) -> ParserM<U, S>): ParserM<U, S> = ParserM { s1 -> this(s1).flatMap { (s2, t) -> f(t)(s2) } }

//  infix fun alt(p: ParserM<T, S>): ParserM<T, S> = ParserM { s -> this(s).orElse { p(s) }}

  infix fun alt(p: Parser<S, A>): Parser<S, A> = Parser(
    // TODO remove repetitions from this.staticParser.first + p.staticParser.first
    StaticParser(this.staticParser.eps || p.staticParser.eps, this.staticParser.first + p.staticParser.first),
    DynamicParser { s ->
      if (s.isEmpty()) {
        val r1 = if (this.staticParser.eps) this.dynamicParser(s) else failure()
        val r2 = if (p.staticParser.eps) p.dynamicParser(s) else failure()
        r1.orElse { r2 }
      } else {
//      if (s.isEmpty() && (this.staticParser.eps || p.staticParser.eps)) return@DynamicParser TODO()
        val first = s.first()
        val r1 = if (this.staticParser.eps || first in this.staticParser.first) this.dynamicParser(s) else failure()
        val r2 = if (p.staticParser.eps || first in p.staticParser.first) p.dynamicParser(s) else failure()
        r1.orElse { r2 }
      }
//      if (first in this.staticParser.first && first in p.staticParser.first) return@DynamicParser this.dynamicParser(s)
//      when(first) {
//        in this.staticParser.first -> this.dynamicParser(s)
//        in p.staticParser.first -> p.dynamicParser(s)
//        else -> failure()
//      }
    }
  )

  fun <B> map(f: (A) -> B): Parser<S, B> = pure<S, (A) -> B>(f) app this

  companion object {
    infix fun <S, A, B> Parser<S, (A) -> B>.app(p: Parser<S, A>): Parser<S, B> {
//      require(staticParser.eps && staticParser.first.isEmpty())
      return Parser(
        StaticParser(staticParser.eps && p.staticParser.eps, if (staticParser.eps) p.staticParser.first + staticParser.first else staticParser.first),
        DynamicParser { s1 -> this.dynamicParser(s1).flatMap { (s2, ff) -> p.dynamicParser(s2).map { (s3, t) -> Pair(s3, ff(t)) } } }
      )
    }

    fun <S, A> pure(a: A) = Parser<S, A>(StaticParser(true, listOf())) { success(Pair(it, a)) }
  }
}

typealias StringParser<A> = Parser<Char, A>

var t: HashMap<StringParser<*>, Map<List<Char>, Result<Pair<List<Char>, *>>>> = hashMapOf()

fun resetTable() {
  t = hashMapOf()
}

fun <A> memo(p: StringParser<A>): StringParser<A> {
  val table = t.getOrPut(p) { hashMapOf<List<Char>, Result<Pair<CharSequence, A>>>() as Map<List<Char>, Result<Pair<List<Char>, *>>> } as HashMap<List<Char>, Result<Pair<List<Char>, A>>>
//  val table = hashMapOf<CharSequence, Result<Pair<CharSequence, T>>>()
  return Parser(
    p.staticParser
  ) { s ->
    table.getOrPut(s) { memoResult { p.dynamicParser(s) } }
  }
}

//fun <T> fix(f: (StringParser<T>) -> StringParser<T>) = object : StringParser<T> {
//  private var r: StringParser<T>? = null
//
//  override fun invoke(s: CharSequence): Result<Pair<CharSequence, T>> {
//    if (r == null) r = f(this)
//    return r!!(s)
//  }
//}

fun term(t: Char): StringParser<String> = StringParser(StaticParser(false, listOf(t))) { s -> if (s.isNotEmpty() && s.first() == t) success(Pair(s.slice(1..<s.size), t.toString())) else failure() }

//fun term(term: CharSequence): ParserM<CharSequence, CharSequence> = ParserM { s -> if (s.startsWith(term)) success(Pair(s.removePrefix(term), term)) else failure() }

fun <S, T> fix(f: (DynamicParser<S, T>) -> DynamicParser<S, T>) = object : DynamicParser<S, T> {
  private var r: DynamicParser<S, T>? = null

  override fun invoke(l: List<S>): Result<Pair<List<S>, T>> {
    if (r == null) r = f(this)
    return r!!(l)
  }
}

fun <S, A> build(f: (Parser<S, A>) -> Parser<S, A>): Parser<S, A> {
  val p1 = f(Parser<S, A>(StaticParser(false, listOf())) { failure() })
  val p2 = f(Parser<S, A>(p1.staticParser) { failure() })
  val a: (DynamicParser<S, A>) -> DynamicParser<S, A> = { p -> f(Parser(p2.staticParser, p)).dynamicParser }
  val d = fix(a)
  return f(Parser(p1.staticParser, d))
}

//fun <S, A> build(f: (Parser<S, A?>) -> Parser<S, A?>): Parser<S, A?> {
//  val p = f(Parser(StaticParser(false, listOf())) { failure() })
//  return f(Parser(p.staticParser, p.dynamicParser))
//}


fun main() {
//  val a: StringParser<String> = (pure<Char, (String) -> (String) -> String> { b -> { it + b } } app term('a') app term('b')) alt term('c')
//  val res = a.dynamicParser(listOf('c'))
//  res {
//    println(it)
//  }

//  val cc: StringParser<String> = (pure<Char, (String) -> (String) -> String> { c -> { it + c } } app Parser() app term('c')) alt term('a')
  val p = build {
    memo((pure<Char, (String) -> (String) -> String> { c -> { a: String -> c + a } } app it app term('c')) alt term('a'))
  }
  val res = p.dynamicParser(listOf('a', 'c', 'c', 'c'))
  res {
    println(it)
  }

//  val x = pure<Char, String>("a")
//  (x.dynamicParser(listOf('a', 'x', 't'))){
//    println(it)
//  }
}
