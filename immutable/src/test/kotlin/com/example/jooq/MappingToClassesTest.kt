package com.example.jooq

import com.example.jooq.db.tables.Actor.Companion.ACTOR
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.beans.ConstructorProperties

/**
 * jOOQ uses the class DefaultRecordMapper to map Record instances to pojos.
 *
 * @see <a href="https://www.jooq.org/javadoc/3.17.x/org.jooq/org/jooq/impl/DefaultRecordMapper.html">DefaultRecordMapper javadoc</a>
 */
class MappingToClassesTest {

    @Test
    fun `can select into an arbitrary object but non matching fields are ignored`() {
        data class TestActor(val id: Int, val firstName: String, val lastName: String)

        Database.withJooq { create ->
            val myDomainActor: TestActor = create.select()
                .from(ACTOR)
                .where(ACTOR.ACTOR_ID.eq(1))
                .fetchSingleInto(TestActor::class.java)

            // Fields which match the column names are mapped automatically.
            expectThat(myDomainActor.firstName).isEqualTo("PENELOPE")
            expectThat(myDomainActor.lastName).isEqualTo("GUINESS")

            // But fields which are not matched, are ignored silently when the class
            // has no mapping annotations. In this scenario, the column in the db
            // is `actor_id`
            expectThat(myDomainActor.id).isEqualTo(0)
        }
    }

    @Test
    @Disabled
    fun `can select into an object using JPA annotations`() {
        // Well, not anymore. This feature will be removed in jOOQ 4.0
        // https://github.com/jOOQ/jOOQ/issues/4263
    }

    @Test
    fun `can select into an immutable object by specifying the parameter order`() {
        data class TestActor
        @ConstructorProperties(value = ["actor_id", "first_name", "last_name"])
        constructor(
            val id: Int,
            val firstName: String,
            val lastName: String
        )

        Database.withJooq { create ->
            val myDomainActor: TestActor = create.select()
                .from(ACTOR)
                .where(ACTOR.ACTOR_ID.eq(1))
                .fetchSingleInto(TestActor::class.java)

            // Fields which match the column names are mapped automatically.
            expectThat(myDomainActor.firstName).isEqualTo("PENELOPE")
            expectThat(myDomainActor.lastName).isEqualTo("GUINESS")

            // Now the actor_id is mapped correctly
            expectThat(myDomainActor.id).isEqualTo(1)
        }
    }
}
