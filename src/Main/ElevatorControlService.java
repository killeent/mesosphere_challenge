package main;

import java.util.List;
import java.util.Set;

/**
 * Interface for an ECS.
 *
 * @author Trevor Killeen (2015)
 */
public interface ElevatorControlService {

    /**
     * Returns the status of the ECS.
     *
     * @return A list of triples of the form (elevatorID, current floor, target floor).
     */
    public List<Triple<Integer>> status();

    /**
     * Update the destination floor for specified elevator.
     *
     * @param elevatorID The elevator to update.
     * @param destinationFloor destination floor.
     * @throws java.lang.IllegalArgumentException if elevatorID, destinationFloor
     * is invalid.
     */
    public void update(int elevatorID, int destinationFloor);

    /**
     * Get the range of acceptable floors for the specified elevator.
     *
     * @throws java.lang.IllegalArgumentException if elevatorID is invalid.
     * @return The (min floor, max floor) for the specified elevator.
     */
    public Pair<Integer> floorRange(int elevatorID);

    /**
     * @return A set of elevator IDs for all elevators in the system.
     */
    public Set<Integer> idSet();

    /**
     * Initiate a pickup request.
     *
     * @param pickupFloor Floor to be picked up on.
     * @param destinationFloor Floor desired.
     * @throws java.lang.IllegalArgumentException if pickup, destination
     * is invalid.
     */
    public void pickup(int pickupFloor, int destinationFloor);

    /**
     * Advance the state of the world by one time-step.
     */
    public void step();

}
