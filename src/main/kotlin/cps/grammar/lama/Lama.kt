package cps.grammar.lama

import Par
import cps.parser.Parser
import cps.parser.Parser.Companion.ret
import cps.parser.Parser.Companion.zero
import cps.parser.memo
import cps.parser.term

fun token(term: CharSequence): Parser<CharSequence> = term(term) bind { whitespace(); ret(it) }
fun token(term: Regex): Parser<CharSequence> = term(term) bind { whitespace(); ret(it) }

fun whitespace(): Parser<Unit> = term("\\s*".toRegex()).map {  }

// Shallow embedding
//fun <T, U> expr(operations: List<Parser<(T, T) -> U>>, operand: Parser<T>): Parser<U> =
//  operations.fold(zero<U>()) { acc, op ->
//    acc alt (operand bind { a ->
//      op bind { action ->
//        operand.map { b -> action(a, b) }
//      }
//    })
//  }
//
//fun plus(): Parser<(Double, Double) -> Double> = token("+").map { { a, b -> a + b} }
//fun minus(): Parser<(Double, Double) -> Double> = token("-").map { { a, b -> a - b} }
//fun mul(): Parser<(Double, Double) -> Double> = token("*").map { { a, b -> a * b} }
//fun divide(): Parser<(Double, Double) -> Double> = token("/").map { { a, b -> a / b} }
//fun modulo(): Parser<(Double, Double) -> Double> = token("%").map { { a, b -> a % b} }
//fun equal(): Parser<(Double, Double) -> Boolean> = token("==").map { { a, b -> a == b} }
//fun notEqual(): Parser<(Double, Double) -> Boolean> = token("!=").map { { a, b -> a != b} }
//fun less(): Parser<(Double, Double) -> Boolean> = token("<").map { { a, b -> a < b} }
//fun greater(): Parser<(Double, Double) -> Boolean> = token(">").map { { a, b -> a > b} }
//fun lessEqual(): Parser<(Double, Double) -> Boolean> = token("<=").map { { a, b -> a <= b} }
//fun greaterEqual(): Parser<(Double, Double) -> Boolean> = token(">=").map { { a, b -> a >= b} }

sealed interface Primary
data class PrimaryVal(val v: Double): Primary
data class PrimaryExpr(val v: ScopeExpr): Primary

data class Binop(val op: String, val l: Primary, val r: Primary)

sealed interface ScopeExpr
data class ScopeExprBinop(val binop: Binop): ScopeExpr

fun expr(operations: List<Parser<(Primary, Primary) -> Binop>>, operand: Parser<Primary>): Parser<Binop> =
  operations.fold(zero()) { acc, op ->
    acc alt (operand bind { a ->
      op bind { action ->
        operand.map { b -> action(a, b) }
      }
    })
  }

fun binop(op: String): Parser<(Primary, Primary) -> Binop> = token(op).map { { l, r -> Binop(op, l, r) } }

object Expr : Par<ScopeExpr>() {
  override val par: Parser<ScopeExpr>
    get() = memo(Basic.map { ScopeExprBinop(it) })
}

object Prim : Par<Primary>() {
  override val par: Parser<Primary>
    get() = memo(
      token("\\d+(\\.\\d+)?".toRegex()).map { PrimaryVal(it.toString().toDouble()) as Primary } alt
              (token("(") bind Expr bind { e -> token(")").map { PrimaryExpr(e) } })
    )
}

object Basic : Par<Binop>() {
  override val par: Parser<Binop>
    get() = memo(
      expr(listOf(
        binop("!!"),
        binop("&&"),
        binop("=="),
        binop("!="),
        binop("<"),
        binop(">"),
        binop("<="),
        binop(">="),
        binop("+"),
        binop("-"),
        binop("*"),
        binop("/"),
        binop("%"),
      ), Prim)
    )
}
