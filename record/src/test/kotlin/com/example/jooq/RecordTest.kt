package com.example.jooq

import com.example.jooq.db.tables.records.ActorRecord
import com.example.jooq.db.tables.references.ACTOR
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
            javier.actorId

            expectThat(javier.actorId)
                .isNotNull()
                .isGreaterThan(0)
            expectThat(javier.lastUpdate)
                .isNotNull()
                .isIn(now().minusSeconds(5).. now())

            println(javier)
        }
    }

    @Test
    fun `can attach a record and insert it`() {
        val actor = ActorRecord()
        actor.firstName = "Amy"
        actor.lastName = "Adams"

        val settings = Database.defaultSettings()
            .withReturnAllOnUpdatableRecord(true)

        Database.withJooq(settings) { dsl ->
            dsl.attach(actor)
            actor.insert()

            println(actor)
        }
    }

    @Test
    fun `can create a new record from ta pojo`() {
        val actor = com.example.jooq.db.tables.pojos.Actor(
            firstName = "Jeremy",
            lastName = "Renner"
        )

        val settings = Database.defaultSettings()
            .withReturnAllOnUpdatableRecord(true)

        Database.withJooq(settings) { dsl ->
            val newActor = dsl.newRecord(ACTOR, actor)
            dsl.attach(newActor)
            newActor.insert()

            expectThat(newActor.actorId)
                .isNotNull()
                .isGreaterThan(0)
            expectThat(newActor.lastUpdate)
                .isNotNull()
                .isIn(now().minusSeconds(5).. now())

            println(newActor)
        }
    }

    @Test
    fun `can delete a record`() {

        val newActorId = Database.withJooq { dsl ->
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