package delegate.agent;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.SimulatorUser;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

import delegate.EmptyPlan;
import delegate.IPlan;
import delegate.NormalPlan;
import delegate.ant.*;
import delegate.model.DMASModel;
import delegate.model.DMASUser;
import delegate.util.TravelDistanceHelper;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Truck extends Vehicle implements TickListener, MovingRoadUser, SimulatorUser, DMASUser
{
    protected static final Logger LOGGER = LoggerFactory.getLogger(Truck.class);

    public static final double VEHICLE_SPEED = 0.2d;
    private static final int VEHICLE_CAPACITY = 1;
    private static final int HOPS = 2;
    private static final int EXPLORATION_FREQUENCY = 200;
    private int exp_tick = 0;
    private int int_tick = 0;

    private DMASModel dmasModel;

    private RandomGenerator rng;
    private SimulatorAPI sim;

    private List<NormalPlan> plans;
    private Optional<NormalPlan> currentPlan;

    private boolean dispatchedExplorationAnt = false;



    public Truck(RandomGenerator rng, Point startPos) {
        super(VehicleDTO.builder()
                .speed(VEHICLE_SPEED)
                .capacity(VEHICLE_CAPACITY)
                .startPosition(startPos).build());
        this.rng = rng;

        currentPlan = Optional.empty();
        this.plans = new ArrayList<>();
    }

    private int tick_test = 200;
    @Override
    protected void tickImpl(TimeLapse time) {
        if(tick_test-- > 0)
            return;

        RoadModel rm = getRoadModel();
        PDPModel pm = getPDPModel();

        List<ExplorationAnt> allExpAnts = dmasModel.getAntsOfType(ExplorationAnt.class);

        if(!currentPlan.isPresent()){
            if(!plans.isEmpty()){
                selectBestPlan(); // We can select a plan
            }
            else if(allExpAnts.stream() // No plan to select, send ant if not done already
                        .noneMatch(explorationAnt -> explorationAnt.getTruck().equals(this))){
                spawnRandomExplorationAnt();

            }

            return;
        }
        else {

            // Follow current plan
            Package nextPack = currentPlan.get().getNextPackage();

            if(nextPack == null) {
                currentPlan = Optional.empty();
                return;
            }

            Point currentPos = rm.getPosition(this);

            if(pm.containerContains(this, nextPack)){
                if(currentPos.equals(nextPack.getDeliveryLocation())){
                    pm.deliver(this, nextPack, time);
                    currentPlan.get().deliveredPackage();
                }
                else
                    tryFollowPathOrCheat(currentPlan.get(), nextPack.getDeliveryLocation(), time);
            }
            else{
                if(currentPos.equals(nextPack.getPickupLocation())){
                    pm.pickup(this, nextPack, time);
                    if(currentPlan.get().getNextPackage().equals(currentPlan.get().getTailPackage()))
                        spawnForwardExplorationAnt();
                }
                else
                    tryFollowPathOrCheat(currentPlan.get(), nextPack.getPickupLocation(), time);
            }
        }
    }

    /**
     * Uses roadmodel.moveTo if this object is close enough to nextP or tries to follow given plan otherwise
     * @param currentPlan
     * @param nextP
     * @param timeLapse
     */
    private void tryFollowPathOrCheat(IPlan currentPlan, Point nextP, TimeLapse timeLapse){
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
        LOGGER.warn("plans size: "+plans.size());
        currentPlan = NormalPlan.getBestPlan(plans);
        plans.clear();

        spawnIntentionAnts();
    }

    private void spawnForwardExplorationAnt(){
        Point spawnLocation = TravelDistanceHelper.getNearestNode(this, getRoadModel());
        Package pack = currentPlan.get().getTailPackage();

        if(pack != null) {
            ForwardExplorationAnt ant = new ForwardExplorationAnt(spawnLocation, pack, this, HOPS);
            plans.clear();
            LOGGER.warn("Sending forward ant");
            sim.register(ant);
        }
    }

    private void spawnRandomExplorationAnt(){
        Point spawnLocation = TravelDistanceHelper.getNearestNode(this, getRoadModel());
        Iterator<Package> it = getRoadModel().getObjectsOfType(Package.class).iterator();
        if(it.hasNext()) {
            RandomExplorationAnt ant = new RandomExplorationAnt(spawnLocation, it.next(), HOPS, this);
            plans.clear();
            LOGGER.warn("Sending random ant");
            sim.register(ant);
        }
    }


    /*private void spawnExplorationAnt() {
        if(++exp_tick < EXPLORATION_FREQUENCY)
            return;
        if(dispatchedExplorationAnt){
            return;
        }

        dispatchedExplorationAnt = true;

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
            Set<Package> packs = getRoadModel().getObjectsOfType(Package.class);
            Iterator<Package> it = packs.iterator();
            if(it.hasNext()){
                ExplorationAnt ant = new ExplorationAnt(spawnLocation, it.next().getDeliveryLocation(), this, HOPS);
                sim.register(ant);
            }


        }
    }*/

    /**
     * Sends Intention ants to all packages in current plan
     */
    private void spawnIntentionAnts() {
        if(currentPlan.isPresent()){
            Point spawnLocation = TravelDistanceHelper.getNearestNode(this, getRoadModel());
            for(Package p : currentPlan.get().getPackages()){
                IntentionAnt ant = new IntentionAnt(spawnLocation, this, p.getPickupLocation());
            }
        }
    }



    @Override
    public void setSimulator(SimulatorAPI api) {
        this.sim = api;
    }

    public void explorationCallback(IPlan plan) {
        if(plan instanceof EmptyPlan)
            return;

        plans.add((NormalPlan) plan);
    }

    @Override
    public boolean initialize(DMASModel dmasModel) {
        this.dmasModel = dmasModel;
        return true;
    }
}