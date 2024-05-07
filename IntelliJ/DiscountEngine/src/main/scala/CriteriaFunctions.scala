package discountengine
import com.typesafe.scalalogging.Logger
import discountengine.DiscountEngine.Order

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CriteriaFunctions {
    // Begin a logger instance
    val logger = Logger("criteria.functions")

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
        logger.debug("Checking qualifying rule #1: Expiry date.")
        // Get the difference between transaction date and expiry date
        val daysBetween = (expiryDateFormatter(order.expiry_date).toEpochDay - transactionDateFormatter(order.timestamp).toEpochDay).toInt
        // Check the qualifying rule
        if (daysBetween >= 30) false
        else {
            logger.info(s"Qualifying rule #1 checked ✔. There are ${daysBetween} days left.")
            true
        }
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
        logger.debug("Applying calculation rule #1.")
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
        logger.debug("Checking qualifying rule #2: Cheese or Wine product.")
        if (order.product_name.startsWith("Cheese") || order.product_name.startsWith("Wine")) {
            logger.info(s"Qualifying rule #2 checked ✔. Product of type ${order.product_name.split(" ")(0)}.")
            true
        }
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
        logger.debug("Applying calculation rule #2.")
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
        logger.debug("Checking qualifying rule #3: Our special day 23rd March.")
        if (transactionDateFormatter(order.timestamp).getDayOfMonth == 23 && transactionDateFormatter(order.timestamp).getMonthValue == 3) {
            logger.debug("Qualifying rule #3 checked ✔. Order has been purchased on 23rd March.")
            true
        }
        else false
    }

    /** Performs the calculation rule number 3: The discount is 50%.
     *
     * Takes an order of type '''Order'''.
     * Calculates the discount percentage and returns the discount value from the total price.
     */
    def specialDayDiscount(order: Order): Double = {
        logger.debug("Applying calculation rule #3.")
        BigDecimal(order.quantity * order.unit_price * 0.5).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }

    /** Checks the qualifying rule number 4: Whether the quantity of the product is more than 5.
     *
     * Takes an order of type '''Order'''.
     * Returns true if it has more than 5 units of the product, and false if not.
     */
    def isMoreThanFive(order: Order): Boolean = {
        logger.debug("Checking qualifying rule #4: More than 5 products.")
        if (order.quantity > 5) {
            logger.info(s"Qualifying rule #4 checked ✔. It has ${order.quantity} units.")
            true
        }
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
        logger.debug("Applying calculation rule #4.")
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
        logger.debug("Checking qualifying rule #5: Online shopping or via our store.")
        if (order.channel == "App") {
            logger.info("Qualifying rule #5 checked ✔. App is used.")
            true
        }
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
        logger.debug("Applying calculation rule #5.")
        val discountPerc = (((order.quantity / 5.0).ceil) * 5).toInt
        BigDecimal(order.quantity * order.unit_price * discountPerc / 100.0).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }

    /** Checks the qualifying rule number 6: Whether the payment method is Visa or Cash.
     *
     * Takes an order of type '''Order'''.
     * Returns true if the payment method is Visa, and false if not.
     */
    def isVisaUsed(order: Order): Boolean = {
        logger.debug("Checking qualifying rule #6: Payment with Visa Card or Cash.")
        if (order.payment_method == "Visa") {
            logger.info("Qualifying rule #6 checked ✔. Visa Card is used.")
            true
        }
        else false
    }

    /** Performs the calculation rule number 6: The discount is 5%.
     *
     * Takes an order of type '''Order'''.
     * Returns the discount value from the total price.
     */
    def visaDiscount(order: Order): Double = {
        logger.debug("Applying calculation rule #6.")
        BigDecimal(order.quantity * order.unit_price * 0.05).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }
}