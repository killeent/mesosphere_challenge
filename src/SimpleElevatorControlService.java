import java.util.*;

/**
 * A simple implementation of an ECS. See README.md for a discussion of this
 * implementation.
 *
 * @author Trevor Killeen (2015)
 */
public class SimpleElevatorControlService implements ElevatorControlService {

    private final List<Elevator> elevators;
    private final List<List<Request>> requests;

    /**
     * Constructs a new ElevatorControlService.
     *
     * @param floorCount Number of floors.
     * @param elevatorCount Number of elevators.
     * @throws java.lang.IllegalArgumentException if floorCount < 1 or
     * elevatorCount < 1.
     */
    public SimpleElevatorControlService(int floorCount, int elevatorCount) {
        if (floorCount < 1 || elevatorCount < 1) {
            throw new IllegalArgumentException("must be at least 1 floor/elevator");
        }
        elevators = new ArrayList<Elevator>(elevatorCount);
        for (int i = 0; i < elevatorCount; i++) {
            // initialize all elevators at the bottom floor
            elevators.add(new Elevator(0, floorCount - 1, 0, 0));
        }
        requests = new ArrayList<List<Request>>(floorCount);
        for (int i = 0; i < elevatorCount; i++) {
            requests.add(new LinkedList<Request>());
        }
    }

    @Override
    public synchronized List<Triple<Integer>> status() {
        List<Triple<Integer>> result = new ArrayList<Triple<Integer>>(elevators.size());
        for(int i = 0; i < elevators.size(); i++) {
            Elevator temp = elevators.get(i);
            result.add(new Triple<Integer>(i, temp.currentFloor, temp.destinationFloor));
        }
        return result;
    }

    @Override
    public synchronized void update(int elevatorID, int destinationFloor) {
        if (!isValidElevatorID(elevatorID)) {
            throw new IllegalArgumentException("invalid elevator");
        }
        if (destinationFloor < 0 || destinationFloor >= requests.size()) {
            throw new IllegalArgumentException("floor out of bounds");
        }
        try {
            elevators.get(elevatorID).setDestinationFloor(destinationFloor);
        } catch (IllegalArgumentException e) {
            // could probably handle this exception better
            throw new IllegalArgumentException("invalid floor: " + e.getMessage());
        }
    }

    // determines if this is a valid elevatorID or not
    private boolean isValidElevatorID(int elevatorID) {
        return elevatorID >= 0 && elevatorID < elevators.size();
    }

    @Override
    public Pair<Integer> floorRange(int elevatorID) {
        if (!isValidElevatorID(elevatorID)) {
            throw new IllegalArgumentException("invalid elevator");
        }
        Elevator temp = elevators.get(elevatorID);
        return new Pair<Integer>(temp.getMinFloor(), temp.getMaxFloor());
    }

    @Override
    public synchronized Set<Integer> idSet() {
        Set<Integer> result = new HashSet<Integer>();
        for (int i = 0; i < elevators.size(); i++) {
            result.add(i);
        }
        return result;
    }

    @Override
    public synchronized void pickup(int pickupFloor, int destinationFloor) {
        if (pickupFloor < 0 || pickupFloor >= requests.size() ||
                destinationFloor < 0 || destinationFloor >= requests.size()) {
            throw new IllegalArgumentException("invalid floors");
        }
        requests.get(pickupFloor).add(new Request(destinationFloor));
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
