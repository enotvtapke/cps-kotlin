package cps.app

import cps.app.Expr.E
import cps.app.Parser.Companion.app
import cps.app.Parser.Companion.pure
import kotlin.streams.toList

object Expr {
  sealed interface Expr
  data class ExprOp(val left: Expr, val right: Term, val op: String) : Expr {
    override fun toString(): String {
//    return "Expr('$op', $left, $right)"
      return "$left$op$right"
    }
  }

  data class ExprVal(val term: Term) : Expr {
    override fun toString() = term.toString()
  }

  sealed interface Term
  data class TermOp(val left: Term, val right: FF, val op: String) : Term {
    override fun toString(): String {
//    return "Term('$op', $left, $right)"
      return "$left$op$right"
    }
  }

  data class TermVal(val ff: FF) : Term {
    override fun toString() = ff.toString()
  }

  sealed interface FF
  data class FFExpr(val expr: Expr) : FF {
    override fun toString() = "($expr)"
  }

  data class FFVal(val n: Int) : FF {
    override fun toString(): String {
      return "$n"
    }
  }

  var e = false
  var t = false
  var f = false

  fun E(): Parser<Char, Expr> = if (e) build { it } else {
    e = true
    build<Char, Expr> {
      memo<Expr>(
        (pure<Char, (Expr) -> (String) -> (Term) -> Expr> { l -> { op -> { r: Term -> ExprOp(l, r, op)}} } app it app term('+') app T()) alt T().map { ExprVal(it) }
      )
    }
  }

  fun T(): Parser<Char, Term> = build<Char, Term> {
    memo(
      (pure<Char, (Term) -> (String) -> (FF) -> Term> { l -> { op -> { r: FF -> TermOp(l, r, op)}} } app it app term('*') app F()) alt F().map { TermVal(it) }
    )
  }

  fun F(): Parser<Char, FF> = build<Char, FF> {
    (pure<Char, (String) -> (Expr) -> (String) -> FF> { _ -> { e: Expr -> { _ -> FFExpr(e)}} } app term('(') app E() app term(')')) alt term('1').map { FFVal(it.toInt()) }
  }
}

fun main() {
//  E()
  (E().dynamicParser("(1)".chars().toList().map { it.toChar() })) {
    println(it)
  }
}