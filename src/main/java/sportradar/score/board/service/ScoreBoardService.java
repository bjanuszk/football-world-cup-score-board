package sportradar.score.board.service;

import sportradar.score.board.model.Match;
import sportradar.score.board.model.TeamScore;

import java.util.List;

public interface ScoreBoardService {
    void startMatch(String homeTeam, String awayTeam);

    void finishMatch(String homeTeam, String awayTeam);

    List<Match> getScoreBoardSummary();

    void updateMatchScore(TeamScore homeTeamScore, TeamScore awayTeamScore);
}
