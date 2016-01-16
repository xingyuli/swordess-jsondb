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

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.swordess.persistence.EntityMetadataManager
import org.swordess.persistence.json.test.model.User
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.reflect.KClass

class JsonDBTest {

    private lateinit var jsondb: JsonDB

    companion object {

        val sourceDataLocation = File("src/test/resources/org/swordess/persistence/json/test/data")
        val destinationDataLocation: File

        init {
            // TODO refactoring - extract class e.g., org.swordess.test.gradle.GradleTest
            destinationDataLocation = File("build/tmp/test/${JsonDBTest::class.java.simpleName}/data")
            if (!destinationDataLocation.exists()) {
                destinationDataLocation.mkdirs()
            }
        }

        fun copySourceToDestination() {
            sourceDataLocation.listFiles().forEach {
                val inStream = it.inputStream() as FileInputStream
                val outStream = File(destinationDataLocation, it.name).outputStream() as FileOutputStream
                inStream.channel.transferTo(0, inStream.available().toLong(), outStream.channel)
                inStream.close()
                outStream.close()
            }
        }

        fun cleanDestinationData() {
            destinationDataLocation.listFiles().forEach { it.delete() }
        }

        fun dataFileContent(c: KClass<*>) =
                File(destinationDataLocation, c.java.getAnnotation(JsonEntity::class.java).filename + Database.JSON_FILE_EXTENSION).readText()

    }

    @Before
    fun setUp() {
        JsonDBTest.copySourceToDestination()
        jsondb = with(JsonDB()) {
            val manager = EntityMetadataManager()
            manager.packagesToScan = listOf("org.swordess.persistence.json.test.model")
            manager.init()

            database = Database()
            database.entityMetadataManager = manager
            database.dataLocation = JsonDBTest.destinationDataLocation.absolutePath
            database.init()

            this@with
        }
    }

    @After
    fun tearDown() {
        jsondb.database.destroy()
        JsonDBTest.cleanDestinationData()
    }

    @Test
    fun testSave() {
        jsondb.save(User(username = "Bar", age = 40))

        assertEquals(
"""{
  "idGenerator": 3,
  "rows": [
    {
      "id": 1,
      "username": "Foo",
      "age": 20
    },
    {
      "id": 2,
      "username": "Bar",
      "age": 40
    }
  ]
}""", dataFileContent(User::class))
    }

    @Test
    fun testFind() {
        assertEquals(1, jsondb.findAll(User::class).size)
        assertEquals("User(id=1, username=Foo, age=20)", jsondb.findOne(User::class, 1).toString())
    }

    @Test
    fun testUpdate() {
        val user = jsondb.findOne(User::class, 1)
        user!!.age = 24
        jsondb.update(user)

        assertEquals(
"""{
  "idGenerator": 2,
  "rows": [
    {
      "id": 1,
      "username": "Foo",
      "age": 24
    }
  ]
}""", dataFileContent(User::class))
    }

    @Test
    fun testRemove() {
        jsondb.remove(User::class, 1)

        assertEquals(
"""{
  "idGenerator": 2,
  "rows": []
}""", dataFileContent(User::class))
    }

}