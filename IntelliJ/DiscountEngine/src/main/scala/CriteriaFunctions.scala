package discountengine
import discountengine.DiscountEngine.Order

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CriteriaFunctions {
    /** Formats the transaction date of each order.
     *
     * Takes the transaction date as a '''String'''.
     * Splits it to get the date without the time.
     * Converts it into '''LocalDate'''.
     */
    def transactionDateFormatter(transDate: String): LocalDate = {
        val orderTransactionDate = transDate.split("T")(0)
        val formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        LocalDate.parse(orderTransactionDate, formatterDateTime)
    }

    /** Formats the expiry date of each product.
     *
     * Takes the expiry date as a '''String'''.
     * Converts it into '''LocalDate'''.
     */
    def expiryDateFormatter(expiryDate: String): LocalDate = {
        val formatterDateTime2 = DateTimeFormatter.ofPattern("M/d/yyyy")
        LocalDate.parse(expiryDate, formatterDateTime2)
    }

    /** Checks the qualifying rule number 1: Whether the order is about to expire or not.
     *
     * Takes an order of type '''Order'''.
     * Gets the difference in days between the transaction day of the order and the expiry day of the product.
     * Return boolean value, it it's about to expire (less than 30 days) it returns true, else it returns false.
     */
    def isAboutToExpire(order: Order): Boolean = {
        // Get the difference between transaction date and expiry date
        val daysBetween = (expiryDateFormatter(order.expiry_date).toEpochDay - transactionDateFormatter(order.timestamp).toEpochDay).toInt
        // Check the qualifying rule
        if (daysBetween >= 30) false
        else true
    }

    /** Performs the calculation rule number 1: The discount according to the number of days remaining until the product is expired.
     *
     * Takes an order of type '''Order'''.
     * Gets the difference in days between the transaction day of the order and the expiry day of the product.
     * Calculates the discount percentage which is (30 - the number of days).
     *
     * Returns the discount value from the total price.
     */
    def expiryDiscount(order: Order): Double = {
        // Get the difference between transaction date and expiry date
        val daysBetween = (expiryDateFormatter(order.expiry_date).toEpochDay - transactionDateFormatter(order.timestamp).toEpochDay).toInt
        // Perform the calculation rule
        val discountPerc = 30 - daysBetween
        BigDecimal(order.quantity * order.unit_price * discountPerc / 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }

    /** Checks the qualifying rule number 2: Whether the product is cheese or wine.
     *
     * Takes an order of type '''Order'''.
     * Returns true if it is a cheese or wine product, and false if not.
     */
    def isCheeseOrWine(order: Order): Boolean = {
        if (order.product_name.startsWith("Cheese") || order.product_name.startsWith("Wine")) true
        else false
    }

    /** Performs the calculation rule number 2: The discount is 10% for cheese & 5% for wine.
     *
     * Takes an order of type '''Order'''.
     * Calculates the discount percentage according to the product type.
     *
     * Returns the discount value from the total price.
     */
    def cheeseOrWineDiscount(order: Order): Double = {
        // Perform the calculation rule after checking the product's category
        if (order.product_name.startsWith("Cheese"))
          BigDecimal(order.quantity * order.unit_price * 0.1).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
        else
          BigDecimal(order.quantity * order.unit_price * 0.05).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }

    /** Checks the qualifying rule number 3: Whether we're on 23rd March or not.
     *
     * Takes an order of type '''Order'''.
     * Returns true if the transaction day is 23rd March, and false if not.
     */
    def isOnSpecialDay(order: Order): Boolean = {
        if (transactionDateFormatter(order.timestamp).getDayOfMonth == 23 && transactionDateFormatter(order.timestamp).getMonthValue == 3) true
        else false
    }

    /** Performs the calculation rule number 3: The discount is 50%.
     *
     * Takes an order of type '''Order'''.
     * Calculates the discount percentage and returns the discount value from the total price.
     */
    def specialDayDiscount(order: Order): Double = {
        BigDecimal(order.quantity * order.unit_price * 0.5).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }

    /** Checks the qualifying rule number 4: Whether the quantity of the product is more than 5.
     *
     * Takes an order of type '''Order'''.
     * Returns true if it has more than 5 units of the product, and false if not.
     */
    def isMoreThanFive(order: Order): Boolean = {
        if (order.quantity > 5) true
        else false
    }

    /** Performs the calculation rule number 4: The discount depends on the quantity.
     *
     * Takes an order of type '''Order'''.
     *
     * Calculates the discount percentage as following:
     *
     * • 5% if 6-9 units
     *
     * • 7% if 10-14 units
     *
     * • 10% if more than 15 units
     *
     * Returns the discount value from the total price.
     */
    def moreThanFiveDiscount(order: Order): Double = {
        if (order.quantity >= 6 && order.quantity <= 9)
          BigDecimal(order.quantity * order.unit_price * 0.05).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
        else if (order.quantity >= 10 && order.quantity <= 14)
          BigDecimal(order.quantity * order.unit_price * 0.07).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
        else
          BigDecimal(order.quantity * order.unit_price * 0.1).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
      }

    /** NEW REQUIREMENTS */

    /** Checks the qualifying rule number 5: Whether the order is made through the App or not.
     *
     * Takes an order of type '''Order'''.
     * Returns true if it's through the App, and false if not.
     */
    def isAppUsed(order: Order): Boolean = {
        if (order.channel == "App") true
        else false
    }

    /** Performs the calculation rule number 5: The discount depends on the quantity.
     *
     * Takes an order of type '''Order'''.
     * Calculates the discount as it depends on result of rounding the quantity to the nearest multiple of 5.
     *
     * ''Discount is calculated as following:''
     *
     * • 1-5 products &rarr; 5% Discount
     *
     * • 6-10 products &rarr; 10% Discount
     *
     * • 11-15 products &rarr; 15% Discount
     *
     * etc ...
     *
     * Returns the discount value from the total price.
     */
    def appDiscount(order: Order): Double = {
        val discountPerc = (((order.quantity / 5.0).ceil) * 5).toInt
        BigDecimal(order.quantity * order.unit_price * discountPerc / 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }

    /** Checks the qualifying rule number 6: Whether the payment method is Visa or Cash.
     *
     * Takes an order of type '''Order'''.
     * Returns true if the payment method is Visa, and false if not.
     */
    def isVisaUsed(order: Order): Boolean = {
        if (order.payment_method == "Visa") true
        else false
    }

    /** Performs the calculation rule number 6: The discount is 5%.
     *
     * Takes an order of type '''Order'''.
     * Returns the discount value from the total price.
     */
    def visaDiscount(order: Order): Double = {
        BigDecimal(order.quantity * order.unit_price * 0.05).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }
}