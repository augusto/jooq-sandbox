package com.example.jooq.app

import com.example.jooq.domain.customer.CustomerService

class CustomerConfig(database: Database) {
    val customerService = CustomerService(database)
}