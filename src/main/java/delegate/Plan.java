package delegate;

import com.github.rinde.rinsim.geom.Point;
import delegate.agent.Truck;
import org.jetbrains.annotations.NotNull;
import delegate.agent.Package;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;


public class Plan implements Comparable<Plan>, Cloneable {


    // Packages in this plan
    private LinkedList<Package> packages;

    private Truck truck;

    private Heuristic heuristic;

    private LinkedList<Point> path;

    public Plan(Truck truck) {
        this.truck = truck;
        this.packages = new LinkedList<>();
        this.path = new LinkedList<>();
    }


    public void addPos(Point p){
        path.add(p);
    }
    
    public void addPos(Collection<Point> ps){
        path.addAll(ps);
    }
    
    public void addPack(Package p){
        if(!packages.contains(p))
            packages.add(p);
    }

    public void addPack(Collection<Package> p){
        packages.addAll(p);
    }

    @Override
    public int compareTo(@NotNull Plan plan) {
        return 0;
    }

    public void addToPath(Point p){
        path.add(p);
    }

    public LinkedList<Point> getPath() {
        return path;
    }

    public Package peekNextPackage(){
        return packages.peek();
    }

    /**
     *
     * @return true if this was last package in plan
     */
    public boolean popNextPackage(){
        packages.poll();
        return packages.isEmpty();
    }


    @Override
    public Plan clone() {
        Plan plan = new Plan(truck);
        plan.addPos(new LinkedList<>(path));
        plan.addPack(new LinkedList<>(packages));
        return plan;
    }

    public Queue<Package> getPackages() {
        return this.packages;
    }
}
