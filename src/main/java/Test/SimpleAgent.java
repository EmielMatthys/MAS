package Test;

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
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.java2d.pipe.SpanShapeRenderer;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class SimpleAgent extends Vehicle implements TickListener, MovingRoadUser, CommUser {

    protected static final Logger LOGGER = LoggerFactory.getLogger(SimpleAgent.class);

    private static final int VEHICLE_CAPACITY = 1;
    private static final double VEHICLE_SPEED = 1000d;
    private final RandomGenerator rng;

    // Roaming stuff
    private Optional<Point> destination;
    private Queue<Point> path;

    //Communication device for messages
    Optional<CommDevice> device;

    //Current parcel
    Optional<Parcel> current ;

    private final double range;

    public SimpleAgent(RandomGenerator rng, Point initialPosition) {
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
        destination = Optional.absent();
        current = Optional.absent();
    }

    public SimpleAgent(RandomGenerator rng, VehicleDTO dto) {
        super(dto);
        this.rng = rng;
        path = new LinkedList<>();
        this.range = 5;
        device = Optional.absent();
        destination = Optional.absent();
        current = Optional.absent();
    }

    public void setLocation(Point loc) {
        super.setStartPosition(loc);
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

        if(!time.hasTimeLeft()){
            return;
        }

        if(!current.isPresent()) {
            // No parcel to be picked up or delivered --> roam

            if (!destination.isPresent()) {
                nextDestination(rm);
            }

            //rm.followPath(this, path, time);
            rm.moveTo(this, destination.get(), time);

            if (rm.getPosition(this).equals(destination.get())) {
                nextDestination(rm);
            }



            // Check for incoming messages
            if(device.get().getUnreadCount() > 0){
                ImmutableList<Message> messages = device.get().getUnreadMessages();

                // Check for incoming assignments
                List<Message> assignements = messages.stream()
                        .filter(message -> ((Package.PackageMessage) message.getContents()).getType()
                                == Package.PackageMessage.MessageType.CONTRACT_ASSIGN)
                        .collect(Collectors.toList());

                // Pick one assignment and cancel all others (so they can broadcast again)
                if(assignements.size() > 0){
                    current = Optional.of((Parcel) assignements.get(0).getSender());
                    LOGGER.debug("Received assignment from " + assignements.get(0).getSender());
                    for(int i = 1; i < assignements.size(); i++){
                        replyCancelContract(assignements.get(i));
                    }
                }
                else {
                    // Send bids to all announcers
                    messages.stream()
                            .filter(message -> ((Package.PackageMessage) message.getContents()).getType()
                                    == Package.PackageMessage.MessageType.CONTRACT_ANNOUNCE)
                            .forEach(message -> replyContractBid(message));
                }
            }
        }

        else{
            final boolean inCargo = pm.containerContains(this, current.get());
            // sanity check: if it is not in our cargo AND it is also not on the
            // RoadModel, we cannot go to curr anymore.
            if (!inCargo && !rm.containsObject(current.get())) {
                current = Optional.absent();
            }
            else if (inCargo) {
                rm.moveTo(this, current.get().getDeliveryLocation(), time);
                if (rm.getPosition(this).equals(current.get().getDeliveryLocation())) {
                    // deliver when we arrive
                    pm.deliver(this, current.get(), time);
                    nextDestination(rm);
                }
            } else {
                // it is still available, go there as fast as possible
                rm.moveTo(this, current.get(), time);
                if (rm.equalPosition(this, current.get())) {
                    // pickup customer
                    pm.pickup(this, current.get(), time);
                }
            }

            if(device.get().getUnreadCount() > 0) {
                ImmutableList<Message> messages = device.get().getUnreadMessages();

                // Check for incoming assignments
                List<Message> assignements = messages.stream()
                        .filter(message -> ((Package.PackageMessage) message.getContents()).getType()
                                == Package.PackageMessage.MessageType.CONTRACT_ASSIGN)
                        .collect(Collectors.toList());

                // Pick one assignment and cancel all others (so they can broadcast again)
                if (assignements.size() > 0) {
                    for (int i = 0; i < assignements.size(); i++) {
                        replyCancelContract(assignements.get(i));
                    }
                }
            }
        }
    }

    /**
     * Sends a cancel contract reply to the sender of the given message.
     * @param message
     */
    private void replyCancelContract(Message message) {
        LOGGER.debug(this + ": sending cancel reply to " + message.getSender());
        device.get().send(new Package.PackageMessage(Package.PackageMessage.MessageType.CONTRACT_CANCEL), message.getSender());
    }

    /**
     * Sends a PackageMessage reply to the sender of the given message.
     * The bidding value is the distance of the shortest path between them.
     * @param message
     */
    private void replyContractBid(Message message) {
        Point thisPosition = getPosition().get();
        Optional<Point> senderPosition = message.getSender().getPosition();

        if(!senderPosition.isPresent()){
            LOGGER.error("senderPosition was absent!");
            return;
        }

        List<Point> pathToParcel = getRoadModel().getShortestPathTo(thisPosition, senderPosition.get());
        double distance = pathToParcel.size();
        device.get().send(new Package.PackageMessage(distance), message.getSender());
    }

    void nextDestination(RoadModel roadModel) {
        destination = Optional.of(roadModel.getRandomPosition(rng));
        path = new LinkedList<>(roadModel.getShortestPathTo(this,
                destination.get()));
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
                .setReliability(1)
                .build());
    }


}

