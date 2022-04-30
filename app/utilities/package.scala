package object utilities {
  def toPersian(numeralInEnglish: String): String = numeralInEnglish.split("").map(_.head).map(_.toInt).map(_ + 1776 - 48).map(_.toChar).mkString
}
