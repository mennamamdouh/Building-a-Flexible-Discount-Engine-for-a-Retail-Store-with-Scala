# Building-a-Flexible-Discount-Engine-for-a-Retail-Store-with-Scala

## Overview ##

A huge retail store wants a rule engine that qualifies orders' transactions to discounts based on a set of *qualifying rules* and automatically calculates the proper discount based on some *calculation rules*. The program is written in __Scala__ in a pure functional manner.

## Business Requirements ##

As mentioned above, the retail store has many orders to add discount  to them according to a specific criteria. This criteria has many *qualifying rules* and each *qualifying rule* has a *calculation rule* to be applied on the orders that meets this qualifying rule. It's a hard process to be implemented manually, so a discount engine is used to ease this process and make changing the criteria a more flexible action.

__Let's dig deeper into the discount engine qualifying rules and their corresponding calculation rules:__
<div align="center">
  <img src="Discount Criteria.png" alt="Image">
  <p><em>Discount Engine Criteria: Qualifying Rules and their corresponding Calculation Rules</em></p>
</div>

__Extra Rules:__

* Transactions that didn't qualify to any discount will have 0% discount.
* Transactions that qualified to more than one discount will get the average of the top 2 discounts.

Business also needs a new file that contains orders' information and the final price after passing the orders through our discount engine.

In this project, I'll to write some code in __Scala__ to implement this __Discount Engine__ so that our Retail Store can use it, add discounts to orders, and get their customers' satisfaction!

## Technical Requirements ##


## Data Source ##


## Code Explaination ##


## How to run the project ? ##