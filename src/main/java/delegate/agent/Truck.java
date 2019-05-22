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

        if (!destination.isPresent()) {
            destination = Optional.of(getRoadModel().getRandomPosition(rng));
        }

        spawnIntentionAnt();

        if (!time.hasTimeLeft()) {
            return;
        }
        //TODO: MOET BEWEGEN VOLGENS PATH VAN EXPLORATIONANT
        getRoadModel().moveTo(this, destination.get(), time);
    }

    private void spawnIntentionAnt() {
        IntentionAnt ant = new IntentionAnt(getRoadModel().getPosition(this), this, this.destination.get());
        try {
            sim.register(ant);
        } catch (IllegalArgumentException e) {

        }
    }

    public void notify(boolean otherPheromone) {
        System.out.println("OTHER PHEROMONE: " + otherPheromone);
    }
}
