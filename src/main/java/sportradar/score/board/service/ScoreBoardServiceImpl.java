package sportradar.score.board.service;

import sportradar.score.board.model.Match;
import sportradar.score.board.model.TeamScore;
import sportradar.score.board.service.validation.ScoreBoardInputValidator;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparingInt;
import static java.util.Comparator.reverseOrder;

public class ScoreBoardServiceImpl implements ScoreBoardService {

  private static final int INITIAL_SCORE = 0;

  private final Map<String, Match> storage = new HashMap<>();
  private final ScoreBoardInputValidator scoreBoardInputValidator;
  public ScoreBoardServiceImpl(ScoreBoardInputValidator scoreBoardInputValidator) {
    this.scoreBoardInputValidator = scoreBoardInputValidator;
  }

  @Override
  public Match startMatch(String homeTeam, String awayTeam) {
    scoreBoardInputValidator.validateInputs(homeTeam, awayTeam);
    validateThatMatchDoesNotExist(homeTeam, awayTeam);

    Match match =
        new Match(
            new TeamScore(homeTeam, INITIAL_SCORE),
            new TeamScore(awayTeam, INITIAL_SCORE),
            Instant.now());
    storage.put(createMatchKey(homeTeam, awayTeam), match);
    return match;
  }

  @Override
  public void finishMatch(String homeTeam, String awayTeam) {
    scoreBoardInputValidator.validateInputs(homeTeam, awayTeam);
    validateThatMatchExists(homeTeam, awayTeam);
    storage.remove(createMatchKey(homeTeam, awayTeam));
  }

  @Override
  public Match updateMatchScore(TeamScore homeTeamScore, TeamScore awayTeamScore) {
    String homeTeamName = homeTeamScore.teamName();
    String awayTeamName = awayTeamScore.teamName();

    scoreBoardInputValidator.validateInputs(homeTeamName, awayTeamName);
    validateThatMatchExists(homeTeamName, awayTeamName);
    String matchKey = createMatchKey(homeTeamName, awayTeamName);
    Match matchToBeUpdated = storage.get(matchKey);
    validateNewScore(matchToBeUpdated, homeTeamScore, awayTeamScore);

    Match updatedMatch = new Match(homeTeamScore, awayTeamScore, matchToBeUpdated.createdAt());
    storage.put(matchKey, updatedMatch);
    return updatedMatch;
  }

  @Override
  public List<Match> getScoreBoardSummary() {
    return storage.values().stream()
        .sorted(
            comparingInt(Match::getTotalScore)
                .reversed()
                .thenComparing(Match::createdAt, reverseOrder()))
        .toList();
  }

  private static String createMatchKey(String homeTeam, String awayTeam) {
    return String.format("%s-%s", homeTeam.toUpperCase(), awayTeam.toUpperCase());
  }

  private void validateThatMatchDoesNotExist(String homeTeam, String awayTeam) {
    if (storage.containsKey(createMatchKey(homeTeam, awayTeam))
        || storage.containsKey(createMatchKey(awayTeam, homeTeam))) {
      throw new IllegalArgumentException("This match has been already started");
    }
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
