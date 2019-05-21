package delegate.model;

import com.github.rinde.rinsim.core.model.Model;

/**
 * Allows ants to leave
 */
public class DMASModel extends Model.AbstractModel<DMASUser> {


    @Override
    public boolean register(DMASUser element) {
        return false;
    }

    @Override
    public boolean unregister(DMASUser element) {
        return false;
    }


}
