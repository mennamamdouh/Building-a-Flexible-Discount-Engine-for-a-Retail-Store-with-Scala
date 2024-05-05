package discountengine
import CriteriaFunctions._
import DBConnection._

import scala.io.{BufferedSource, Source}

object DiscountEngine extends App{
    // Read the source file and store its data into list without the header.
    val source: BufferedSource = Source.fromFile("src/main/resources/TRX1000.csv")
    val orders: List[String] = source.getLines().drop(1).toList
    //orders.foreach(println)

    // Get a connection to the target database which in the final results will be stored.
    val conn: java.sql.Connection = getConnection()

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
     * Takes the order of type '''Order''', and a list of tuples represents each qualifying and calculation rule pair.
     * Filters the orders which pass the qualifying rules, then maps them to the corresponding calculation rules.
     * Checks if there's any discount applied, it calculates the final price after adding the discounts.
     * Final discount = average of the top 2 discounts.
     * Finally, it writes the order's information with the final price to the database.
     */
    def getOrderWithDiscount(order: Order, listOfRules: List[(Order => Boolean, Order => Double)]): Unit = {
        val topTwoDiscounts = listOfRules.filter(_._1(order)).map(_._2(order)).take(2)
        if (topTwoDiscounts.isEmpty)
            writeToDB(conn, order, BigDecimal((order.quantity * order.unit_price)).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
        else {
            val finalDiscount = topTwoDiscounts.sum / topTwoDiscounts.length
            writeToDB(conn, order, BigDecimal((order.quantity * order.unit_price) - finalDiscount).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
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

    // Call the functions to be run on each order.
    orders.map(toOrder).map(x => getOrderWithDiscount(x, getDiscountRules))
    closeConnection(conn)
}