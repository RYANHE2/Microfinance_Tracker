# Microfinance Loan Tracker (SDG 1, No Poverty)

A ScalaFX desktop app for community lenders: register borrowers, disburse
loans (micro or emergency), record repayments, and view a default-risk
dashboard.

## Requirements

- JDK 21
- sbt 1.9+ (this project pins 1.10.7 via `project/build.properties`)

## Setup and run

```bash
sbt run
```

First run downloads ScalaFX, JavaFX platform binaries, and upickle, and
needs an internet connection once. Data is stored in `./data/*.json`,
created automatically on first save.

## Features

1. **Register borrower**: add a household with income and dependents.
2. **Disburse loan**: issue a Micro loan or Emergency loan against a borrower.
3. **Record repayment**: log a payment against an existing loan.
4. **Risk dashboard**: outstanding balance and risk level per loan.

## Third-party libraries and licenses

See `docs/citations.md` for the full table. Summary:

- ScalaFX, BSD 3-Clause License, https://www.scalafx.org/
- OpenJFX, GPL v2 with Classpath Exception, https://openjfx.io/
- upickle / ujson, MIT License, https://github.com/com-lihaoyi/upickle
- ScalaTest, Apache 2.0 License, https://www.scalatest.org/

No other external code, images, or assets were used in this project.

## AI use summary

See `ai/interaction_log.md` and `ai/declaration.md` for the full log and
signed declaration. AI-assisted code blocks are tagged `// ai-assisted: #N`.

## Project structure

```
src/main/scala/
  model/        domain classes (Person, Loan hierarchies, Repayment)
  persistence/  generic Repository[T] + JSON-backed implementations
  service/      RiskEngine
  ui/           JFXApp3 shell + screens
```
