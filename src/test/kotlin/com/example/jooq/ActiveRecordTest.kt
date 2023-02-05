package com.example.jooq

import com.example.jooq.db.tables.Actor.ACTOR
import org.junit.jupiter.api.Test

/**
 * When using Active Record (Generator setting generator.generate.isRecords), jooq will
 * create an active record which can be used to insert/update records back to the DB.
 *
 * @see <a href="https://www.martinfowler.com/eaaCatalog/activeRecord.html">Data Source Architectural Patterns: Active record in Patterns of Enterprise Application Architecture.</a>
 */
class ActiveRecordTest {
    @Test
    fun `can create a new mutable record`() {
        Database.withJooq { dsl ->
            val javier = dsl.newRecord(ACTOR)

            javier.firstName = "Javier"
            javier.lastName = "Bardem"

            javier.store()

            println(javier)
        }
    }
}