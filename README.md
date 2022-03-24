# CS347 Fault Tolerant Systems

## JIDSAM: Java Implementations of Data Stream Anonymisation Methods

Uses gradle version 7.3.3 and java openjdk 11.0.14

### Execution

In the project's root run the command: `gradle run`

| Version  | Execution command               |
| -------- | ------------------------------- |
| Castle   | `gradle castle` or `gradle run` |
| Castle-L | `gradle castlel`                |
| B-Castle | `gradle bcastle`                |
| FADS     | `gradle fads`                   |
| FADS-L   | `gradle fadsl`                  |
| XBAND    | `gradle xband`                  |

Command Line Arguments are also supported if the source code is compiled

    -c | Castle Variant: 
       | 1: Castle l
       | 2: B-Castle
       | 3: FADS
       | 4: FADSL
       | 5: XBAND
       | 6: CASTLE
    -k | k-anonymity value
    -d | delta tuple expiration value
    -b | beta cluster persistance value
    -o | XBAND Omega
    -g | XBAND Gamma

### Output

Development/Internal output occurs to the terminal where you can see the cluster generalisations and the tuples included.

External output can be seen in [output.txt](./app/output.txt).

### Compare Algorithms

To generate graphs comparing the algorithms run `gradle compare`

### Automated Tests

Each of the defined classes have automated testing which can be executed using: `gradle test`
