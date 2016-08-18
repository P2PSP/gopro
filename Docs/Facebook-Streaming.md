# ENABLING FACEBOOK STREAMING:
1. Create Facebook Developer account.
2. Create an app under your Developer account. We need the [app ID](#getting-app-ID) associated with this app under your Developer account. [Link](https://developers.facebook.com/docs/android/getting-started) to official docs.
3. In Android Studio, open the `gradle.properties` file. Add the following lines:
  ```
  #Facebook app ID
  facebook_app_id=<your-app-ID-here>
  ```
4. Create a [Test User](https://developers.facebook.com/docs/apps/test-users).
5. Now run the app, and log onto this test user's profile.
6. Upload live stream. This live stream can be viewed when logged into the test user's profile.

## GETTING FACEBOOK APP ID:
### WINDOWS:
1. Download and extract OpenSSL from [here](https://wiki.openssl.org/index.php/Binaries).
2. Navigate to where the Java `keytool` file is present. For me it was: `C:\Program Files\Java\jdk1.8.0_72\bin\keytool`.
3. `Shift` + `Right Click` inside the folder > `Open command window here` (Or, open command prompt and `cd` to the `bin` directory).
4. Run the following in the command prompt:
    ```
    keytool -exportcert -alias androiddebugkey -keystore %HOMEPATH%\.android\debug.keystore | openssl sha1 -binary | openssl base64
    ```
    In the above command, replace the 'openssl' with the location of the `openssl.exe` file that is found in the extracted folder.
5. Paste the key obtained in the command window in the Facebook Developer portal to obtain your app ID. 
