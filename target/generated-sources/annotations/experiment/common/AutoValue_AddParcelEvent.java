package experiment.common;

import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;

@javax.annotation.Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_AddParcelEvent extends AddParcelEvent {
  private final long getTime;
  private final ParcelDTO getParcelDTO;

  AutoValue_AddParcelEvent(
      long getTime,
      ParcelDTO getParcelDTO) {
    this.getTime = getTime;
    if (getParcelDTO == null) {
      throw new NullPointerException("Null getParcelDTO");
    }
    this.getParcelDTO = getParcelDTO;
  }

  @Override
  public long getTime() {
    return getTime;
  }

  @Override
  public ParcelDTO getParcelDTO() {
    return getParcelDTO;
  }

  @Override
  public String toString() {
    return "AddParcelEvent{"
        + "getTime=" + getTime
        + ", getParcelDTO=" + getParcelDTO
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof AddParcelEvent) {
      AddParcelEvent that = (AddParcelEvent) o;
      return (this.getTime == that.getTime())
          && (this.getParcelDTO.equals(that.getParcelDTO()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (getTime >>> 32) ^ getTime;
    h *= 1000003;
    h ^= getParcelDTO.hashCode();
    return h;
  }
}
