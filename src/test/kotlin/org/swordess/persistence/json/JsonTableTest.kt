package org.swordess.persistence.json

import org.junit.Test
import org.swordess.persistence.json.java.test.model.User
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsonTableTest {

    @Test
    fun testExceptionShouldBeThrownIfGivenIdGeneratorLessThan1() {
        try {
            JsonTable<User>(metadata = JsonEntityMetadata(User::class), idGenerator = 0)
        } catch (e: IllegalArgumentException) {
            assertEquals("idGenerator must be greater than or equal to 1", e.message)
        }
    }

    @Test
    fun testRefreshTableIsEmpty() {
        val table = createUserTable()
        assertTrue(table.isEmpty())
        assertEquals(0, table.size())
    }

    @Test
    fun testReplaceShouldReturnFalseIfCannotBeFound() {
        val table = createUserTable()
        assertFalse(table.replace(
                User().apply { username = "foo" },
                User().apply { username = "bar" }
        ))
    }

    private fun createUserTable() = JsonTable<User>(JsonEntityMetadata(User::class))

}