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

    public boolean equals(FeasibilityPheromone ph) {
        if (sourcePackage.equals(ph.getSourcePackage()))
            return true;
        return false;
    }
}
