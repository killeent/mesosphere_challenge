## Elevator Challenge

Challenge Performed from 1pm-5pm PST, on 2/4/15. 

### Build Information

### Top-level Interface

First we define the goals of the system as related to the four interface
functions that we need to provide.

List((int, int, int)) status(): returns a list of results of calling update() on
each elevator.

(int, int) update(int): returns the current floor and goal floor  of the
elevator specified by the passed elevator ID.

Both of these functions are unchanged. In general, because they are merely
returning the state of the world, they do not inform development, other than
requiring us to provide an interface for getting information about the state of
the world.

void pickup(int, int): requests a pickup at the floor specified by the first
parameter, to travel to the floor at the second parameter

We extend the basic interface by allowing the client to specify their desired
floor when requesting elevator service, as opposed to just desiring to go up
or down. It is important to note that we are still not implementing the
real-world functionality of requesting a new floor while in the elevator. 

In order to support this functionality, we will need to track the passengers
in an elevator and their desired floors -> we will "open the doors" and let
passengers out on their specific floor, while "keeping" the other passengers
inside the elevator. 

Additionally, we will need to make sure that someone eventually picks up a 
passenger at the requested floor. To do so, we will need to make sure that a
pickup request either initiates the movement of an elevator to pick up that
passenger or registers the request with an elevator already performing work.

void step(): Time-step our world. To do so, we will simply iterate through
each of our elevators and have them move one floor towards their intended
direction. This is the simplest implementation. 

### Synchronization Issues

We make the following assumptions about our world to inform development. 

1. Requests (in the form of pickup() API calls) are independent of calls
to step().
2. Calls to step should be executed atomically, i.e. no interleaving of 
pickup requests. 

As a result, we decided to have a single lock guarding the state of our world.
During a call to pickup() we:

1. acquire the global lock
2. make our request
3. release the lock

During a call to step() we:
1. acquire the global lock
2. iterate through all elevators, performing their actions one by one
3. release the lock

Though in the real world we could pickups and the movement of elevators
are continuous and occur in parallel, given the limited timespan necessary
to implement this project, we chose to have this simple implementation. 

### Scheduling Issues

I chose to implement a SCAN-like (Elevator) Algorithm used for disk scheduling 
to handle elevator scheduling. The SCAN algorithm is modeled after elevator 
service so it has to be the optimal algorithm right? :) 

The SCAN algorithm means that the elevator will only move in one direction
(either up or down) until reaching the top or bottom, before reversing
course. This constrains our behavior at each floor because the elevator will 
only pick up people who are intending to travel in the same direction we are 
headed.

This algorithm is better because it is more efficient -> we will serve more
passengers per floor traveled. For example, suppose under the FCFS algorithm
we have three passengers and one elevator on a 5 story building. The passenger
requests are (2, 4), (2, 1) and (2, 5) in that order. Under FCFS, we would
travel from 2->4->1->5 to drop off the passengers, requiring 9 total floors. 
Under our algorithm, we would go from 2->5->1, requiring only 7 floors.

There are a number of ways we could further optimize scheduling, that we 
rejected to simplify the implementation:

1. Instead of having the elevator go all the way to the top floor when
moving up, and all the way to the bottom floor when going down, we could
simply stop at the highest/lowest floor requested by any passenger in the
elevator. However, one could imagine where a person could starve if the
elevator handled requests that pinged between floors 2 and 5 while someone
was waiting at floor 1. By guaranteeing that the elevator simply travels
all the way up and all the way down, we guarantee eventual satisifaction of
a request at a cost of efficiency.

2. If we have a lot of elevators, we could only have a subset of them working
at low request rates to improve efficiency (as measured by people serviced /
total floors moved). However, as with the above concern, this would greatly
increase the complexity of our system.

### Class Design

The main class we design is the ElevatorControlSystem class, which implements
the interface we designed above, and contains the state of our world.

state:

* lock guarding the state of the system
* an array of lists of Requests, one for each floor of the building
* an array of Elevators

public functions:

* the four API functions described above
* a constructor, which takes in a user-specified floor count and elevator
count
* getIDs(): returns the set of elevator IDs

discussion:

The API is discussed above, and the constructor is fairly self-explanatory.
Instead, let's consider the class design. 

First, we have an array of lists of requests -> i.e. arr[0] contains the 
list of requests for the 0th floor. When an elevator arrives at a floor, it 
will check and  see if there are people waiitng. It will pick up all those
whose requests are in the direction that elevator is traveling.

We also have an array of elevators. This provides the ElevatorControlSystem
access to each elevator in the system and allows it to query them for
information. 

The next class we consider is the Request class. 

state:

* desired floor

public functions:

* a constructor, which takes in the specified desired floor
* getDesiredFloor(): returns the floor desired by this request

discussion:

The request class is fairly simple, but is designed for modularity and 
extensibility. We can think of the request as a person so we could imagine
adding various extensions (e.g. if the person as a fireman, perhaps they
have a key that allows them exclusive access to the elevator, and their
request could embody this greater priority). 

The last class we consider is the Elevator class.

state: 

* current floor
* min, max floor
* destination floor
* a list of passengers (Requests)

public functions:

* a constructor, which takes in a specified initial floor and min, max floors
* a step() function, to be called whenever a global step occurs. This simply
moves the elevator one floor. 
* a releasePassengers() function, which lets passengers out of the elevator
that are at their requested floor
* a serviceRequests() function, which lets passengers into the elevator

discussion:

The challenge with the Elevator is that it is inherently coupled to the 
ElevatorControlSystem, so we want to design it in a way so that the 
dependencies are one sided. Thus we made our Elevator simply keep track of 
who it holds andwhere it is headed, and made the ECS responsible for calling
the step, releasePassengers and serviceRequests functions at the appropriate
times. 

Additionally, we like to add the ability to specify the min and max
floors. Not only is this necessary to implement our SCAN algorithm, it allows
us to create elevators that only service certain ranges of the building. For
example, many buildings have elevators that only service a certain range of
floors. 

class organization:

For now, we chose to implement Request and Elevator as inner classes within
the ElevatorControlSystem because they are fairly specific to the ECS 
implementation. One could imagine eventually moving these classes out (and/or)
defining interfaces for them, but for now we will not.

### Implementation Notes

### Final Thoughts