package delegate.util;

import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Connection;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import java.util.List;

public class TravelDistanceHelper {

    public static Measure<Double, Length> calcShortestTravelDistance(RoadModel rm, Point certainPointFrom, RoadUser userEndpoint){

        if(!GraphRoadModel.class.isInstance(rm))
            throw new IllegalArgumentException("RoadModel has to be graphroadmodel");

        Optional<? extends Connection<?>> userConnection = ((GraphRoadModel)rm).getConnection(userEndpoint);

        Point to;
        if( userConnection.isPresent() ){
            to = userConnection.get().to();
        } else {
            to = rm.getPosition(userEndpoint);
        }

        List<Point> path = rm.getShortestPathTo(certainPointFrom, to);
        Measure<Double, Length> distance = rm.getDistanceOfPath(path);
        return distance;
    }

    public static Measure<Double, Length> calcShortestTravelDistance(RoadModel rm, RoadUser userStartpoint, Point certainEndpoint){

        if(!GraphRoadModel.class.isInstance(rm))
            throw new IllegalArgumentException("RoadModel has to be graphroadmodel");

        Optional<? extends Connection<?>> userConnection = ((GraphRoadModel)rm).getConnection(userStartpoint);

        Point from;
        if( userConnection.isPresent() ){
            from = userConnection.get().to();
        } else {
            from = rm.getPosition(userStartpoint);
        }

        List<Point> path = rm.getShortestPathTo(from, certainEndpoint);
        Measure<Double, Length> distance = rm.getDistanceOfPath(path);
        return distance;
    }

    public static Point getNearestNode(RoadUser roadUser, RoadModel rm){
        if(!GraphRoadModel.class.isInstance(rm))
            throw new IllegalArgumentException("RoadModel has to be graphroadmodel");

        Optional<? extends Connection<?>> userConnection = ((GraphRoadModel)rm).getConnection(roadUser);
        Point userPos = rm.getPosition(roadUser);

        if(!userConnection.isPresent()){
            // User already on node
            return userPos;
        }
        else{
            Point from = userConnection.get().from();
            Point to = userConnection.get().to();


            if(Point.distance(userPos, from) < Point.distance(userPos, to))
                return from;
            else
                return to;
        }
    }
}
