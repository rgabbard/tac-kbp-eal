package com.bbn.kbp.events;

import com.bbn.bue.common.TextGroupImmutable;

import org.immutables.value.Value;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a participant in a document-level entity for purposes of scoring.  This can be either
 * an entity, a value-like filler, or something in the system output which fails to align to
 * anything in the ERE.  We form global identifiers over all these types by concatenating the
 * scoring entity type with its within-type ID.
 */
@TextGroupImmutable
@Value.Immutable
public abstract class ScoringCorefID {

  @Value.Parameter
  public abstract ScoringEntityType scoringEntityType();

  @Value.Parameter
  public abstract String withinTypeID();

  @Value.Derived
  public String globalID() {
    return scoringEntityType().name() + "-" + withinTypeID();
  }

  @Value.Check
  protected void check() {
    checkArgument(!withinTypeID().isEmpty());
  }

  @Override
  public String toString() {
    return globalID();
  }

  public static class Builder extends ImmutableScoringCorefID.Builder {}
}
