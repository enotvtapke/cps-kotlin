import cps.Result
import cps.parser.*

fun <T> fix(f: (Parser<T>) -> Parser<T>) = object : Parser<T> {
  private var r: Parser<T>? = null

  override fun invoke(s: String): Result<T> {
    if (r == null) r = f(this)
    return r!!(s)
  }
}

object C : Parser<String> {
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

fun main() {
  val c = seq(fix { c ->
    rule(
      seq(c, term("c")),
      eps(),
    )
  }, term("$"))

  val t = fix { t ->
    rule(
      seq(t, term("+"), term("b")),
      seq(t, term("-"), term("b")),
      term("a")
    )
  }

  val s = fix { s ->
    rule(
      seq(s, s, s),
      seq(s, s),
      term("s"),
    )
  }

//  val input = Path("/home/enotvtapke/thesis/cps-kotlin/src/main/resources/ccc_input").readText()
//  println(input)

  (s("ssssssssssssssssssssssssssssssssssssssssssssssssssss")) { res ->
    println("Success: $res")
  }
}
