package com.eyuppastirmaci.cogniariumbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class CogniariumbackendApplication

fun main(args: Array<String>) {
	runApplication<CogniariumbackendApplication>(*args)
}
