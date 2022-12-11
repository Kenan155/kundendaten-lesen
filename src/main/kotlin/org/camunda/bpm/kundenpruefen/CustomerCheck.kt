package org.camunda.bpm.kundenpruefen

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import entity.Customer
import org.camunda.bpm.client.ExternalTaskClient
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.util.logging.Logger
import com.fasterxml.jackson.module.kotlin.readValue

object CustomerCheck {
    private val LOGGER = Logger.getLogger(CustomerCheck::class.java.name)
    @JvmStatic
    fun main(args: Array<String>) {
        val client: ExternalTaskClient = ExternalTaskClient.create()
            .baseUrl("http://localhost:8080/engine-rest")
            .asyncResponseTimeout(10000) // long polling timeout
            .build()

        // subscribe to an external task topic as specified in the process
        client.subscribe("request-senden")
            .lockDuration(1000) // the default lock duration is 20 seconds, but you can override this
            .handler { externalTask, externalTaskService ->
                val prename: String = externalTask.getVariable("vorname")
                val surname: String = externalTask.getVariable("nachname")
                val email: String = externalTask.getVariable("email")
                val income: Int = externalTask.getVariable("einkommen")
                val creditsum: Int = externalTask.getVariable("kreditsumme")
                val duration: Int = externalTask.getVariable("laufzeit")

                LOGGER.info("Der Vorname des Antragstellers ist: $prename")
                LOGGER.info("Der Nachname des Antragstellers ist: $surname")
                LOGGER.info("Die Email des Antragstellers ist: $email")
                LOGGER.info("Das Einkommen des Antragstellers ist: $income")
                LOGGER.info("Die gewünschte Kreditsumme ist: $creditsum")
                LOGGER.info("Die gewünschte Laufzeit ist: $duration")

                val customerForChecking = Customer(
                    prename = prename,
                    surname = surname,
                    email = email,
                    income = income,
                    creditsum = creditsum,
                    duration = duration,
                )

                val fclient = HttpClient.newHttpClient()

                val request = HttpRequest.newBuilder()
                    .uri(URI("http://localhost:3000/customers"))
                    .GET()
                    .build()

                val response  = fclient.send(request, BodyHandlers.ofString())

                println(response.body().toString())

                val mapper = jacksonObjectMapper()

                val json = response.body()
                val customers = mapper.readValue<List<Customer>>(json)

                customers.forEach { println(it.toString()) }

                // it == ... entspricht it.equals(...)
                val result = customers.filter { it == customerForChecking }

                if (result.isEmpty()) {
                    println("Mitarbeiter Melden")
                } else
                    println("An Pascal weiter")

                externalTaskService.complete(externalTask)
            }
            .open()
    }
}