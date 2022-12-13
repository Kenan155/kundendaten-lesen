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

    /**
     * Basis-URL der Camunda-Schnittstelle.
     */
    private const val BASE_URL = "http://localhost:8080/engine-rest"

    /**
     * Task Topic auf das Subscribed wird, wie im Prozess angegeben
     */
    private const val SUBSCRIBE_TOPIC = "request-senden"

    /**
     * Die URL der Customer Datenbank
     */
    private const val DB_URL = "http://localhost:3000/customers"
    @JvmStatic
    fun main(args: Array<String>) {
        val client: ExternalTaskClient = ExternalTaskClient.create()
            .baseUrl(BASE_URL)
            .asyncResponseTimeout(10000) // long polling timeout
            .build()

        // subscribe to an external task topic as specified in the process
        client.subscribe(SUBSCRIBE_TOPIC)
            .lockDuration(1000) // the default lock duration is 20 seconds, but you can override this
            .handler { externalTask, externalTaskService ->

                /**
                 * Speichern der Daten aus dem Prozess in Variablen
                 */
                val prename: String = externalTask.getVariable("prename")
                val surname: String = externalTask.getVariable("surname")
                val email: String = externalTask.getVariable("email")
                val income: Int = externalTask.getVariable("income")
                val creditsum: Int = externalTask.getVariable("creditsum")
                val duration: Int = externalTask.getVariable("duration")

                LOGGER.info("Der Vorname des Antragstellers ist: $prename")
                LOGGER.info("Der Nachname des Antragstellers ist: $surname")
                LOGGER.info("Die Email des Antragstellers ist: $email")
                LOGGER.info("Das Einkommen des Antragstellers ist: $income")
                LOGGER.info("Die gewünschte Kreditsumme ist: $creditsum")
                LOGGER.info("Die gewünschte Laufzeit ist: $duration")

                /**
                 * Erzeugen eines Customer-Objekts aus den eingegangenen Kundendaten
                 */
                val customerForChecking = Customer(
                    id = null,
                    prename = prename,
                    surname = surname,
                    email = email,
                    income = income,
                    creditsum = creditsum,
                    duration = duration,
                )

                /**
                 * Erzeugen des Http Clients um Requests an die Datenbank senden zu können.
                 */
                val customerDbClient = HttpClient.newHttpClient()

                /**
                 * Bauen des Requests an die Datenbank.
                 */
                val request = HttpRequest.newBuilder()
                    .uri(URI(DB_URL))
                    .GET()
                    .build()

                /**
                 * Abschicken des Requests an die Datenbank und abspeichern der Antwort.
                 */
                val response  = customerDbClient.send(request, BodyHandlers.ofString())

                /**
                 * Bauen eines Mapper-Objekts um die JSON Daten aus der DB auf Objekte zu mappen.
                 */
                val mapper = jacksonObjectMapper()

                /**
                 * JSON Daten aus dem Response Body extrahieren und eine Liste an Customer-Objekten erzeugen.
                 */
                val json = response.body()
                val customers = mapper.readValue<List<Customer>>(json)


                /**
                 * Customer-Objekte aus der DB mit dem Customer aus dem Prozess
                 *
                 * it == ... entspricht it.equals(...)
                 */
                val result = customers.filter { it == customerForChecking }

                /**
                 * Wenn der Customer in der DB vorhanden ist, werden die zusätzlichen Daten aus der DB zurückgegeben.
                 * Andernfalls wird nichts zurückgegeben.
                 */
                if (result.isEmpty()) {
                    println("Mitarbeiter Melden")
                } else
                    println("An Pascal weiter")

                externalTaskService.complete(externalTask)
            }
            .open()
    }
}

