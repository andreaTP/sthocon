package com.typesafe.config

import scala.collection.mutable

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

import java.{time => jt, util => ju}
import java.util.{concurrent => juc}

object MacroUtils {

  // I'm relying on the fact that the compiler is single thread
  var i = 0

  def extractString(c: Context)(str: c.Expr[String]): String = {
    import c.universe._

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

  def index = MacroUtils.i

  def config = ct.config.ConfigFactory.parseString(configStrings(index))

  def getStringImpl(c: Context)(path: c.Expr[String]): c.Expr[String] = {
    import c.universe._

    val pathStr = MacroUtils.extractString(c)(path)

    val res = config.getString(pathStr)

    c.Expr[String](q"""$res""")
  }

  def getIntImpl(c: Context)(path: c.Expr[String]): c.Expr[Int] = {
    import c.universe._

    val pathStr = MacroUtils.extractString(c)(path)

    val res = config.getInt(pathStr)

    c.Expr[Int](q"""$res""")
  }


  def getBooleanImpl(c: Context)(path: c.Expr[String]): c.Expr[Boolean] = {
    import c.universe._

    val pathStr = MacroUtils.extractString(c)(path)

    val res = config.getBoolean(pathStr)

    c.Expr[Boolean](q"""$res""")
  }

  def getLongImpl(c: Context)(path: c.Expr[String]): c.Expr[Long] = {
    import c.universe._

    val pathStr = MacroUtils.extractString(c)(path)

    val res = config.getLong(pathStr)

    c.Expr[Long](q"""$res""")
  }

  def getDoubleImpl(c: Context)(path: c.Expr[String]): c.Expr[Double] = {
    import c.universe._

    val pathStr = MacroUtils.extractString(c)(path)

    val res = config.getDouble(pathStr)

    c.Expr[Double](q"""$res""")
  }

  def hasPathImpl(c: Context)(path: c.Expr[String]): c.Expr[Boolean] = {
    import c.universe._

    val pathStr = MacroUtils.extractString(c)(path)

    val res = config.hasPath(pathStr)

    c.Expr[Boolean](q"""$res""")
  }

  def getStringListImpl(c: Context)(path: c.Expr[String]): c.Expr[ju.List[String]] = {
    import c.universe._
    import scala.collection.JavaConverters._

    val pathStr = MacroUtils.extractString(c)(path)

    val res = config.getStringList(pathStr).asScala

    // this could be better probably ...
    c.Expr[ju.List[String]](q"""{
      import scala.collection.JavaConverters._
      List(..$res).asJava
    }""")
  }

  def getDurationImpl(c: Context)(path: c.Expr[String]): c.Expr[jt.Duration] = {
    import c.universe._

    val pathStr = MacroUtils.extractString(c)(path)

    val res = config.getDuration(pathStr).toNanos

    c.Expr[jt.Duration](q"""{
      java.time.Duration.ofNanos($res)
    }""")
  }

  // def getDurationTUImpl(c: Context)(path: c.Expr[String], tu: c.Expr[juc.TimeUnit]): c.Expr[Long] = {
  //   import c.universe._
  //
  //   val pathStr = MacroUtils.extractString(c)(path)
  //
  //   val res = config.getDuration(pathStr)
  //
  //   c.Expr[Long](q"""{
  //     $res.get($tu).toNanos
  //   }""")
  // }

  def getConfigImpl(c: Context)(path: c.Expr[String]): c.Expr[Config] = {
    import c.universe._

    val pathStr = MacroUtils.extractString(c)(path)

    val configStr = config.getConfig(pathStr).root.render

    MacroUtils.i += 1

    Config.configStrings.update(MacroUtils.i, configStr)

    c.Expr[Config](q"""{
        new com.typesafe.config.Config()
      }""")
  }

  // def withFallbackImpl(c: Context)(config: c.Expr[Config]): c.Expr[Config] = {
  //   import c.universe._
  //
  //   val configStr = Config.config.root.render ++ q"""$config.root.render"""
  //
  //   MacroUtils.i += 1
  //
  //   Config.configStrings.update(MacroUtils.i, configStr)
  //
  //   c.Expr[Config](q"""{
  //       new com.typesafe.config.Config()
  //     }""")
  // }

}

class Config() {

  def hasPath(path: String): Boolean = macro Config.hasPathImpl

  def getString(path: String): String = macro Config.getStringImpl

  def getBoolean(path: String): Boolean = macro Config.getBooleanImpl

  def getInt(path: String): Int = macro Config.getIntImpl

  def getLong(path: String): Long = macro Config.getLongImpl

  def getDouble(path: String): Double = macro Config.getDoubleImpl

  def getStringList(path: String): ju.List[String] = macro Config.getStringListImpl

  def getDuration(path: String): jt.Duration = macro Config.getDurationImpl

  // def getDuration(path: String, unit: juc.TimeUnit): Long = macro Config.getDurationTUImpl

  def getConfig(path: String): Config = macro Config.getConfigImpl

  // def withFallback(config: Config): Config = macro Config.withFallbackImpl
}


object ConfigFactory {

  def parseStringImpl(c: Context)(s: c.Expr[String]) = {
    import c.universe._

    MacroUtils.i += 1

    val configStr = MacroUtils.extractString(c)(s)

    Config.configStrings.update(MacroUtils.i, configStr)

    // println("configStr "+configStr)

    val res =
      c.Expr[com.typesafe.config.Config](q"""{
          new com.typesafe.config.Config()
        }""")

    res
  }

  def parseString(s: String): Config = macro parseStringImpl

}
