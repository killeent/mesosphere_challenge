import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A simple implementation of an ECS. See README.md for a discussion of this
 * implementation.
 *
 * @author Trevor Killeen (2015)
 */
public class SimpleElevatorControlService implements ElevatorControlService {

    public SimpleElevatorControlService() {

    }

    @Override
    public synchronized List<Triple<Integer>> status() {
        return null;
    }

    @Override
    public synchronized void update(int elevatorID, int destinationFloor) {

    }

    @Override
    public synchronized Set<Integer> idSet() {
        return null;
    }

    @Override
    public synchronized void pickup(int pickupFloor, int destinationFloor) {

    }

    @Override
    public synchronized void step() {

    }

    // Encapsulates the state of an elevator
    private static class Elevator {
        private List<Request> passengers;   // people in this elevator

        private int minFloor;
        private int maxFloor;
        private int currentFloor;
        private int destinationFloor;

        /**
         * Constructs a new elevator.
         *
         * @param minFloor minimum floor this elevator can travel to.
         * @param maxFloor maximum floor this elevator can travel to.
         * @param currentFloor current floor of this elevator.
         * @param destinationFloor destination floor of this elevator.
         *
         * @throws java.lang.IllegalArgumentException if minFloor > maxFloor.
         */
        public Elevator(int minFloor, int maxFloor, int currentFloor, int destinationFloor) {
            if (minFloor > maxFloor) {
                throw new IllegalArgumentException("invalid min/max floors");
            }

            this.minFloor = minFloor;
            this.maxFloor = maxFloor;
            this.currentFloor = currentFloor;
            this.destinationFloor = destinationFloor;

            passengers = new LinkedList<Request>();
        }

        // getters

        public int getDestinationFloor() {
            return destinationFloor;
        }

        public int getMinFloor() {
            return minFloor;
        }

        public int getMaxFloor() {
            return maxFloor;
        }

        public int getCurrentFloor() {
            return currentFloor;
        }

        /**
         * Sets the destination floor for this elevator.
         *
         * @throws java.lang.IllegalArgumentException if not in the range
         * of floors this elevator can go to.
         */
        public void setDestinationFloor(int destination) {
            if (destination < minFloor || destination > maxFloor) {
                throw new IllegalArgumentException("invalid floor");
            }
            this.destinationFloor = destination;
        }

        /**
         * Moves one position towards the destination floor.
         */
        public void step() {
            if (currentFloor < destinationFloor) {
                currentFloor++;
            } else if (currentFloor > destinationFloor) {
                currentFloor--;
            }
        }

        /**
         * Service some requests.
         */
        public void serviceRequests(List<Request> requests) {
            passengers.addAll(requests);
        }

        /**
         * Releases all passengers whose destination is the current floor.
         */
        public void releasePassengers() {
            for (Iterator<Request> iter = passengers.iterator(); iter.hasNext();) {
                Request req = iter.next();
                if (req.getDesiredFloor() == currentFloor) {
                    iter.remove();
                }
            }
        }
    }

    // Encapsulates a request for a floor
    private static class Request {
        private int desiredFloor;

        /**
         * Construct a new floor request.
         *
         * @param desiredFloor Floor desired.
         */
        public Request(int desiredFloor) {
            this.desiredFloor = desiredFloor;
        }

        /**
         * @return The desired floor.
         */
        public int getDesiredFloor() {
            return desiredFloor;
        }
    }
}
