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
    private static final int INTENTION_DELAY = 30;
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

    public Truck(RandomGenerator rng, VehicleDTO dto) {
        super(dto);
        this.rng = rng;

        currentPlan = Optional.empty();
        this.plans = new ArrayList<>();
    }

    private int tick_test = 80;
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
            spawnIntentionAnts();
            if(!currentPlan.isPresent())
                return;

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

                    try{
                        pm.pickup(this, nextPack, time);

                    }catch (IllegalArgumentException e){
                        currentPlan = Optional.empty();
                        return;
                    }
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
                if(currentPlan.getPath().isEmpty())
                    rm.moveTo(this, nextP, timeLapse);
            }
        }
    }

    /**
     * sets the best plan in list of plans if not empty (currently just the first)
     */
    private void selectBestPlan() {
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
            sim.register(ant);
        }
    }

    private void spawnRandomExplorationAnt(){
        Point spawnLocation = TravelDistanceHelper.getNearestNode(this, getRoadModel());

        Set<Package> packages = getRoadModel().getObjectsOfType(Package.class);
        if(packages.size() <= 0)
            return;

        Optional<Package> p = packages.stream().skip(rng.nextInt(packages.size() )).findFirst();

        if(p.isPresent()){
            RandomExplorationAnt ant = new RandomExplorationAnt(spawnLocation, p.get(), 1, this); // Random ants have one hop
            plans.clear();
            sim.register(ant);
        }
    }

    /**
     * Sends Intention ants to all packages in current plan
     */
    private void spawnIntentionAnts() {
        if(++exp_tick < INTENTION_DELAY)
            return;
        exp_tick = 0;

        if(currentPlan.isPresent()){
            Point spawnLocation = TravelDistanceHelper.getNearestNode(this, getRoadModel());


            currentPlan.get().getPackages().stream()
                    .filter(aPackage -> getRoadModel().containsObject(aPackage))
                    .forEach(p -> {
                                IntentionAnt ant = new IntentionAnt(spawnLocation, this, p.getPickupLocation());
                                sim.register(ant);
                            });
        }
    }



    @Override
    public void setSimulator(SimulatorAPI api) {
        this.sim = api;
    }

    public void explorationCallback(IPlan plan) {
        if(plan instanceof EmptyPlan)
            return;

        if(plan.getPath().isEmpty()){
            LOGGER.warn("LEEG PAD");
        }

        plans.add((NormalPlan) plan);
    }

    public void intentionCallback(){
        //plans.clear();
        //currentPlan = Optional.empty();
    }

    @Override
    public boolean initialize(DMASModel dmasModel) {
        this.dmasModel = dmasModel;
        return true;
    }

    public void setStartLocation(Point p) {
        super.setStartPosition(p);
    }
}