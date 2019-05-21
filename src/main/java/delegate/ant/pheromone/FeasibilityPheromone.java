package delegate.ant.pheromone;

import delegate.agent.Package;

public class FeasibilityPheromone extends Pheromone {

    final Package sourcePackage;

    public FeasibilityPheromone(Package sourcePackage) {
        this.sourcePackage = sourcePackage;
    }

    public Package getSourcePackage() {
        return sourcePackage;
    }
}
