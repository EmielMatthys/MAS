package delegate.agent;

import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import delegate.ant.FeasibilityAnt;

import java.util.Optional;

public class Package extends Parcel implements TickListener, RoadUser {


    private boolean first = true;

    public Package(ParcelDTO parcelDto) {
        super(parcelDto);
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        if (first) {
            RoadModel rm = getRoadModel();
            rm.register(new FeasibilityAnt(rm.getPosition(this)));
            first = false;
        }
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {

    }


}
