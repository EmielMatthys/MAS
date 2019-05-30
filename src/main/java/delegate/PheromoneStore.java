package delegate;

import delegate.ant.pheromone.Pheromone;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
        return this.stream()
                .filter(type::isInstance)
                .map(pheromone -> (Y) pheromone)
                .collect(Collectors.toList());

    }
}
