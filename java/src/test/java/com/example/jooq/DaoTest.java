package com.example.jooq;

import com.example.jooq.db.tables.daos.FilmDao;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DaoTest {

    @Test
    void generates_daos() {
        Configuration conf = new DefaultConfiguration()
                .set(Database.dataSource())
                .set(SQLDialect.POSTGRES)
                .set(Database.defaultSettings());

        var filmDao = new FilmDao(conf);

        var film = filmDao.fetchOneByFilmId(16);

        assertThat(film.title()).isEqualTo("ALLEY EVOLUTION");
    }
}
