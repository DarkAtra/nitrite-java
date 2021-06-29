/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.integration.stream;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.integration.Retry;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.streams.DocumentStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.util.Iterator;

import static org.dizitart.no2.integration.TestUtil.createDb;
import static org.dizitart.no2.collection.Document.createDocument;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class DocumentCursorTest {
    private Nitrite db;

    @Rule
    public Retry retry = new Retry(3);

    @Test
    public void testFindResult() {
        db = createDb();
        NitriteCollection collection = db.getCollection("test");
        collection.insert(createDocument("first", "second"));

        DocumentCursor result = collection.find();
        assertTrue(result instanceof DocumentStream);
    }

    @Test(expected = InvalidOperationException.class)
    public void testIteratorRemove() {
        db = createDb();
        NitriteCollection collection = db.getCollection("test");
        collection.insert(createDocument("first", "second"));

        DocumentCursor cursor = collection.find();
        Iterator<Document> iterator = cursor.iterator();
        if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    @Test
    public void testValidateProjection() {
        db = createDb();
        NitriteCollection collection = db.getCollection("test");
        collection.insert(createDocument("first", "second"));

        Document projection = createDocument("first", createDocument("second", null));
        RecordStream<Document> project = collection.find().project(projection);
        assertNotNull(project);
    }

    @After
    public void cleanUp() throws Exception {
        if (db != null && !db.isClosed()) {
            db.close();
        }
    }
}