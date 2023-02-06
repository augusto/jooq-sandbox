package com.example.jooq

import com.example.jooq.db.tables.Actor.Companion.ACTOR
import com.example.jooq.db.tables.FilmActor.Companion.FILM_ACTOR
import com.example.jooq.db.tables.Store.Companion.STORE
import com.example.jooq.db.tables.pojos.Store
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isGreaterThan

class SandboxTest {
    @Test
    fun `select number of movies each actor appears`() {
        val customSettings = Settings()
            .withQueryTimeout(5) //query timeout in seconds

        Database.withJooq(customSettings) { dsl ->
            val result = dsl.select(ACTOR.FIRST_NAME, ACTOR.LAST_NAME, DSL.count())
                .from(ACTOR)
                .join(FILM_ACTOR).on(ACTOR.ACTOR_ID.eq(FILM_ACTOR.ACTOR_ID.cast(SQLDataType.INTEGER)))
                .groupBy(ACTOR.FIRST_NAME, ACTOR.LAST_NAME)
                .orderBy(ACTOR.FIRST_NAME.asc(), ACTOR.LAST_NAME.asc())
                .limit(20)
                .fetch()

            println(result)
        }
    }

    @Test
    fun `select films into generated pokos`() {

        Database.withJooq { dsl ->
            val result = dsl.select()
                .from(STORE)
                .fetchInto(Store::class.java)

            expectThat(result.size).isGreaterThan(0)

            println(result)
        }
    }
}