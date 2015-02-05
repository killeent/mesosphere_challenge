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

    public static final int MAX_ELEVATORS = 16;

    /**
     * Constructs a new ElevatorControlService.
     *
     * @param floorCount Number of floors.
     * @param elevatorCount Number of elevators.
     * @throws java.lang.IllegalArgumentException if floorCount < 1 or
     * elevatorCount < 1, or elevatorCount > MAX_ELEVATORS.
     */
    public SimpleElevatorControlService(int floorCount, int elevatorCount) {
        if (floorCount < 1 || elevatorCount < 1) {
            throw new IllegalArgumentException("must be at least 1 floor/elevator");
        }
        if (elevatorCount > MAX_ELEVATORS) {
            throw new IllegalArgumentException("too many elevators");
        }
        elevators = new ArrayList<Elevator>(elevatorCount);
        for (int i = 0; i < elevatorCount; i++) {
            // initialize all elevators at the bottom floor
            elevators.add(new Elevator(i, 0, floorCount - 1, 0, 0));
        }
        requests = new ArrayList<List<Request>>(floorCount);
        for (int i = 0; i < elevatorCount; i++) {
            requests.add(new LinkedList<Request>());
        }
    }

    @Override
    public synchronized List<Triple<Integer>> status() {
        List<Triple<Integer>> result = new ArrayList<Triple<Integer>>(elevators.size());
        for (Elevator e : elevators) {
            result.add(new Triple<Integer>(e.getID(), e.getCurrentFloor(), e.getDestinationFloor()));
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
        for (Elevator e : elevators) {
            result.add(e.getID());
        }
        return result;
    }

    @Override
    public synchronized void pickup(int pickupFloor, int destinationFloor) {
        if (pickupFloor < 0 || pickupFloor >= requests.size() ||
                destinationFloor < 0 || destinationFloor >= requests.size()) {
            throw new IllegalArgumentException("invalid floors");
        }
        if (pickupFloor == destinationFloor) {
            throw new IllegalArgumentException("pickup and destination cannot be same");
        }
        requests.get(pickupFloor).add(new Request(destinationFloor));
    }

    @Override
    public synchronized void step() {
        // move each elevator one step
        for(Elevator e : elevators) {
            // save the old floor
            int prevFloor = e.getCurrentFloor();

            // move the elevator towards its desired floor
            e.step();

            // first check if any of the elevators passengers are at their
            // desired floor
            e.releasePassengers();

            // if we are the destination floor, reverse course to top
            // or bottom
            if (e.getCurrentFloor() == e.getDestinationFloor()) {
                if (e.getCurrentFloor() > prevFloor) {
                    // at the 'top' => go down
                    update(e.getID(), e.getMinFloor());
                } else {
                    // at the 'bottom' => go up
                    update(e.getID(), e.getMaxFloor());
                }
            }

            // if there are any waiters at this floor, let them in the
            // elevator if its going in the right direction
            for (Iterator<Request> iter = requests.get(e.getCurrentFloor()).iterator(); iter.hasNext();) {
                Request req = iter.next();
                int reqDest = req.getDesiredFloor();

                if (reqDest >= e.getDestinationFloor() && reqDest < e.getCurrentFloor()) {
                    iter.remove();
                    e.serviceRequest(req);
                }
            }
        }
    }

    // Encapsulates the state of an elevator
    private static class Elevator {
        private List<Request> passengers;   // people in this elevator

        private int id;
        private int minFloor;
        private int maxFloor;
        private int currentFloor;
        private int destinationFloor;

        /**
         * Constructs a new elevator.
         *
         * @param id of this elevator.
         * @param minFloor minimum floor this elevator can travel to.
         * @param maxFloor maximum floor this elevator can travel to.
         * @param currentFloor current floor of this elevator.
         * @param destinationFloor destination floor of this elevator.
         *
         * @throws java.lang.IllegalArgumentException if minFloor > maxFloor.
         */
        public Elevator(int id, int minFloor, int maxFloor, int currentFloor, int destinationFloor) {
            if (minFloor > maxFloor) {
                throw new IllegalArgumentException("invalid min/max floors");
            }

            this.id = id;
            this.minFloor = minFloor;
            this.maxFloor = maxFloor;
            this.currentFloor = currentFloor;
            this.destinationFloor = destinationFloor;

            passengers = new LinkedList<Request>();
        }

        // getters

        public int getID() {
            return id;
        }

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
         * Service a request.
         */
        public void serviceRequest(Request req) {
            passengers.add(req);
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
