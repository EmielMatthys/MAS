package delegate.ant;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.SimulatorUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
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

public class ExplorationAnt extends Ant implements SimulatorUser {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ExplorationAnt.class);

    private int hops;

    private Truck truck;

    private SimulatorAPI sim;

    private Optional<Package> aPackage;

    private Point destination;

    private Plan plan;

    private static final int CLONE_MAX = 5;


    private double estimatedArrival;


    /**
     * Instantiate ExplorationAnt. Will start moving to package pickup location.
     * @param startLocation spawn location
     * @param aPackage package
     * @param truck truck to report to
     * @param hops hops
     */
    public ExplorationAnt(Point startLocation, Package aPackage, Truck truck, int hops) {
        super(startLocation);
        this.LIFETIME = Integer.MAX_VALUE;
        this.aPackage = Optional.of(aPackage);
        this.truck = truck;
        this.hops = hops;

        this.destination = aPackage.getPickupLocation();

        this.plan = new Plan(truck);
    }

    /**
     * Instantiate ExplorationAnt with forced destination. To be used when spawned by truck isntead of old exploration ant
     * @param startLocation spawn
     * @param aPackage source package
     * @param truck truck to report to
     * @param hops hops
     * @param destination forced destination
     */
    public ExplorationAnt(Point startLocation, Package aPackage, Truck truck, int hops, Point destination) {
        this(startLocation, aPackage, truck, hops);
        this.destination = destination;
    }

    public ExplorationAnt(Point startLocation, Truck truck, int hops, Point randomDest) {
        super(startLocation, 200);
        this.LIFETIME = Integer.MAX_VALUE; //TODO HIER ZIT LIFETIME FOUT
        this.aPackage = Optional.absent();
        this.truck = truck;
        this.hops = hops;

        this.destination = randomDest;

        this.plan = new Plan(truck);

    }

    @Override
    public void tick(TimeLapse timeLapse) {
        if(!roadModel.containsObjectAt(this, destination)){
            roadModel.moveTo(this, destination, timeLapse);
        }
        else{
            getLIFETIME();
        }
    }

    @Override
    public void setSimulator(SimulatorAPI api) {
        this.sim = api;
    }

    @Override
    public void visit(LocationAgent t) {
        if(this.deathMark || LIFETIME == 0)
            return;

        List<FeasibilityPheromone> feasibilityPheromones = getDmasModel().detectPheromone(t, FeasibilityPheromone.class);
        if(feasibilityPheromones.isEmpty()) {
            hops = 0;
            LIFETIME = 0;
            return;
        }

        if(hops-- <= 0){
            //TODO report path to truck
        }
        else{
            int i = 0;
            for(FeasibilityPheromone p : feasibilityPheromones){
                if(i++ > CLONE_MAX)
                    break;

                ExplorationAnt ant = new ExplorationAnt(t.getPosition(), p.getSourcePackage(), truck, hops);
                sim.register(ant);
            }
        }

        LIFETIME = 0;
    }

    @Override
    public void visit(Package t) {
        // Detect intention pheromones
        // if found --> stop looking
        // else --> go to destination

        if(!aPackage.isPresent()){
            // Truck has no package yet, make this the new one
            aPackage = aPackage.of(t);
        }

        if(!t.equals(aPackage.get()))
            return;

        List<IntentionPheromone> intentionPheromones = getDmasModel().detectPheromone(t, IntentionPheromone.class);

        if(!intentionPheromones.isEmpty()) {
            markDead();
            return;
        }

        destination = aPackage.get().getDeliveryLocation();

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