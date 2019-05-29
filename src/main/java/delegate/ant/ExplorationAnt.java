package delegate.ant;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.SimulatorUser;
import com.github.rinde.rinsim.core.model.road.MoveProgress;
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

import java.util.LinkedList;
import java.util.List;

import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class ExplorationAnt extends Ant implements SimulatorUser {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ExplorationAnt.class);

    private int hops;

    private Truck truck;

    private SimulatorAPI sim;

    private Optional<Package> aPackage;
    private Queue<Package> packageQueue = new LinkedList<>();

    private Point destination;
    private Optional<Point> startNode = Optional.absent();

    private Plan plan;

    private static final int CLONE_MAX = 2;

    private List<Point> traveledNodes = new LinkedList<>();

    private double estimatedArrival;

    //Constructor voor empty truck edge case:
    public ExplorationAnt(Point spawnLocation, Point randomLocation, Truck truck, int hops){
        super(spawnLocation, Integer.MAX_VALUE);
        this.destination = randomLocation;
        this.truck = truck;
        this.hops = hops;

        plan = new Plan(truck);
    }

    //Constructor for splitting ant normal case: destination = package source
    private ExplorationAnt(Point spawnLocation, Truck truck, int hops, Queue<Package> toVisit, Plan oldPlan){
        this(spawnLocation, toVisit.peek().getPickupLocation(), truck, hops);

        this.packageQueue = toVisit;
        this.plan = oldPlan.clone();

    }

    /**
     *  To be used by Truck when it has package loaded
     * @param spawnLocation spawn location
     * @param truck truck
     * @param hops hops
     * @param startNode Node from which to start recording the path
     */
    public ExplorationAnt(Point spawnLocation, Truck truck, int hops, Point startNode, Queue<Package> packages){
        this(spawnLocation, startNode, truck, hops);
        packageQueue = new LinkedList<>(packages);
        destination = startNode;
        this.startNode = Optional.of(startNode);
    }

    @Override
    public void tick(TimeLapse timeLapse) {

        if(startNode.isPresent()){ // Go to startnode, ignore destination
            if(roadModel.containsObjectAt(this, startNode.get())){
                startNode = Optional.absent();
            }
            else{
                MoveProgress result = roadModel.moveTo(this, startNode.get(), timeLapse);
                traveledNodes.addAll(result.travelledNodes());
            }
        }
        else {
            if (!roadModel.containsObjectAt(this, destination)) {
                MoveProgress result = roadModel.moveTo(this, destination, timeLapse);
                traveledNodes.addAll(result.travelledNodes());
            }
            else
                markDead();
        }
        plan.addPos(traveledNodes);
        traveledNodes.clear();
    }

    @Override
    public void setSimulator(SimulatorAPI api) {
        this.sim = api;
    }

    @Override
    public void visit(LocationAgent t) {
        if(this.deathMark || LIFETIME == 0)
            return;

        if(startNode.isPresent() && !startNode.get().equals(t.getPosition())){
            //While moving to startnode, dont pick up pheromones
            Point pos = t.getPosition();
            return;
        }

        List<FeasibilityPheromone> feasibilityPheromones = getDmasModel().detectPheromone(t, FeasibilityPheromone.class);

        if(t.getPosition().equals(destination)){
            // We are expecting pheromones here

            // if hops == 0 --> stop here and report back to truck, also hops-- afterwards
            if(hops-- <= 0){
                plan.addPos(traveledNodes);
                plan.addPos(t.getPosition());
                truck.explorationCallback(plan);
                LIFETIME = 0;
                return;
            }

            // Remove current package from queue head, since we dealt with it
            //this.packageQueue.poll();

            if(feasibilityPheromones.isEmpty()) {
                hops = 0;
                LIFETIME = 0;
                return;
            }

            cloneFromPheromones(feasibilityPheromones, t.getPosition());

            LIFETIME = 0;
            sim.unregister(this);
        }
        else{
            return;
            /*
            // Found unexpected pheromones --> add them to list and split but dont abort
            cloneFromPheromones(feasibilityPheromones, t.getPosition());
            if(packageQueue.isEmpty())
                LIFETIME = 0;

            // split ants will investigate new unexpected package after reaching current destination
            // this main ant will ignore it
            */
        }
    }

    private void cloneFromPheromones(List<FeasibilityPheromone> pheromones, Point spawnPos){
        if(pheromones.isEmpty())
            return;

        Set<Package> packages = pheromones.stream()
                .map(FeasibilityPheromone::getSourcePackage)
                .filter(p -> !packageQueue.contains(p))
                .filter(p -> roadModel.containsObject(p))
                .collect(Collectors.toSet());

        int i = 0;
        for(Package p : packages){
            if(i++ > CLONE_MAX)
                break;

            Queue<Package> newQueue = new LinkedList<>(packageQueue);
            newQueue.offer(p);
            ExplorationAnt ant = new ExplorationAnt(spawnPos, truck, hops, newQueue, plan);
            sim.register(ant);
        }
    }

    @Override
    public void visit(Package t) {
        // Detect intention pheromones
        // if found --> stop looking
        // else --> go to destination

        if(packageQueue.isEmpty() || !packageQueue.peek().equals(t)) // Ignore all but our next package
            return;

        List<IntentionPheromone> intentionPheromones = getDmasModel().detectPheromone(t, IntentionPheromone.class);

        if(!intentionPheromones.isEmpty()) {
            markDead();
            return;
        }
        plan.addPack(t);
        destination = t.getDeliveryLocation();
    }

    private void addToQueue(Package p){
        packageQueue.add(p);
    }
}