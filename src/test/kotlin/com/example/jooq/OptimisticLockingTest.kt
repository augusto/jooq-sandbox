package com.example.jooq

import com.example.jooq.db.tables.OptimisticLocking.OPTIMISTIC_LOCKING
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
 *
 * @see <a href="https://www.jooq.org/doc/latest/manual/sql-execution/crud-with-updatablerecords/optimistic-locking/">Optimistic locking with Jooq</a>
 */
class OptimisticLockingTest {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Test
    fun `jooq throws exception when there's an optimistic lock conflict`() {
        Database.withJooq { create ->
            val optimisticLockingRow = create.newRecord(OPTIMISTIC_LOCKING)

            val newRecordId = UUID.randomUUID()
            optimisticLockingRow[OPTIMISTIC_LOCKING.ID] = newRecordId
            optimisticLockingRow[OPTIMISTIC_LOCKING.FIRST_NAME] = "Johnnyy"
            optimisticLockingRow[OPTIMISTIC_LOCKING.LAST_NAME] = "Deep"
            optimisticLockingRow[OPTIMISTIC_LOCKING.ACTIVE] = true

            optimisticLockingRow.store()

            val recordOne = create.fetchSingle(OPTIMISTIC_LOCKING, OPTIMISTIC_LOCKING.ID.eq(newRecordId))
            val recordTwo = create.fetchSingle(OPTIMISTIC_LOCKING, OPTIMISTIC_LOCKING.ID.eq(newRecordId))

            expectThat(recordOne[OPTIMISTIC_LOCKING.VERSION]).isEqualTo(1)
            expectThat(recordTwo[OPTIMISTIC_LOCKING.VERSION]).isEqualTo(1)

            //First change succeeds
            recordOne[OPTIMISTIC_LOCKING.FIRST_NAME] = "Johnny"
            expectThat(recordOne.changed()).isTrue()
            recordOne.store()

            //Second change on the record fails
            recordTwo[OPTIMISTIC_LOCKING.FIRST_NAME] = "Johnny"
            expectThat(recordTwo.changed()).isTrue()
            expectThrows<DataChangedException> { recordTwo.store() }
                .and {
                    get { message }.isEqualTo("Database record has been changed or doesn't exist any longer")
                    logger.error("Exception", subject)
                }
        }
    }
}