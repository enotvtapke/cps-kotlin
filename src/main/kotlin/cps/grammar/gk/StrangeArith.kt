package cps.grammar.gk

import Par
import cps.parser.Parser
import cps.parser.memo
import cps.parser.term

sealed interface Expr
data class ExprTerm(val t: Term) : Expr
data class ExprSum(val x: Expr, val y: Term) : Expr
data class ExprSub(val x: Expr, val y: Term) : Expr

sealed interface Term
data class TermVal(val v: Val) : Term
data class TermMul(val x: Term, val y: Val) : Term

sealed interface Val
data class ValNumber(val n: Int) : Val
data class ValPar(val expr: Expr) : Val

object F : Par<Val>() {
  override val par: Parser<Val>
    get() = memo(
      ((term("\\d".toRegex())).map { ValNumber(it.toString().toInt()) as Val }) alt
              (term("(") bind E bind { e -> term(")").map { ValPar(e) } })
    )
}

object T : Par<Term>() {
  override val par: Parser<Term>
    get() = memo(
      F.map { TermVal(it) as Term } alt
              (T bind { t -> term("*") bind { F.map { f -> TermMul(t, f) } } })
    )
}

object E : Par<Expr>() {
  override val par: Parser<Expr>
    get() = memo(
      T.map { ExprTerm(it) as Expr } alt
              (E bind { e -> term("+") bind { T.map { t -> ExprSum(e, t) } } }) alt
              (E bind { e -> term("-") bind { T.map { t -> ExprSub(e, t) } } })
    )
}
