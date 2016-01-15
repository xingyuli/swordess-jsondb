/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vic Lau
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.swordess.common.lang.io

import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchService
import java.util.ArrayList

class DirectoryEvent internal constructor(val kind: WatchEvent.Kind<out Any>, val dir: Path, val change: Path)

class DirectoryWatcher(dir: String, vararg val events: WatchEvent.Kind<*>) : Runnable {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val dir = File(dir).toPath()

    private lateinit var watchService: WatchService
    private var handlers: MutableList<(DirectoryEvent)->Unit> = ArrayList()
    // TODO support comma separated string
    var filenameExtensionInclude: String? = null

    fun init() {
        watchService = FileSystems.getDefault().newWatchService()
        dir.register(watchService, *events)

        // TODO stop the thread manually via the ServletContextListener so that its possible to reload the app
        with(Thread(this)) {
            setUncaughtExceptionHandler { t, e -> logger.warn(e.message, e) }
            start()
        }

        logger.info("watcher for {} has been initialized", dir)
    }

    fun destroy() {
        watchService.close()
        logger.info("watcher for {} has been destroyed", dir)
    }

    override fun run() {
        while (true) {
            val key = try {
                watchService.take()
            } catch (e: InterruptedException) {
                logger.info("interrupted")
                return
            } catch (e: ClosedWatchServiceException) {
                logger.info("watch service is closed")
                return
            }

            for (event in key.pollEvents()) {
                // an OVERFLOW event can occur regardless if events are lost or discarded
                if (event.kind() === StandardWatchEventKinds.OVERFLOW) {
                    continue
                }

                val filename = event.context() as Path

                // assume there is no modification after null check
                if (filenameExtensionInclude == null || filename.toString().endsWith(filenameExtensionInclude as String)) {
                    fireEvent(event.kind(), dir.resolve(filename))
                }
            }

            val valid = key.reset()
            if (!valid) {
                break
            }
        }
    }

    private fun fireEvent(kind: WatchEvent.Kind<out Any>, file: Path) {
        handlers.forEach { it(DirectoryEvent(kind, dir, file)) }
    }

    fun addHandler(handler: (DirectoryEvent) -> Unit) {
        handlers.add(handler)
    }

    fun removeHandler(handler: (DirectoryEvent) -> Unit) {
        handlers.remove(handler)
    }

}