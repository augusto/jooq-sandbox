package com.example.jooq

import com.example.jooq.app.CustomerConfig
import com.example.jooq.app.Database
import com.example.jooq.controller.customer.*
import com.example.jooq.formats.MoshiMessage
import com.example.jooq.formats.moshiMessageLens
import org.http4k.core.*
import org.http4k.core.Method.*
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.DebuggingFilters.PrintRequest
import org.http4k.filter.DebuggingFilters.PrintResponse
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer

object JooqGradleLiquibase {

    val database = Database()

    val customerConfig = CustomerConfig(database)

    val filters = PrintRequest()
        .then(PrintResponse())

    val routes =
        routes(
            "/customers" bind POST to PostCustomer(customerConfig.customerService),
            "/customers" bind GET to GetCustomers(customerConfig.customerService),
            "/customers/{customerId}" bind GET to GetCustomer(customerConfig.customerService),
            "/customers/{customerId}" bind PUT to PutCustomer(customerConfig.customerService),
            "/customers/{customerId}" bind DELETE to DeleteCustomer(customerConfig.customerService),
            "/ping" bind GET to {
                Response(OK).body("pong")
            },

            "/formats/json/moshi" bind GET to {
                Response(OK).with(moshiMessageLens of MoshiMessage("Barry", "Hello there!"))
            }
        )

    val app = filters.then(routes)
}

fun main() {
    val server = JooqGradleLiquibase.app.asServer(SunHttp(9000)).start()

    println("Server started on " + server.port())
}
