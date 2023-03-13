package com.example.jooq;

import org.jooq.Records;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.Test;

import static com.example.jooq.Database.withJooq;
import static com.example.jooq.db.tables.Film.FILM;
import static com.example.jooq.db.tables.Actor.ACTOR;
import static com.example.jooq.db.tables.FilmActor.FILM_ACTOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.array;
import static org.jooq.impl.DSL.row;

public class NestedRecordTest {

    record FilmProjection(int id, String title, ActorProjection[] actors) {
    }

    record ActorProjection(String firstName, String lastName) {
    }

    @Test
    public void select_nested_records() {
        withJooq((dsl) -> {

            // select "public"."film"."film_id",
            //        "public"."film"."title",
            //        array(
            //              select row ("public"."actor"."first_name", "public"."actor"."last_name") as "nested"
            //                from "public"."actor" join "public"."film_actor" on "public"."actor"."actor_id" = cast("public"."film_actor"."actor_id" as int)
            //               where "public"."film"."film_id" = cast("public"."film_actor"."film_id" as int)) as "nested"
            //   from "public"."film"
            //  where "public"."film"."film_id" = ?
            var film = dsl.select(
                            FILM.FILM_ID,
                            FILM.TITLE,
                            array(
                                    DSL.select(row(ACTOR.FIRST_NAME, ACTOR.LAST_NAME).mapping(ActorProjection.class, ActorProjection::new))
                                            .from(ACTOR).join(FILM_ACTOR)
                                            .on(ACTOR.ACTOR_ID.eq(FILM_ACTOR.ACTOR_ID.cast(SQLDataType.INTEGER)))
                                            .where(FILM.FILM_ID.eq(FILM_ACTOR.FILM_ID.cast(SQLDataType.INTEGER)))
                            )
                    )
                    .from(FILM)
                    .where(FILM.FILM_ID.eq(15))
                    .fetchSingle(Records.mapping(FilmProjection::new));

            assertThat(film.title()).isEqualTo("ALIEN CENTER");
            assertThat(film.actors()).hasSize(6)
                    .contains(new ActorProjection("HUMPHREY", "WILLIS"));
        });
    }
}
