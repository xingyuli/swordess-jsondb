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

package org.swordess.persistence.json.java;

import com.google.common.base.Charsets;
import kotlin.jvm.JvmClassMappingKt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.swordess.persistence.EntityMetadataManager;
import org.swordess.persistence.json.Database;
import org.swordess.persistence.json.JsonDB;
import org.swordess.persistence.json.JsonEntity;
import org.swordess.persistence.json.java.test.model.Group;
import org.swordess.persistence.json.java.test.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JsonDBTest {

    private static File sourceDataLocation = new File("src/test/resources/org/swordess/persistence/json/test/data");
    private static File destinationDataLocation;

    private JsonDB jsondb;

    static {
        destinationDataLocation = new File("build/tmp/test/" + JsonDBTest.class.getSimpleName() + "/data");
        if (!destinationDataLocation.exists()) {
            destinationDataLocation.mkdirs();
        }
    }

    private static void copySourceToDestination() throws IOException {
        for (File it : sourceDataLocation.listFiles()) {
            FileOutputStream outStream = new FileOutputStream(new File(destinationDataLocation, it.getName()));
            Files.copy(it.toPath(), outStream);
            outStream.close();
        }
    }

    private static void cleanDestinationData() {
        for (File it : destinationDataLocation.listFiles()) {
            it.delete();
        }
    }

    private static String dataFileContent(Class c) throws IOException {
        JsonEntity anno = (JsonEntity) c.getAnnotation(JsonEntity.class);
        StringBuilder content = new StringBuilder();
        for (Iterator<String> iter = Files.readAllLines(new File(destinationDataLocation, anno.filename() + Database.JSON_FILE_EXTENSION).toPath(), Charsets.UTF_8).iterator(); iter.hasNext();) {
            String line = iter.next();
            content.append(line);
            if (iter.hasNext()) {
                content.append('\n');
            }
        }
        return content.toString();
    }

    @Before
    public void setUp() throws IOException {
        copySourceToDestination();

        EntityMetadataManager manager = new EntityMetadataManager();
        manager.setPackagesToScan(Arrays.asList("org.swordess.persistence.json.java.test.model"));
        manager.init();

        Database database = new Database();
        database.setEntityMetadataManager(manager);
        database.setDataLocation(destinationDataLocation.getAbsolutePath());
        database.init();

        jsondb = new JsonDB();
        jsondb.setDatabase(database);
    }

    @After
    public void tearDown() {
        jsondb.database.destroy();
        cleanDestinationData();
    }

    @Test
    public void testSaveToEmptyFile() throws IOException {
        Group group = new Group();
        group.setName("hands-on");
        jsondb.save(group);

        String expected = "{\n" +
                "  \"idGenerator\": 2,\n" +
                "  \"rows\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"name\": \"hands-on\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        assertEquals(expected, dataFileContent(Group.class));
    }

    @Test
    public void testSave() throws IOException {
        User user = new User();
        user.setUsername("Bar");
        user.setAge(40);
        jsondb.save(user);

        String expected = "{\n" +
                "  \"idGenerator\": 3,\n" +
                "  \"rows\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"username\": \"Foo\",\n" +
                "      \"age\": 20\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"username\": \"Bar\",\n" +
                "      \"age\": 40\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        assertEquals(expected, dataFileContent(User.class));
    }

    @Test
    public void testFind() {
        assertEquals(1, jsondb.findAll(JvmClassMappingKt.getKotlinClass(User.class)).size());

        User found = jsondb.findOne(JvmClassMappingKt.getKotlinClass(User.class), 1);
        assertEquals(1L, found.getId().longValue());
        assertEquals("Foo", found.getUsername());
        assertEquals(20, found.getAge().intValue());
    }

    @Test
    public void testUpdate() throws IOException {
        User record = jsondb.findOne(JvmClassMappingKt.getKotlinClass(User.class), 1);
        record.setAge(24);
        jsondb.update(record);

        String expected = "{\n" +
                "  \"idGenerator\": 2,\n" +
                "  \"rows\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"username\": \"Foo\",\n" +
                "      \"age\": 24\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        assertEquals(expected, dataFileContent(User.class));
    }

    @Test
    public void testRemove() throws IOException {
        jsondb.remove(JvmClassMappingKt.getKotlinClass(User.class), 1);

        String expected = "{\n" +
                "  \"idGenerator\": 2,\n" +
                "  \"rows\": []\n" +
                "}";
        assertEquals(expected, dataFileContent(User.class));
    }

    @Test
    public void testExceptionShouldBeThrownWhenManualIdHasBeenSet() {
        User user = new User();
        user.setId(2L);
        try {
            jsondb.save(user);
            fail("exception is expected");
        } catch (RuntimeException e) {
            assertEquals("row with manual id should not be added: 2, call replace instead", e.getMessage());
        }
    }

}
