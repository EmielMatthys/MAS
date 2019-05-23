package delegate;

import com.github.rinde.rinsim.core.model.road.RoadUser;
import delegate.ant.Ant;
import delegate.model.DMASUser;

public interface AntAcceptor extends DMASUser, RoadUser {

    void accept(Ant ant);
}
