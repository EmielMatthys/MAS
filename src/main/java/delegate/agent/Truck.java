package delegate.agent;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import delegate.ant.Ant;
import delegate.ant.IntentionAnt;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;

public class Truck extends Vehicle implements TickListener, MovingRoadUser {
    private static final double VEHICLE_SPEED = 0.5d;
    private static final int VEHICLE_CAPACITY = 1;

    private RandomGenerator rng;
    private Simulator sim;

    //Temp to give with Intention Ant
    private Optional<Point> destination;

    private List<Ant> ants = new ArrayList<>();

    boolean first = true;


    public Truck(RandomGenerator rng, Simulator sim, Point startPos) {
        super(VehicleDTO.builder()
                .speed(VEHICLE_SPEED)
                .capacity(VEHICLE_CAPACITY)
                .startPosition(startPos).build());
        this.rng = rng;
        this.sim = sim;
        this.destination = Optional.absent();
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        if (first) {
            rng.nextDouble();
            this.destination = Optional.of(getRoadModel().getRandomPosition(rng));
            IntentionAnt ant = new IntentionAnt(getRoadModel().getPosition(this), this, this.destination.get());
            sim.register(ant);
            ants.add(ant);
            first = false;
        }
        if (!time.hasTimeLeft()) {
            return;
        }

        getRoadModel().moveTo(this, destination.get(), time);


    }

    public void notify(boolean otherPheromone, Ant ant) {
        ants.remove(ant);
        sim.unregister(ant);
    }
}
