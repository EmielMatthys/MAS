package delegate.ant;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.SimulatorUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import delegate.agent.Package;
import delegate.agent.Truck;
import delegate.ant.pheromone.FeasibilityPheromone;

import java.util.List;
import java.util.Set;

public class ExplorationAnt extends Ant implements Cloneable, SimulatorUser {

    private int hops;

    private Truck truck;

    private ExplorationState state;

    private SimulatorAPI sim;

    private Package aPackage;

    public ExplorationAnt(Point startLocation, Package aPackage, ExplorationState state, Truck truck, int hops) {
        super(startLocation);
        this.LIFETIME = Integer.MAX_VALUE;
        //.destination = destination;
        this.aPackage = aPackage;
        this.truck = truck;
        this.hops = hops;

        this.state = state;
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        /*
        1. go to starting point until reached
        2. smell for pheromones
        3. hops--; if hops > 0 : split this ant for each pheromone with new starting point as destination of package
        4. if hops == 0, report path to truck
         */

        // Are we going to deliverylocation of package?
        if(state == ExplorationState.TO_DELIVERY_LOCATION){

            // Have we reached the deliverylocation of the package?
            if(getRoadModel().containsObjectAt(this, aPackage.getDeliveryLocation())){
                Point currentLocation = aPackage.getDeliveryLocation();

                // Smell for pheromones
                List<FeasibilityPheromone> feasPheromones = getDmasModel().detectPheromone(currentLocation, FeasibilityPheromone.class);

                hops--;
                if(hops <= 0){
                    //TODO return path to truck
                }
                else{
                    for (FeasibilityPheromone ph : feasPheromones){
                        ExplorationAnt newAnt = new ExplorationAnt(
                                currentLocation, // our current location
                                ph.getSourcePackage(),
                                ExplorationState.TO_PACKAGE_SOURCE,
                                truck,
                                hops
                        );
                        sim.register(newAnt);
                    }
                    sim.unregister(this);
                }
            }
            else if(timeLapse.hasTimeLeft())
                getRoadModel().moveTo(this, aPackage.getDeliveryLocation(), timeLapse);
        }
        else if(state == ExplorationState.TO_PACKAGE_SOURCE){
            if(getRoadModel().containsObjectAt(this, aPackage.getPickupLocation())){
                this.setState(ExplorationState.TO_DELIVERY_LOCATION);
            }
            else if(timeLapse.hasTimeLeft())
                getRoadModel().moveTo(this, aPackage.getDeliveryLocation(), timeLapse);
        }

    }

    public void setState(ExplorationState state) {
        this.state = state;
    }

    @Override
    public void setSimulator(SimulatorAPI api) {
        this.sim = api;
    }

    public enum ExplorationState{
        TO_DELIVERY_LOCATION, TO_PACKAGE_SOURCE
    }
}
