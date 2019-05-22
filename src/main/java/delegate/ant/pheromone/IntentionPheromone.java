package delegate.ant.pheromone;

import com.github.rinde.rinsim.geom.Point;
import delegate.agent.Truck;

public class IntentionPheromone extends Pheromone {

    private Truck truck;

    public IntentionPheromone(long lifetime, Point location, Truck truck) {
        super(lifetime, location);
        this.truck = truck;
    }

    public Truck getOriginator() {
        return this.truck;
    }
}
