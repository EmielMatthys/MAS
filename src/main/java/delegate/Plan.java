package delegate;

import com.github.rinde.rinsim.geom.Point;
import delegate.agent.Truck;
import delegate.agent.Package;
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

    /**
     * Heuristic for sorting best plans.
     *
     * @param plan
     * @return
     */
    @Override
    public int compareTo(@NotNull Plan plan) {

        Package pack = packages.get(0);
        long orderTime1 = pack.getOrderAnnounceTime();
        long orderTime2 = plan.getPackages().get(0).getOrderAnnounceTime();

        if (orderTime1 > orderTime2)                        return -1;
        else if (orderTime1 == orderTime2)  {
            if (path.size() > plan.getPath().size())        return -1;
            else if (path.size() == plan.getPath().size())  return 0;
            else                                            return 1;
        }
        else                                                return 1;
    }

    public void addToPath(Point p){
        path.add(p);
    }

    public Queue<Point> getPath() {
        return path;
    }

    public List<Package> getPackages() { return packages; }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        path.forEach(x -> sb.append(x).append(" -> "));
        return sb.toString();
    }

    public boolean containsPoint(Point p) {
        for (Point p1 : path) {
            if (p1.equals(p))
                return true;
        }
        return false;
    }

    public void addPackage(Package p) {
        packages.add(p);
    }
}
