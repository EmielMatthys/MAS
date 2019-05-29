package delegate;

import com.github.rinde.rinsim.core.model.road.MoveProgress;
import com.github.rinde.rinsim.geom.Point;
import delegate.agent.Package;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

public interface IPlan   {
    void recordMovement(MoveProgress progress);
    Collection<Package> getPackages();
    Queue<Point> getPath();

    @Nullable
    Package getTailPackage();
    void addPoint(Point p);
    void addPackage(Package p);
    IPlan clone();
}
