# Gravel Pit System

To run the tests use:
```bash
sbt test
```
To compile and run use:
```bash 
sbt run
```
After starting program you will be greeted by the prompt to enter commands:  
- **`produce [minGrain] [maxGrain] [amount]`** - create or update gravel pit with give ***[amount]*** returning all gravel pits in the system if input does not pass validation was appropriate error  
- **`order [minGrain] [maxGrain] [amount]`** - order ***[amount]*** of gravel with grain interval ***[minGrain]*** and ***[maxGrain]*** if sufficient amount of gravel is available in system it will return order with list of amounts of gravel and its grain interval otherwise an error with amount of gravel missing to finalize an order.      
- **`delete [minGrain] [maxGrain]`** - delete a pit with ***[minGrain]*** and ***[maxGrain]*** if that pit exists it will be return otherwise an error will be returned.   
- **`status`** - print out current gravel pits in system   
- **`orders`** - print out all realised orders  

## Implementation
The Implementation of Gravel Pit System is done in Scala.
There are four main classes `Pit`, `GravelPitSystemImpl`, `Order`, `OrderSystemImpl`.

##### Pit
Manages amount of gravel located on the pit and it's grain interval.

##### GravelPitSystemImpl
Manages gravel pits, deals with production and deletion of pits and handles incoming orders.

##### Order
Stores amount of ordered gravel, it's grain interval, pits from which gravel was taken to fulfill the order and timestamp at which order was posted.

##### OrderSystemImpl
Logs fulfilled orders and prepares the best list of gravel pits to fulfill given order.

##### Errors
System wide Errors that are return throughout running system. Base error class
```scala
sealed abstract class Error
```
which other more specific errors extend `NotAValidInterval`, `NegativeGrain`, `NegativeGravelAmount`, `NoValidGravelPit`, `NonExistingGravelPit` and `InsufficientGravelAmount` which returns the amount of missing gravel needed for the operation. 

_More details can found in docs comments in code._
### Order Algorithm

#### Naive "first served"
This naive way of handling incoming orders has so pretty obvious limitation.   
For example: 

We have two productions:
1. Production - Grain = (0,  5), Amount = 50
2. Production - Grain = (5, 20), Amount = 200

First one is narrower and the second is wider and have more gravel.

If we had to use the naive algorithm and we have this incoming orders
1. Order - Grain = (0, 20), Amount = 40
2. Order - Grain = (0, 10), Amount = 30

The first order will take from the first production and the second wont have enough gravel on the first pit to be finalized.

We see the problems with this algorithm on this example. 
It does take in to consideration all available productions that can be used for the order. 
Also it does not use any available information such as **Processed Orders**, **Grain Intervals**, **Gravel Amounts**.

So taking it into consideration I constructed my own algorithm for processing orders.

#### Implemented Algorithm
This algorithm uses metrics to calculate from which pit it is the most optimal to take gravel for an order.
Also I took inspiration form *Adaptive integration*, where to have better accuracy we cut the integration area in to two
and integrate both of them for better approximation. My algorithm work with the same idea. 
It cuts order amount in to smaller and smaller values until we do not gain or gain negligible profits. 
The other condition is that the values reach defined by us threshold.

The Metrics that are being taken into consideration are **Processed Orders**, **Grain Intervals**, **Gravel Amounts**.
Making Orders prioritize the pits that are less in use and ones with higher amount of gravel 
or the ones with bigger grain Intervals as they should have harder time to be in the scope of the ordered grain.

So with the above example the first order would take most of it ordered amount form the second production as it is bigger
and have more gravel. This would allow the second order to bo finalized.

This algorithm can be further expanded by adding more metrics taking more thinks into consideration when choosing 
appropriate pits for the order. For example my metrics do not care about real time of placed orders or the time and 
frequency of productions so that it can be used to better approximate best order solution or even if the order can not be
finalized approximate when it can be done. 
The other thing are the weights associated with the metrics as they are set manually 
and do not have to be the best for performance. For the best performance the weights in my opinion change throughout the
running of the system as the productions and orders can vary in their frequency, so something like neural network working 
in the background of the system making sure that the metrics can calculate the best possible performance.