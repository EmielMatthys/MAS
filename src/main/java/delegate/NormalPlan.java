package delegate;

import com.github.rinde.rinsim.core.model.road.MoveProgress;
import com.github.rinde.rinsim.geom.Point;
import delegate.agent.Package;

import javax.annotation.Nullable;
import java.util.*;

public class NormalPlan implements IPlan {


    private final Queue<Point> path;
    private final List<Package> packages;

    private int index = 0;

    public NormalPlan() {
        packages = new ArrayList<>();
        path = new LinkedList<>();
    }

    @Override
    public void recordMovement(MoveProgress progress) {
        path.addAll(progress.travelledNodes());
    }

    @Override
    public Collection<Package> getPackages() {
        return packages;
    }

    @Override
    public Queue<Point> getPath() {
        return path;
    }

    @Override
    public Package getTailPackage() {
        if(packages.isEmpty())
            return null;
        return packages.get(packages.size() - 1);
    }

    @Override
    public IPlan clone() {
        NormalPlan plan = new NormalPlan();
        plan.packages.addAll(this.packages);
        plan.path.addAll(this.path);
        return plan;
    }

    @Override
    public void addPoint(Point p) {
        path.add(p);
    }

    @Override
    public void addPackage(Package p) {
        packages.add(p);
    }

    public void deliveredPackage(){
        index++;
    }

    @Nullable
    public Package getNextPackage(){
        if(index < packages.size()){
            return packages.get(index);
        }

        return null;
    }

    public static Optional<NormalPlan> getBestPlan(Collection<NormalPlan> plans){
        return plans.stream().sorted(Comparator.comparingInt(p -> p.getPath().size())).findFirst();
    }
}
