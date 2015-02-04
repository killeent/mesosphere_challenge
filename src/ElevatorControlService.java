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
     */
    public void update(int elevatorID, int destinationFloor);

    /**
     * @return A set of elevator IDs for all elevators in the system.
     */
    public Set<Integer> idSet();

    /**
     * Initiate a pickup request.
     *
     * @param pickupFloor Floor to be picked up on.
     * @param destinationFloor Floor desired.
     */
    public void pickup(int pickupFloor, int destinationFloor);

    /**
     * Advance the state of the world by one time-step.
     */
    public void step();

}
