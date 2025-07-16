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

- It's not possible to have 2 matches started at the same time for the same teams ie: 
U17 Poland vs U17 Germany is not playing at the same time when U21 Poland vs U21 Germany
- I consider Poland vs Germany and Germany vs Poland as the same match, which is not allowed at the same time
- It is possible to finish only existing match, otherwise library throws exception