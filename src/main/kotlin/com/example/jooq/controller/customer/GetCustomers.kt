package com.example.jooq.controller.customer

import com.example.jooq.domain.customer.Customer
import com.example.jooq.domain.customer.CustomerService
import org.http4k.core.*
import org.http4k.format.Moshi.auto

object GetCustomers {
    operator fun invoke(customerService: CustomerService): HttpHandler {
        val customersLens = Body.auto<List<Customer>>().toLens()

        return { _ ->
            val allCustomers = customerService.getAllCustomers()
            Response(Status.OK).with(customersLens of allCustomers)
        }
    }
}