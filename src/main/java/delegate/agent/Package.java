package delegate.agent;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.SimulatorUser;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import delegate.ant.FeasibilityAnt;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Package extends Parcel implements TickListener, RoadUser, SimulatorUser {


    private SimulatorAPI sim;
    private boolean first = true;


    public Package(ParcelDTO parcelDto) {
        super(parcelDto);
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        if (first) {
            RoadModel rm = getRoadModel();

            FeasibilityAnt ant1 = new FeasibilityAnt(rm.getPosition(this), rm.getRandomPosition(sim.getRandomGenerator()), this);
            FeasibilityAnt ant2 = new FeasibilityAnt(rm.getPosition(this), rm.getRandomPosition(sim.getRandomGenerator()), this);
            FeasibilityAnt ant3 = new FeasibilityAnt(rm.getPosition(this), rm.getRandomPosition(sim.getRandomGenerator()), this);
            FeasibilityAnt ant4 = new FeasibilityAnt(rm.getPosition(this), rm.getRandomPosition(sim.getRandomGenerator()), this);


            sim.register(ant1);
            sim.register(ant2);
            sim.register(ant3);
            sim.register(ant4);
            first = false;
        }


    }

    @Override
    public void afterTick(TimeLapse timeLapse) {

    }


    @Override
    public void setSimulator(SimulatorAPI api) {
        this.sim = api;
    }
}
