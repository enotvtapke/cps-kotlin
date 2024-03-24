package cps.grammar

import Par
import cps.parser.Parser
import cps.parser.memo
import cps.parser.term
import runParser

object Exponential {
  data object E : Par<CharSequence>() {
    override val par: Parser<CharSequence> =
      memo(
        (E bind { e -> term("e").map { (e.toString() + it) as CharSequence } })
                alt
                F
      )
  }

  data object F : Par<CharSequence>() {
    override val par: Parser<CharSequence> =
      memo(
        (term("(") bind E bind { e -> term(")").map { "($e)" as CharSequence } }) alt term("f")
      )
  }
}

private fun gen(n: Int) = "(".repeat(n) + "f" + ")".repeat(n)

fun main() {
  for (i in 0..100) {
//    counter = 0
    println("$i")
    println(runParser(Exponential.F, gen(i)))
  }
}
