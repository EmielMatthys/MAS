package Test;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
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

public class SimpleAgent implements TickListener, MovingRoadUser, CommUser {

    private final RandomGenerator rng;
    private Optional<CollisionGraphRoadModelImpl> roadModel;
    private Optional<Point> destination;
    private Queue<Point> path;
    Optional<CommDevice> device;

    private final double range;

    SimpleAgent(RandomGenerator rng) {
        this.rng = rng;
        roadModel = Optional.absent();
        destination = Optional.absent();
        path = new LinkedList<>();
        this.range = 5;
        device = Optional.absent();
    }

    @Override
    public void initRoadUser(RoadModel model) {
        roadModel = Optional.of((CollisionGraphRoadModelImpl) model);
        Point p;
        do {
            p = model.getRandomPosition(rng);
        } while (roadModel.get().isOccupied(p));
        roadModel.get().addObjectAt(this, p);

    }

    @Override
    public double getSpeed() {
        return 1;
    }

    void nextDestination() {
        path = null;

        do{
            try {
                destination = Optional.of(roadModel.get().getRandomPosition(rng));
                path = new LinkedList<>(roadModel.get().getShortestPathTo(this,
                        destination.get()));
            }
            catch(PathNotFoundException e) {}
        } while(path == null);

    }

    @Override
    public void tick(TimeLapse timeLapse) {
        if (!destination.isPresent()) {
            nextDestination();
        }

        roadModel.get().followPath(this, path, timeLapse);

        if (roadModel.get().getPosition(this).equals(destination.get())) {
            nextDestination();
        }
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}

    @Override
    public Optional<Point> getPosition() {
        if (roadModel.get().containsObject(this)) {
            return Optional.of(roadModel.get().getPosition(this));
        }
        return Optional.absent();
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

