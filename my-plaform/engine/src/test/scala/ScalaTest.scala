import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Dataset, SparkSession}

trait SessionNamesDemo{}

object ScalaTest {
  val sessionNamesDemo = new SessionNamesDemo{}
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder().appName("testApp").master("local").getOrCreate()

    val df = sparkSession.createDataFrame(Seq(
      ("ming", 20, 15552211521L),
      ("hong", 19, 13287994007L),
      ("zhi", 21, 15552211523L)
    )) toDF("name", "age", "phone")

    df.show()

    val data: Dataset[String] = df.toJSON
    println(data.collect())
  }
}

