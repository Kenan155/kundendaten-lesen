package entity

data class Customer(
    val id: Int? = 0,
    val prename: String,
    val surname: String,
    val email: String? = "Nicht vorhanden",
    val creditRating: String? = "Eeees",
    val income: Int? = 0,
    val creditsum: Int? = 0,
    val duration: Int? = 0,
    val bankLoans: Int? = 0,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Customer
        return prename == other.prename && surname == other.surname
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return "prename = $prename"
    }
}