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
import com.github.rinde.rinsim.core.model.road.RoadUser;
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
    private static final int EXPLORATION_FREQUENCY = 200;
    private int exp_tick = 0;
    private int int_tick = 0;

    private RandomGenerator rng;
    private SimulatorAPI sim;

    private List<Plan> plans;
    private Optional<Plan> currentPlan;



    public Truck(RandomGenerator rng, Point startPos) {
        super(VehicleDTO.builder()
                .speed(VEHICLE_SPEED)
                .capacity(VEHICLE_CAPACITY)
                .startPosition(startPos).build());
        this.rng = rng;

        currentPlan = Optional.absent();
        this.plans = new ArrayList<>();
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        RoadModel rm = getRoadModel();
        PDPModel pm = getPDPModel();


        if(!currentPlan.isPresent() || currentPlan.get().getPath().peekLast() == null){
            selectBestPlan();
        }

        if(currentPlan.isPresent() && time.hasTimeLeft()){
            Package nextP = currentPlan.get().peekNextPackage();
            Point currentPos = rm.getPosition(this);

            if(pm.containerContains(this, nextP)){
                // We are moving towards a destination

                if(currentPos.equals(nextP.getDeliveryLocation())){
                    pm.deliver(this, nextP, time);
                    if(currentPlan.get().popNextPackage())
                        currentPlan = Optional.absent();
                }
                else
                    tryFollowPathOrCheat(currentPlan.get(), nextP.getDeliveryLocation(), time);
            }
            else{
                // We are moving towards source

                if(currentPos.equals(nextP.getPickupLocation())){
                    pm.pickup(this, nextP, time);
                }
                else
                    tryFollowPathOrCheat(currentPlan.get(), nextP.getPickupLocation(), time);
            }
        }

        spawnExplorationAnt();
    }

    /**
     * Uses roadmodel.moveTo if this object is close enough to nextP or tries to follow given plan otherwise
     * @param currentPlan
     * @param nextP
     * @param timeLapse
     */
    private void tryFollowPathOrCheat(Plan currentPlan, Point nextP, TimeLapse timeLapse){
        RoadModel rm = getRoadModel();
        Point currentPos = rm.getPosition(this);

        if(Point.distance(currentPos, nextP) <= 0.5){
            // Very close to destination, cheat to get there
            rm.moveTo(this, nextP, timeLapse);
        }
        else{
            try{
                rm.followPath(this, currentPlan.getPath(), timeLapse);
            }catch (IllegalArgumentException e){
                currentPlan.getPath().poll();
            }
        }
    }

    /**
     * sets the best plan in list of plans if not empty (currently just the first)
     */
    private void selectBestPlan() {
        if(plans.isEmpty()){
            currentPlan = Optional.absent();
            return;
        }
        LOGGER.warn("plans size: "+plans.size());
        currentPlan = Optional.of(plans.stream()
                .filter(plan -> getRoadModel().containsObject(plan.peekNextPackage()))
                .findAny().get());
        plans.clear();
    }

    private void spawnExplorationAnt() {
        if(++exp_tick < EXPLORATION_FREQUENCY)
            return;

        exp_tick = 0;
        Point spawnLocation = TravelDistanceHelper.getNearestNode(this, getRoadModel());

        if(currentPlan.isPresent()){
            // Send ants with forwarded start
            Point lastPoint = currentPlan.get().getPath().peekLast();
            if(lastPoint == null)
                return;
            ExplorationAnt newAnt = new ExplorationAnt(spawnLocation, this, HOPS, lastPoint, currentPlan.get().getPackages());
            sim.register(newAnt);
        }else{
            // No plan --> random ants
            ExplorationAnt ant = new ExplorationAnt(spawnLocation, getRoadModel().getRandomPosition(rng), this, HOPS-1);
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

        //plan.getPath().poll();
        LOGGER.warn("received pheromone callback: first point=" + plan.getPath().peek() + " truck pos="+getRoadModel().getPosition(this));
        this.plans.add(plan);
    }

}