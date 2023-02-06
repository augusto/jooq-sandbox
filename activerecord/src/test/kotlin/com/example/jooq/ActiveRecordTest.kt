package com.example.jooq

import com.example.jooq.db.tables.Actor.ACTOR
import com.example.jooq.db.tables.records.ActorRecord
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isGreaterThan
import strikt.assertions.isLessThan
import strikt.assertions.isNotNull
import strikt.assertions.withNotNull
import java.time.LocalDateTime
import java.time.LocalDateTime.now

/**
 * When using Active Record (Generator setting generator.generate.isRecords), jooq will
 * create an active record which can be used to insert/update records back to the DB.
 *
 * @see <a href="https://www.martinfowler.com/eaaCatalog/activeRecord.html">Data Source Architectural Patterns: Active record in Patterns of Enterprise Application Architecture.</a>
 */
class ActiveRecordTest {
    @Test
    fun `can create a new active record instance`() {
        val settings = Database.defaultSettings()
            .withReturnAllOnUpdatableRecord(true)

        Database.withJooq(settings) { dsl ->
            // When created in this way, the ActorRecord is attached to the current
            // db connection.
            val javier: ActorRecord = dsl.newRecord(ACTOR)


            javier.firstName = "Javier"
            javier.lastName = "Bardem"

            // Note: The DB defaults both the actor_id and last_updated columns.
            // Jooq by default only refreshes the PK. To refresh
            // all values, set `Settings.withReturnAllOnUpdatableRecord(true)`
            javier.store()

            expectThat(javier)
                .and { get { actorId }.isGreaterThan(0) }
                .and {
                    get { lastUpdate }
                        .isGreaterThan(now().minusSeconds(5))
                        .isLessThan(now())
                }

            println(javier)
        }
    }

    @Test
    fun `can convert attach a record and store it`() {
        val actor = ActorRecord(null, "Amy", "Adams", null)

        val settings = Database.defaultSettings()
            .withReturnAllOnUpdatableRecord(true)

        Database.withJooq(settings) { dsl ->
            dsl.attach(actor)
            // not great, but reset the fields so the defaults are taken from the db.
            actor.reset(ACTOR.ACTOR_ID)
            actor.reset(ACTOR.LAST_UPDATE)
            actor.store()

            println(actor)
        }
    }
}