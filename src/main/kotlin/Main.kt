import cps.Result
import cps.parserFun.*
import kotlin.io.path.Path
import kotlin.io.path.readText

//fun <T> fix(f: (T) -> T): T = object : T {
//  operator fun invoke(t: T): T = f(this)
//}()
//
//fun <T> fix(p: (Parser<T>) -> Parser<T>): Parser<T> {
//  val a = p(a)
//  fun a(s: String): Result<T> = p(::a)(s)
//  return ::a
//}

fun <T> fix1(f: (Parser<T>) -> Parser<T>) = object : Parser<T> {
  private var r: Parser<T>? = null

  override fun invoke(s: String): Result<T> {
    if (r == null) r = f(this)
    return r!!(s)
  }
}

//fun <T> fix(f: (Parser<T>) -> Parser<T>) : Parser<T> = f { s -> fix(f)(s) }

//fun <T> fix(f: (Parser<T>) -> Parser<T>): Parser<T> = f(fix(f))

object C: Parser<String> {
  private var r: Parser<String>? = null
  override fun invoke(s: String): Result<String> {
    if (r == null) {
      r = rule(
        seq(this, term("c")),
        term("$"),
      )
    }
    return r!!(s)
  }
}

fun main(args: Array<String>) {
  println("Hello World!")

  // Try adding program arguments via Run/Debug configuration.
  // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
  println("Program arguments: ${args.joinToString()}")

  val ccc = seq(fix1 {
    rule(
      seq(it, term("c")),
      eps(),
    )
  }, term("$"))

  val t = fix1 { t ->
    rule(
      seq(t, term("+"), term("b")),
      seq(t, term("-"), term("b")),
      term("a")
    )
  }

  val s = fix1 { s ->
    rule(
      seq(s, s, s),
      seq(s, s),
      term("s"),
    )
  }

//  fun ccc(s: String): Result<String> = rule(
//    seq(::ccc, term("c")),
//    term("c"),
//  )(s)
//
//  val t = rule(
//    seq(term("a"), term("b"), term("c"),),
//    seq(term("c"), term("b"), term("a"),),
//  )

//  (t("cbaa")){ res ->
//    println("t Success: $res")
//  }

  val input = Path("/home/enotvtapke/thesis/cps-kotlin/src/main/resources/ccc_input").readText()

  println(input)

  (s("ssssssssssssssssssssssssssssssssssssssssssssssssssss")) { res ->
    println("Success: $res")
  }
}
