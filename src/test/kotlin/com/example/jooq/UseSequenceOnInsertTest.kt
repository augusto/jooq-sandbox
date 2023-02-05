package com.example.jooq

import com.example.jooq.db.Public
import com.example.jooq.db.Sequences.ACTOR_ACTOR_ID_SEQ
import com.example.jooq.db.tables.Actor.ACTOR
import org.jooq.impl.DSL.asterisk
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isGreaterThan
import org.jooq.impl.DSL.`val` as dslVal

class UseSequenceOnInsertTest {

    @Test
    fun `query the sequence before inserting (don't do this!)`() {
        Database.withJooq { dsl ->

            val andrew = dsl.newRecord(ACTOR)

            andrew[ACTOR.ACTOR_ID] = dsl.nextval(ACTOR_ACTOR_ID_SEQ).toInt()
            andrew[ACTOR.FIRST_NAME] = "Andrew"
            andrew[ACTOR.LAST_NAME] = "Garfield"
            andrew.store()

            expectThat(andrew[ACTOR.ACTOR_ID]).isGreaterThan(0)
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

            expectThat(tobey[ACTOR.ACTOR_ID]).isGreaterThan(0)
            println(tobey)
        }
    }

    @Test
    fun `can create a new record using a db sequence`() {
        Database.withJooq { dsl ->
            // The sakila schema uses INTEGER for primary keys instead of BIGINT, so we
            // need to instantiate our own copy of the sequence. This is not great, but it
            // shows how good is the type safety in jooq!
            // There might be a way to override this in the code generation.
            val actorSequence = Internal.createSequence(
                "actor_actor_id_seq",
                Public.PUBLIC,
                SQLDataType.INTEGER.nullable(false),
                null,
                null,
                null,
                null,
                false,
                null
            )

            val tom = dsl.insertInto(ACTOR, ACTOR.ACTOR_ID, ACTOR.FIRST_NAME, ACTOR.LAST_NAME)
                .values(actorSequence.nextval(), dslVal("Tom"), dslVal("Holland"))
                .returningResult(asterisk())
                .fetchSingle()

            expectThat(tom[ACTOR.ACTOR_ID]).isGreaterThan(0)
            println(tom)
        }
    }
}