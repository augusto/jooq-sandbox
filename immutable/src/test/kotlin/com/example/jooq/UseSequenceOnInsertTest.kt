package com.example.jooq

import com.example.jooq.db.sequences.ACTOR_ACTOR_ID_SEQ
import com.example.jooq.db.tables.Actor.Companion.ACTOR
import org.jooq.impl.DSL.asterisk
import org.jooq.impl.SQLDataType
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotNull
import org.jooq.impl.DSL.`val` as dslVal

class UseSequenceOnInsertTest {
    @Test
    fun `query the sequence before inserting (don't do this!)`() {
        Database.withJooq { dsl ->

            val actorId = dsl.nextval(ACTOR_ACTOR_ID_SEQ).toInt()

            val andrew = dsl.insertInto(ACTOR, ACTOR.ACTOR_ID, ACTOR.FIRST_NAME, ACTOR.LAST_NAME)
                .values(actorId, "Andrew", "Garfield")
                .returningResult(asterisk())
                .fetchSingle()

            println(andrew)
        }
    }

    @Test
    fun `can create a new record and let the db pick the id from the default value`() {
        Database.withJooq { dsl ->
            val tobey = dsl.insertInto(ACTOR, ACTOR.FIRST_NAME, ACTOR.LAST_NAME)
                .values("Tobey", "Maguire")
                .returningResult(asterisk())
                .fetchSingle()

            expectThat(tobey[ACTOR.ACTOR_ID])
                .isNotNull().isGreaterThan(0)
            println(tobey)
        }
    }

    @Test
    fun `can create a new record using an explicit sequence`() {
        Database.withJooq { dsl ->
            // In this case, the values need to be wrapped in calls to `DSL.val`, which doesn't help readability.
            val tom = dsl.insertInto(ACTOR, ACTOR.ACTOR_ID, ACTOR.FIRST_NAME, ACTOR.LAST_NAME)
                .values(ACTOR_ACTOR_ID_SEQ.nextval().cast(SQLDataType.INTEGER), dslVal("Tom"), dslVal("Holland"))
                .returningResult(asterisk())
                .fetchSingle()

            expectThat(tom[ACTOR.ACTOR_ID])
                .isNotNull().isGreaterThan(0)
            println(tom)
        }
    }
}