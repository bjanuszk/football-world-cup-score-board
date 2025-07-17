package sportradar.score.board.service.validation;

public class ScoreBoardInputValidator {
  public void validateInputs(String homeTeamName, String awayTeamName) {
    if (isNullOrEmpty(homeTeamName) || isNullOrEmpty(awayTeamName)) {
      throw new IllegalArgumentException("Team name cannot be empty");
    }
    if (homeTeamName.equalsIgnoreCase(awayTeamName)) {
      throw new IllegalArgumentException("Team names cannot be the same");
    }
  }

  private static boolean isNullOrEmpty(String input) {
    return input == null || input.isBlank();
  }
}
