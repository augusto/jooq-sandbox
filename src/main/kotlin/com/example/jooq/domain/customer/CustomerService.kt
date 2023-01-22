package com.example.jooq.domain.customer

import com.example.jooq.app.Database
import com.example.jooq.db.tables.Customer.CUSTOMER
import org.jooq.Record

class CustomerService(private val database: Database) {
    fun newCustomer(customer: Customer) {
        database.withJooq {
            it.insertInto(CUSTOMER, CUSTOMER.ID, CUSTOMER.FIRSTNAME, CUSTOMER.LASTNAME, CUSTOMER.ACTIVE)
                .values(customer.id, customer.firstName, customer.lastName, customer.active)
                .execute()
        }
    }

    fun getAllCustomers(): List<Customer> {
        return database.withJooq { ctx ->
            val result = ctx.select()
                .from(CUSTOMER)
                .orderBy(CUSTOMER.ID)
                .fetch()

            result
                .map { it.toCustomer() }
                .toList()
        }
    }

    fun getCustomer(customerId: Int): Customer? {
        return database.withJooq {
            val record = it.select()
                .from(CUSTOMER)
                .where(CUSTOMER.ID.eq(customerId))
                .orderBy(CUSTOMER.ID)
                .fetchOne()

            record?.toCustomer()
        }
    }

    fun delete(customerId: Int) : Boolean {
        return database.withJooq { ctx ->
            val deletedRows = ctx.delete(CUSTOMER)
                .where(CUSTOMER.ID.eq(customerId))
                .execute()

            deletedRows==1
        }
    }

    fun updateCustomer(updatedCustomer: Customer) : Boolean{
        return database.withJooq { ctx ->
            val updatedRows = ctx.update(CUSTOMER)
                .set(CUSTOMER.FIRSTNAME, updatedCustomer.firstName)
                .set(CUSTOMER.LASTNAME, updatedCustomer.lastName)
                .set(CUSTOMER.ACTIVE, updatedCustomer.active)
                .where(CUSTOMER.ID.eq(updatedCustomer.id))
                .execute()

            updatedRows==1
        }
    }
    private fun Record.toCustomer() = Customer(
        id = this[CUSTOMER.ID],
        firstName = this[CUSTOMER.FIRSTNAME],
        lastName = this[CUSTOMER.LASTNAME],
        active = this[CUSTOMER.ACTIVE]
    )

}