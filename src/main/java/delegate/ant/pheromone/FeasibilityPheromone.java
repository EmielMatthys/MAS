package delegate.ant.pheromone;

import com.github.rinde.rinsim.geom.Point;
import delegate.agent.Package;

public class FeasibilityPheromone extends Pheromone {

    final Package sourcePackage;

    public FeasibilityPheromone(Package sourcePackage, Point location) {
        super(location);
        this.sourcePackage = sourcePackage;
    }

    public Package getSourcePackage() {
        return sourcePackage;
    }
}
