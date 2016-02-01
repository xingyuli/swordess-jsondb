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

package org.swordess.persistence.json

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import org.swordess.common.lang.io.DirectoryWatcher
import org.swordess.common.lang.io.resourceNameAsFileAbsolutePath
import org.swordess.common.lang.io.resourceNameAsStream
import org.swordess.common.lang.withSuffix
import org.swordess.common.lang.withoutSuffix
import org.swordess.persistence.EntityMetadataManager
import java.io.File
import java.io.IOException
import java.nio.file.StandardWatchEventKinds
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

class Database {

    companion object {
        @JvmStatic const val DEFAULT_CHARSET = "UTF-8"
        @JvmStatic const val JSON_FILE_EXTENSION = ".json"
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    private val gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()
    private val locks = ConcurrentHashMap<Class<*>, ReentrantReadWriteLock>()

    private lateinit var watcher: DirectoryWatcher

    // key: filename without extension
    private val filenameToTable = ConcurrentHashMap<String, JsonTable<*>>()

    // MUST be initialized by client code
    lateinit var entityMetadataManager: EntityMetadataManager
    lateinit var dataLocation: String

    var charset = DEFAULT_CHARSET

    fun init() {
        watcher = DirectoryWatcher(dataLocation.withoutSuffix("/").resourceNameAsFileAbsolutePath(),
                StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE)
        watcher.filenameExtensionInclude = JSON_FILE_EXTENSION
        watcher.addHandler({ evt ->
            val filenameWithoutExtension = evt.change.fileName.toString().withoutSuffix(JSON_FILE_EXTENSION)
            if (filenameWithoutExtension in filenameToTable) {
                filenameToTable -= filenameWithoutExtension
                logger.info("cache for {} has been discarded", evt.change)
            }
        })

        watcher.init()

        logger.info("database has been initialized")
    }

    fun destroy() {
        watcher.destroy()
        logger.info("database has been destroyed")
    }

    operator fun <T : Any> get(entityType: KClass<T>): JsonTable<T> {
        val metadata = getMetadata(entityType)

        @Suppress("UNCHECKED_CAST")
        return filenameToTable.getOrPut(metadata.filename, { load<Any>(metadata) }) as JsonTable<T>
    }

    private fun <T> load(metadata: JsonEntityMetadata): JsonTable<T> {
        lockFor(metadata.belongingClass.java).readLock().withLock {
            resourceNameOf(metadata).resourceNameAsStream().reader(charset).use { reader ->
                val content = try {
                    gson.fromJson(reader, JsonObject::class.java)
                } catch (e: IOException) {
                    throw RuntimeException("unable to load json file: ${resourceNameOf(metadata)}", e)
                }

                if (content != null) {
                    return JsonTable(metadata, content.get("idGenerator").asLong, content.getAsJsonArray("rows"))
                } else {
                    return JsonTable(metadata)
                }
            }
        }
    }

    fun <T> persist(table: JsonTable<T>) {
        // 1. persist operation leads to ENTRY_MODIFY event
        // 2. the event will be handled by the registered DirectoryEventHandler
        //    so that the cached version of this table will be removed
        // 3. the next call for this table needs a fresh loading
        lockFor(table.metadata.belongingClass.java).writeLock().withLock {
            val outFile = File(resourceNameOf(table.metadata).resourceNameAsFileAbsolutePath())
            outFile.outputStream().writer(charset).use { writer -> gson.toJson(table.asTransfer(), writer) }
        }
    }

    private fun lockFor(entityType: Class<*>): ReentrantReadWriteLock {
        var lock: ReentrantReadWriteLock? = locks[entityType]
        if (lock == null) {
            lock = ReentrantReadWriteLock()
            locks[entityType] = lock
        }
        return lock
    }

    private fun resourceNameOf(metadata: JsonEntityMetadata): String {
        return dataLocation.withSuffix("/") + metadata.filename + JSON_FILE_EXTENSION
    }

    fun <T : Any> getMetadata(entityType: KClass<T>): JsonEntityMetadata = entityMetadataManager[entityType] as JsonEntityMetadata

}
