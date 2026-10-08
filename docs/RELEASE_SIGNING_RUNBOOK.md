# Teleprompter Pro — Production Signing Runbook

Google Play uses an upload key for the bundle you submit and a separate app-signing key for the APKs delivered to users.

## Generate upload keystore on Windows

Use Java keytool on your own PC to generate an RSA upload key of at least 2048 bits. Recommended: RSA 4096.

Example:

keytool -genkeypair -v -keystore teleprompter-upload.jks -alias teleprompter-upload -keyalg RSA -keysize 4096 -validity 10000

Export the public certificate:

keytool -exportcert -rfc -keystore teleprompter-upload.jks -alias teleprompter-upload -file teleprompter-upload.pem

## GitHub Actions secrets

Create these repository secrets:
- ANDROID_KEYSTORE_BASE64
- ANDROID_KEYSTORE_PASSWORD
- ANDROID_KEY_ALIAS
- ANDROID_KEY_PASSWORD

Never commit the JKS file or passwords.

## Windows PowerShell Base64

[Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\teleprompter-upload.jks")) | Set-Clipboard

Paste that value into ANDROID_KEYSTORE_BASE64.

## Play App Signing
For a new Play app, use Play App Signing. Google manages the app-signing key and you keep the upload key secure.

## Verification
After building the AAB, verify it with jarsigner -verify -verbose -certs and confirm the package is com.navneet.teleprompter.

## Release sequence
1. Build signed release APK and AAB.
2. Install the signed release APK on a physical device.
3. Test all core functionality.
4. Upload the AAB to Internal testing.
5. Test the Play-installed build.
6. Complete Play App content declarations.
7. Complete Store listing.
8. Complete Data Safety.
9. Complete Special Use FGS declaration and video.
10. Submit through the required testing/production track.
