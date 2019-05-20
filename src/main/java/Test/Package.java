package Test;

import com.github.rinde.rinsim.core.model.comm.*;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class Package extends Parcel implements CommUser, TickListener {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Package.class);
    private static final double COMM_RANGE = 5;
    private Optional<CommDevice> device;

    private static final long TIME_WAIT = 2000;
    private PackageState state = PackageState.BROADCAST;

    enum PackageState {
        BROADCAST, LISTENING, ASSIGNED
    }

    public Package(ParcelDTO parcelDto) {
        super(parcelDto);
    }

    @Override
    public Optional<Point> getPosition() {
        return Optional.of(getRoadModel().getPosition(this));
    }

    @Override
    public void setCommDevice(CommDeviceBuilder builder) {
        this.device = Optional.of(builder.
                setMaxRange(COMM_RANGE)
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

            Message best = messages
                    .stream()
                    .sorted(new Comparator<Message>() {
                        @Override
                        public int compare(Message message, Message t1) {
                            PackageMessage contents1 = ((PackageMessage) message.getContents());
                            PackageMessage contents2 = ((PackageMessage) message.getContents());

                            return Double.compare(contents1.getValue(), contents2.getValue());
                        }
                    })
                    .collect(Collectors.toCollection(ArrayList::new))
                    .get(0);

            device.get().send(new PackageMessage(PackageMessage.MessageType.CONTRACT_ASSIGN), best.getSender());
            state = PackageState.ASSIGNED;
        }
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}


    public static class PackageMessage implements MessageContents {
        enum MessageType{
            CONTRACT_ANNOUNCE, CONTRACT_BID, CONTRACT_ASSIGN
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
    }
}
