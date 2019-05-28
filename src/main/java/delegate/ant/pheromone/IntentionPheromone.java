package delegate.ant.pheromone;

import com.github.rinde.rinsim.geom.Point;
import delegate.agent.Truck;

public class IntentionPheromone extends Pheromone {

    private Truck truck;

    public IntentionPheromone(long lifetime, Point location, Truck truck) {
        super(lifetime);
        this.truck = truck;
    }

    public Truck getOriginator() {
        return this.truck;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) ||
                (o instanceof IntentionPheromone && truck.equals(((IntentionPheromone) o).truck));
    }
}
