package delegate.ant;

import com.github.rinde.rinsim.core.model.road.DynamicGraphRoadModel;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Connection;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import delegate.LocationAgent;
import delegate.agent.Package;
import delegate.agent.Truck;
import delegate.ant.pheromone.FeasibilityPheromone;
import delegate.util.TravelDistanceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.util.Length;

import javax.measure.Measure;
import java.util.List;
import java.util.Set;

public class FeasibilityAnt extends Ant {

    protected static final Logger LOGGER = LoggerFactory.getLogger(FeasibilityAnt.class);

    Package sourcePackage;
    private final Point destination;

    public FeasibilityAnt(Package sourcePackage, Point destination) {
        super(sourcePackage.getPickupLocation());
        this.sourcePackage = sourcePackage;

        this.destination = destination;
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        RoadModel rm = getRoadModel();
        if(rm.containsObjectAt(this, destination)){
            markDead();
            return;
        }

        rm.moveTo(this, destination, timeLapse);
    }

    @Override
    public void visit(LocationAgent t) {
        LOGGER.debug(this + " visiting LocationAgent "+ t);

        Measure<Double, ?> distance = TravelDistanceHelper.calcShortestTravelDistance(roadModel, startLocation, this);

        double time = distance.getValue() / Truck.VEHICLE_SPEED;

        getDmasModel().dropPheromone(t, new FeasibilityPheromone(sourcePackage, distance.getValue(), time));
    }
}