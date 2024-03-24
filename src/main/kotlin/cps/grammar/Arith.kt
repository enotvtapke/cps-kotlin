package cps.grammar

import Par
import cps.parser.Parser
import cps.parser.memo
import cps.parser.term
import runParser

object Arith {
  sealed interface Expr
  data class ExprTerm(val t: Term) : Expr {
    override fun toString()= "$t"
  }
  data class ExprSum(val x: Expr, val y: Term) : Expr {
    override fun toString()= "$x+$y"
  }

  data class ExprSub(val x: Expr, val y: Term) : Expr {
    override fun toString()= "$x-$y"
  }

  sealed interface Term
  data class TermVal(val v: Val) : Term {
    override fun toString()= "$v"
  }
  data class TermMul(val x: Term, val y: Val) : Term {
    override fun toString()= "$x*$y"
  }

  sealed interface Val
  data class ValNumber(val n: Int) : Val {
    override fun toString()= "$n"
  }
  data class ValPar(val expr: Expr) : Val {
    override fun toString()= "($expr)"
  }


  object F : Par<Val>() {
    override val par: Parser<Val>
      get() = memo(
        ((term("\\d+".toRegex())).map { ValNumber(it.toString().toInt()) as Val }) alt
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
}

fun main() {
  runParser(Arith.E, "1" + "+1+1".repeat(40000))
}