package com.example.jooq;

import com.example.jooq.db.Tables;
import com.example.jooq.db.tables.Actor;
import com.example.jooq.db.tables.pojos.Film;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.jooq.Database.withJooq;
import static com.example.jooq.db.Tables.*;
import static com.example.jooq.db.tables.Actor.ACTOR;
import static com.example.jooq.db.tables.Film.FILM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.SQLDataType.INTEGER;

public class BasicSqlTest {

    @Test
    void can_generate_sql_without_a_connection() {
        var query = DSL.select(FILM.TITLE)
                .from(FILM)
                .where(FILM.FILM_ID.eq(19));
        var sql = query.getSQL();
        var bindValues = query.getBindValues();

        assertThat(sql).isEqualTo("""
                select "public"."film"."title" from "public"."film" where "public"."film"."film_id" = ?""");
        assertThat(bindValues).isEqualTo(List.of(19));

    }

    @Test
    void select_record() {
        withJooq((dsl) -> {
            var film = dsl.fetchSingle(FILM, FILM.FILM_ID.eq(19));

            assertThat(film.getTitle()).isEqualTo("AMADEUS HOLY");
        });
    }

    @Test
    void select_into_pojo() {
        withJooq((dsl) -> {
            var film = dsl.select()
                    .from(FILM)
                    .where(FILM.FILM_ID.eq(19))
                    .fetchSingleInto(Film.class);

            assertThat(film.title()).isEqualTo("AMADEUS HOLY");
        });
    }

    record FilmAndActor(String filmTitle, String actorFirstName, String actorLastName) {
    }

    @Test
    void select_with_joins() {
        withJooq((dsl) -> {
            var filmAndActors = dsl.select(FILM.TITLE, ACTOR.FIRST_NAME, ACTOR.LAST_NAME)
                    .from(FILM)
                    .join(FILM_ACTOR).on(FILM.FILM_ID.eq(FILM_ACTOR.FILM_ID.cast(INTEGER)))
                    .join(Tables.ACTOR).on(FILM_ACTOR.ACTOR_ID.cast(INTEGER).eq(ACTOR.ACTOR_ID))
                    .where(FILM.FILM_ID.eq(19))
                    .fetchInto(FilmAndActor.class);

            assertThat(filmAndActors).hasSize(6);
            assertThat(filmAndActors).contains(new FilmAndActor("AMADEUS HOLY", "JAMES", "PITT"));
        });
    }

    @Test
    void select_with_custom_mapper() {
        withJooq((dsl) -> {

            var filmAndActors = dsl.select(FILM.TITLE, ACTOR.FIRST_NAME, ACTOR.LAST_NAME)
                    .from(FILM)
                    .join(FILM_ACTOR).on(FILM.FILM_ID.eq(FILM_ACTOR.FILM_ID.cast(INTEGER)))
                    .join(Tables.ACTOR).on(FILM_ACTOR.ACTOR_ID.cast(INTEGER).eq(ACTOR.ACTOR_ID))
                    .where(FILM.FILM_ID.eq(19))
                    .fetch().map((record) -> new FilmAndActor(record.get(FILM.TITLE),
                            record.get(ACTOR.FIRST_NAME),
                            record.get(ACTOR.LAST_NAME)));

            assertThat(filmAndActors).hasSize(6);
            assertThat(filmAndActors).contains(new FilmAndActor("AMADEUS HOLY", "JAMES", "PITT"));
        });
    }

    @Test
    void upsert_actor() {
        withJooq((dsl) -> {
            // insert into "public"."actor" ("first_name", "last_name")
            // values (?, ?)
            // on conflict ("actor_id") do update set "first_name" = ?, "last_name" = ?
            // returning "public"."actor"."actor_id"
            var insertedRow = dsl.insertInto(Actor.ACTOR, ACTOR.FIRST_NAME, ACTOR.LAST_NAME)
                    .values("John", "Doe")
                    .onDuplicateKeyUpdate()
                    .set(Actor.ACTOR.FIRST_NAME, "John")
                    .set(Actor.ACTOR.LAST_NAME, "Doe")
                    .returning(Actor.ACTOR.ACTOR_ID)
                    .fetchSingle();

            assertThat(insertedRow.getActorId()).isPositive();
        });
    }
}
