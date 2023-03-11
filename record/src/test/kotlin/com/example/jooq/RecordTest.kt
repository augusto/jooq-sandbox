package com.example.jooq

import com.example.jooq.db.tables.Actor.ACTOR
import com.example.jooq.db.tables.records.ActorRecord
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*
import java.time.LocalDateTime.now

/**
 * When using Record (Generator setting generator.generate.isRecords), jooq will create a Record classes
 * (Row Data Gateway or similar to an Active Record) which can be used to insert/update records back to the DB.
 *
 * Records are created using mutable Java classes, so the nullable data is lost in Kotlin.
 *
 * @see <a href="https://www.martinfowler.com/eaaCatalog/activeRecord.html">Active record in Patterns of Enterprise Application Architecture.</a>
 * @see <a href="https://martinfowler.com/eaaCatalog/rowDataGateway.html">Row Data Gateway in Patterns of Enterprise Application Architecture.</a>
 */
class RecordTest {
    @Test
    fun `can create a new record instance`() {
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
    fun `can attach a record and store it`() {
        val actor = ActorRecord(null, "Amy", "Adams", null)

        val settings = Database.defaultSettings()
            .withReturnAllOnUpdatableRecord(true)

        Database.withJooq(settings) { dsl ->
            dsl.attach(actor)
            // not great, but reset the fields so the defaults are computed in the db.
            actor.reset(ACTOR.ACTOR_ID)
            actor.reset(ACTOR.LAST_UPDATE)
            actor.store()

            println(actor)
        }
    }

    @Test
    fun `can delete a record`() {

        val newActorId= Database.withJooq { dsl ->
            val actor = dsl.newRecord(ACTOR)
            actor.firstName = "Joe"
            actor.lastName = "Blogs"
            actor.store()

            actor.actorId!!
        }

        Database.withJooq { dsl ->
            val actor = dsl.fetchSingle(ACTOR, ACTOR.ACTOR_ID.eq(newActorId))
            actor.delete()

            val maybeActor = dsl.fetchOne(ACTOR, ACTOR.ACTOR_ID.eq(newActorId))
            expectThat(maybeActor).isNull()
        }
    }
}