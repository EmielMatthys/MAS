package delegate;

import delegate.ant.pheromone.Pheromone;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PheromoneStore extends LinkedList<Pheromone> {

    public List<Pheromone> incomingList = new ArrayList<>();

    public void addPerhomone(Pheromone pheromone) {
        incomingList.add(pheromone);
    }

    public void update(){
        // Remove disappeared Pheromones

        this.removeIf(Pheromone::disappeared);

        this.addAll(incomingList);
        incomingList.clear();
    }

    public <Y extends Pheromone> List<Y>  detectPheromone(Class<Y> type) {
        ArrayList<Y> result = new ArrayList<>();
        for(Pheromone ph : this){
            if(type.isInstance(ph))
                result.add((Y) ph);
        }
        return result;
    }
}
