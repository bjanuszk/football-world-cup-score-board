# Football World Cup Score Board library

A simple Java library for managing football World Cup games.  
It allows basic operations such as:
- **Start a game**
- **Finish a game** 
- **Update score**
- **Get summary of games by total score**

## Build

To build the project, run:

```bash
./gradlew build
```

## Test

To run tests, execute:

```bash
./gradlew test
```

## Notes
TBD

## Assumptions 

#### Input validation
- While validating given team names I skipped cases where team name contains white-space characters like: " Poland ", "Pol and"
#### Start a game
- It's not possible to have 2 matches started at the same time for the same teams ie: 
U17 Poland vs U17 Germany is not playing at the same time when U21 Poland vs U21 Germany
- I consider Poland vs Germany and Germany vs Poland as the same match, which is not allowed at the same time
#### Finish a game
- It is possible to finish only existing match, otherwise library throws exception
#### Update score
- Updating a score of non-existing match is not allowed
- It is allowed to update the same match with the same score multiple times. 
I.e.: more than one scout is updating the match score after goal
- It's not allowed to update a match with lower score (I do not consider VAR checks)