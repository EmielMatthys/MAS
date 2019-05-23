package delegate;

import com.github.rinde.rinsim.geom.Point;
import delegate.agent.Truck;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Plan implements Comparable<Plan> {


    // Packages in this plan
    private List<Package> packages;

    private Truck truck;

    private Heuristic heuristic;

    private Queue<Point> path;

    public Plan(Truck truck) {
        this.truck = truck;
        this.packages = new ArrayList<>();
        this.path = new LinkedList<>();
    }

    @Override
    public int compareTo(@NotNull Plan plan) {
        return 0;
    }

    public void addToPath(Point p){
        path.add(p);
    }

    public Queue<Point> getPath() {
        return path;
    }
}
