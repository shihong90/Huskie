package com.kkb.engine.config

/**
 * Created by like
 */
private[config] abstract class ConfigEntry[T] (
                                                val key: String,
                                                val valueConverter: String => T,
                                                val stringConverter: T => String,
                                                val doc: String,
                                                val isPublic: Boolean) {

  import ConfigEntry._

  registerEntry(this)

  def defaultValueString: String

  def readFrom(reader: ConfigReader): T

  def defaultValue: Option[T] = None

  override def toString: String = {
    s"ConfigEntry(key=$key, defaultValue=$defaultValueString, doc=$doc, public=$isPublic)"
  }

}

private class ConfigEntryWithDefault[T] (
                                          key: String,
                                          _defaultValue: T,
                                          valueConverter: String => T,
                                          stringConverter: T => String,
                                          doc: String,
                                          isPublic: Boolean)
  extends ConfigEntry(key, valueConverter, stringConverter, doc, isPublic) {

  override def defaultValue: Option[T] = Some(_defaultValue)

  override def defaultValueString: String = stringConverter(_defaultValue)

  def readFrom(reader: ConfigReader): T = {
    reader.get(key).map(valueConverter).getOrElse(_defaultValue)
  }

}

private class ConfigEntryWithDefaultString[T] (
                                                key: String,
                                                _defaultValue: String,
                                                valueConverter: String => T,
                                                stringConverter: T => String,
                                                doc: String,
                                                isPublic: Boolean)
  extends ConfigEntry(key, valueConverter, stringConverter, doc, isPublic) {

  override def defaultValue: Option[T] = Some(valueConverter(_defaultValue))

  override def defaultValueString: String = _defaultValue

  def readFrom(reader: ConfigReader): T = {
    val value = reader.get(key).getOrElse(reader.substitute(_defaultValue))
    valueConverter(value)
  }

}


/**
 * A config entry that does not have a default value.
 */
private[config] class OptionalConfigEntry[T](
                                              key: String,
                                              val rawValueConverter: String => T,
                                              val rawStringConverter: T => String,
                                              doc: String,
                                              isPublic: Boolean)
  extends ConfigEntry[Option[T]](key, s => Some(rawValueConverter(s)),
    v => v.map(rawStringConverter).orNull, doc, isPublic) {

  override def defaultValueString: String = "<undefined>"

  override def readFrom(reader: ConfigReader): Option[T] = {
    reader.get(key).map(rawValueConverter)
  }

}

/**
 * A config entry whose default value is defined by another config entry.
 */
private class FallbackConfigEntry[T] (
                                       key: String,
                                       doc: String,
                                       isPublic: Boolean,
                                       private[config] val fallback: ConfigEntry[T])
  extends ConfigEntry[T](key, fallback.valueConverter, fallback.stringConverter, doc, isPublic) {

  override def defaultValueString: String = s"<value of ${fallback.key}>"

  override def readFrom(reader: ConfigReader): T = {
    reader.get(key).map(valueConverter).getOrElse(fallback.readFrom(reader))
  }

}

private[config] object ConfigEntry {

  private val knownConfigs = new java.util.concurrent.ConcurrentHashMap[String, ConfigEntry[_]]()

  def registerEntry(entry: ConfigEntry[_]): Unit = {
    val existing = knownConfigs.putIfAbsent(entry.key, entry)
    require(existing == null, s"Config entry ${entry.key} already registered!")
  }

  def findEntry(key: String): ConfigEntry[_] = knownConfigs.get(key)

}
