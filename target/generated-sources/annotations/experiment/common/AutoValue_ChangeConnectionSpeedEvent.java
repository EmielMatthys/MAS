package experiment.common;

import com.github.rinde.rinsim.geom.Point;

@javax.annotation.Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_ChangeConnectionSpeedEvent extends ChangeConnectionSpeedEvent {
  private final long getTime;
  private final Point getFrom;
  private final Point getTo;
  private final double getFactor;

  AutoValue_ChangeConnectionSpeedEvent(
      long getTime,
      Point getFrom,
      Point getTo,
      double getFactor) {
    this.getTime = getTime;
    if (getFrom == null) {
      throw new NullPointerException("Null getFrom");
    }
    this.getFrom = getFrom;
    if (getTo == null) {
      throw new NullPointerException("Null getTo");
    }
    this.getTo = getTo;
    this.getFactor = getFactor;
  }

  @Override
  public long getTime() {
    return getTime;
  }

  @Override
  public Point getFrom() {
    return getFrom;
  }

  @Override
  public Point getTo() {
    return getTo;
  }

  @Override
  public double getFactor() {
    return getFactor;
  }

  @Override
  public String toString() {
    return "ChangeConnectionSpeedEvent{"
        + "getTime=" + getTime
        + ", getFrom=" + getFrom
        + ", getTo=" + getTo
        + ", getFactor=" + getFactor
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ChangeConnectionSpeedEvent) {
      ChangeConnectionSpeedEvent that = (ChangeConnectionSpeedEvent) o;
      return (this.getTime == that.getTime())
          && (this.getFrom.equals(that.getFrom()))
          && (this.getTo.equals(that.getTo()))
          && (Double.doubleToLongBits(this.getFactor) == Double.doubleToLongBits(that.getFactor()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (getTime >>> 32) ^ getTime;
    h *= 1000003;
    h ^= getFrom.hashCode();
    h *= 1000003;
    h ^= getTo.hashCode();
    h *= 1000003;
    h ^= (Double.doubleToLongBits(getFactor) >>> 32) ^ Double.doubleToLongBits(getFactor);
    return h;
  }
}
