package sportradar.score.board.service;

import sportradar.score.board.model.Match;

import java.util.List;

public interface ScoreBoardService {
    void startMatch(String homeTeam, String awayTeam);

    List<Match> getScoreBoardSummary();
}
