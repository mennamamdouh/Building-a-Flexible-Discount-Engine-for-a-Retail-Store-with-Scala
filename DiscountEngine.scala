import java.io.{File, FileOutputStream, PrintWriter}
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
    orders.map(toOrder).map(processed_order).foreach(writeLine)
    writer.close()
}
