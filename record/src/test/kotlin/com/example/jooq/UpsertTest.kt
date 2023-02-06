package com.example.jooq

import com.example.jooq.db.sequences.ACTOR_ACTOR_ID_SEQ
import com.example.jooq.db.tables.Actor.Companion.ACTOR
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class UpsertTest {
    @Test
    fun `can upsert a record`() {
        Database.withJooq { dsl ->
            val actorId = dsl.nextval(ACTOR_ACTOR_ID_SEQ).toInt()

            dsl.insertInto(ACTOR, ACTOR.ACTOR_ID, ACTOR.FIRST_NAME, ACTOR.LAST_NAME)
                .values(actorId, "Mer yl", "Stre ep")
                .execute()

            // insert into "public"."actor" ("actor_id", "first_name", "last_name")
            // values (8, 'Meryl', 'Streep')
            // on conflict ("actor_id")
            // do update set "first_name" = 'Meryl', "last_name" = 'Streep'
            dsl.insertInto(ACTOR, ACTOR.ACTOR_ID, ACTOR.FIRST_NAME, ACTOR.LAST_NAME)
                .values(actorId, "Meryl", "Streep")
                .onDuplicateKeyUpdate()
                .set(ACTOR.FIRST_NAME, "Meryl")
                .set(ACTOR.LAST_NAME, "Streep")
                .execute()

            val meryl = dsl.fetchSingle(ACTOR, ACTOR.ACTOR_ID.eq(actorId))

            expectThat(meryl[ACTOR.FIRST_NAME]).isEqualTo("Meryl")
            expectThat(meryl[ACTOR.LAST_NAME]).isEqualTo("Streep")

            println(meryl)
        }
    }
}