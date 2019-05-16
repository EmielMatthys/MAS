package Test;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.PathNotFoundException;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.LinkedList;
import java.util.Queue;

public class SimpleAgent extends Vehicle implements TickListener, MovingRoadUser, CommUser {

    private static final int VEHICLE_CAPACITY = 1;
    private static final double VEHICLE_SPEED = 10;
    private final RandomGenerator rng;
    private Optional<Point> destination;
    private Queue<Point> path;
    Optional<CommDevice> device;

    private final double range;

    SimpleAgent(RandomGenerator rng, Point initialPosition) {
        super(VehicleDTO.builder()
                .startPosition(initialPosition)
                .speed(VEHICLE_SPEED)
                .capacity(VEHICLE_CAPACITY)
                .build());
        this.rng = rng;
        destination = Optional.absent();
        path = new LinkedList<>();
        this.range = 5;
        device = Optional.absent();
    }

    @Override
    public double getSpeed() {
        return 1;
    }

    void nextDestination() {
        path = null;

        do{
            try {
                destination = Optional.of(getRoadModel().getRandomPosition(rng));
                path = new LinkedList<>(getRoadModel().getShortestPathTo(this,
                        destination.get()));
            }
            catch(PathNotFoundException e) {}
        } while(path == null);

    }

    @Override
    protected void tickImpl(TimeLapse time) {
        if (!destination.isPresent()) {
            nextDestination();
        }

        getRoadModel().followPath(this, path, time);

        if (getRoadModel().getPosition(this).equals(destination.get())) {
            nextDestination();
        }
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}

    @Override
    public Optional<Point> getPosition() {
        return Optional.of(getRoadModel().getPosition(this));
    }

    @Override
    public void setCommDevice(CommDeviceBuilder builder) {
        if (range >= 0) {
            builder.setMaxRange(range);
        }
        device = Optional.of(builder
                .build());
    }


}

