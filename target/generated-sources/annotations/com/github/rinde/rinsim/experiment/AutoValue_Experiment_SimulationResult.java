package com.github.rinde.rinsim.experiment;


@javax.annotation.Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_Experiment_SimulationResult extends Experiment.SimulationResult {
  private final Experiment.SimArgs getSimArgs;
  private final Object getResultObject;

  AutoValue_Experiment_SimulationResult(
      Experiment.SimArgs getSimArgs,
      Object getResultObject) {
    if (getSimArgs == null) {
      throw new NullPointerException("Null getSimArgs");
    }
    this.getSimArgs = getSimArgs;
    if (getResultObject == null) {
      throw new NullPointerException("Null getResultObject");
    }
    this.getResultObject = getResultObject;
  }

  @Override
  public Experiment.SimArgs getSimArgs() {
    return getSimArgs;
  }

  @Override
  public Object getResultObject() {
    return getResultObject;
  }

  @Override
  public String toString() {
    return "SimulationResult{"
        + "getSimArgs=" + getSimArgs
        + ", getResultObject=" + getResultObject
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Experiment.SimulationResult) {
      Experiment.SimulationResult that = (Experiment.SimulationResult) o;
      return (this.getSimArgs.equals(that.getSimArgs()))
          && (this.getResultObject.equals(that.getResultObject()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= getSimArgs.hashCode();
    h *= 1000003;
    h ^= getResultObject.hashCode();
    return h;
  }
}
