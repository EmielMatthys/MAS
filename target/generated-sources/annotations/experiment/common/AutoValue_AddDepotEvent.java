package experiment.common;

import com.github.rinde.rinsim.geom.Point;

@javax.annotation.Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_AddDepotEvent extends AddDepotEvent {
  private final long getTime;
  private final Point getPosition;

  AutoValue_AddDepotEvent(
      long getTime,
      Point getPosition) {
    this.getTime = getTime;
    if (getPosition == null) {
      throw new NullPointerException("Null getPosition");
    }
    this.getPosition = getPosition;
  }

  @Override
  public long getTime() {
    return getTime;
  }

  @Override
  public Point getPosition() {
    return getPosition;
  }

  @Override
  public String toString() {
    return "AddDepotEvent{"
        + "getTime=" + getTime
        + ", getPosition=" + getPosition
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof AddDepotEvent) {
      AddDepotEvent that = (AddDepotEvent) o;
      return (this.getTime == that.getTime())
          && (this.getPosition.equals(that.getPosition()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (getTime >>> 32) ^ getTime;
    h *= 1000003;
    h ^= getPosition.hashCode();
    return h;
  }
}
