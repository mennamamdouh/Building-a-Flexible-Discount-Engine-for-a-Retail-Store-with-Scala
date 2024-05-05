package discountengine

import discountengine.DiscountEngine.Order
import java.sql.DriverManager
import io.github.cdimascio.dotenv.Dotenv

object DBConnection {
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
        DriverManager.getConnection(jdbcUrl, username, password)
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
        statement.executeUpdate()
        statement.close()
    }

    def closeConnection(conn: java.sql.Connection) = {
        // Close the connection
        conn.close()
    }
}
