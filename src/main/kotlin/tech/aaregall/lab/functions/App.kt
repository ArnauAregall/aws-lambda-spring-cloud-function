package tech.aaregall.lab.functions

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class App

fun main(args: Array<String>) {
	runApplication<App>(*args)
}
