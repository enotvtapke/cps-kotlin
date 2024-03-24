package cps.grammar

import Par
import cps.parser.Parser
import cps.parser.eps
import cps.parser.memo
import cps.parser.term
import runParser

data object Poly : Par<CharSequence?>() {
  override val par: Parser<CharSequence?> =
    memo(
      eps<CharSequence, CharSequence>().map { it ?: "" as CharSequence? }
              alt (term("a") bind { a -> Poly bind { p -> term("a").map { a.toString() + p + it } } })
              alt (term("b") bind { a -> Poly bind { p -> term("b").map { a.toString() + p + it } } })
              alt (term("c") bind { a -> Poly bind { p -> term("c").map { a.toString() + p + it } } })
    )
}

fun main() {
  val s = "a".repeat(10) + "b".repeat(15) + "c".repeat(20)
  println(runParser(Poly, s + s.reversed()))
}
