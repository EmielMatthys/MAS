package zitting2.opl;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;

public class Agent extends Vehicle implements MovingRoadUser, CommUser, TickListener {

    static final double MIN_RANGE = .2;
    static final double MAX_RANGE = 1.5;
    private static final double VEHICLE_SPEED = 50;


    java.util.Optional<CommDevice> device;
    Optional<Point> destination;
    private final double range;
    private final RandomGenerator rng;
    private final double reliability;

    private Optional<Parcel> curr;

    public Agent(RandomGenerator rng, Point position){

        super(VehicleDTO.builder()
                .capacity(1)
                .speed(VEHICLE_SPEED)
                .startPosition(position)
                .build());

        this.rng = rng;
        this.range = MIN_RANGE + rng.nextDouble() * (MAX_RANGE - MIN_RANGE);
        this.reliability = rng.nextDouble();

        device = java.util.Optional.empty();
        destination = Optional.absent();
        curr = Optional.absent();
    }

    @Override
    public Optional<Point> getPosition() {
        return Optional.of(getRoadModel().getPosition(this));
    }

    @Override
    public void setCommDevice(CommDeviceBuilder builder) {
        if (range >= 0) {
            builder.setMaxRange(range);
        }
        device = java.util.Optional.of(builder
                .setReliability(reliability)
                .build());
    }

    @Override
    public double getSpeed() {
        return VEHICLE_SPEED;
    }


    @Override
    protected void tickImpl(TimeLapse time) {

        RoadModel rm = getRoadModel();
        PDPModel pm = getPDPModel();

        if (!destination.isPresent()) {
            destination = Optional.of(getRoadModel().getRandomPosition(rng));
        }
        getRoadModel().moveTo(this, destination.get(), time);
        if (getRoadModel().getPosition(this).equals(destination.get())) {
            destination = Optional.absent();
        }

        //Check if current parcel to deliver
        if(curr.isPresent()){
            final boolean inCargo = pm.containerContains(this, curr.get());
            // sanity check: if it is not in our cargo AND it is also not on the
            // RoadModel, we cannot go to curr anymore.
            if (!inCargo && !rm.containsObject(curr.get())) {
                curr = Optional.absent();
            } else if (inCargo) {
                // if it is in cargo, go to its destination
                rm.moveTo(this, curr.get().getDeliveryLocation(), time);
                if (rm.getPosition(this).equals(curr.get().getDeliveryLocation())) {
                    // deliver when we arrive
                    pm.deliver(this, curr.get(), time);
                }
            } else {
                // it is still available, go there as fast as possible
                rm.moveTo(this, curr.get(), time);
                if (rm.equalPosition(this, curr.get())) {
                    // pickup customer
                    pm.pickup(this, curr.get(), time);
                }
            }
        }
        else{
            // No current parcel present --> wait for contract
            // Check if messages present
            if (device.get().getUnreadCount() > 0) {
                final List<Message> messages = device.get().getUnreadMessages();

                // reading all unread messages
                for (final Message message : messages) {

                }
            }
        }
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {

    }


}
