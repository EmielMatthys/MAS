package experiment.common;

import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;

@javax.annotation.Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_AddVehicleEvent extends AddVehicleEvent {
  private final long getTime;
  private final VehicleDTO getVehicleDTO;

  AutoValue_AddVehicleEvent(
      long getTime,
      VehicleDTO getVehicleDTO) {
    this.getTime = getTime;
    if (getVehicleDTO == null) {
      throw new NullPointerException("Null getVehicleDTO");
    }
    this.getVehicleDTO = getVehicleDTO;
  }

  @Override
  public long getTime() {
    return getTime;
  }

  @Override
  public VehicleDTO getVehicleDTO() {
    return getVehicleDTO;
  }

  @Override
  public String toString() {
    return "AddVehicleEvent{"
        + "getTime=" + getTime
        + ", getVehicleDTO=" + getVehicleDTO
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof AddVehicleEvent) {
      AddVehicleEvent that = (AddVehicleEvent) o;
      return (this.getTime == that.getTime())
          && (this.getVehicleDTO.equals(that.getVehicleDTO()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (getTime >>> 32) ^ getTime;
    h *= 1000003;
    h ^= getVehicleDTO.hashCode();
    return h;
  }
}
