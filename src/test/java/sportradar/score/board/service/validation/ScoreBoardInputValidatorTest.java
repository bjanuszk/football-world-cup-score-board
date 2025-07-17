package sportradar.score.board.service.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ScoreBoardInputValidatorTest {

  private final ScoreBoardInputValidator testObject = new ScoreBoardInputValidator();

  @ParameterizedTest
  @CsvSource(
      value = {
        "null, Germany",
        "'', Germany",
        "Poland, null",
        "Poland, ''",
        "'', ''",
        "null, null",
      },
      nullValues = {"null"})
  void shouldReportErrorWhenTeamNameIsNullOrEmpty(String homeTeamName, String awayTeamName) {

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> testObject.validateInputs(homeTeamName, awayTeamName));

    assertEquals("Team name cannot be empty", exception.getMessage());
  }

  @ParameterizedTest
  @CsvSource({"Poland, Poland", "Poland, POLAND", "Poland, poland"})
  void shouldReportErrorWhenTeamNamesAreTheSame(String homeTeamName, String awayTeamName) {

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> testObject.validateInputs(homeTeamName, awayTeamName));

    assertEquals("Team names cannot be the same", exception.getMessage());
  }

  @ParameterizedTest
  @CsvSource({"Poland, Germany", "Poland, England"})
  void shouldPassValidationForCorrectData(String homeTeamName, String awayTeamName) {
    testObject.validateInputs(homeTeamName, awayTeamName);
  }
}
