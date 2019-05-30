package experiment.common;

import com.github.rinde.rinsim.core.model.ModelBuilder;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;

@javax.annotation.Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_PDPCollisionGraphRoadModel_Builder extends PDPCollisionGraphRoadModel.Builder {
  private final boolean getAllowVehicleDiversion;
  private final ModelBuilder<CollisionGraphRoadModel, RoadUser> getDelegateModelBuilder;

  AutoValue_PDPCollisionGraphRoadModel_Builder(
      boolean getAllowVehicleDiversion,
      ModelBuilder<CollisionGraphRoadModel, RoadUser> getDelegateModelBuilder) {
    this.getAllowVehicleDiversion = getAllowVehicleDiversion;
    if (getDelegateModelBuilder == null) {
      throw new NullPointerException("Null getDelegateModelBuilder");
    }
    this.getDelegateModelBuilder = getDelegateModelBuilder;
  }

  @Override
  public boolean getAllowVehicleDiversion() {
    return getAllowVehicleDiversion;
  }

  @Override
  public ModelBuilder<CollisionGraphRoadModel, RoadUser> getDelegateModelBuilder() {
    return getDelegateModelBuilder;
  }


  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof PDPCollisionGraphRoadModel.Builder) {
      PDPCollisionGraphRoadModel.Builder that = (PDPCollisionGraphRoadModel.Builder) o;
      return (this.getAllowVehicleDiversion == that.getAllowVehicleDiversion())
          && (this.getDelegateModelBuilder.equals(that.getDelegateModelBuilder()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= getAllowVehicleDiversion ? 1231 : 1237;
    h *= 1000003;
    h ^= getDelegateModelBuilder.hashCode();
    return h;
  }

  private static final long serialVersionUID = 6896293955429749530L;
}
