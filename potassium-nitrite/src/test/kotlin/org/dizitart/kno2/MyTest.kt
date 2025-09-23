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
package org.dizitart.kno2

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.dizitart.no2.Nitrite
import org.dizitart.no2.collection.Document
import org.dizitart.no2.common.mapper.EntityConverter
import org.dizitart.no2.common.mapper.NitriteMapper
import org.dizitart.no2.index.IndexType
import org.dizitart.no2.mvstore.MVStoreModule
import org.dizitart.no2.repository.annotations.Id
import org.dizitart.no2.repository.annotations.Index
import org.dizitart.no2.repository.annotations.Indices
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*

class MyTest {

    /**
     * This was executed using nitrite v4.3.0. The database was then copied to /resources/no2-v4.3.0.db.
     */
    // @Test
    fun createTestDatabase() {

        val databaseSecret = "password"

        val module = MVStoreModule.withConfig()
            .filePath(Path.of("./my.db").toAbsolutePath().toFile())
            .encryptionKey(databaseSecret.toCharArray())
            .compress(true)
            .build()

        val database = Nitrite.builder()
            .loadModule(module)
            .disableRepositoryTypeValidation()
            .registerEntityConverter(PersonEntityConverter())
            .openOrCreate("user", databaseSecret)

        database.use {
            database.getRepository<Person> {
                repeat(10) { i ->
                    insert(
                        Person(
                            id = UUID.randomUUID().toString(),
                            name = "Joe $i",
                            yearOfBirth = 2000
                        )
                    )
                }
            }
        }
    }

    @Test
    fun read() {

        Configurator.setLevel("org.dizitart.no2.mvstore", Level.INFO)

        // setup no2-v4.3.0.db as a temp file
        val databasePath = Files.createTempFile("nitrite-native", ".db")
        val templateDb = MyTest::class.java.getResourceAsStream("/no2-v4.3.0.db")!!
        Files.copy(templateDb, databasePath, StandardCopyOption.REPLACE_EXISTING)

        // open the database
        val databaseSecret = "password"

        val module = MVStoreModule.withConfig()
            .filePath(databasePath.toAbsolutePath().toFile())
            .encryptionKey(databaseSecret.toCharArray())
            .compress(true)
            .build()

        val database = Nitrite.builder()
            .loadModule(module)
            .disableRepositoryTypeValidation()
            .registerEntityConverter(PersonEntityConverter())
            .openOrCreate("user", databaseSecret)

        database.use {
            assertThat(database.getRepository<Person>().size(), equalTo(10))
        }
    }

    @Indices(
        value = [
            Index(fields = ["yearOfBirth"], type = IndexType.NON_UNIQUE),
        ]
    )
    data class Person(
        @Id
        val id: String,
        val name: String,
        val yearOfBirth: Int
    )

    class PersonEntityConverter : EntityConverter<Person> {

        override fun getEntityType(): Class<Person> = Person::class.java

        override fun toDocument(person: Person, nitriteMapper: NitriteMapper): Document {
            return documentOf(
                Person::id.name to person.id,
                Person::name.name to person.name,
                Person::yearOfBirth.name to person.yearOfBirth
            )
        }

        override fun fromDocument(document: Document, nitriteMapper: NitriteMapper): Person {
            return Person(
                id = document.get(Person::id.name, String::class.java),
                name = document.get(Person::name.name, String::class.java),
                yearOfBirth = document.get(Person::yearOfBirth.name, Int::class.java)
            )
        }
    }
}
