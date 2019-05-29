package delegate.ant;

import com.github.rinde.rinsim.geom.Point;
import delegate.IPlan;
import delegate.NormalPlan;
import delegate.agent.Package;
import delegate.agent.Truck;

public class RandomExplorationAnt extends ExplorationAnt {


    public RandomExplorationAnt(Point spawn, Package destination, int hops, Truck truck) {
        super(spawn, destination.getDeliveryLocation(), hops, truck, new NormalPlan());
    }
}
