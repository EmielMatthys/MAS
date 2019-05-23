package Test;

import com.github.rinde.rinsim.core.model.comm.*;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.Option;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Timer;
import java.util.stream.Collectors;

public class Package extends Parcel implements CommUser, TickListener, RoadUser {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Package.class);
    private static final double COMM_RANGE = 5;
    private Optional<CommDevice> device = Optional.absent();

    private static final long TIME_WAIT = 2000;
    private PackageState state = PackageState.BROADCAST;
    private Optional<CommUser> assigned_truck = Optional.absent();

    private static final int TIMEOUT = 200;
    private int timeOutTick = 0;



    enum PackageState {
        BROADCAST, LISTENING, ASSIGNED
    }

    public Package(ParcelDTO parcelDto) {
        super(parcelDto);
    }

    @Override
    public Optional<Point> getPosition() {
        RoadModel rm = getRoadModel();
        if(rm.containsObject(this)){
            return Optional.of(rm.getPosition(this));
        }
        else {
            return Optional.absent();
        }
    }

    public void setLocation(Point loc) {
        super.setStartPosition(loc);
    }

    @Override
    public void setCommDevice(CommDeviceBuilder builder) {
        this.device = Optional.of(builder.
                setMaxRange(COMM_RANGE)
                .setReliability(1)
                .build());
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        RoadModel rm = getRoadModel();
        PDPModel pm = getPDPModel();

        if(state == PackageState.BROADCAST){
            device.get().broadcast(new PackageMessage(PackageMessage.MessageType.CONTRACT_ANNOUNCE));
            state = PackageState.LISTENING;
        }
        else if(state == PackageState.LISTENING){
            ImmutableList<Message> messages = device.get().getUnreadMessages();
            if(messages.size() == 0){
                state = PackageState.BROADCAST;
                return;
            }

            ArrayList<Message> biddings = messages
                    .stream()
                    .filter(message -> ((PackageMessage) message.getContents()).getType() == PackageMessage.MessageType.CONTRACT_BID)
                    .sorted((message, t1) -> {
                        PackageMessage contents1 = ((PackageMessage) message.getContents());
                        PackageMessage contents2 = ((PackageMessage) message.getContents());

                        return Double.compare(contents1.getValue(), contents2.getValue());
                    })
                    .collect(Collectors.toCollection(ArrayList::new));

            if(biddings.size() > 0) {
                Message best = biddings.get(0);
                LOGGER.debug("Sending contract assignment from "+this+" to " + best.getSender());
                device.get().send(new PackageMessage(PackageMessage.MessageType.CONTRACT_ASSIGN), best.getSender());
                assigned_truck = Optional.of(best.getSender());
                state = PackageState.ASSIGNED;
                timeOutTick = TIMEOUT;

            }
        }

        else if(state == PackageState.ASSIGNED){
            if(timeOutTick <= 0){
                assigned_truck = Optional.absent();
                state = PackageState.BROADCAST;
            }
            timeOutTick--;

            ImmutableList<Message> messages = device.get().getUnreadMessages();
            if(messages.size() == 0)
                return;

            if(messages
                    .stream()
                    .anyMatch(message -> ((PackageMessage)message.getContents()).getType() == PackageMessage.MessageType.CONTRACT_CANCEL)) {

                LOGGER.debug("Contract for package " + this + " assignment was canceled! Broadcasting again...");
                assigned_truck = Optional.absent();
                state = PackageState.BROADCAST;
            }

        }
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}


    public static class PackageMessage implements MessageContents {
        enum MessageType{
            CONTRACT_ANNOUNCE, CONTRACT_BID, CONTRACT_ASSIGN, CONTRACT_CANCEL
        }

        private final MessageType type;
        private final double value;

        public PackageMessage(MessageType type){
            this.type = type;
            this.value = 0;
        }

        public PackageMessage(double value){
            this.type = MessageType.CONTRACT_BID;
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        public MessageType getType() {
            return type;
        }
    }
}
