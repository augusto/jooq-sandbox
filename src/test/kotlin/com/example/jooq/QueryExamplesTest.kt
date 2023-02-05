package com.example.jooq

import com.example.jooq.db.tables.Actor.ACTOR
import com.example.jooq.domain.Actor
import org.jooq.Record
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class QueryExamplesTest {
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

        Database.withJooq { create ->
            val actors = create.select()
                .from(ACTOR)
                .limit(10)
                .fetch {
                    Actor(
                        id = it[ACTOR.ACTOR_ID],
                        firstName = it[ACTOR.FIRST_NAME],
                        lastName = it[ACTOR.LAST_NAME]
                    )
                }
            print(actors)
        }
    }

    @Test
    fun `Record API (row)`() {
        Database.withJooq { create ->
            val selectActorById = create.select()
                .from(ACTOR)
                .where(ACTOR.ACTOR_ID.eq(1))

            var actor: Record = selectActorById.fetchSingle()

            actor[ACTOR.LAST_NAME] = "New Name"
            expectThat(actor.changed()).isTrue()
        }
    }
}
