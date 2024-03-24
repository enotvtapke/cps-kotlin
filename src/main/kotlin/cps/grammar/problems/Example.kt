package cps.grammar.problems

import Par
import cps.Result
import cps.parser.Parser
import cps.parser.ParserM
import cps.parser.memo
import cps.parser.term
import fix
import runParser

fun infixBin(operand: Parser<CharSequence>, operation: Parser<CharSequence>): ParserM<CharSequence, CharSequence> = operand bind { l -> operation bind { op -> operand.map { r -> l.toString() + op + r } }}

fun asda() {
  fix {
    memo(infixBin(it, term("+")) alt term("1"))
  }
//  infixBin(infixBin(term("1"), term("*")), term("+"))
  b(a(term("c"), 2), 2)
}

fun a(p: ParserM<CharSequence, CharSequence>, n: Int): ParserM<CharSequence, CharSequence> =
  memo { s ->
    (b(p, n) bind List(n) { term("a") }.reduce { a, b -> a bind { aa -> b.map { aa.toString() + it } } } alt term("A"))(s)
  }

//fun b(n: Int): ParserM<CharSequence, CharSequence> =
//  memo { s ->
//    (a(n) bind List(n) { term("b") }.reduce { a, b -> a bind { aa -> b.map { aa.toString() + it } } } alt term("B"))(s)
//  }

fun b(p: ParserM<CharSequence, CharSequence>, n: Int): ParserM<CharSequence, CharSequence> =
  memo { s ->
    (p bind List(n) { term("b") }.reduce { a, b -> a bind { aa -> b.map { aa.toString() + it } } } alt term("B"))(s)
  }

fun c(p: ParserM<CharSequence, CharSequence>) = memo(p bind { pp -> term("c").map { (pp.toString() + it) as CharSequence } })

data object C : Par<CharSequence>() {
  override val par: Parser<CharSequence>
    get() = memo(c(C) bind { cc -> term("c").map { (cc.toString() + it) as CharSequence } } alt term("C"))
}

private fun <T, S> memoize(p: ParserM<CharSequence, CharSequence>) {

}

fun cc(n: Int) = object : ParserM<CharSequence, CharSequence> {
  private val pp = memo((this bind { pp ->
    List(n) { term("b") }.reduce { a, b -> a bind { aa -> b.map { aa.toString() + it } } }
      .map { (pp.toString() + it) as CharSequence }
  }) alt term("a"))
  override fun invoke(s: CharSequence): Result<Pair<CharSequence, CharSequence>> = pp(s)
}

//data object P: ParserM<CharSequence, CharSequence> {
//  private val pp = memo((P bind { pp ->
//    List(2) { term("b") }.reduce { a, b -> a bind { aa -> b.map { aa.toString() + it } } }
//      .map { (pp.toString() + it) as CharSequence }
//  }) alt term("a"))
//  override fun invoke(s: CharSequence): Result<Pair<CharSequence, CharSequence>> = pp(s)
//}

//val pp:ParserM<CharSequence, CharSequence> = memo((pp bind { pp ->
//  List(2) { term("b") }.reduce { a, b -> a bind { aa -> b.map { aa.toString() + it } } }
//    .map { (pp.toString() + it) as CharSequence }
//}) alt term("a"))

fun p(n: Int): Parser<CharSequence> = memo(object : ParserM<CharSequence, CharSequence> {
  override fun invoke(s: CharSequence): Result<Pair<CharSequence, CharSequence>> =
    (ParserM<CharSequence, CharSequence> { ss ->
      (p(n) bind { pp ->
        List(n) { term("b") }.reduce { a, b -> a bind { aa -> b.map { aa.toString() + it } } }.map { (pp.toString() + it) as CharSequence }
      })(ss)
    } alt term("a"))(s)

  override fun equals(other: Any?): Boolean {
    return true
  }

  override fun hashCode(): Int {
    return 1
  }
}
)

fun f(n: Int): Parser<CharSequence> = memo(g(n) alt term("f"))
fun g(n: Int): Parser<CharSequence> = memo(f(n) alt term("f"))

fun main() {
//  println(runParser(a(5), "Baaaaa"))

//  val p = fix {
//    memo(infixBin(it, term("+")) alt term("1"))
//  }
//  println(runParser(p, "1+1+1"))

  fun seq(p1: Parser<CharSequence>, p2: Parser<CharSequence>) = memo(p1 bind { pp1 -> term(";") bind p2.map { pp2 -> "$pp1 $pp2" as CharSequence } })
//  fun stmt(x: Parser<CharSequence>) = Parser<CharSequence> { s ->
//      fix {
//        seq(it, it) alt (x bind { x -> term(":=1").map { "$x$it" } })
//      }(s)
//    }
//  println(runParser(stmt(term("x")), "x:=1;x:=1"))

  val stmt = Parser<CharSequence> { s ->
    fix {
      seq(it, it) alt term("x:=1")
    }(s)
  }

  fun stmt(): Parser<CharSequence> {
    return Parser<CharSequence> { s ->
      (seq(stmt(), stmt()) alt term("x:=1"))(s)
    }
  }
  println(runParser(stmt(), "x:=1;x:=1"))

//  println(runParser(cc(2), "abbbbbb"))

//  val a1 = a(5)
//  val b1 = b(5)
//  println(runParser(a(5), "Baaaaa"))
//  val v = "c".repeat(50000)
//  println(1)
//  println(runParser(C, "C" + v).size)
}
