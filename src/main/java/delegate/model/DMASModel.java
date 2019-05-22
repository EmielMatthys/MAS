package delegate.model;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.SimulatorUser;
import com.github.rinde.rinsim.core.model.DependencyProvider;
import com.github.rinde.rinsim.core.model.Model.AbstractModel;
import com.github.rinde.rinsim.core.model.ModelBuilder.AbstractModelBuilder;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.auto.value.AutoValue;
import delegate.ant.Ant;
import delegate.ant.pheromone.Pheromone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.*;

/**
 *
 */
public class DMASModel extends AbstractModel<DMASUser> implements TickListener, SimulatorUser {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DMASModel.class);

    RoadModel rm;
    List<Pheromone> pheromones;
    List<Ant> ants;

    SimulatorAPI simulator;

    public DMASModel(RoadModel rm) {
        this.pheromones = new ArrayList<>();
        this.ants = new ArrayList<>();
        this.rm = rm;
    }

    public void dropPheromone(Pheromone pheromone){
        LOGGER.debug("Dropped pheromone " + pheromone.getClass() + " at " + pheromone.getLocation());
        if(!pheromones.contains(pheromone)){
            pheromones.add(pheromone);
        }
    }

    public <Y extends Pheromone> List<Y> detectPheromone(Point location, Class<Y> type){
        ArrayList<Y> result = new ArrayList<>();

        for(Pheromone ph : pheromones){
            if(type.isInstance(ph) && ph.getLocation().equals(location))
                result.add((Y) ph);
        }
        return result;
    }

    @Override
    public boolean register(DMASUser element) {
        if(element instanceof Ant)
            ants.add((Ant) element);

        return element.initialize(this);
    }

    @Override
    public boolean unregister(DMASUser element) {
        if(element instanceof Ant)
            ants.remove(element);
        else if(element instanceof Pheromone)
            pheromones.remove(element);
        return true;
    }


    @Override
    public void tick(TimeLapse timeLapse) {

    }

    @Override
    public void afterTick(TimeLapse timeLapse) {
        updatePheromones();
        updateAnts();
    }

    private void updateAnts() {
        for(Ant ant : ants){
            if (ant.died())
                simulator.unregister(ant);

        }
    }

    private void updatePheromones() {
        for(Pheromone ph : pheromones){
            if (ph.disappeared())
                simulator.unregister(ph);

        }
    }

    public static Builder builder(){
        return new AutoValue_DMASModel_Builder();
    }

    @Override
    public void setSimulator(SimulatorAPI api) {
        simulator = api;
    }

    @AutoValue
    public static class Builder extends AbstractModelBuilder<DMASModel, DMASUser>{

        public Builder(){
            setDependencies(RoadModel.class);
        }

        @Override
        public DMASModel build(DependencyProvider dependencyProvider) {
            RoadModel rm = dependencyProvider.get(RoadModel.class);
            return new DMASModel(rm);
        }
    }
}
