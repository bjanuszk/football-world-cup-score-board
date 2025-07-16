package sportradar.score.board.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import sportradar.score.board.model.Match;

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

    assertEquals("Cannot start match for the same teams", exception.getMessage());
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
    "Germany, Poland",
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
    testObject.startMatch(POLAND, GERMANY);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> testObject.finishMatch(POLAND, "England"));

    assertEquals("Given match does not exist", exception.getMessage());
    assertEquals(1, testObject.getScoreBoardSummary().size());
  }
}
