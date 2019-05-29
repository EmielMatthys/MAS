package delegate;

import com.github.rinde.rinsim.core.model.road.MoveProgress;
import com.github.rinde.rinsim.geom.Point;
import delegate.agent.Package;

import javax.annotation.Nullable;
import java.util.*;

public class EmptyPlan implements IPlan, Cloneable {
    @Override
    public void recordMovement(MoveProgress progress) {

    }

    @Override
    public Collection<Package> getPackages() {
        return new ArrayList<>();
    }

    @Override
    public Queue<Point> getPath() {
        return new LinkedList<>();
    }


    @Override @Nullable
    public Package getTailPackage() {
        return null;
    }

    /**
     * Cloning an emptyplan results in a normal plan
     * @return new NormalPlan
     */
    @Override
    public NormalPlan clone() {
        return new NormalPlan();
    }

    @Override
    public void addPoint(Point p) {

    }

    @Override
    public void addPackage(Package p) {

    }
}
