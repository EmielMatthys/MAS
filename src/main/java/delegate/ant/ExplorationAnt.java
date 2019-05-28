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
    private Optional<Point> startNode;

    private Plan plan;

    private static final int CLONE_MAX = 5;

    private List<Point> traveledNodes = new LinkedList<>();

    private double estimatedArrival;

    //Constructor voor empty truck edge case:
    public ExplorationAnt(Point spawnLocation, Point randomLocation, Truck truck, int hops){
        super(spawnLocation);
        this.destination = randomLocation;
        this.truck = truck;
        this.hops = hops;
    }

    //Constructor for splitting ant normal case: destination = package source
    private ExplorationAnt(Point spawnLocation, Truck truck, int hops, Queue<Package> toVisit){
        this(spawnLocation, toVisit.peek().getPickupLocation(), truck, hops);

        this.packageQueue = toVisit;
    }

    /**
     *  To be used by Truck when it has package loaded
     * @param spawnLocation spawn location
     * @param loadedPackage package this ant is associated with
     * @param truck truck
     * @param hops hops
     * @param startNode Node from which to start recording the path
     */
    public ExplorationAnt(Point spawnLocation, Package loadedPackage, Truck truck, int hops, Point startNode){
        this(spawnLocation, loadedPackage, truck, hops, new LinkedList<>());
        destination = startNode;
        this.startNode = Optional.of(startNode);
    }

    @Override
    public void tick(TimeLapse timeLapse) {

        if(startNode.isPresent()){
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

        if(startNode.isPresent()) //While moving to startnode, dont pick up pheromones
            return;

        List<FeasibilityPheromone> feasibilityPheromones = getDmasModel().detectPheromone(t, FeasibilityPheromone.class);

        if(t.getPosition().equals(destination)){
            // We are expecting pheromones here

            // if hops == 0 --> stop here and report back to truck, also hops-- afterwards
            if(hops-- <= 0){
                truck.explorationCallback(traveledNodes);
                return;
            }

            // Remove current package from queue head, since we dealt with it
            this.packageQueue.poll();

            if(feasibilityPheromones.isEmpty()) {
                hops = 0;
                LIFETIME = 0;
                return;
            }

            // Found pheromones --> split and destroy this ant
            Set<Package> packages = feasibilityPheromones.stream()
                    .map(FeasibilityPheromone::getSourcePackage)
                    .filter(p -> !packageQueue.contains(p))
                    .collect(Collectors.toSet());

            int i = 0;
            for(Package p : packages){
                if(i++ > CLONE_MAX)
                    break;

                ExplorationAnt ant = new ExplorationAnt(t.getPosition(), truck, hops, new LinkedList<>(packageQueue));
                ant.addToQueue(p);
                sim.register(ant);
            }
            LIFETIME = 0;
            sim.unregister(this);
        }
        else{
            // Found unexpected pheromones --> add them to list and split but dont abort
            if(feasibilityPheromones.isEmpty())
                return;

            Set<Package> packages = feasibilityPheromones.stream()
                    .map(FeasibilityPheromone::getSourcePackage)
                    .collect(Collectors.toSet());

            int i = 0;
            for(Package p : packages){
                if(i++ > CLONE_MAX)
                    break;

                ExplorationAnt ant = new ExplorationAnt(t.getPosition(), truck, hops, new LinkedList<>(packageQueue));
                ant.addToQueue(p);
                sim.register(ant);
            }

            // split ants will investigate new unexpected package after reaching current destination
            // this main ant will ignore it
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

        destination = t.getDeliveryLocation();
    }

    private void addToQueue(Package p){
        packageQueue.add(p);
    }
}