package experiment.common;

import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

@javax.annotation.Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_PDPRoadModel_DestinationObject extends PDPRoadModel.DestinationObject {
  private final PDPRoadModel.DestType type;
  private final Point dest;
  private final RoadUser roadUser;

  AutoValue_PDPRoadModel_DestinationObject(
      PDPRoadModel.DestType type,
      Point dest,
      RoadUser roadUser) {
    if (type == null) {
      throw new NullPointerException("Null type");
    }
    this.type = type;
    if (dest == null) {
      throw new NullPointerException("Null dest");
    }
    this.dest = dest;
    if (roadUser == null) {
      throw new NullPointerException("Null roadUser");
    }
    this.roadUser = roadUser;
  }

  @Override
  PDPRoadModel.DestType type() {
    return type;
  }

  @Override
  Point dest() {
    return dest;
  }

  @Override
  RoadUser roadUser() {
    return roadUser;
  }

  @Override
  public String toString() {
    return "DestinationObject{"
        + "type=" + type
        + ", dest=" + dest
        + ", roadUser=" + roadUser
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof PDPRoadModel.DestinationObject) {
      PDPRoadModel.DestinationObject that = (PDPRoadModel.DestinationObject) o;
      return (this.type.equals(that.type()))
          && (this.dest.equals(that.dest()))
          && (this.roadUser.equals(that.roadUser()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= type.hashCode();
    h *= 1000003;
    h ^= dest.hashCode();
    h *= 1000003;
    h ^= roadUser.hashCode();
    return h;
  }
}
