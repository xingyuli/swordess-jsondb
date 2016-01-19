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

import org.slf4j.LoggerFactory
import org.swordess.persistence.DB
import java.util.ArrayList
import kotlin.reflect.KClass

class JsonDB : DB {

    private val logger = LoggerFactory.getLogger(javaClass)

    // MUST be initialized by client code
    lateinit var database: Database

    override fun <T : Any> save(obj: T): T = if (isPersistent(obj)) { update0(obj); obj } else save0(obj)

    private fun <T : Any> save0(obj: T): T {
        val table: JsonTable<T> = database[obj.javaClass.kotlin]
        table.add(obj)

        database.persist(table)

        return obj
    }

    override fun <T : Any> findOne(entityType: KClass<T>, id: Long): T? {
        val metadata = database.getMetadata(entityType)

        val table = database[entityType]
        return table.firstOrNull { id == metadata.idMetadata.getter(it) }
    }

    // TODO refactoring - move up to interface CrudRepository?
    fun <T : Any> findAll(entityType: KClass<T>): List<T> = ArrayList<T>().apply { addAll(database[entityType]) }

    override fun update(entity: Any) {
        if (!isPersistent(entity)) {
            save0(entity)
        } else {
            update0(entity)
        }
    }

    private fun update0(entity: Any) {
        val metadata = database.getMetadata(entity.javaClass.kotlin)
        val id = metadata.idMetadata.getter(entity)

        val table = database[entity.javaClass.kotlin]
        table.firstOrNull { id == metadata.idMetadata.getter(it) }?.let {
            table.replace(it, entity)
            database.persist(table)
        }
    }

    override fun <T : Any> remove(entityType: KClass<T>, id: Long) {
        val metadata = database.getMetadata(entityType)

        val table = database[entityType]
        table.firstOrNull { id == metadata.idMetadata.getter(it) }?.let {
            table.remove(it)
            database.persist(table)
        }
    }

    private fun isPersistent(obj: Any): Boolean = database.getMetadata(obj.javaClass.kotlin).idMetadata.getter(obj) != null

}
