package cps.grammar.lama

import Par
import cps.parser.*
import cps.parser.ParserM.Companion.ret
import runParser
import kotlin.io.path.*
import kotlin.time.measureTime

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
data class PrimaryConst(val v: Double): Primary {
  override fun toString() = "Const (${v.toLong()})"
}
data class PrimaryVar(val v: String): Primary {
  override fun toString() = "Var (\"$v\")"
}

data class PrimaryExpr(val v: ScopeExpr): Primary {
  override fun toString() = "PrimaryExpr ($v)"
}

data class Binop(val op: String, val l: Primary, val r: Primary): Primary {
  override fun toString() = "Binop (\"$op\", $l, $r)"
}

sealed interface ScopeExpr
data class ScopeExprBinop(val binop: Primary): ScopeExpr {
  override fun toString() = "ScopeExprBinop($binop)"
}

private fun <X, Y, S> left(s: (X, Y) -> S, c: (X) -> X, x: X, y: Y) = s(c(x), y)

fun expr(operations: List<Parser<(Primary, Primary) -> Binop>>, operand: Parser<Primary>): Parser<Primary> {
  val n = operations.size
  val operationsAssoc = operations.map {
    it.map<((Primary) -> Primary, Primary, Primary) -> Primary> { o -> { c, x, y -> left(o, c, x, y) } }
  }

  fun inner(l: Int, c: (Primary) -> Primary): Parser<Primary> {
    return when(l) {
      n -> operand.map { c(it) }
      else -> inner(l + 1) { it } bind { x ->
        opt(operationsAssoc[l].bind { o -> inner(l) { o(c, x, it) } } as ParserM<Primary?, CharSequence>).map { b -> b ?: c(x) }
      }
    }
  }

  return inner(0) { it }
}

//fun expr(operations: List<Parser<(Primary, Primary) -> Binop>>, operand: Parser<Primary>): Parser<Primary> =
//  operations.fold(zero()) { acc, op ->
//    acc alt (operand bind { a ->
//      op bind { action ->
//        operand.map { b -> action(a, b) }
//      }
//    })
//  }

fun binop(op: String): Parser<(Primary, Primary) -> Binop> = token(op).map { { l, r -> Binop(op, l, r) } }

object Expr : Par<Primary>() {
  override val par: Parser<Primary>
    get() = memo(Basic)
}

object Prim : Par<Primary>() {
  override val par: Parser<Primary>
    get() = memo(
      token("\\d+(\\.\\d+)?".toRegex()).map { PrimaryConst(it.toString().toDouble()) as Primary } alt
              token("[a-z][a-z_A-Z0-9']*".toRegex()).map { v -> PrimaryVar(v.toString()) } alt
              (token("(") bind Expr bind { e -> token(")").map { e } })
//              alt (Expr.map { PrimaryExpr(it) })
    )
}

object Basic : Par<Primary>() {
  override val par: Parser<Primary>
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

fun test(iter: Int) {
  val left = Path("src/main/resources/lamaExpr/left.txt").readText()
  val right = Path("src/main/resources/lamaExpr/right.txt").readText()
  val middle = Path("src/main/resources/lamaExpr/middle.txt").readText()

  val logFile = Path("src/main/resources/lamaExpr/log.txt")
  logFile.writeText("input_size time\n")

  for (i in iter - 1..<iter) {
    val full = left.repeat(i) + middle + right.repeat(i)
    val numOfIter = 5
    val meanTime = generateSequence {
      measureTime {
        check(runParser(Expr, full).size == 1)
      }
    }.take(numOfIter).sumOf { it.inWholeMilliseconds }.toDouble() / numOfIter
    logFile.appendText("${full.length} $meanTime\n")
  }
}

fun main() {
  test(60)
//  val t = Path("src/main/resources/lamaExpr/middle.txt").readText()
//  (1..2).forEach { i ->
//    val numOfIter = 100
//    val time = generateSequence {
//      measureTime {
//        runParser(Expr, Path("src/main/resources/lamaExpr/$i.txt").readText(), true)
//      }
//    }.take(numOfIter).sumOf { it.inWholeMilliseconds }.toDouble() / numOfIter
//    println(time)
//  }

//  var l = t
//  (1..30).forEach { i ->
//    val numOfIter = 10
//    val time = generateSequence {
//      measureTime {
//        runParser(Expr, l, false)
//      }
//    }.take(numOfIter).sumOf { it.inWholeMilliseconds }.toDouble() / numOfIter
//    println(time)
//    l += "+$t"
//  }
}
