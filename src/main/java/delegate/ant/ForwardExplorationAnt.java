package delegate.ant;

import com.github.rinde.rinsim.geom.Point;
import delegate.EmptyPlan;
import delegate.LocationAgent;
import delegate.agent.Package;
import delegate.agent.Truck;
import delegate.ant.pheromone.FeasibilityPheromone;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class ForwardExplorationAnt extends ExplorationAnt {

    final Package forwardPack;

    public ForwardExplorationAnt(Point spawn, Package lastPack, Truck truck, int hops){
        super(spawn, lastPack.getDeliveryLocation(), hops, truck, new EmptyPlan());
        forwardPack = lastPack;
    }
}
