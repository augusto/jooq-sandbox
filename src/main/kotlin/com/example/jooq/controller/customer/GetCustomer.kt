package com.example.jooq.controller.customer

import com.example.jooq.domain.customer.Customer
import com.example.jooq.domain.customer.CustomerService
import org.http4k.core.*
import org.http4k.format.Moshi.auto
import org.http4k.lens.Path
import org.http4k.lens.int

object GetCustomer {
    operator fun invoke(customerService: CustomerService): HttpHandler {
        val customersLens = Body.auto<Customer>().toLens()
        val customerIdLens = Path.int().of("customerId")

        return { req ->
            val customerId = customerIdLens(req)
            val customer = customerService.getCustomer(customerId)

            if (customer != null) Response(Status.OK).with(customersLens of customer)
            else Response(Status.NOT_FOUND)
        }
    }
}
