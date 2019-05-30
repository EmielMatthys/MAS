package com.github.rinde.rinsim.experiment;

import com.github.rinde.rinsim.core.model.ModelBuilder;
import com.github.rinde.rinsim.scenario.Scenario;
import com.google.common.base.Optional;

@javax.annotation.Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_Experiment_SimArgs extends Experiment.SimArgs {
  private final Scenario getScenario;
  private final MASConfiguration getMasConfig;
  private final long getRandomSeed;
  private final int getRepetition;
  private final boolean isShowGui;
  private final PostProcessor<?> getPostProcessor;
  private final Optional<ModelBuilder<?, ?>> getUiCreator;

  AutoValue_Experiment_SimArgs(
      Scenario getScenario,
      MASConfiguration getMasConfig,
      long getRandomSeed,
      int getRepetition,
      boolean isShowGui,
      PostProcessor<?> getPostProcessor,
      Optional<ModelBuilder<?, ?>> getUiCreator) {
    if (getScenario == null) {
      throw new NullPointerException("Null getScenario");
    }
    this.getScenario = getScenario;
    if (getMasConfig == null) {
      throw new NullPointerException("Null getMasConfig");
    }
    this.getMasConfig = getMasConfig;
    this.getRandomSeed = getRandomSeed;
    this.getRepetition = getRepetition;
    this.isShowGui = isShowGui;
    if (getPostProcessor == null) {
      throw new NullPointerException("Null getPostProcessor");
    }
    this.getPostProcessor = getPostProcessor;
    if (getUiCreator == null) {
      throw new NullPointerException("Null getUiCreator");
    }
    this.getUiCreator = getUiCreator;
  }

  @Override
  public Scenario getScenario() {
    return getScenario;
  }

  @Override
  public MASConfiguration getMasConfig() {
    return getMasConfig;
  }

  @Override
  public long getRandomSeed() {
    return getRandomSeed;
  }

  @Override
  public int getRepetition() {
    return getRepetition;
  }

  @Override
  public boolean isShowGui() {
    return isShowGui;
  }

  @Override
  public PostProcessor<?> getPostProcessor() {
    return getPostProcessor;
  }

  @Override
  public Optional<ModelBuilder<?, ?>> getUiCreator() {
    return getUiCreator;
  }


  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Experiment.SimArgs) {
      Experiment.SimArgs that = (Experiment.SimArgs) o;
      return (this.getScenario.equals(that.getScenario()))
          && (this.getMasConfig.equals(that.getMasConfig()))
          && (this.getRandomSeed == that.getRandomSeed())
          && (this.getRepetition == that.getRepetition())
          && (this.isShowGui == that.isShowGui())
          && (this.getPostProcessor.equals(that.getPostProcessor()))
          && (this.getUiCreator.equals(that.getUiCreator()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= getScenario.hashCode();
    h *= 1000003;
    h ^= getMasConfig.hashCode();
    h *= 1000003;
    h ^= (getRandomSeed >>> 32) ^ getRandomSeed;
    h *= 1000003;
    h ^= getRepetition;
    h *= 1000003;
    h ^= isShowGui ? 1231 : 1237;
    h *= 1000003;
    h ^= getPostProcessor.hashCode();
    h *= 1000003;
    h ^= getUiCreator.hashCode();
    return h;
  }
}
