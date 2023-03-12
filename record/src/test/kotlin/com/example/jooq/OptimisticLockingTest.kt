package com.example.jooq

import com.example.jooq.db.tables.references.ACTOR
import com.example.jooq.db.tables.references.OPTIMISTIC_LOCKING
import org.jooq.exception.DataChangedException
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue
import java.util.*

/**
 * Jooq can manage optimistic locking for UpdatableRecords. For this to work, 2 settings need to be enabled:
 * 1) in the code generation, enable updatableRecords; 2) in the DSLContext settings, enable executeWithOptimisticLocking.
 *
 * @see <a href="https://www.jooq.org/doc/latest/manual/sql-execution/crud-with-updatablerecords/optimistic-locking/">Optimistic locking with Jooq</a>
 */
class OptimisticLockingTest {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Test
    fun `use optimistic locking with versioninig`() {
        // Jooq doesn't use optimistic locking by default. To enable it the settings
        // need to have `withExecuteWithOptimisticLocking = true`
        Database.withJooq { create ->
            val optimisticLockingRow = create.newRecord(OPTIMISTIC_LOCKING)

            val newRecordId = UUID.randomUUID()
            optimisticLockingRow.id = newRecordId
            optimisticLockingRow.firstName = "Johnnyy"
            optimisticLockingRow.lastName = "Deep"
            optimisticLockingRow.active = true

            optimisticLockingRow.store()

            val recordOne = create.fetchSingle(OPTIMISTIC_LOCKING, OPTIMISTIC_LOCKING.ID.eq(newRecordId))
            val recordTwo = create.fetchSingle(OPTIMISTIC_LOCKING, OPTIMISTIC_LOCKING.ID.eq(newRecordId))

            expectThat(recordOne.version).isEqualTo(1)
            expectThat(recordTwo.version).isEqualTo(1)

            // Change on the first record succeeds
            recordOne.firstName = "Johnny"
            expectThat(recordOne.changed()).isTrue()
            recordOne.store()

            // Change on the second record fails
            recordTwo.firstName = "Johnny"
            expectThat(recordTwo.changed()).isTrue()
            expectThrows<DataChangedException> { recordTwo.store() }
                .and {
                    get { message }.isEqualTo("Database record has been changed or doesn't exist any longer")
                }
        }
    }

    @Test
    fun `use a pessimistic lock if the table does not have an optimistic lock column`() {
        Database.withJooq { create ->
            val orlando = create.newRecord(ACTOR)
            orlando[ACTOR.FIRST_NAME] = "orlando"
            orlando[ACTOR.LAST_NAME] = "Bloomm"

            orlando.store()
            val orlandoId = orlando.actorId

            val orlandoOne = create.fetchSingle(ACTOR, ACTOR.ACTOR_ID.eq(orlandoId))
            val orlandoTwo = create.fetchSingle(ACTOR, ACTOR.ACTOR_ID.eq(orlandoId))

            orlandoOne.lastName = "Bloom"
            orlandoOne.store()

            // Records retrieved from the DB always have the original values. Jooq uses these values
            // to do a 'best effort optimistic locking'. When store() is called, jooq
            // does a `select ... for update` (see query below) and compares the values with the original
            // values that are on the record. If any value has changed, jooq throws an error.
            //
            // select "public"."actor"."actor_id", "public"."actor"."first_name", "public"."actor"."last_name", "public"."actor"."last_update"
            // from "public"."actor"
            // where "public"."actor"."actor_id" = ?
            // for update
            orlandoTwo.lastName = "Bloom"
            expectThrows<DataChangedException> { orlandoTwo.store() }
                .and {
                    logger.error("Exception", subject)
                    get { message }.isEqualTo("Database record has been changed")
                }
        }
    }
}