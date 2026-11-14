# Contributing to knit

Thanks for considering a contribution. knit is maintained by one person in spare time, so replies can take a few days.

## Before you start

- For bug reports, search existing issues first, then open one using the bug report form.
- For new features or API changes, open an issue first and describe the problem and the proposed API. This avoids spending time on a change that does not fit the library's scope.
- For questions about usage, use [GitHub Discussions](https://github.com/manug243/knit/discussions) instead of issues.

## Build and test

Requirements:

- JDK 21
- Android SDK platform 37
- macOS with Xcode 26, only if you want to build or test the iOS targets

Commands:

```shell
./gradlew :knit:desktopTest :knit:testAndroidHostTest :knit:compileKotlinWasmJs :knit:compileKotlinJs
./gradlew :knit:lint :androidApp:lintDebug
./gradlew :androidApp:assembleDebug
./gradlew :example:run
```

The iOS targets can only be tested on macOS:

```shell
./gradlew :knit:iosSimulatorArm64Test
```

On other systems `./gradlew :knit:compileKotlinIosSimulatorArm64` at least compiles the iOS code. The iOS demo is built by the CI with `xcodebuild`.

Behavior of the native iOS bars (Liquid Glass, animations, touches) can only be judged on a simulator or a device. If you change iOS code, describe in the pull request how you checked it.

## Pull requests

- Target the `develop` branch. Never open pull requests against `main`. `main` only receives merges from `develop` and release tags.
- Keep each pull request focused on one change.
- Use [conventional commit](https://www.conventionalcommits.org/) messages, for example `feat: add tab badges` or `fix: keep the tab bar titles after a fade-in`.
- Public API changes must include KDoc and tests. The library uses `explicitApi()`.
- Add or update tests for any behaviour change.
- Add an entry under `Unreleased` in [CHANGELOG.md](CHANGELOG.md) for user-visible changes.
- Make sure the build and test commands above pass before requesting a review.

The pull request template contains a checklist that covers these points.

## Code style

- Follow the existing Kotlin style in the repository.
- Shared behavior lives in `commonMain`. Platform code is limited to the shells that draw the bars (`KnitPlatformShell.*.kt`). Navigation 3 stays the single source of truth for navigation.

## Conduct and security

By participating, you agree to abide by the [Code of Conduct](CODE_OF_CONDUCT.md). Do not report security vulnerabilities in public issues; see [SECURITY.md](SECURITY.md).

## License

By contributing, you agree that your contributions are licensed under the Apache License 2.0, the same license as the project.
