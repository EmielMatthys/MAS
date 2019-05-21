package delegate.agent;

import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;

public class Package extends Parcel {
    public Package(ParcelDTO parcelDto) {
        super(parcelDto);
    }
}
