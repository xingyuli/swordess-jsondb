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

package org.swordess.persistence

import org.slf4j.LoggerFactory
import org.swordess.common.lang.Classes
import org.swordess.persistence.json.JsonEntity
import org.swordess.persistence.json.JsonEntityMetadata
import java.util.ArrayList
import java.util.HashMap
import kotlin.reflect.KClass

class EntityMetadataManager {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val metadata = HashMap<KClass<*>, EntityMetadata>()

    // MUST be initialized by client code
    lateinit var packagesToScan: List<String>

    fun init() {
        scanEntityClasses().forEach {
            if (!metadata.containsKey(it)) {
                metadata.put(it, JsonEntityMetadata(it))
            }
        }
        logger.info("scanned metadata\n{}", metadata.entries.joinToString(",\n"))
    }

    operator fun get(entityType: KClass<*>): EntityMetadata? = metadata[entityType]

    private fun scanEntityClasses(): List<KClass<*>> {
        val entityClasses = ArrayList<KClass<*>>()
        packagesToScan.forEach {
            Classes.underPackage(it, { isJsonEntityClass(it) }).forEach { entityClasses.add(it) }
        }
        return entityClasses
    }

    private fun isJsonEntityClass(c: KClass<*>) = c.java.isAnnotationPresent(JsonEntity::class.java)

}