# Test Coverage Guide

This project uses **JaCoCo** (Java Code Coverage) to measure and report test coverage.

## Quick Start

### Generate Coverage Report for All Services

```bash
# Run tests and generate coverage reports for all services
mvn clean test jacoco:report

# View reports (open in browser)
open services/user-service/target/site/jacoco/index.html
open services/product-service/target/site/jacoco/index.html
open services/order-service/target/site/jacoco/index.html
open services/api-gateway/target/site/jacoco/index.html
```

### Generate Coverage Report for a Specific Service

```bash
# User Service
mvn clean test jacoco:report -pl services/user-service -am

# Product Service
mvn clean test jacoco:report -pl services/product-service -am

# Order Service
mvn clean test jacoco:report -pl services/order-service -am

# API Gateway
mvn clean test jacoco:report -pl services/api-gateway -am
```

## Viewing Coverage Reports

After running `mvn test jacoco:report`, HTML reports are generated in:

```
services/{service-name}/target/site/jacoco/index.html
```

### Report Structure

- **Overview**: Overall coverage statistics
- **Packages**: Coverage by package
- **Classes**: Coverage by class
- **Source Files**: Line-by-line coverage with color coding
  - **Green**: Covered lines
  - **Yellow**: Partially covered (branches)
  - **Red**: Not covered

## Coverage Metrics

JaCoCo tracks several coverage metrics:

- **Line Coverage**: Percentage of lines executed
- **Branch Coverage**: Percentage of branches (if/else, switch) executed
- **Method Coverage**: Percentage of methods executed
- **Class Coverage**: Percentage of classes executed
- **Instruction Coverage**: Percentage of bytecode instructions executed

## Coverage Thresholds

The project is configured with minimum coverage thresholds:

- **Line Coverage**: Minimum 50% per package
- **Coverage Check**: Runs automatically during `mvn verify`

### Running Coverage Check

```bash
# Run tests and check coverage thresholds
mvn clean verify

# If coverage is below threshold, build will fail
```

### Adjusting Coverage Thresholds

Edit `pom.xml` in the root directory:

```xml
<execution>
    <id>jacoco-check</id>
    <goals>
        <goal>check</goal>
    </goals>
    <configuration>
        <rules>
            <rule>
                <element>PACKAGE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.50</minimum>  <!-- 50% minimum -->
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

## Integration with CI/CD

### GitHub Actions Example

```yaml
name: Test Coverage

on: [push, pull_request]

jobs:
  coverage:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 25
        uses: actions/setup-java@v3
        with:
          java-version: '25'
          distribution: 'temurin'
      - name: Run tests with coverage
        run: mvn clean test jacoco:report
      - name: Upload coverage reports
        uses: actions/upload-artifact@v3
        with:
          name: coverage-reports
          path: |
            services/*/target/site/jacoco/
```

## Coverage Best Practices

### 1. Aim for High Coverage

- **Target**: 80%+ line coverage
- **Critical Code**: 90%+ coverage
- **Minimum**: 50% (enforced by build)

### 2. Focus on Business Logic

- Prioritize coverage for:
  - Business logic handlers
  - Service classes
  - Controllers
  - Domain models

### 3. Don't Obsess Over 100%

- Some code is hard to test (e.g., configuration)
- Focus on meaningful coverage
- Test edge cases and error paths

### 4. Review Coverage Reports Regularly

- Check reports after major changes
- Identify untested code paths
- Add tests for critical missing coverage

## Excluding Code from Coverage

Sometimes you need to exclude certain code from coverage:

### Exclude Classes

Add to `pom.xml`:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>**/config/**</exclude>
            <exclude>**/dto/**</exclude>
            <exclude>**/*Application.class</exclude>
        </excludes>
    </configuration>
</plugin>
```

### Exclude with Annotations

Use `@Generated` annotation:

```java
@Generated  // Excluded from coverage
public class ConfigClass {
    // ...
}
```

## Troubleshooting

### "Skipping JaCoCo execution due to missing execution data file"

**Solution**: Run tests first, then generate report:

```bash
mvn clean test
mvn jacoco:report
```

Or run both together:

```bash
mvn clean test jacoco:report
```

### Coverage Report Not Generated

**Check**:
1. Tests ran successfully (`mvn test`)
2. JaCoCo plugin is configured in `pom.xml`
3. Check `target/site/jacoco/` directory exists

### Low Coverage

**Improve coverage by**:
1. Adding unit tests for uncovered methods
2. Adding integration tests for uncovered endpoints
3. Testing error paths and edge cases
4. Reviewing coverage report to identify gaps

## Advanced Usage

### Generate Coverage for Specific Tests

```bash
# Run only unit tests
mvn test -Dtest=*Test jacoco:report

# Run only integration tests
mvn test -Dtest=*IntegrationTest jacoco:report
```

### Aggregate Coverage Reports

For multi-module projects, create an aggregate report:

```xml
<!-- In root pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>report-aggregate</id>
            <phase>verify</phase>
            <goals>
                <goal>report-aggregate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Coverage in IDE

Most IDEs support JaCoCo:

- **IntelliJ IDEA**: Built-in coverage support
- **Eclipse**: Install EclEmma plugin
- **VS Code**: Use Java Test Runner extension

## Resources

- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
- [Coverage Best Practices](https://www.jacoco.org/jacoco/trunk/doc/maven.html)

## Summary

```bash
# Quick commands
mvn clean test jacoco:report          # Generate all reports
mvn verify                            # Run tests + check coverage
open services/*/target/site/jacoco/index.html  # View reports
```

