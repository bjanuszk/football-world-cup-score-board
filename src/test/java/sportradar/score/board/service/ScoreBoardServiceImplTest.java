package sportradar.score.board.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sportradar.score.board.model.Match;
import sportradar.score.board.model.TeamScore;
import sportradar.score.board.service.validation.ScoreBoardInputValidator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ScoreBoardServiceImplTest {

  private static final String POLAND = "Poland";
  private static final String GERMANY = "Germany";
  private static final int INITIAL_SCORE = 0;

  @Mock private ScoreBoardInputValidator scoreBoardInputValidator;
  @InjectMocks private ScoreBoardServiceImpl testObject;

  @Test
  void shouldAddMatchToScoreBoardWhenMatchHasStarted() {

    Match createdMatch = testObject.startMatch(POLAND, GERMANY);

    List<Match> existingMatches = testObject.getScoreBoardSummary();

    then(scoreBoardInputValidator).should().validateInputs(POLAND, GERMANY);
    assertEquals(
        new Match(new TeamScore(POLAND, INITIAL_SCORE), new TeamScore(GERMANY, INITIAL_SCORE)),
        createdMatch);
    assertEquals(1, existingMatches.size());
    assertEquals(createdMatch, existingMatches.getFirst());
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

    Match initialMatch = testObject.startMatch(POLAND, GERMANY);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> testObject.startMatch(homeTeamName, awayTeamName));

    assertEquals("This match has been already started", exception.getMessage());

    List<Match> result = testObject.getScoreBoardSummary();
    assertEquals(1, result.size());
    assertEquals(initialMatch, result.getFirst());
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
        assertThrows(IllegalArgumentException.class, () -> testObject.finishMatch(POLAND, GERMANY));

    assertEquals("Given match does not exist", exception.getMessage());
    then(scoreBoardInputValidator).should().validateInputs(POLAND, GERMANY);
    assertTrue(testObject.getScoreBoardSummary().isEmpty());
  }

  @Test
  void shouldNotUpdateMatchScoreIfMatchDoesNotExist() {

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> testObject.updateMatchScore(new TeamScore(POLAND, 1), new TeamScore(GERMANY, 0)));

    assertEquals("Given match does not exist", exception.getMessage());
    then(scoreBoardInputValidator).should().validateInputs(POLAND, GERMANY);
    assertTrue(testObject.getScoreBoardSummary().isEmpty());
  }

  @Test
  void shouldUpdateExistingMatch() {

    testObject.startMatch(POLAND, GERMANY);
    int newPolandScore = 1;

    Match updatedMatch =
        testObject.updateMatchScore(
            new TeamScore(POLAND, newPolandScore), new TeamScore(GERMANY, INITIAL_SCORE));

    List<Match> result = testObject.getScoreBoardSummary();
    assertEquals(
        new Match(new TeamScore(POLAND, newPolandScore), new TeamScore(GERMANY, INITIAL_SCORE)),
        updatedMatch);
    assertEquals(1, result.size());
    assertEquals(updatedMatch, result.getFirst());
  }

  @Test
  void shouldAllowUpdatingTheSameMatchWithTheSameScoreMultipleTimes() {

    testObject.startMatch(POLAND, GERMANY);
    int newPolandScore = 1;

    testObject.updateMatchScore(
        new TeamScore(POLAND, newPolandScore), new TeamScore(GERMANY, INITIAL_SCORE));
    Match updatedMatch =
        testObject.updateMatchScore(
            new TeamScore(POLAND, newPolandScore), new TeamScore(GERMANY, INITIAL_SCORE));

    List<Match> result = testObject.getScoreBoardSummary();
    assertEquals(
        new Match(new TeamScore(POLAND, newPolandScore), new TeamScore(GERMANY, INITIAL_SCORE)),
        updatedMatch);
    assertEquals(1, result.size());
    assertEquals(updatedMatch, result.getFirst());
  }

  @ParameterizedTest
  @CsvSource({"2, 1", "1, 2"})
  void shouldReportErrorWhenTryingToUpdateMatchWithLowerScore(
      int homeTeamScore, int awayTeamScore) {

    testObject.startMatch(POLAND, GERMANY);
    int existingScore = 2;
    testObject.updateMatchScore(
        new TeamScore(POLAND, existingScore), new TeamScore(GERMANY, existingScore));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                testObject.updateMatchScore(
                    new TeamScore(POLAND, homeTeamScore), new TeamScore(GERMANY, awayTeamScore)));

    assertEquals("Cannot update match with lower score", exception.getMessage());
    List<Match> result = testObject.getScoreBoardSummary();
    assertEquals(1, result.size());
    assertEquals(
        new Match(new TeamScore(POLAND, existingScore), new TeamScore(GERMANY, existingScore)),
        result.getFirst());
  }
}
