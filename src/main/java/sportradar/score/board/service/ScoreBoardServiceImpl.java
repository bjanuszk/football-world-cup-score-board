package sportradar.score.board.service;

import sportradar.score.board.model.Match;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreBoardServiceImpl implements ScoreBoardService {

  private static final int INITIAL_SCORE = 0;

  private final Map<String, Match> storage = new HashMap<>();

  @Override
  public void startMatch(String homeTeam, String awayTeam) {

    validateInputs(homeTeam, awayTeam);
    checkForMatchDuplication(homeTeam, awayTeam);

    Match match = new Match(homeTeam, awayTeam, INITIAL_SCORE, INITIAL_SCORE);
    storage.put(createMatchKey(homeTeam, awayTeam), match);
  }

  @Override
  public void finishMatch(String homeTeam, String awayTeam) {

  }

  @Override
  public List<Match> getScoreBoardSummary() {
    return storage.values().stream().toList();
  }

  private static void validateInputs(String homeTeam, String awayTeam) {
    if (homeTeam == null || awayTeam == null) {
      throw new IllegalArgumentException("Team name cannot be null");
    }
    if (homeTeam.equals(awayTeam)) {
      throw new IllegalArgumentException("Cannot start match for the same teams");
    }
  }

  private void checkForMatchDuplication(String homeTeam, String awayTeam) {
    if (storage.containsKey(createMatchKey(homeTeam, awayTeam))
        || storage.containsKey(createMatchKey(awayTeam, homeTeam))) {
      throw new IllegalArgumentException("This match has been already started");
    }
  }

  private static String createMatchKey(String homeTeam, String awayTeam) {
    return String.format("%s%s", homeTeam.toUpperCase(), awayTeam.toUpperCase());
  }
}
