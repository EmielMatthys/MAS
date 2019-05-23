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
        LOGGER.warn(this + " visiting LocationAgent "+ t);

        Measure<Double, ?> distance = TravelDistanceHelper.calcShortestTravelDistance(roadModel, startLocation, this);

        double time = distance.getValue() / Truck.VEHICLE_SPEED;

        getDmasModel().dropPheromone(t, new FeasibilityPheromone(sourcePackage, distance.getValue(), time));
    }


}


/*
if(roadModel.containsObjectAt(this, destination)){
            LIFETIME = 0;
            return;
        }

        Set<Package> allPackages = this.roadModel.getObjectsOfType(Package.class);

        // Loop through all packages in roadmodel
        for(Package p : allPackages){

            // Skip own package
            if(p.equals(sourcePackage))
                continue;

            Point pDest = p.getDeliveryLocation();
            Point myPos = roadModel.getPosition(this);

            // Check if on deliverylocation
            DynamicGraphRoadModel dgrm = (DynamicGraphRoadModel) roadModel;
            if (Point.distance(myPos, pDest) < REQUIRED_DISTANCE_MIN){

                // Drop pheromone pointing to source on destination location
                this.dmasModel.dropPheromone(new FeasibilityPheromone(sourcePackage, pDest));
            }
        }

        if(timeLapse.hasTimeLeft()){
            roadModel.moveTo(this, destination, timeLapse);
        }

        this.LIFETIME--;
 */