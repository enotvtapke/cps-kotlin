package cps.grammar

import Par
import cps.parser.Parser
import cps.parser.memo
import cps.parser.term
import runParser

object NaiveExpr {
  sealed interface Expr
  data class ExprOp(val left: Expr, val right: Expr, val op: String) : Expr {
    override fun toString(): String {
//    return "Expr('$op', $left, $right)"
      return "$left$op$right"
    }
  }

  data class ExprVal(val n: Int) : Expr {
    override fun toString() = "$n"
  }

  object E : Par<Expr>() {
    override val par: Parser<Expr>
      get() = memo(
        (E bind { e -> (term("+") alt term("*")) bind { op -> E.map { t -> ExprOp(e, t, op.toString()) as Expr } } })
                alt ((term("\\d+".toRegex())).map { ExprVal(it.toString().toInt()) })
      )
  }
}

fun main() {
  println(runParser(NaiveExpr.E, "1+1*1+1+1*1+1"))
}