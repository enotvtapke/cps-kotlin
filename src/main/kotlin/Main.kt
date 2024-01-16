import cps.Result
import cps.failure
import cps.grammar.E
import cps.grammar.lama.Expr
import cps.grammar.lama.Prim
import cps.parser.*
import cps.parser.ParserM.Companion.fetch
import cps.parser.ParserM.Companion.update
import cps.success
import java.nio.CharBuffer.wrap
import kotlin.io.path.Path
import kotlin.io.path.writeText

fun <T> fix(f: (Parser<T>) -> Parser<T>) = object : Parser<T> {
  private var r: Parser<T>? = null

  override fun invoke(s: CharSequence): Result<Pair<CharSequence, T>> {
    if (r == null) r = f(this)
    return r!!(s)
  }
}

abstract class Par <T> : Parser<T> {
  private var r: Parser<T>? = null
  abstract val par: Parser<T>

  override fun invoke(s: CharSequence): Result<Pair<CharSequence, T>> {
    if (r == null) r = par
    return r!!(s)
  }
}

abstract class ParM <T, S> : ParserM<T, S> {
  private var r: ParserM<T, S>? = null
  abstract val par: ParserM<T, S>

  override fun invoke(s: S): Result<Pair<S, T>> {
    if (r == null) r = par
    return r!!(s)
  }
}

object CCC: Par<CharSequence>() {
  override val par: Parser<CharSequence>
    get() = memo((CCC bind term("c")) alt term("$"))
}

fun term1(term: CharSequence): ParserM<CharSequence, Pair<CharSequence, Int>> = ParserM { s -> if (s.first.startsWith(term)) success(Pair(Pair(s.first.removePrefix(term), s.second), term)) else failure() }

object CCCState: ParM<CharSequence, Pair<CharSequence, Int>>() {
  override val par: ParserM<CharSequence, Pair<CharSequence, Int>>
    get() = memom((CCCState modify {
      s -> Pair(s.first, s.second + 1)
    } bind { c ->
      fetch<Pair<CharSequence, Int>>() bind { s ->
        if (s.second % 2 == 0) term1("c").map { (c.toString() + it.toString()) as CharSequence } else term1("a").map { (c.toString() + it.toString()) as CharSequence }
      }
    }) alt term1("$"))
}

object CCCStateFail: ParM<CharSequence, Pair<CharSequence, Int>>() {
  override val par: ParserM<CharSequence, Pair<CharSequence, Int>>
    get() = memom((
            update<Pair<CharSequence, Int>> { s -> Pair(s.first, (s.second + 1) % 2) } bind CCCStateFail
            bind { c ->
              fetch<Pair<CharSequence, Int>>() bind { s ->
                if (s.second % 2 == 0) term1("c").map { (c.toString() + it.toString()) as CharSequence } else term1("a").map { (c.toString() + it.toString()) as CharSequence }
              }
            }
    ) alt term1("$"))
}

object Test: ParM<CharSequence, Pair<CharSequence, Int>>() {
  override val par: ParserM<CharSequence, Pair<CharSequence, Int>>
    get() = ParserM.ret<CharSequence, Pair<CharSequence, Int>>("a") modify { s -> Pair("a", 2) }
}


//object Test: ParM<CharSequence, Pair<CharSequence, Int>>() {
//  override val par: ParserM<CharSequence, Pair<CharSequence, Int>>
//    get() = ParserM.ret<CharSequence, Pair<CharSequence, Int>>("a") bind { t ->
//      ParserM { s -> success(Pair(Pair("a", 2), "b")) }
//    }
//}
sealed interface C
data class CCont(val cc: C, val c: String): C
data class CEnd(val a: String): C

fun main() {
//  runParser(
//    Expr,
//    "1+((1*2)-2)"
//  )

  runParser(
    E,
    "(1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+(1+(1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+((1+1+(1+2)*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2))*2)"
  )

//  runParser(CCCStateFail, Pair("\$" + "cc".repeat(1), 0))
//  runParser(CCC, "\$cccccc")


//  runParser(
//    fix { ccc: Parser<C> ->
//      memo(((ccc bind { cc -> term("c").map { CCont(cc, it.toString()) as C } }) alt term("a").map { CEnd("a") }))
//    },
//    "a" + "c".repeat(3000)
//  )
//
//  println(runParser(
//    fix { ccc: Parser<(C) -> C> ->
//      memo(((ccc bind { cc -> term("c").map { { p: C -> CCont(cc(p), it.toString()) as C } } }) alt term("a").map { { p -> CEnd("a" + (p as CEnd).toString()) } }))
//    },
//    "a" + "c".repeat(3000)
//  )!!(CEnd("p")).toString())
//
//  runParser(
//    fix { pali: Parser<CharSequence> ->
//      memo(
//        (term("a") bind pali bind term("a")) alt
//        (term("b") bind pali bind term("b")) alt
//        (term("c") bind pali bind term("c")) alt
//        term("d")
//      )
//    },
//    "ada" + ("a".repeat(2) + "b".repeat(42) + "c".repeat(80)).repeat(100) + "d" + ("c".repeat(80) + "b".repeat(42) + "a".repeat(2)).repeat(100)
//  )

//  runParser(
//    fix { sss: Parser<CharSequence> ->
//      memo(
//        (term(""))
//      )
//    }
//  )
}

private fun <T, S> runParser(p: ParserM<T, S>, input: S): T? {
  var m = 0
  var res1: T? = null
  (p(input)) { res ->
    println("Success: $res")
    m += 1
    res1 = res.second
  }
  Path("./out.txt").writeText(res1.toString())
  println("Num of results: $m")
  return res1
}

private fun <T> runParser(p: Parser<T>, input: CharSequence): T? {
  var m = 0
  var res1: T? = null
  (p(wrap(input))) { res ->
//    println("Success: $res")
    m += 1
    res1 = res.second
  }
  Path("./out.txt").writeText(res1.toString())
  println("Num of results: $m")
  return res1
}

