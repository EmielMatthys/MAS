package zitting2.opl;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;


public class CommParcel extends Parcel implements TickListener, CommUser {

    private Optional<CommDevice> device;

    enum AssignmentState {
        PENDING, ASSIGNED, IDLE;
    }

    AssignmentState state;

    public CommParcel(ParcelDTO parcelDto) {
        super(parcelDto);
        state = AssignmentState.IDLE;
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        PDPModel pm = getPDPModel();

        if(pm.getParcelState(this).isDelivered() || state == AssignmentState.IDLE)
            return;

        // If not yet picked up
        if(!pm.getParcelState(this).isPickedUp()){

            // Contract not yet assigned --> broadcast
            if(state == AssignmentState.PENDING){

                device.get().broadcast(SimpleContractMessage.CONTRACT_ANNOUNCE);
            }
        }
    }

    @Override
    public Optional<Point> getPosition() {
        return Optional.of(getRoadModel().getPosition(this));
    }

    @Override
    public void setCommDevice(CommDeviceBuilder builder) {
        device = Optional.of(builder.build());
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {

    }

    enum SimpleContractMessage implements MessageContents {
        CONTRACT_ANNOUNCE, CONTRACT_ASSIGN;
    }

    public static class ContractBidMessage implements  MessageContents {
        final double biddingValue;

        public ContractBidMessage(double biddingValue) {
            this.biddingValue = biddingValue;
        }

        public double getBiddingValue() {
            return biddingValue;
        }
    }
}
