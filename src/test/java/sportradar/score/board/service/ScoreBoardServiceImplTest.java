package sportradar.score.board.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import sportradar.score.board.model.Match;
import sportradar.score.board.model.TeamScore;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreBoardServiceImplTest {

  private static final String POLAND = "Poland";
  private static final String GERMANY = "Germany";
  private static final int INITIAL_SCORE = 0;
  private final ScoreBoardServiceImpl testObject = new ScoreBoardServiceImpl();

  @Test
  void shouldAddMatchToScoreBoardWhenMatchHasStarted() {

    testObject.startMatch(POLAND, GERMANY);

    List<Match> result = testObject.getScoreBoardSummary();

    assertEquals(1, result.size());
    assertEquals(new Match(POLAND, GERMANY, INITIAL_SCORE, INITIAL_SCORE), result.getFirst());
  }

  @Test
  void shouldReportErrorWhenTeamNameIsNullWhileStartingMatch() {

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> testObject.startMatch(null, null));

    assertEquals("Team name cannot be null", exception.getMessage());
    assertTrue(testObject.getScoreBoardSummary().isEmpty());
  }

  @ParameterizedTest
  @CsvSource({
    "Poland, Germany",
    "Germany, Poland",
    "Poland, germany",
    "poland, Germany",
    "poland, GERMANY",
    "POLAND, gerMANY",
  })
  void shouldReportErrorWhileTryingToStartAlreadyExistingMatch(
      String homeTeamName, String awayTeamName) {

    testObject.startMatch(POLAND, GERMANY);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> testObject.startMatch(homeTeamName, awayTeamName));

    assertEquals("This match has been already started", exception.getMessage());

    List<Match> result = testObject.getScoreBoardSummary();
    assertEquals(1, result.size());
    assertEquals(new Match(POLAND, GERMANY, INITIAL_SCORE, INITIAL_SCORE), result.getFirst());
  }

  @Test
  void shouldReportErrorWhenHomeAndAwayTeamNamesAreSame() {

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> testObject.startMatch(POLAND, POLAND));

    assertEquals("Team names cannot be the same", exception.getMessage());
    assertTrue(testObject.getScoreBoardSummary().isEmpty());
  }

  @Test
  void shouldReportErrorWhenTeamNameIsNullWhileFinishingMatch() {

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> testObject.finishMatch(null, null));

    assertEquals("Team name cannot be null", exception.getMessage());
  }

  @ParameterizedTest
  @CsvSource({
    "Poland, Germany",
    "Poland, germany",
    "poland, Germany",
    "poland, GERMANY",
    "POLAND, gerMANY",
  })
  void shouldRemoveExistingMatchFromScoreBoardWhenMatchIsFinished(
      String homeTeamName, String awayTeamName) {
    testObject.startMatch(POLAND, GERMANY);

    testObject.finishMatch(homeTeamName, awayTeamName);

    assertTrue(testObject.getScoreBoardSummary().isEmpty());
  }

  @Test
  void shouldReportErrorWhileTryingToFinishNotExistingMatch() {

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> testObject.finishMatch(POLAND, "England"));

    assertEquals("Given match does not exist", exception.getMessage());
    assertTrue(testObject.getScoreBoardSummary().isEmpty());
  }

  @Test
  void shouldReportErrorWhenTeamNameIsNullWhileUpdatingMatch() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> testObject.updateMatchScore(new TeamScore(null, 1), new TeamScore(null, 2)));

    assertEquals("Team name cannot be null", exception.getMessage());
    assertTrue(testObject.getScoreBoardSummary().isEmpty());
  }

  @Test
  void shouldNotUpdateMatchScoreIfMatchDoesNotExist() {

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> testObject.updateMatchScore(new TeamScore(POLAND, 1), new TeamScore(GERMANY, 0)));

    assertEquals("Given match does not exist", exception.getMessage());
    assertTrue(testObject.getScoreBoardSummary().isEmpty());
  }

  @Test
  void shouldUpdateExistingMatch() {

    testObject.startMatch(POLAND, GERMANY);
    int newPolandScore = 1;

    testObject.updateMatchScore(
        new TeamScore(POLAND, newPolandScore), new TeamScore(GERMANY, INITIAL_SCORE));

    List<Match> result = testObject.getScoreBoardSummary();
    assertEquals(1, result.size());
    assertEquals(new Match(POLAND, GERMANY, newPolandScore, INITIAL_SCORE), result.getFirst());
  }

  @Test
  void shouldAllowUpdatingTheSameMatchWithTheSameScoreMultipleTimes() {

    testObject.startMatch(POLAND, GERMANY);
    int newPolandScore = 1;

    testObject.updateMatchScore(
        new TeamScore(POLAND, newPolandScore), new TeamScore(GERMANY, INITIAL_SCORE));
    testObject.updateMatchScore(
            new TeamScore(POLAND, newPolandScore), new TeamScore(GERMANY, INITIAL_SCORE));

    List<Match> result = testObject.getScoreBoardSummary();
    assertEquals(1, result.size());
    assertEquals(new Match(POLAND, GERMANY, newPolandScore, INITIAL_SCORE), result.getFirst());
  }

  @ParameterizedTest
  @CsvSource({
          "2, 1",
          "1, 2"
  })
  void shouldReportErrorWhenTryingToUpdateMatchWithLowerScore(int homeTeamScore, int awayTeamScore) {

    testObject.startMatch(POLAND, GERMANY);
    int existingScore = 2;
    testObject.updateMatchScore(
            new TeamScore(POLAND, existingScore), new TeamScore(GERMANY, existingScore));

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> testObject.updateMatchScore(new TeamScore(POLAND, homeTeamScore), new TeamScore(GERMANY, awayTeamScore)));

    assertEquals("Cannot update match with lower score", exception.getMessage());
    List<Match> result = testObject.getScoreBoardSummary();
    assertEquals(1, result.size());
    assertEquals(new Match(POLAND, GERMANY, existingScore, existingScore), result.getFirst());
  }
}
