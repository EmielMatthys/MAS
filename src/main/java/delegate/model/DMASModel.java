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
import delegate.AntAcceptor;
import delegate.LocationAgent;
import delegate.PheromoneStore;
import delegate.ant.Ant;
import delegate.ant.pheromone.FeasibilityPheromone;
import delegate.ant.pheromone.Pheromone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.*;

/**
 *
 */
public class DMASModel extends AbstractModel<DMASUser> implements TickListener, SimulatorUser {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DMASModel.class);
    private static final double DETECTION_DISTANCE = 0.2;

    RoadModel rm;
    //List<Pheromone> pheromones;
    List<Ant> ants;

    Map<AntAcceptor, PheromoneStore> pheromoneMap;

    SimulatorAPI simulator;

    public DMASModel(RoadModel rm) {
        this.pheromoneMap = new HashMap<>();
        this.ants = new ArrayList<>();
        this.rm = rm;
    }

    public <Y extends Pheromone> List<Y>  detectPheromone(AntAcceptor t, Class<Y> type) {
        ArrayList<Y> result = new ArrayList<>();

        if(pheromoneMap.get(t) != null)
            result.addAll(pheromoneMap.get(t).detectPheromone(type));

        return result;
    }

    public <Y extends Ant> List<Y> getAntsOfType(Class<Y> type){
        ArrayList<Y> result = new ArrayList<>();
        for(Ant a : ants){
            if(type.isInstance(a)){
                result.add((Y) a);
            }
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
        else if(element instanceof AntAcceptor)
            pheromoneMap.remove(element);

        return true;
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        List<Ant> ants_c = new ArrayList<>(ants);

        for(Ant ant : ants_c){
            for (AntAcceptor acceptor: pheromoneMap.keySet()
                 ) {
                if(withinSmellingDistance(ant, acceptor)){
                    acceptor.accept(ant);
                }
            }
        }
    }

    private boolean withinSmellingDistance(Ant ant, AntAcceptor acceptor){
        if(rm.containsObject(acceptor))
            return Point.distance(rm.getPosition(ant), rm.getPosition(acceptor)) <= DETECTION_DISTANCE;
        return false;
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
        for(PheromoneStore ps : pheromoneMap.values()){
            ps.update();
        }

    }

    public static Builder builder(){
        return new AutoValue_DMASModel_Builder();
    }

    @Override
    public void setSimulator(SimulatorAPI api) {
        simulator = api;
    }


    public boolean addAntAcceptor(AntAcceptor acceptor) {
        this.pheromoneMap.put(acceptor, new PheromoneStore());
        return true;
    }

    public void dropPheromone(AntAcceptor t, Pheromone pheromone) {
        if(!pheromoneMap.get(t).contains(pheromone)) {
            pheromoneMap.get(t).addPerhomone(pheromone);
            simulator.register(pheromone);
        }
    }

    @Override
    @Nonnull
    public <U> U get(Class<U> type) {
        synchronized (this) {
            return type.cast(this);
        }
    }




    @AutoValue
    public static class Builder extends AbstractModelBuilder<DMASModel, DMASUser>{

        public Builder(){
            setDependencies(RoadModel.class);
            setProvidingTypes(DMASModel.class);
        }

        @Override
        public DMASModel build(DependencyProvider dependencyProvider) {
            RoadModel rm = dependencyProvider.get(RoadModel.class);
            return new DMASModel(rm);
        }
    }
}
