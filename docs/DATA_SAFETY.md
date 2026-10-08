# Teleprompter Pro — Data Safety Worksheet

For the current binary, the intended Play Console answers are:

**Does the app collect or share any required user data?** No.

**Is data encrypted in transit?** Not applicable when no user data is collected.

**Can users request deletion of their data?** Not applicable to developer-controlled data because the app has no user account or cloud profile.

Current codebase characteristics:
- No network/data-service dependency in the app module.
- No advertising SDK.
- No analytics SDK.
- No login/account system.
- Script processing is local.

Before submission, re-check all Gradle dependencies, manifest permissions and SDKs. If a future release adds analytics, crash reporting, cloud sync, authentication or another data-processing SDK, update both this document and Play Console Data Safety.

Google requires the declaration to accurately cover data handled by the app and third-party SDKs.
