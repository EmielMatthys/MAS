package delegate.ant.pheromone;

import delegate.agent.Package;

public class FeasibilityPheromone extends Pheromone {

    final Package sourcePackage;

    private final double estimatedTravelTime;
    private final double estimatedDistance;

    public FeasibilityPheromone(Package sourcePackage, double estimatedTravelTime, double estimatedDistance) {
        super();
        this.sourcePackage = sourcePackage;
        this.estimatedTravelTime = estimatedTravelTime;
        this.estimatedDistance = estimatedDistance;
    }

    public Package getSourcePackage() {
        return sourcePackage;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
