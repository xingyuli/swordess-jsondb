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

import com.google.gson.Gson
import com.google.gson.JsonArray
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicLong

// table schema
data class JsonTableTransfer(val idGenerator: Long, val rows: List<*>)

public class JsonTable<E>(val metadata: JsonEntityMetadata, idGenerator: Long = 1L, rows: JsonArray = JsonArray()) : Iterable<E> {

    // get() indicates the next id
    private val idGenerator: AtomicLong

    internal val rows: MutableList<E>

    init {
        this.idGenerator = if (idGenerator >= 1) {
            AtomicLong(idGenerator)
        } else throw IllegalArgumentException("idGenerator must be greater than or equal to 1")

        this.rows = rows.mapTo(ArrayList(), { Gson().fromJson<E>(it, metadata.belongingClass.java) })
    }

    private fun nextId(): Long = idGenerator.andIncrement

    fun isEmpty(): Boolean = rows.isEmpty()

    fun size(): Int = rows.size

    fun add(e: E): Boolean {
        val id = metadata.idMetadata.getter(e)
        if (id == null) {
            // inject id if not set
            // TODO ignore id passed in?
            metadata.idMetadata.setter(e, nextId())
        }
        return rows.add(e)
    }

    fun replace(e: E, replacement: E): Boolean {
        for (i in rows.indices) {
            val current = rows[i]
            if (current == e) {
                rows[i] = replacement
                return true
            }
        }
        return false
    }

    fun remove(e: E): Boolean = rows.remove(e)

    fun asTransfer(): JsonTableTransfer = JsonTableTransfer(idGenerator.get(), rows)

    override fun iterator(): Iterator<E> = rows.iterator()

}
