package delegate.ant;

import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import delegate.agent.Package;
import delegate.ant.pheromone.FeasibilityPheromone;

import java.util.Set;

public class FeasibilityAnt extends Ant implements TickListener {

    Point destination;
    Package sourcePackage;

    public FeasibilityAnt(Point startLocation, Point destination, Package sourcePackage) {
        super(startLocation);
        this.destination = destination;
        this.sourcePackage = sourcePackage;
    }

    @Override
    public void tick(TimeLapse timeLapse) {
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
            if (pDest.equals(myPos)){

                // Drop pheromone pointing to source on destination location
                this.dmasModel.dropPheromone(new FeasibilityPheromone(sourcePackage, myPos));
            }
        }

        if(timeLapse.hasTimeLeft()){
            roadModel.moveTo(this, destination, timeLapse);
        }

        this.LIFETIME--;
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {    }
}
