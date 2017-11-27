package com.appham.sharemarks

import com.appham.sharemarks.model.MarkItem
import com.appham.sharemarks.presenter.HtmlManager
import org.junit.*
import org.junit.Assume.assumeTrue
import java.io.File
import java.io.InputStream
import java.net.URL
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals


val testPath = "http://localhost:8765/"

class HtmlManagerTest {

    companion object {

        @BeforeClass
        @JvmStatic
        fun setup() {
            val proc = Runtime.getRuntime().exec("curl -Is " + testPath + " | head -1",
                    null, File("testHtml/server"))
            Scanner(proc.inputStream).use {
                while (it.hasNextLine()) {
                    val line = it.nextLine()
                    println(line)
                    if (line.endsWith("200 OK")) {
                        println("Server is running!")
                        return
                    }
                }
            }
            proc.waitFor(5, TimeUnit.SECONDS)
            System.out.println("Setting up server")
            runCommand("npm restart")
        }

        @AfterClass
        @JvmStatic
        fun shutdown() {
            System.out.println("Stop server")
            runCommand("npm stop")
        }

        private fun runCommand(cmd: String) {
            val proc = Runtime.getRuntime().exec(cmd, null, File("testHtml/server"))
            proc.waitFor(5, TimeUnit.SECONDS)
        }

        @get:Rule
        val htmlManager = HtmlManager()

    }

    @Before
    fun `assume a localhost is serving test pages`() {
        var stream: InputStream? = null
        var isRunning = true
        try {
            stream = URL(testPath).openConnection().content as InputStream
        } catch (e: UnknownHostException) {
            isRunning = false
        } finally {
            stream?.close()
            assumeTrue("Assuming a running host at: " + testPath, isRunning)
        }
    }

    @Test
    (expected = RuntimeException::class)
    fun `not found page should lead to exception`() {
        val url = URL(testPath.plus("notfoundpage404"))
        val observable = htmlManager.parseHtml(url, MarkItem.create(null, null, null, url, null))
        observable.blockingFirst()
    }

    @Test
    fun `on pages without images the image url should be favicon url`() {
        val url = URL(testPath.plus("test-no-img.html"))
        val observable = htmlManager.parseHtml(url, MarkItem.create(null, null, null, url, null))
        val item = observable.blockingFirst()
        assertEquals("http://localhost/favicon.ico", item.imageUrl)
    }
}