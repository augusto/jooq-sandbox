package com.example.jooq

import com.example.jooq.db.keys.FILM_ACTOR__FILM_ACTOR_FILM_ID_FKEY
import com.example.jooq.db.keys.FILM__FILM_LANGUAGE_ID_FKEY
import com.example.jooq.db.tables.pojos.Language
import com.example.jooq.db.tables.references.FILM
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

//Navigation only works with Records
class NavigationTest {
    @Test
    fun `Can navigate Many to One foreign keys`() {
        Database.withJooq { dsl ->

            val film = dsl.fetchSingle(FILM, FILM.FILM_ID.eq(5))

            val language = film.fetchParent(FILM__FILM_LANGUAGE_ID_FKEY)?.into(Language::class.java)

            expectThat(language?.name?.trim()).isEqualTo("English")
        }
    }

    @Test
    fun `Can navigate One to Many foreign keys`() {
        Database.withJooq { dsl ->
            val film = dsl.fetchSingle(FILM, FILM.FILM_ID.eq(5))

            val filmActor = film.fetchChildren(FILM_ACTOR__FILM_ACTOR_FILM_ID_FKEY)

            expectThat(filmActor.size).isEqualTo(5)
        }
    }
}