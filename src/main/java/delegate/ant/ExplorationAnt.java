package delegate.ant;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.SimulatorUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import delegate.LocationAgent;
import delegate.Plan;
import delegate.agent.Package;
import delegate.agent.Truck;
import delegate.ant.pheromone.FeasibilityPheromone;
import delegate.ant.pheromone.IntentionPheromone;
import delegate.ant.pheromone.Pheromone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class ExplorationAnt extends Ant implements Cloneable, SimulatorUser {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ExplorationAnt.class);

    private int hops;

    private Truck truck;

    private ExplorationState state;

    private SimulatorAPI sim;

    private Package aPackage;

    private Point destination;

    private Plan plan;

    private static final int CLONE_MAX = 5;


    private double estimatedArrival;


    public ExplorationAnt(Point startLocation, Package aPackage, ExplorationState state, Truck truck, int hops) {
        super(startLocation);
        this.LIFETIME = Integer.MAX_VALUE;
        //.destination = destination;
        this.aPackage = aPackage;
        this.truck = truck;
        this.hops = hops;

        this.destination = aPackage.getPickupLocation();
        this.state = state;

        this.plan = new Plan(truck);
    }

    public ExplorationAnt(Point startLocation, Package aPackage, Truck truck, int hops){
        this(startLocation, aPackage, ExplorationState.TO_DELIVERY_LOCATION, truck, hops);
    }

    @Override
    public void tick(TimeLapse timeLapse) {



    }

    public void setState(ExplorationState state) {
        this.state = state;
    }

    @Override
    public void setSimulator(SimulatorAPI api) {
        this.sim = api;
    }

    @Override
    public void visit(LocationAgent t) {
        // Detect feasibility pheromones
        // for each -> send exploration ant to the package source location
        // if hops == 0 --> stop
        List<FeasibilityPheromone> feasibilityPheromones = getDmasModel().detectPheromone(t, FeasibilityPheromone.class);


    }

    @Override
    public void visit(Package t) {
        // Detect intention pheromones
        // if found --> stop looking
        // else --> go to destination

        if(!t.equals(aPackage))
            return;

        List<IntentionPheromone> intentionPheromones = getDmasModel().detectPheromone(t, IntentionPheromone.class);

        if(!intentionPheromones.isEmpty()) {
            markDead();
            return;
        }

        destination = aPackage.getDeliveryLocation();

    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); //TODO
    }

    public enum ExplorationState{
        TO_DELIVERY_LOCATION, TO_PACKAGE_SOURCE
    }
}



/*
 Point currentLocation = getRoadModel().getPosition(this);


        // Are we going to deliverylocation of package?
        if(state == ExplorationState.TO_DELIVERY_LOCATION){

            // Have we reached the deliverylocation of the package?
            if(getRoadModel().containsObjectAt(this, aPackage.getDeliveryLocation())){

                // Smell for pheromones
                List<FeasibilityPheromone> feasPheromones = getDmasModel().detectPheromone(currentLocation, FeasibilityPheromone.class);

                hops--;
                if(hops <= 0){
                    //TODO return path to truck
                    truck.explorationCallback(plan);
                    this.LIFETIME = 0;
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
                    return;
                }
            }
            else if(timeLapse.hasTimeLeft()) {
                getRoadModel().moveTo(this, aPackage.getDeliveryLocation(), timeLapse);
                if(first)
                    plan.addToPath(getRoadModel().getDestination(this));
            }
        }
        else if(state == ExplorationState.TO_PACKAGE_SOURCE){
            if(getRoadModel().containsObjectAt(this, aPackage.getPickupLocation())){
                this.setState(ExplorationState.TO_DELIVERY_LOCATION);
            }
            else if(timeLapse.hasTimeLeft())
                getRoadModel().moveTo(this, aPackage.getPickupLocation(), timeLapse);
        }
 */