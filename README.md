Elevator Challenge

Challenge Performed from 1pm-5pm PST, on 2/4/15. 

Top-level Interface

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