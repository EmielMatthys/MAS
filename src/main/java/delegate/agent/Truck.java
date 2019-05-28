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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Truck extends Vehicle implements TickListener, MovingRoadUser, SimulatorUser
{
    protected static final Logger LOGGER = LoggerFactory.getLogger(Truck.class);

    public static final double VEHICLE_SPEED = 0.2d;
    private static final int VEHICLE_CAPACITY = 1;
    private static final int HOPS = 1;
    private static final int EXPLORATION_FREQUENCY = 60;
    private int exp_tick = 0;
    private int int_tick = 0;

    private RandomGenerator rng;
    private SimulatorAPI sim;
    private Queue<Point> path = new LinkedList<>();

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


    public Truck(RandomGenerator rng, Point startPos, Package p){
        this(rng, startPos);

        this.currentPackage = Optional.of(p);
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        RoadModel rm = getRoadModel();
        PDPModel pm = getPDPModel();

        spawnExplorationAnt();

        //TODO Truck should always follow a path

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
                }
            } else {
                // it is still available, go there as fast as possible
                rm.moveTo(this, currentPackage.get(), time);
                if (rm.equalPosition(this, currentPackage.get())) {
                    // pickup customer
                    pm.pickup(this, currentPackage.get(), time);
                }
            }
        }
    }

    private void spawnExplorationAnt() {
        if(++exp_tick < EXPLORATION_FREQUENCY)
            return;

        exp_tick = 0;

        Point spawnLocation = TravelDistanceHelper.getNearestNode(this, getRoadModel());

        if(currentPackage.isPresent() && getPDPModel().containerContains(this, currentPackage.get())){

            ExplorationAnt ant = new ExplorationAnt(spawnLocation, currentPackage.get(), this, HOPS, currentPackage.get().getDeliveryLocation());
            sim.register(ant);
        }
        else if(currentPackage.isPresent()){
            ExplorationAnt ant = new ExplorationAnt(spawnLocation, currentPackage.get(), this, HOPS);
            sim.register(ant);
        }
        else{
            // No current package --> random exploration
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

        plan.getPath().poll();
        LOGGER.warn("received pheromone callback: first point=" + plan.getPath().peek() + " truck pos="+getRoadModel().getPosition(this));
        this.plans.add(plan);
    }

    public void explorationCallback(Queue<Point> plan) {

        LOGGER.warn("received pheromone callback: " + plan + " truck pos="+getRoadModel().getPosition(this));
        this.path = plan;
    }
}