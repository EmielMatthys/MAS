package delegate.ant;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.SimulatorUser;
import com.github.rinde.rinsim.core.model.road.MoveProgress;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import delegate.IPlan;
import delegate.LocationAgent;
import delegate.Plan;
import delegate.agent.Package;
import delegate.agent.Truck;
import delegate.ant.pheromone.FeasibilityPheromone;
import delegate.ant.pheromone.IntentionPheromone;
import delegate.ant.pheromone.Pheromone;
import org.omg.CORBA.TIMEOUT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class ExplorationAnt extends Ant implements SimulatorUser {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ExplorationAnt.class);

    private int hops;

    private final Truck truck;

    private SimulatorAPI sim;

    private Point destination;

    private final IPlan plan;

    private static final int CLONE_MAX = 6;


    public ExplorationAnt(Point spawn, Point destination, int hops, Truck truck, IPlan plan){
        super(spawn, Integer.MAX_VALUE);
        this.destination = destination;
        this.hops = hops;
        this.truck = truck;
        this.plan = plan;
        //this.SPEED = 10;
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        RoadModel rm = getRoadModel();
        if(!rm.containsObjectAt(this, destination)){
            MoveProgress progress = rm.moveTo(this, destination, timeLapse);
            plan.recordMovement(progress);
        }else{
//            LIFETIME = 0;
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

        // Only react to it if its our destination
        if(!t.getPosition().equals(destination))
            return;

        List<FeasibilityPheromone> feasibilityPheromones = getDmasModel().detectPheromone(t, FeasibilityPheromone.class);

        if(hops-- <= 0){
            plan.addPoint(t.getPosition());
            truck.explorationCallback(plan);
        }
        else {
            boolean clonedAtleasOne = cloneFromPheromones(feasibilityPheromones, t.getPosition());
            if(!clonedAtleasOne){
                hops = 0;
                return;
            }
        }

        LIFETIME = 0;
        sim.unregister(this);
    }

    private boolean cloneFromPheromones(List<FeasibilityPheromone> pheromones, Point spawnPos){
        if(pheromones.isEmpty())
            return false;

        Set<Package> packages = pheromones.stream()
                .map(FeasibilityPheromone::getSourcePackage)
                .filter(p -> !plan.getPackages().contains(p))
                .filter(p -> roadModel.containsObject(p))
                .collect(Collectors.toSet());

        int i = 0;
        for(Package p : packages){
            if(i++ > CLONE_MAX)
                break;

            ExplorationAnt ant = new ExplorationAnt(spawnPos, p.getPickupLocation(), hops, truck, plan.clone());
            sim.register(ant);
        }
        return i > 0;
    }

    @Override
    public void visit(Package t) {
        // Detect intention pheromones
        // if found --> stop looking
        // else --> go to destination

        if(!t.getPickupLocation().equals(destination))
            return;

        List<IntentionPheromone> intentionPheromones = getDmasModel().detectPheromone(t, IntentionPheromone.class);

        boolean unknownIntentionPher = intentionPheromones.stream()
                .anyMatch(pheromone -> !pheromone.getOriginator().equals(truck));

        if(unknownIntentionPher) {
            markDead();
            return;
        }
        plan.addPoint(t.getPickupLocation());
        plan.addPackage(t);
        destination = t.getDeliveryLocation();
    }

    public Truck getTruck() {
        return truck;
    }
}