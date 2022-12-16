package entity

/**
 * Unveränderliche Daten eines Cutomers.
 *
 * @author [Kenan Dilmen](mailto:dike1011@h-ka.de)
 *
 *  @author [Alexander Würtenberger](mailto:wual1018@h-ka.de)
 *
 * @property id ID eines Customers.
 * @property prename Der Vorname eines Customers.
 * @property surname Der Nachname eines Customers.
 * @property email Die E-Mail eines Customers, über die mit ihm kommuniziert wird.
 * @property creditRating Vergleichbar mit dem "Schufarating" eines Customers.
 * @property income Das Jahresnettoeinkommen in €.
 * @property creditamount Die Kreditsumme die sich ein Cutomer wünscht in €.
 * @property duration Die Wunschlaufzeit des Kredits in ganzen Monaten.
 * @property bankLoans Die Summe an bereits genehmigten Krediten.
 */
data class Customer(
    val id: Int?,
    val prename: String,
    val surname: String,
    val email: String = "Nicht vorhanden",
    val creditRating: String = "Nicht vorhanden",
    val income: Int = 0,
    val creditamount: Int = 0,
    val duration: Int = 0,
    val bankLoans: Int = 0,
) {
    /**
     * Vergleich mit einem anderen Objekt oder null.
     * @param other Das zu vergleichende Objekt oder null.
     * @return True, falls das zu vergleichende (Customer-) Objekt den gleichen prename und surname hat.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Customer
        return prename == other.prename && surname == other.surname
    }

    /**
     * Hashwert aufgrund der ID.
     * @return Der Hashwert.
     */
    override fun hashCode() = id?.hashCode() ?: this::class.hashCode()

    /**
     * Customer als String
     * @return String mit den Properties prename und surname.
     */
    override fun toString(): String {
        return "Der Kunde heißt = $prename $surname"
    }
}