import java.io.{File, FileOutputStream, PrintWriter}
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.io.{BufferedSource, Source}

object DiscountEngine extends App{
    // Read the source file and store its data into list without the header.
    val source: BufferedSource = Source.fromFile("src/main/resources/TRX1000.csv")
    val orders: List[String] = source.getLines().drop(1).toList
    //orders.foreach(println)

    // Define the target file which in the final results will be written.
    val f: File = new File("src/main/resources/processed_orders.csv")
    val heading = s"timestamp,product_name,expiry_date,quantity,unit_price,channel,payment_method,final_price"
    val writer = new PrintWriter(new FileOutputStream(f,false))
    writer.write(heading)

    /** Stores the attributes of each order with specified data types. */
    case class Order(timestamp: String, product_name: String, expiry_date: String, quantity: Int, unit_price: Double, channel: String, payment_method: String)

    /** Converts each parsed line to an object of type '''Order'''.
     *
     * Takes a line of type '''String'''.
     * Splits each line and passes its parts to the case class '''Order''' then returns the object.
     */
    def toOrder(line: String): Order = {
        val parsedLine = line.split(",")
        Order(parsedLine(0), parsedLine(1), parsedLine(2), Integer.parseInt(parsedLine(3)), parsedLine(4).toDouble, parsedLine(5), parsedLine(6))
    }

    /** Calculates the final discount that will be applied on each order.
     *
     * Takes the order of type '''Order''', and a list of tuples represents each qualifying and caluclation rule pair.
     * Filters the orders which pass the qualifying rules, then maps them to the corresponding calculation rules.
     * After checking all qualifying rules and calculates the discounts, it takes the top 2 discounts and gets their average.
     */
    def getOrderWithDiscount(order: Order, listOfRules: List[(Order => Boolean, Order => Double)]): Unit = {
        println(processed_order(order))
        val topTwoDiscounts = listOfRules.filter(_._1(order)).map(_._2(order)).take(2)
        if (topTwoDiscounts.isEmpty) println("This order has a discount of 0 LE.")
        else {
            val finalDiscount = topTwoDiscounts.sum / topTwoDiscounts.length
            val finalPrice = BigDecimal((order.quantity * order.unit_price) - finalDiscount).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
            println(s"This order has a discount of ${finalDiscount} LE. Final price = ${finalPrice}")
        }
    }

    /** Get a list of the qualifying and calculation rule pairs as list of tuples. */
    def getDiscountRules: List[(Order => Boolean, Order => Double)] = {
        List(
            (isAboutToExpire, expiryDiscount),
            (isCheeseOrWine, cheeseOrWineDiscount),
            (isOnSpecialDay, specialDayDiscount),
            (isMoreThanFive, moreThanFiveDiscount))
    }

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
     * Returns the final price after applying the discount.
     */
    def expiryDiscount(order: Order): Double = {
        // Get the difference between transaction date and expiry date
        val daysBetween = (expiryDateFormatter(order.expiry_date).toEpochDay - transactionDateFormatter(order.timestamp).toEpochDay).toInt
        // Perform the calculation rule
        val discountPerc: Double = (30 - daysBetween)
        BigDecimal(order.quantity * order.unit_price * discountPerc / 100).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }

    /** Checks the qualifying rule number 2: Whether the product is cheese or wine.
     *
     * Takes an order of type '''Order'''.
     * Returns true if it is a cheese or wine product, and false if not.
     */
    def isCheeseOrWine(order: Order): Boolean = {
        // Check the qualifying rule
        if (order.product_name.startsWith("Cheese") || order.product_name.startsWith("Wine")) true
        else false
    }

    /** Performs the calculation rule number 2: The discount is 10% for cheese & 5% for wine.
     *
     * Takes an order of type '''Order'''.
     * Calculates the discount percentage according to the product type.
     * Returns the final price after applying the discount.
     */
    def cheeseOrWineDiscount(order: Order): Double = {
        // Perform the calculation rule after chechking the product's category
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
        // Check the qualifying rule
        if (transactionDateFormatter(order.timestamp).getDayOfMonth == 23 && transactionDateFormatter(order.timestamp).getMonthValue == 3) true
        else false
    }

    /** Performs the calculation rule number 3: The discount is 50%.
     *
     * Takes an order of type '''Order'''.
     * Calculates the discount percentage and returns the final price after applying the discount.
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
        // Check the qualifying rule
        if (order.quantity > 5) true
        else false
    }

    /** Performs the calculation rule number 4: The discount depends on the quantity.
     *
     * Takes an order of type '''Order'''.
     * Calculates the discount percentage as: 5% if 6-9 units, 7% if 10-14 units, 10% if more than 15 units
     */
    def moreThanFiveDiscount(order: Order): Double = {
        if (order.quantity >= 6 && order.quantity <= 9)
            BigDecimal(order.quantity * order.unit_price * 0.05).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
        else if (order.quantity >= 10 && order.quantity <= 14)
            BigDecimal(order.quantity * order.unit_price * 0.07).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
        else
            BigDecimal(order.quantity * order.unit_price * 0.1).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }

    /** Converts an object of type '''Order''' to a '''String''' written in a defined format.
     *
     * Takes an object of the type '''Order'''.
     * Concats its attributes in a single line of type '''String''' after adding the final price of the order to the end of the line.
     */
    def processed_order(order: Order): String = {
        s"${order.timestamp}," +
          s"${order.product_name}," +
          s"${order.expiry_date}," +
          s"${order.quantity}," +
          s"${order.unit_price}," +
          s"${order.channel}," +
          s"${order.payment_method}," +
          s"0"
    }

    /** Writes to the target file.
     *
     * Takes the processed order as a line of type '''String''' and writes it to the target file.
     */
    def writeLine(line: String): Unit = {
        writer.write("\n"+line)
    }

    // Call the functions to be run on each order.
    orders.map(toOrder).map(x => getOrderWithDiscount(x, getDiscountRules))
    writer.close()
}