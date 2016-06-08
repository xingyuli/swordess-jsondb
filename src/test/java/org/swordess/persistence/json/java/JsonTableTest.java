package org.swordess.persistence.json.java;

import com.google.gson.JsonArray;
import kotlin.jvm.JvmClassMappingKt;
import org.junit.Test;
import org.swordess.persistence.json.JsonEntityMetadata;
import org.swordess.persistence.json.JsonTable;
import org.swordess.persistence.json.java.test.model.User;

import static org.junit.Assert.*;

public class JsonTableTest {

    @Test
    public void testExceptionShouldBeThrownIfGivenIdGeneratorLessThan1() {
        try {
            new JsonTable<User>(new JsonEntityMetadata(JvmClassMappingKt.getKotlinClass(User.class)), 0, new JsonArray());
        } catch (IllegalArgumentException e) {
            assertEquals("idGenerator must be greater than or equal to 1", e.getMessage());
        }
    }

    @Test
    public void testRefreshTableIsEmpty() {
        JsonTable<User> table = createUserTable();
        assertTrue(table.isEmpty());
        assertEquals(0, table.size());
    }

    @Test
    public void testReplaceShouldReturnFalseIfCannotBeFound() {
        JsonTable<User> table = createUserTable();

        User u = new User();
        u.setUsername("foo");

        User replacement = new User();
        u.setUsername("bar");

        assertFalse(table.replace(u, replacement));
    }

    private JsonTable<User> createUserTable() {
        return new JsonTable<>(new JsonEntityMetadata(JvmClassMappingKt.getKotlinClass(User.class)), 1L, new JsonArray());
    }

}
