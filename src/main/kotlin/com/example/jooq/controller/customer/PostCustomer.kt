package com.example.jooq.controller.customer

import com.example.jooq.domain.customer.Customer
import com.example.jooq.domain.customer.CustomerService
import org.http4k.core.*
import org.http4k.format.Moshi.auto

object PostCustomer {
    operator fun invoke(customerService: CustomerService): HttpHandler {
        val customerLens = Body.auto<Customer>().toLens()

        return { req ->
            val customer = customerLens(req)
            customerService.newCustomer(customer)
            Response(Status.OK).with(customerLens of customer)
        }
    }
}