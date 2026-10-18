# Continuous Integration

## Flutter CI

The workflow is defined in [flutter-ci.yml](../.github/workflows/flutter-ci.yml).

It checks the Flutter application in `gobison/`.

## Triggers

The workflow runs on every push and when a pull request is opened, reopened, or updated.

Push checks provide early feedback. Pull request checks help the team review changes before merging.

## Environment

The workflow runs on `ubuntu-latest` and uses Flutter `3.47.7` from the stable channel.

## Checks

1. Install dependencies with `flutter pub get`.
2. Analyze code with `flutter analyze --no-fatal-infos`.
3. Run tests and collect coverage with `flutter test --coverage`.
4. Generate an HTML coverage report and upload the coverage files.

## Analyzer policy

Info-level findings remain visible in the CI logs but do not fail the workflow. Warnings and errors still cause the analysis step to fail.

This allows the team to address existing style suggestions separately while keeping checks for warnings and errors active.

## Viewing results

Open the repository's Actions tab and select a Flutter CI run. Select the `flutter-check` job to view the logs for each step.

For a successful run, download the `flutter-coverage` artifact and extract the ZIP file. Open `html/index.html` in a browser to view the coverage report.

Coverage shows which recorded lines were executed by tests.
It does not prove that the code is correct or that every scenario has been tested.