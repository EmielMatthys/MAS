package simplepdp;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;


public class AGV extends Vehicle implements CommUser {

    private static final int CAPACITY = 3;
    private static final double SPEED = .3d;

    private Optional<Parcel> current;
    private CommDevice device;

    protected AGV(Point startPoint) {
        super(VehicleDTO.builder()
                .capacity(CAPACITY)
                .startPosition(startPoint)
                .speed(SPEED)
                .build());
        current = Optional.absent();
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        final PDPModel pm = getPDPModel();

        if(current.isPresent()){
            final boolean inCargo = pm.containerContains(this, current.get());

            if(!inCargo && !rm.containsObject(current.get())){
                current = Optional.absent();
            }else if(inCargo) {
                rm.moveTo(this, current.get().getDeliveryLocation(), time);
                if (rm.getPosition(this).equals(current.get().getDeliveryLocation())) {
                    pm.deliver(this, current.get(), time);
                }
            }else{
                rm.moveTo(this, current.get(), time);
                if(rm.equalPosition(this, current.get())){
                    pm.pickup(this, current.get(), time);
                }
            }
        }else{
          current = Optional.of(RoadModels.findClosestObject(rm.getPosition(this), rm, Parcel.class));
        }
    }

    @Override
    public Optional<Point> getPosition() {
        return Optional.of(getRoadModel().getPosition(this));
    }

    @Override
    public void setCommDevice(CommDeviceBuilder builder) {
        this.device = builder.build();
    }
}
