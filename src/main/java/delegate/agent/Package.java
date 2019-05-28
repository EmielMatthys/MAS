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
import delegate.AntAcceptor;
import delegate.LocationAgent;
import delegate.ant.Ant;
import delegate.ant.FeasibilityAnt;
import delegate.model.DMASModel;
import delegate.model.DMASUser;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Package extends Parcel implements TickListener, RoadUser, SimulatorUser, AntAcceptor {


    private SimulatorAPI sim;

    private static final int FEAS_ANT_INTERVAL = 50;
    private static final int FEAS_ANT_COUNT = 4;
    private int ant_tick = 0;

    private final LocationAgent locationAgent;
    DMASModel dmasModel;


    public Package(ParcelDTO parcelDto) {
        super(parcelDto);
        locationAgent = new LocationAgent(this);
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        if(getPDPModel().getParcelState(this).isPickedUp()){
            dmasModel.unregister(this);
        }

        if(getPDPModel().getParcelState(this).isDelivered()){
            sim.unregister(locationAgent);
            sim.unregister(this);
            return;
        }

        if (ant_tick == FEAS_ANT_INTERVAL) {
            RoadModel rm = getRoadModel();

            for(int i = 0; i < FEAS_ANT_COUNT;i++){
                FeasibilityAnt ant = new FeasibilityAnt(this, rm.getRandomPosition(sim.getRandomGenerator()));
                sim.register(ant);
            }

            ant_tick = 0;
        }
        ant_tick++;

    }

    @Override
    public void afterTick(TimeLapse timeLapse) {

    }


    @Override
    public void setSimulator(SimulatorAPI api) {
        this.sim = api;
        sim.register(locationAgent);
    }

    @Override
    public void accept(Ant ant) {
        ant.visit(this);
    }

    @Override
    public boolean initialize(DMASModel dmasModel) {
        dmasModel.addAntAcceptor(this);
        this.dmasModel = dmasModel;
        return true;
    }

    public LocationAgent getLocationAgent() {
        return locationAgent;
    }
}
