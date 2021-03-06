package com.kunyan.userportrait.temp.lcm.crawler


import java.io.IOException
import java.net.{SocketException, SocketTimeoutException, UnknownHostException}
import java.util.regex.Pattern

import com.kunyan.userportrait.temp.lcm.crawler.util.WeiBo
import org.jsoup.{HttpStatusException, Jsoup}

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
 * Created by lcm on 2016/5/10.
 * 用于启动爬取数据的程序
 */
object Controller {

  val UA = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36 QIHU 360SE se.360.cn"

  val IPS = new ListBuffer[String]

  /**
   *
   * @param args 输入的数据文件和输出的数据文件
   */
  def main(args: Array[String]) {

    //保存微博的信息
    var listUaAndUid = new ListBuffer[(String, String)]

    //切换代理ip
    for (id <- 1 to 5) {

      getIPs
      Thread.sleep(1000)

    }

    changIP()

    //读取源数据文件
    for (line <- Source.fromFile(args(0))("UTF-8").getLines()) {

      val lineArr = line.split("\t")

      if (lineArr.size == 8) {

        //保存微博的信息
        if (lineArr(3).contains("weibo.com")) {

          //保存单条信息的ua、uid
          val pattern = Pattern.compile("/\\d{7,10}")
          var m = pattern.matcher(lineArr(4))
          var uid = ""

          if (m.find()) {

            uid = m.group()
            var have = false

            listUaAndUid.foreach(line => {

              if (line._2 == uid) have = true

            })

            if (!have) {

              listUaAndUid = listUaAndUid.+=((lineArr(2), uid))

            }
          }

          m = pattern.matcher(lineArr(5))

          if (m.find()) {

            uid = m.group()
            var have = false

            listUaAndUid.foreach(line => {

              if (line._2 == uid) have = true

            })

            if (!have) {

              listUaAndUid = listUaAndUid.+=((lineArr(2), uid))

            }
          }
        }
      }
    }

    //爬取微博信息并保存
    WeiBo.crawlWeiBoInfo(listUaAndUid, args(1))
  }

  /**
   * 用于获取代理IP
   * @return：IP数组
   */
  def getIPs: Array[String] = {

    var ipPort: Array[String] = null

    try {

      val doc = Jsoup.connect("http://qsdrk.daili666api.com/ip/?tid=558465838696598&num=500&delay=5&foreign=none&ports=80,8080")
        .userAgent(UA)
        .timeout(30000)
        .followRedirects(true)
        .execute()

      ipPort = doc.body().split("\r\n")

      for (ip <- ipPort) {

        IPS.+=(ip)

      }

    } catch {

      case ex: HttpStatusException => ex.printStackTrace()

      case ex: SocketTimeoutException => ex.printStackTrace()

      case ex: SocketException => ex.printStackTrace()

      case ex: UnknownHostException => ex.printStackTrace()

      case ex: IOException => ex.printStackTrace()

    }

    ipPort
  }

  /**
   * 用来改变代理IP
   */
  def changIP(): Unit = {

    val ipPort = IPS((Math.random() * IPS.length).toInt).split(":")
    val ip = ipPort(0)
    val port = ipPort(1)

    System.getProperties.setProperty("http.proxyHost", ip)
    System.getProperties.setProperty("http.proxyPort", port)

  }
}



