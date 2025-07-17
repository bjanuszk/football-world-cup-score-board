package sportradar.score.board.service;

import sportradar.score.board.model.Match;
import sportradar.score.board.model.TeamScore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreBoardServiceImpl implements ScoreBoardService {

  private static final int INITIAL_SCORE = 0;

  private final Map<String, Match> storage = new HashMap<>();

  @Override
  public void startMatch(String homeTeam, String awayTeam) {

    validateInputs(homeTeam, awayTeam);
    validateThatMatchDoesNotExist(homeTeam, awayTeam);

    Match match =
        new Match(new TeamScore(homeTeam, INITIAL_SCORE), new TeamScore(awayTeam, INITIAL_SCORE));
    storage.put(createMatchKey(homeTeam, awayTeam), match);
  }

  @Override
  public void finishMatch(String homeTeam, String awayTeam) {
    validateInputs(homeTeam, awayTeam);
    validateThatMatchExists(homeTeam, awayTeam);
    storage.remove(createMatchKey(homeTeam, awayTeam));
  }

  @Override
  public void updateMatchScore(TeamScore homeTeamScore, TeamScore awayTeamScore) {
    validateInputs(homeTeamScore.teamName(), awayTeamScore.teamName());
    validateThatMatchExists(homeTeamScore.teamName(), awayTeamScore.teamName());
    String matchKey = createMatchKey(homeTeamScore.teamName(), awayTeamScore.teamName());
    Match matchToBeUpdated = storage.get(matchKey);
    validateNewScore(matchToBeUpdated, homeTeamScore, awayTeamScore);

    storage.put(matchKey, new Match(homeTeamScore, awayTeamScore));
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
      throw new IllegalArgumentException("Team names cannot be the same");
    }
  }

  private void validateThatMatchDoesNotExist(String homeTeam, String awayTeam) {
    if (storage.containsKey(createMatchKey(homeTeam, awayTeam))
        || storage.containsKey(createMatchKey(awayTeam, homeTeam))) {
      throw new IllegalArgumentException("This match has been already started");
    }
  }

  private static String createMatchKey(String homeTeam, String awayTeam) {
    return String.format("%s%s", homeTeam.toUpperCase(), awayTeam.toUpperCase());
  }

  private void validateThatMatchExists(String homeTeam, String awayTeam) {
    if (!storage.containsKey(createMatchKey(homeTeam, awayTeam))) {
      throw new IllegalArgumentException("Given match does not exist");
    }
  }

  private static void validateNewScore(
      Match matchToBeUpdated, TeamScore homeTeamScore, TeamScore awayTeamScore) {
    if (homeTeamScore.score() < matchToBeUpdated.homeTeamScore().score()
        || awayTeamScore.score() < matchToBeUpdated.awayTeamScore().score()) {
      throw new IllegalArgumentException("Cannot update match with lower score");
    }
  }
}
