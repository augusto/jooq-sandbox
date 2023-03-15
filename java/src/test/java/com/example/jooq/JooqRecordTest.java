package com.example.jooq;

import com.example.jooq.db.tables.records.ActorRecord;
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
            ActorRecord actorRecord = dsl.newRecord(ACTOR);
            actorRecord.setFirstName("John");
            actorRecord.setLastName("Doe");
            actorRecord.insert();
            assertThat(actorRecord.getActorId()).isPositive();

            // Read
            ActorRecord existingActor = dsl.fetchSingle(ACTOR, ACTOR.ACTOR_ID.eq(actorRecord.getActorId()));
            assertThat(existingActor.getFirstName()).isEqualTo("John");

            // Update
            existingActor.setLastName("Smith");
            existingActor.update();

            // Delete
            existingActor.delete();
        });
    }
}
