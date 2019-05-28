package delegate.agent;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.SimulatorUser;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.DynamicGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import delegate.Plan;
import delegate.ant.Ant;
import delegate.ant.ExplorationAnt;
import delegate.ant.IntentionAnt;
import delegate.ant.pheromone.FeasibilityPheromone;
import delegate.ant.pheromone.Pheromone;
import delegate.util.TravelDistanceHelper;
import org.apache.commons.math3.analysis.function.Exp;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.swt.graphics.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.attribute.standard.Destination;
import java.util.ArrayList;
import java.util.List;

public class Truck extends Vehicle implements TickListener, MovingRoadUser, SimulatorUser
{
    protected static final Logger LOGGER = LoggerFactory.getLogger(Truck.class);

    public static final double VEHICLE_SPEED = 0.2d;
    private static final int VEHICLE_CAPACITY = 1;
    private static final int HOPS = 2;
    private static final int EXPLORATION_FREQUENCY = 150;
    private int exp_tick = 0;
    private int int_tick = 0;

    boolean first = true;

    private RandomGenerator rng;
    private SimulatorAPI sim;

    private Optional<Package> currentPackage;

    //Temp to give with Intention Ant
    private Optional<Point> destination;

    private List<Plan> plans;

    private Optional<Path> path;

    private boolean exploration;



    public Truck(RandomGenerator rng, Point startPos) {
        super(VehicleDTO.builder()
                .speed(VEHICLE_SPEED)
                .capacity(VEHICLE_CAPACITY)
                .startPosition(startPos).build());
        this.rng = rng;

        this.destination = Optional.absent();
        this.currentPackage = Optional.absent();
        this.exploration = true;
        this.plans = new ArrayList<>();
    }


    public Truck(RandomGenerator rng, Point startPos, Package p){
        this(rng, startPos);
        this.exploration = true;
        this.currentPackage = Optional.of(p);
        path = Optional.absent();
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        RoadModel rm = getRoadModel();
        PDPModel pm = getPDPModel();
        //TODO Path starts @DeliveryLocation but seems to work for now


        //TODO Truck should always follow a path
        if (!plans.isEmpty()) {
            //try next point in path
            try {
                //LOGGER.warn(plans.get(0).toString());
                rm.followPath(this, plans.get(0).getPath(), time);

            } catch (IllegalArgumentException e) {
                plans.get(0).getPath().poll();
            }
            if (plans.get(0).getPath().isEmpty()) {
                plans.remove(0);
                if (!plans.isEmpty() && !plans.get(0).getPackages().isEmpty())
                    currentPackage = Optional.of(plans.get(0).getPackages().get(0));
                LOGGER.warn("PATH EMPTY");
            }
        }
        else spawnExplorationAnt();

        if(currentPackage.isPresent()){
            final boolean inCargo = pm.containerContains(this, currentPackage.get());
            // sanity check: if it is not in our cargo AND it is also not on the
            // RoadModel, we cannot go to curr anymore.
            if (!inCargo && !rm.containsObject(currentPackage.get())) {
                currentPackage = Optional.absent();
            }
            else if (inCargo) {
                //rm.moveTo(this, currentPackage.get().getDeliveryLocation(), time);
                if (rm.getPosition(this).equals(currentPackage.get().getDeliveryLocation())) {
                    // deliver when we arrive
                    pm.deliver(this, currentPackage.get(), time);
                    LOGGER.warn("PACKAGE DELIVERED");
                    exploration = true;
                }
            } else {
                // it is still available, go there as fast as possible
                //rm.moveTo(this, currentPackage.get(), time);
                if (rm.equalPosition(this, currentPackage.get())) {
                    // pickup customer
                    LOGGER.warn("PICKED UP PACKAGE");
                    exploration = true;
                    plans.clear();
                    pm.pickup(this, currentPackage.get(), time);
                }
            }
        }
    }

    private void spawnExplorationAnt() {

        if(++exp_tick < EXPLORATION_FREQUENCY)
            return;
        if (!exploration)
            return;
        first = false;
        exp_tick = 0;

        Point spawnLocation = TravelDistanceHelper.getNearestNode(this, getRoadModel());

        if(currentPackage.isPresent() && getPDPModel().containerContains(this, currentPackage.get())){
            LOGGER.warn("SENDING ANT TO PACKAGE DESTINATION FOR FURTHER EXPLORATION");
            exploration = false;
            ExplorationAnt ant = new ExplorationAnt(spawnLocation, currentPackage.get(), this, HOPS, currentPackage.get().getDeliveryLocation());
            sim.register(ant);
        }
        else if(currentPackage.isPresent()){
            LOGGER.warn("GOING TO PACKAGE");
            ExplorationAnt ant = new ExplorationAnt(spawnLocation, currentPackage.get(), this, HOPS);
            sim.register(ant);
            exploration = false;
        }
        else{
            // No current package --> random exploration
            //LOGGER.warn("RANDOM DESTINATION");
            this.exploration = true;
            ExplorationAnt ant = new ExplorationAnt(spawnLocation,this, 1, getRoadModel().getRandomPosition(rng));
            sim.register(ant);
        }
    }

    private void spawnIntentionAnt() {

    }



    @Override
    public void setSimulator(SimulatorAPI api) {
        this.sim = api;
    }

    public void explorationCallback(Plan plan) {
        //TODO Naar andere calls ook luisteren
        LOGGER.warn("received pheromone callback: first point=" + plan.getPath().peek() + " truck pos=" + getRoadModel().getPosition(this));
        if (!plan.getPath().isEmpty()) {
            plan.getPath().poll();
            this.plans.add(plan);
        }
        if (!plan.getPackages().isEmpty() && !currentPackage.isPresent()) {
            currentPackage = Optional.of(plan.getPackages().get(0));

        }
    }
}

/*
 // TODO: helemaal begin: geen assigned package
        // TODO: Paths maken

        PDPModel pm = getPDPModel();
        DynamicGraphRoadModelImpl rm = (DynamicGraphRoadModelImpl) getRoadModel();

        if(!plans.isEmpty()){
            Plan plan = getBestPlan(plans);
            rm.followPath(this, plan.getPath(), time);
            return;
        }

        if(currentPackage.isPresent()){
            final boolean inCargo = pm.containerContains(this, currentPackage.get());
            // sanity check: if it is not in our cargo AND it is also not on the
            // RoadModel, we cannot go to curr anymore.
            if (!inCargo && !rm.containsObject(currentPackage.get())) {
                currentPackage = Optional.absent();
            }
            else if(inCargo){
                if (rm.getPosition(this).equals(currentPackage.get().getDeliveryLocation())) {
                    // deliver when we arrive
                    pm.deliver(this, currentPackage.get(), time);
                    // TODO hier moet nieuw pad worden geactiveerd?
                    // TODO: Choose best path of all received callbacks, follow it and send intention ants.
                }
                else{
                    destination = Optional.absent();
                    tick++;
                    if(tick >= EXPLORATION_FREQUENCY){
                        spawnExplorationAnt();
                        tick = 0;
                    }
                }
            }
            else{
                // On rm but not cargo
                if (rm.equalPosition(this, currentPackage.get())) {
                    // pickup customer
                    pm.pickup(this, currentPackage.get(), time);
                }
                destination = Optional.of(currentPackage.get().getPickupLocation());
            }
        }

        //spawnIntentionAnt();

        if (!time.hasTimeLeft()) {
            return;
        }
        //TODO: MOET BEWEGEN VOLGENS PATH VAN EXPLORATIONANT
        if(destination.isPresent())
            getRoadModel().moveTo(this, destination.get(), time);
 */