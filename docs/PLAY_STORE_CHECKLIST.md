# Teleprompter Pro — Play Store Checklist

## Build
- Application ID: com.navneet.teleprompter
- Version: 1.0.4
- Version code: 4
- Target SDK: 36
- Minimum SDK: 26
- Production upload: signed Android App Bundle (.aab)
- Device test: signed release APK

## Before submission
- [ ] Play developer account verified
- [ ] Create Play Console app with package com.navneet.teleprompter
- [ ] Play App Signing configured
- [ ] Upload keystore generated and securely backed up
- [ ] GitHub Actions signing secrets configured
- [ ] Signed release AAB builds successfully
- [ ] Signed release APK tested on physical device
- [ ] Play Internal testing upload succeeds
- [ ] Privacy Policy published on HTTPS URL
- [ ] Privacy Policy linked in Play Console and app
- [ ] Data Safety completed
- [ ] Ads declaration completed
- [ ] Target audience/content declarations completed
- [ ] Content rating completed
- [ ] Special-use foreground-service declaration completed
- [ ] FGS demonstration video prepared
- [ ] Store icon and screenshots prepared
- [ ] Store listing completed
- [ ] Reviewer instructions completed
- [ ] Release notes completed

## Security
Never commit JKS/keystore files, private keys, passwords, GitHub tokens, or Play service-account credentials.

## Release gate
Do not merge this branch into main until the signed release APK is tested and the signed AAB passes Play Console validation.
