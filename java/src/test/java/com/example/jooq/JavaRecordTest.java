package com.example.jooq;

import com.example.jooq.db.tables.pojos.Actor;
import com.example.jooq.db.tables.pojos.Film;
import org.jooq.Records;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.jooq.db.tables.Film.FILM;
import static com.example.jooq.db.tables.Actor.ACTOR;
import static com.example.jooq.db.tables.FilmActor.FILM_ACTOR;
import static org.assertj.core.api.Assertions.assertThat;

public class JavaRecordTest {

    @Test
    void select_record() {
        Database.withJooq((dsl) -> {
            var film = dsl.fetchSingle(FILM, FILM.FILM_ID.eq(19));

            assertThat(film.getTitle()).isEqualTo("AMADEUS HOLY");
        });
    }

    @Test
    void select_into_pojo() {
        Database.withJooq((dsl) -> {
            var film = dsl.select()
                    .from(FILM)
                    .where(FILM.FILM_ID.eq(19))
                    .fetchSingleInto(Film.class);

            assertThat(film.title()).isEqualTo("AMADEUS HOLY");
        });
    }

    record FilmProjection(int id, String title, ActorProjection[] actors) {
    }

    record ActorProjection(String firstName, String lastName) {
    }

    @Test
    public void select_nested_records() {
        Database.withJooq((dsl) -> {

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
                            DSL.array(
                                    DSL.select(DSL.row(ACTOR.FIRST_NAME, ACTOR.LAST_NAME).mapping(ActorProjection.class, ActorProjection::new))
                                            .from(ACTOR).join(FILM_ACTOR)
                                            .on(ACTOR.ACTOR_ID.eq(FILM_ACTOR.ACTOR_ID.cast(SQLDataType.INTEGER)))
                                            .where(FILM.FILM_ID.eq(FILM_ACTOR.FILM_ID.cast(SQLDataType.INTEGER)))
                            )
                    )
                    .from(FILM)
                    .where(FILM.FILM_ID.eq(15))
                    .fetchSingle(Records.mapping(FilmProjection::new));

            assertThat(film.title()).isEqualTo("ALIEN CENTER");
            assertThat(film.actors()).hasSize(6);
            assertThat(film.actors()).contains(new ActorProjection("HUMPHREY", "WILLIS"));
        });
    }
}
