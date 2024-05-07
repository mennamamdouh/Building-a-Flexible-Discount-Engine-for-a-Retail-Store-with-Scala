package discountengine

import com.typesafe.scalalogging.Logger
import discountengine.DiscountEngine.Order

import java.sql.{DriverManager, SQLException}
import io.github.cdimascio.dotenv.Dotenv

object DBConnection {
    // Begin a logger instance
    val logger = Logger("database.connection")

    def getConnection(): java.sql.Connection = {
        // Load the environment variables
        val dotenv = Dotenv.load()
        // Get the connection credentials
        val localhost = dotenv.get("DB_LOCALHOST")
        val port = dotenv.get("DB_PORT")
        val service = dotenv.get("DB_SERVICE")
        // Define the connection string
        val jdbcUrl = s"jdbc:oracle:thin:@${localhost}:${port}/${service}"
        val username = dotenv.get("DB_USERNAME")
        val password = dotenv.get("DB_PASSWORD")
        // Return the connection
        try {
            val connection = DriverManager.getConnection(jdbcUrl, username, password)
            logger.info("Successfully connected to the database.")
            connection
        }
    }

    def writeToDB(conn: java.sql.Connection, order: Order, priceBeforeDiscount: Double, finalPrice: Double) {
        // Define the statement and the parameters that will be inserted
        val statement = conn.prepareStatement("INSERT INTO ORDERS VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
        statement.setString(1, order.timestamp)
        statement.setString(2, order.product_name)
        statement.setString(3, order.expiry_date)
        statement.setInt(4, order.quantity)
        statement.setDouble(5, order.unit_price)
        statement.setString(6, order.channel)
        statement.setString(7, order.payment_method)
        statement.setDouble(8, priceBeforeDiscount)
        statement.setDouble(9, finalPrice)

        // Execute the statement
        try {
            statement.executeUpdate()
            logger.info("Order is added to the database successfully.")
        } catch {
            case e: SQLException => logger.error("Cannot add the record to the database.")
        }
        statement.close()
    }

    def closeConnection(conn: java.sql.Connection) = {
        // Close the connection
        logger.debug("About to close the connection...")
        conn.close()
    }
}
