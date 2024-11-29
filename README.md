[![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
![gradle-version](https://img.shields.io/badge/gradle-8.5.2-blue?logo=gradle)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

[![Website](https://img.shields.io/badge/Author-vivienmahe.com-purple)](https://vivienmahe.com/)
[![X/Twitter](https://img.shields.io/twitter/follow/VivienMahe)](https://twitter.com/VivienMahe)

# Kmplate-lib

Kmplate-lib is a template to easily create a new Kotlin Multiplatform library with Maven Central publishing configuration.

### 1. Setup
After cloning this repo as a template, the first thing you need to do is configure it with your desired project name and package name. This is done by running the `renameProject` Gradle task.

##### Run the `renameProject` Task
The `renameProject` task customizes the library to fit your project by:
1. **Deleting any existing directory** with the target project name (`projectName`), if it exists.
2. **Renaming directories** (e.g., `changehere`) to match the provided `projectName`.
3. **Updating file references** (e.g., `settings.gradle.kts`, `build.gradle.kts`) with the new project name and package name.
4. **Updating `package` and `import` statements** in all relevant `.kt` and configuration files.

##### Usage
Run the task using the following command:
```bash
./gradlew renameProject -PprojectName=MyLibrary -PpackageName=com.example.mylibrary
```

This will:
- Rename directories to use `mylibrary`.
- Update file references to use `com.example.mylibrary`.

---

##### Dry Run Option
Before making changes, you can preview the changes this task will make by using the `-PdryRun` option:

```bash
./gradlew renameProject -PprojectName=MyLibrary -PpackageName=com.example.mylibrary -PdryRun
```

This will log all planned changes without applying them.

##### Task Output
After running the task, you will see:
- A **summary of renamed directories**.
- A **list of updated files**.
- Any skipped or failed operations (e.g., if a file is locked).

---

##### Additional Notes
1. **Mandatory Parameters**:
   - `projectName`: The desired name for your project.
   - `packageName`: The package name to use throughout the library.

2. **Run Only Once**:
   - This task is intended to be run only once when setting up the library for your project.

3. **Dry Run**:
   - Always recommended to run with `-PdryRun` first to ensure the changes align with your expectations.

---

With this task, you can seamlessly configure your library with minimal manual effort.

### 2. Configure the library for Maven Central publishing
Open `buildSrc/src/main/kotlin/ProjectConfiguration.kt` and update all the properties within the `object Maven` block.

### 3. Configure publishing on Maven Central with Sonatype

Publishing the library is done via Github Actions, from the workflow `.github/workflows/publish.yml`, and will automatically publish a new version of the library to Maven Central, for every new  
release created on Github.

- First, you need to create an account on Sonatype. Follow this guide: https://central.sonatype.org/publish/publish-guide/. You should end up with a **username**, a **password** and a **staging  
  profile ID**.
- Once you have your account, you need to request the creation of your groupId (ie. 'com.mycompany.myname'). Create an issue on their Jira. Example: https://issues.sonatype.org/browse/OSSRH-97913.
- Then, create your secret key by following this guide: https://central.sonatype.org/publish/requirements/gpg/. You should end up with a **secret key**, a **secret key ID** and a **secret key password  
  **.

To configure the publishing, we need to create 6 [Github Actions secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets):

1. `OSSRH_GPG_SECRET_KEY`: The value of the secret key created.
2. `OSSRH_GPG_SECRET_KEY_ID`: The ID of the secret key created (the last 16 digits).
3. `OSSRH_GPG_SECRET_KEY_PASSWORD`: The password of the secret key created.
4. `OSSRH_PASSWORD`: Your Sonatype account password.
5. `OSSRH_STAGING_PROFILE_ID`: Your Sonatype staging profile ID.
6. `OSSRH_USERNAME`: Your Sonatype account username.

### 4. Configure Slack notifications for Github build status
You can configure Slack to get notifications about Github build status.

##### 1. Create a webhook post messages on Slack
1. For Github Actions to post messages on Slack, you must create a new webhook URL by using the [Incoming Webhook](https://slack.com/apps/A0F7XDUAZ-incoming-webhooks) app.
2. Create a new [Github Actions secret](https://docs.github.com/en/actions/security-guides/encrypted-secrets) with name `SLACK_WEBHOOK_URL`, and copy paste the webhook created in the previous step as  
   value of this secret.

##### 2. Configure the Slack bot to post on Slack
We will configure 2 Slack bots to post message on Slack: one bot to check for outdated dependencies, and one bot for the build status.  
To configure these 2 Slack bots, we need to create 3 [Github Actions variables](https://docs.github.com/en/actions/learn-github-actions/variables):

1. `SLACK_GITHUB_ACTIONS_CHANNEL_NAME`: the name of the Slack channel where Github Actions will post messages (ie. `myproject_build_status`).
2. `SLACK_GITHUB_ACTIONS_DEPENDENCY_UPDATES_ICON_URL`: the icon URL to be used as a profile picture for the "Dependency Updates" Slack bot.
3. `SLACK_GITHUB_ACTIONS_ICON_URL`: the icon URL to be used as a profile picture for the "Github Actions CI" Slack bot.

