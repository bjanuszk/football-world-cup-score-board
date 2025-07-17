package sportradar.score.board.model;

import java.time.Instant;

public record Match(TeamScore homeTeamScore, TeamScore awayTeamScore, Instant createdAt) {
  public int getTotalScore() {
    return homeTeamScore.score() + awayTeamScore.score();
  }
}
