package com.mafia.databaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@DiscriminatorValue("VOTE")
public class VoteAction extends GameAction {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_id", nullable = false)
  private GamePlayer target;
}
