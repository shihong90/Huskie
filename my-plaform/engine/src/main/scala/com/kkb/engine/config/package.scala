package com.kkb.engine

/**
 * Created by like
 */
package object config {
  val PARALLELISM = ConfigBuilder("platform.engine.parallelism")
    .doc("引擎的并行度")
    .intConf
    .createWithDefault(6)
}
