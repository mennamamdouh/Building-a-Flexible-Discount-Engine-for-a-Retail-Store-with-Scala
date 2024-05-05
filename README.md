# Building-a-Flexible-Discount-Engine-for-a-Retail-Store-with-Scala

## Overview ##

A huge retail store wants a rule engine that qualifies orders' transactions to discounts based on a set of *qualifying rules* and automatically calculates the proper discount based on some *calculation rules*. The program is written in __Scala__ in a pure functional manner.

## Business Requirements ##

As mentioned above, the retail store has many orders to add discount  to them according to a specific criteria. This criteria has many *qualifying rules* and each *qualifying rule* has a *calculation rule* to be applied on the orders that meets this qualifying rule. It's a hard process to be implemented manually, so a discount engine is used to ease this process and make changing the criteria a more flexible action.

__Let's dig deeper into the discount engine qualifying rules and their corresponding calculation rules:__
<div align="center">
  <img src="images/Discount Criteria.png" alt="Image">
  <p><em>Discount Engine Criteria: Qualifying Rules and their corresponding Calculation Rules</em></p>
</div>

__Extra Rules:__

* Transactions that didn't qualify to any discount will have 0% discount.
* Transactions that qualified to more than one discount will get the average of the top 2 discounts.

Business also needs a new file that contains all orders' information besides the final price after passing the orders through our discount engine.

## Technical Requirements ##

Our discount engine needs to be written in __Scala__. Scala supports many programming paradigms, *Imperative Programming*, *Functional Programming*, and *OOP*.

But, to have this discount engine to be more flexible in enhancement, the code needs to be written in __pure functional manner__. So many technical requirements need to be satisfied by our code:

* No mutable variables or data structures allowed
* No loops allowed
* All functions must be pure:
    * Output depends solely on input
    * Inputs to the functions don't get mutated
    * Have a predictable behavior
    * No side effects

So in this project, I'll to write some code in __Scala__ with *Functional Programming Paradigm* to implement this __Discount Engine__ so that our Retail Store can use it, add discounts to orders, and get their customers' satisfaction!

## Data Source ##

Data source is simply a *csv* file which contains some orders' information such as:
* Order's date and time
* Product name of this order
* Expiry date of the product
* Quantity of the product
* Unit price of the product
* Channel of the order
    * Store
    * App
* Payment method
    * Cash
    * Visa

To explore the data and download it please check [TRX1000.csv](data/TRX1000.csv) file.

## Code Explaination ##


## How to run the project ? ##