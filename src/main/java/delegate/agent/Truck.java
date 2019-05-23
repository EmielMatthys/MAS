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
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.attribute.standard.Destination;
import java.util.ArrayList;
import java.util.List;

public class Truck extends Vehicle implements TickListener, MovingRoadUser, SimulatorUser
{
    protected static final Logger LOGGER = LoggerFactory.getLogger(Truck.class);

    private static final double VEHICLE_SPEED = 0.2d;
    private static final int VEHICLE_CAPACITY = 1;
    private static final int HOPS = 1;
    private static final int EXPLORATION_FREQUENCY = 60;
    private int tick = 0;

    private RandomGenerator rng;
    private SimulatorAPI sim;

    private Optional<Package> currentPackage;

    //Temp to give with Intention Ant
    private Optional<Point> destination;

    private List<Plan> plans;


    public Truck(RandomGenerator rng, Point startPos) {
        super(VehicleDTO.builder()
                .speed(VEHICLE_SPEED)
                .capacity(VEHICLE_CAPACITY)
                .startPosition(startPos).build());
        this.rng = rng;

        this.destination = Optional.absent();
        this.currentPackage = Optional.absent();

        this.plans = new ArrayList<>();
    }

    public Truck(RandomGenerator rng, Point startPos, Point destination){
        this(rng, startPos);

        this.destination = Optional.of(destination);
    }

    public Truck(RandomGenerator rng, Point startPos, Package p){
        this(rng, startPos);

        this.currentPackage = Optional.of(p);
    }

    @Override
    protected void tickImpl(TimeLapse time) {
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
    }

    private Plan getBestPlan(List<Plan> plans) {
        return plans.get(0);
    }

    private void spawnExplorationAnt() {
        ExplorationAnt ant = new ExplorationAnt(getRoadModel().getPosition(this), currentPackage.get(), this, HOPS);
        try{
            sim.register(ant);
        }catch (IllegalArgumentException e){}
    }

    private void spawnIntentionAnt() {
        IntentionAnt ant = new IntentionAnt(getRoadModel().getPosition(this), this, this.destination.get());
        try {
            sim.register(ant);
        } catch (IllegalArgumentException e) {

        }
    }

    public void notify(boolean otherPheromone) {
        System.out.println("OTHER PHEROMONE: " + otherPheromone);
    }

    @Override
    public void setSimulator(SimulatorAPI api) {
        this.sim = api;
    }

    public void explorationCallback(Plan plan) {

        plan.getPath().poll();
        LOGGER.warn("received pheromone callback: first point=" + plan.getPath().peek() + " truck pos="+getRoadModel().getPosition(this));
        this.plans.add(plan);
    }
}
