package discountengine
import CriteriaFunctions._
import DBConnection._

import scala.io.{BufferedSource, Source}
import com.typesafe.scalalogging.Logger

object DiscountEngine extends App{
    // Begin a logger instance
    val logger = Logger("discount.engine")
    logger.info("Welcome to our Discount Engine. Hope you enjoy your discount!")
    logger.debug("Starting the Discount Engine...")

    // Read the source file and store its data into list without the header.
    logger.debug("Reading data...")
    val source: BufferedSource = Source.fromFile("src/main/resources/TRX1000.csv")
    val orders: List[String] = source.getLines().drop(1).toList
    logger.info("Successfully read data from the csv file.")

    // Get a connection to the target database which in the final results will be stored.
    logger.debug("Connecting to the database...")
    val conn: java.sql.Connection = getConnection()

    /** Stores the attributes of each order with specified data types. */
    case class Order(timestamp: String, product_name: String, expiry_date: String, quantity: Int, unit_price: Double, channel: String, payment_method: String)

    logger.debug(s"Starting encapsulating orders into objects...")
    /** Converts each parsed line to an object of type '''Order'''.
     *
     * Takes a line of type '''String'''.
     * Splits each line and passes its parts to the case class '''Order''' then returns the object.
     */
    def toOrder(line: String): Order = {
        logger.debug(s"Encapsulating order (${line}) to an object...")
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
        logger.debug("Calculating the discount...")
        val topTwoDiscounts = listOfRules.filter(_._1(order)).map(_._2(order)).sorted.reverse.take(2)
        logger.info(s"Top 2 discounts: ${topTwoDiscounts}")
        val priceBeforeDiscount = BigDecimal((order.quantity * order.unit_price)).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
        if (topTwoDiscounts.isEmpty) {
            logger.info(s"Total discount = 0 LE.")
            logger.debug("Writing to the database...")
            writeToDB(conn, order, priceBeforeDiscount, priceBeforeDiscount)
        }
        else {
            val finalDiscount = topTwoDiscounts.sum / topTwoDiscounts.length
            logger.info(s"Total discount = ${finalDiscount} LE.")
            logger.debug("Writing to the database...")
            writeToDB(conn, order, priceBeforeDiscount, BigDecimal(priceBeforeDiscount - finalDiscount).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
        }
    }

    /** Get a list of the qualifying and calculation rule pairs as list of tuples. */
    def getDiscountRules: List[(Order => Boolean, Order => Double)] = {
        List(
            (isAboutToExpire, expiryDiscount),
            (isCheeseOrWine, cheeseOrWineDiscount),
            (isOnSpecialDay, specialDayDiscount),
            (isMoreThanFive, moreThanFiveDiscount),
            (isAppUsed, appDiscount),
            (isVisaUsed, visaDiscount)
        )
    }

    // Call the functions to be run on each order.
    orders.map(toOrder).map(x => getOrderWithDiscount(x, getDiscountRules))
    logger.info("All orders have been processed successfully.")
    logger.debug("Closing the connection with the database...")
    closeConnection(conn)
    if(conn.isClosed) logger.info("Database connection is closed now.")
    logger.info("Missions accomplished ✔. Good Bye!")
}