package com.example.jooq

import com.example.jooq.db.tables.references.ACTOR
import com.example.jooq.db.tables.references.FILM
import com.example.jooq.db.tables.references.FILM_ACTOR
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class FetchRowAndChildrenTest {

    @Test
    fun `Can select nested records`() {
        data class ActorProjection(val firstName: String, val lastName: String)
        data class FilmProjection(val id: Int, val title: String, val actors: List<ActorProjection>)

        Database.withJooq { dsl ->

            // select "public"."film"."film_id",
            //        "public"."film"."title",
            //        array(
            //              select row ("public"."actor"."first_name", "public"."actor"."last_name") as "nested"
            //                from "public"."actor" join "public"."film_actor" on "public"."actor"."actor_id" = cast("public"."film_actor"."actor_id" as int)
            //               where "public"."film"."film_id" = cast("public"."film_actor"."film_id" as int)) as "nested"
            //   from "public"."film"
            //  where "public"."film"."film_id" = ?
            val film = dsl.select(
                FILM.FILM_ID,
                FILM.TITLE,
                DSL.array(
                    DSL.select(DSL.row(ACTOR.FIRST_NAME, ACTOR.LAST_NAME))
                        .from(ACTOR).join(FILM_ACTOR)
                        .on(ACTOR.ACTOR_ID.eq(FILM_ACTOR.ACTOR_ID.cast(SQLDataType.INTEGER)))
                        .where(FILM.FILM_ID.eq(FILM_ACTOR.FILM_ID.cast(SQLDataType.INTEGER)))
                ).`as`("nested")
            )
                .from(FILM)
                .where(FILM.FILM_ID.eq(15))
                .fetchSingle { film ->
                    val actors = film.value3()
                        .map { actor -> ActorProjection(actor[ACTOR.FIRST_NAME]!!, actor[ACTOR.LAST_NAME]!!) }
                    FilmProjection(film[FILM.FILM_ID]!!, film[FILM.TITLE]!!, actors)
                }

            expectThat(film.title).isEqualTo("ALIEN CENTER")
            expectThat(film.actors).hasSize(6)
            expectThat(film.actors).contains(ActorProjection("HUMPHREY", "WILLIS"))
        }
    }
}