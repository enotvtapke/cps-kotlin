import cps.Result
import cps.parser.*
import java.nio.CharBuffer
import java.nio.CharBuffer.wrap
import kotlin.io.path.Path
import kotlin.io.path.readText

fun <T> fix(f: (Parser<T>) -> Parser<T>) = object : Parser<T> {
  private var r: Parser<T>? = null

  override fun invoke(s: CharSequence): Result<Pair<CharSequence, T>> {
    if (r == null) r = f(this)
    return r!!(s)
  }
}

//object C : Parser<String> {
//  private var r: Parser<String>? = null
//  override fun invoke(s: String): Result<String> {
//    if (r == null) {
//      r = rule(
//        seq(this, term("c")),
//        term("$"),
//      )
//    }
//    return r!!(s)
//  }
//}

fun main() {
  runParser(
    fix { ccc: Parser<String> ->
      memo(((ccc bind { t -> term("c").map { "" } }) alt eps()))
    } bind term("$"),
    "c".repeat(100000) + "$"
  )

  runParser(
    fix { pali: Parser<CharSequence> ->
      memo(
        (term("a") bind pali bind term("a")) alt
        (term("b") bind pali bind term("b")) alt
        (term("c") bind pali bind term("c")) alt
        term("d")
      )
    },
    "ada" + ("a".repeat(2) + "b".repeat(42) + "c".repeat(80)).repeat(100) + "d" + ("c".repeat(80) + "b".repeat(42) + "a".repeat(2)).repeat(100)
  )

//  runParser(
//    fix { sss: Parser<CharSequence> ->
//      memo(
//        (term(""))
//      )
//    }
//  )
}

private fun <T> runParser(p: Parser<T>, input: CharSequence) {
  var m = 0
  (p(wrap(input))) { res ->
    println("Success: $res")
    m += 1
  }
  println("Num of results: $m")
}

