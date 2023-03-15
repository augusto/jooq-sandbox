package com.example.jooq

import com.example.jooq.db.tables.Actor.Companion.ACTOR
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class BasicQueryExamplesTest {
    @Test
    fun `Query actors`() {
        Database.withJooq { create ->
            val actors = create.select()
                .from(ACTOR)
                .limit(10)
                .fetch() // fetch executes the query.

            expectThat(actors).hasSize(10)
            print(actors)
        }
    }

    @Test
    fun `Fetch one actor`() {
        Database.withJooq { create ->
            // select "public"."actor"."actor_id", "public"."actor"."first_name", "public"."actor"."last_name", "public"."actor"."last_update"
            // from "public"."actor"
            // where "public"."actor"."actor_id" = ?
            val selectActorById = create.select()
                .from(ACTOR)
                .where(ACTOR.ACTOR_ID.eq(1))

            // Returns a Record<T> or null
            expectThat(selectActorById.fetchOne()).isNotNull()
            // Returns a Record or throws exception
            expectThat(selectActorById.fetchSingle()).isNotNull()
            // Returns an Optional<Record>
            expectThat(selectActorById.fetchOptional().isPresent).isTrue()
            // Returns the first record or null
            expectThat(selectActorById.fetchAny()).isNotNull()
        }
    }

    @Test
    fun `Query actors and map to domain classes`() {
        data class TestActor(val id:Int, val firstName:String, val lastName: String)

        Database.withJooq { create ->
            // select "public"."actor"."actor_id", "public"."actor"."first_name", "public"."actor"."last_name", "public"."actor"."last_update"
            // from "public"."actor"
            // fetch next ? rows only
            val actors = create.select()
                .from(ACTOR)
                .limit(10)
                .fetch {
                    TestActor(
                        id = it[ACTOR.ACTOR_ID]!!,
                        firstName = it[ACTOR.FIRST_NAME]!!,
                        lastName = it[ACTOR.LAST_NAME]!!
                    )
                }
            print(actors)
        }
    }
}
