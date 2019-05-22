package delegate.agent;

import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

public class Truck extends Vehicle {
    private static final double VEHICLE_SPEED = 1000;
    private static final int VEHICLE_CAPACITY = 1;


    public Truck(Point startPos) {
        super(VehicleDTO.builder()
                .speed(VEHICLE_SPEED)
                .capacity(VEHICLE_CAPACITY)
                .startPosition(startPos).build());
    }

    @Override
    protected void tickImpl(TimeLapse time) {

    }
}
