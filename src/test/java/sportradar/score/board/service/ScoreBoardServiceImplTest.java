package sportradar.score.board.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.UUID;
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
    assertEquals(new TeamScore(POLAND, INITIAL_SCORE), createdMatch.homeTeamScore());
    assertEquals(new TeamScore(GERMANY, INITIAL_SCORE), createdMatch.awayTeamScore());
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

    Match initialMatch = testObject.startMatch(POLAND, GERMANY);
    int newPolandScore = 1;

    Match updatedMatch =
        testObject.updateMatchScore(
            new TeamScore(POLAND, newPolandScore), new TeamScore(GERMANY, INITIAL_SCORE));

    List<Match> result = testObject.getScoreBoardSummary();
    assertEquals(
        new Match(
            new TeamScore(POLAND, newPolandScore),
            new TeamScore(GERMANY, INITIAL_SCORE),
            initialMatch.createdAt()),
        updatedMatch);
    assertEquals(1, result.size());
    assertEquals(updatedMatch, result.getFirst());
  }

  @Test
  void shouldAllowUpdatingTheSameMatchWithTheSameScoreMultipleTimes() {

    Match initialMatch = testObject.startMatch(POLAND, GERMANY);
    int newPolandScore = 1;

    testObject.updateMatchScore(
        new TeamScore(POLAND, newPolandScore), new TeamScore(GERMANY, INITIAL_SCORE));
    Match updatedMatch =
        testObject.updateMatchScore(
            new TeamScore(POLAND, newPolandScore), new TeamScore(GERMANY, INITIAL_SCORE));

    List<Match> result = testObject.getScoreBoardSummary();
    assertEquals(
        new Match(
            new TeamScore(POLAND, newPolandScore),
            new TeamScore(GERMANY, INITIAL_SCORE),
            initialMatch.createdAt()),
        updatedMatch);
    assertEquals(1, result.size());
    assertEquals(updatedMatch, result.getFirst());
  }

  @ParameterizedTest
  @CsvSource({"2, 1", "1, 2"})
  void shouldReportErrorWhenTryingToUpdateMatchWithLowerScore(
      int homeTeamScore, int awayTeamScore) {

    Match initialMatch = testObject.startMatch(POLAND, GERMANY);
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
        new Match(
            new TeamScore(POLAND, existingScore),
            new TeamScore(GERMANY, existingScore),
            initialMatch.createdAt()),
        result.getFirst());
  }

  @Test
  void shouldReturnEmptyScoreBoardSummaryIfNoMatchesArePresent() {

    List<Match> scoreBoardSummary = testObject.getScoreBoardSummary();

    assertTrue(scoreBoardSummary.isEmpty());
  }

  @Test
  void shouldReturnScoreBoardSummaryOrderedByTotalScore() {

    Match match_1_2 = createMatchWithScore(1, 2);
    Match match_1_4 = createMatchWithScore(1, 4);
    Match match_3_1 = createMatchWithScore(3, 1);
    Match match_0_0 = createMatchWithScore(0, 0);

    List<Match> scoreBoardSummary = testObject.getScoreBoardSummary();

    assertEquals(4, scoreBoardSummary.size());
    assertEquals(List.of(match_1_4, match_3_1, match_1_2, match_0_0), scoreBoardSummary);
  }

  @Test
  void shouldReturnScoreBoardSummaryOrderedByRecentlyAddedForTheSameTotalScore() {

    Match first = createMatchWithScore(2, 2);
    Match second = createMatchWithScore(1, 3);
    Match third = createMatchWithScore(4, 0);

    List<Match> scoreBoardSummary = testObject.getScoreBoardSummary();

    assertEquals(3, scoreBoardSummary.size());
    assertEquals(List.of(third, second, first), scoreBoardSummary);
  }

  @Test
  void shouldReturnScoreBoardSummaryOrderedByTotalScoreAndThenRecentlyAdded() {

    Match match_1_2 = createMatchWithScore(1, 2);
    Match match_1_4 = createMatchWithScore(1, 4);
    Match match_1_3 = createMatchWithScore(1, 3);
    Match newerMatch_1_3 = createMatchWithScore(1, 3);
    Match newerMatch_1_2 = createMatchWithScore(1, 2);

    List<Match> scoreBoardSummary = testObject.getScoreBoardSummary();

    assertEquals(5, scoreBoardSummary.size());
    assertEquals(
        List.of(match_1_4, newerMatch_1_3, match_1_3, newerMatch_1_2, match_1_2),
        scoreBoardSummary);
  }

  private Match createMatchWithScore(int homeTeamScore, int awayTeamScore) {
    String homeTeamName = UUID.randomUUID().toString();
    String awayTeamName = UUID.randomUUID().toString();
    Match match = testObject.startMatch(homeTeamName, awayTeamName);
    return testObject.updateMatchScore(
        new TeamScore(match.homeTeamScore().teamName(), homeTeamScore),
        new TeamScore(match.awayTeamScore().teamName(), awayTeamScore));
  }
}
