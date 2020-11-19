package com.kkb.utils

import com.typesafe.config.ConfigFactory

/**
 * Created by like
 */
class GlobalConfigUtils {
  /**
   *加载application.conf配置文件(kv对格式的内容)
   *
   * @return
   */
  private def conf = ConfigFactory.load()

  def heartColumnFamily = "MM"//conf.getString("heart.table.columnFamily")
  val getProp = (argv:String) => conf.getString(argv)

}

/**
 * 为了配置访问的安全性的一种写法
 */
object GlobalConfigUtils extends GlobalConfigUtils









