package com.example.jooq.controller.customer

import com.example.jooq.domain.customer.CustomerService
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path
import org.http4k.lens.int

object DeleteCustomer {
    operator fun invoke(customerService: CustomerService): HttpHandler {
        val customerIdLens = Path.int().of("customerId")

        return { req ->
            val customerId = customerIdLens(req)
            val wasDeleted = customerService.delete(customerId)

            if (wasDeleted) Response(Status.NO_CONTENT)
            else Response(Status.NOT_FOUND)
        }
    }
}