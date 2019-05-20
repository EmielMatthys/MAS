package Test;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.Option;
import java.util.LinkedList;
import java.util.Queue;

public class SimpleAgent extends Vehicle implements TickListener, MovingRoadUser, CommUser {

    protected static final Logger LOGGER = LoggerFactory.getLogger(SimpleAgent.class);

    private static final int VEHICLE_CAPACITY = 1;
    private static final double VEHICLE_SPEED = 10;
    private final RandomGenerator rng;
    //private Optional<Point> destination;
    private Queue<Point> path;
    Optional<CommDevice> device;

    Optional<Parcel> current;

    private final double range;

    SimpleAgent(RandomGenerator rng, Point initialPosition) {
        super(VehicleDTO.builder()
                .startPosition(initialPosition)
                .speed(VEHICLE_SPEED)
                .capacity(VEHICLE_CAPACITY)
                .build());
        this.rng = rng;
        //destination = Optional.absent();
        path = new LinkedList<>();
        this.range = 5;
        device = Optional.absent();
    }

    @Override
    public double getSpeed() {
        return 1;
    }



    @Override
    protected void tickImpl(TimeLapse time) {
        //LOGGER.debug("SimpleAgent tick");
        //TODO: roam the graph when unassigned

        RoadModel rm = getRoadModel();
        PDPModel pm = getPDPModel();

        if(current.isPresent()){
            final boolean inCargo = pm.containerContains(this, current.get());
            // sanity check: if it is not in our cargo AND it is also not on the
            // RoadModel, we cannot go to curr anymore.
            if (!inCargo && !rm.containsObject(current.get())) {
                current = Optional.absent();
            } else if (inCargo) {
                rm.moveTo(this, current.get().getDeliveryLocation(), time);
                if (rm.getPosition(this).equals(current.get().getDeliveryLocation())) {
                    // deliver when we arrive
                    pm.deliver(this, current.get(), time);
                }
            } else {
                // it is still available, go there as fast as possible
                rm.moveTo(this, current.get(), time);
                if (rm.equalPosition(this, current.get())) {
                    // pickup customer
                    pm.pickup(this, current.get(), time);
                }
            }
        }

        if(device.get().getUnreadCount() > 0){

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

