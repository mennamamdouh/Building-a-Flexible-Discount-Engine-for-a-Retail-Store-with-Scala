CREATE TABLE ORDERS (
    OrderTime VARCHAR2(50),
    ProductName VARCHAR2(255),
    ExpiryDate VARCHAR2(50),
    Quantity INT,
    UnitPrice DECIMAL(10, 2),
    Channel VARCHAR2(20),
    PaymentMethod VARCHAR2(20),
    PriceBeforeDiscount DECIMAL(10, 2),
    FinalPrice DECIMAL(10, 2)
)