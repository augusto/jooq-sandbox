package com.example.jooq;

import com.example.jooq.db.tables.pojos.Actor;
import com.example.jooq.db.tables.records.FilmRecord;
import org.junit.jupiter.api.Test;

import static com.example.jooq.Database.withJooq;
import static com.example.jooq.db.tables.Actor.ACTOR;
import static com.example.jooq.db.tables.Film.FILM;
import static org.assertj.core.api.Assertions.assertThat;

public class JooqRecordTest {
    @Test
    void select_record() {
        withJooq((dsl) -> {
            FilmRecord film = dsl.fetchSingle(FILM, FILM.FILM_ID.eq(19));

            assertThat(film.getTitle()).isEqualTo("AMADEUS HOLY");
        });
    }

    @Test
    void create_update_delete_record() {
        withJooq((dsl) -> {
            var newActorPojo = new Actor(null, "John", "Doe", null);
            var actorRecord = dsl.newRecord(ACTOR, newActorPojo);
            actorRecord.insert();

            actorRecord.setLastName("Smith");
            actorRecord.update();

            actorRecord.delete();
        });
    }
}
