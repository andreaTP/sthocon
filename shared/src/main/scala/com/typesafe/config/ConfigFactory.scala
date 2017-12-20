package com.typesafe.config

import scala.collection.mutable

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object MacroUtils {

  // I'm relying on the fact that the compiler is single thread
  var i = 0

  def extractString(c: Context)(str: c.Expr[String]): String = {
    import c.universe._
    import scala.reflect.runtime.universe.{ reify, showRaw, show }
    println( show( str ) )

    str.tree match {
      case q"""$strLit""" =>
        strLit match {
          case Literal(Constant(str)) =>
            str.toString
          case _ =>
            println("have to go on here to resolve strings ... ")
            throw new Exception(
              "Please provide a plain string literal in source base path")
        }
    }
  }

}

object Config {
  val configStrings = mutable.Map[Int, String]()

  var confStr: String = _

  def getStringImpl(c: Context)(path: c.Expr[String]): c.Expr[String] = {
    import c.universe._

    val index = MacroUtils.i

    val config = ct.config.ConfigFactory.parseString(configStrings(index))

    val pathStr = MacroUtils.extractString(c)(path)

    val res = config.getString(pathStr)

    c.Expr[String](q"""$res""")
  }
}

class Config() {

  def getString(path: String): String = macro Config.getStringImpl

}


object ConfigFactory {

  def parseStringImpl(c: Context)(s: c.Expr[String]) = {
    import c.universe._

    MacroUtils.i += 1

    val configStr = MacroUtils.extractString(c)(s)

    Config.configStrings.update(MacroUtils.i, configStr)

    println("configStr "+configStr)

    val res =
      c.Expr[com.typesafe.config.Config](q"""{
          new com.typesafe.config.Config()
        }""")

    res
  }

  def parseString(s: String): Config = macro parseStringImpl

}
