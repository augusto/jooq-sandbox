package com.example.jooq.controller.customer

import com.example.jooq.domain.customer.Customer
import com.example.jooq.domain.customer.CustomerService
import org.http4k.core.*
import org.http4k.format.Moshi.auto
import org.http4k.lens.Path
import org.http4k.lens.int

object PutCustomer {
    data class UpdateCustomer(val firstName:String, val lastName:String, val active:Boolean)
    operator fun invoke(customerService: CustomerService): HttpHandler {
        val customerIdLens = Path.int().of("customerId")
        val updateCustomerLens = Body.auto<UpdateCustomer>().toLens()
        val customerLens = Body.auto<Customer>().toLens()

        return { req ->
            val customerId = customerIdLens(req)
            val updateCustomer = updateCustomerLens(req)
            val updatedCustomer = Customer(
                id = customerId,
                firstName = updateCustomer.firstName,
                lastName = updateCustomer.lastName,
                active = updateCustomer.active
            )

            val wasUpdated = customerService.updateCustomer(updatedCustomer)

            if(wasUpdated) Response(Status.OK).with(customerLens of updatedCustomer)
            else Response(Status.NOT_FOUND)
        }
    }
}
