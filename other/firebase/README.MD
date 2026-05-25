# Init firebase project (03.08.2023)
- create new project
- download and add google-service.json
- add realtime db
- set rules for realtime db
- add authenification via email without approval
- Enable firebase functions
- deploy functions
- enable messaging .. https://stackoverflow.com/questions/57767439/an-error-occurred-when-trying-to-authenticate-to-the-fcm-servers

# Init firebase-function development (15.07.2020)

Setting up firebase function connection:
https://firebase.google.com/docs/cli#mac-linux-npm
1) Download and install npm.
2) $ npm install -g firebase-tools
X) Probably you need to setup windows permissions to be able to run firebase command.
https://docs.microsoft.com/de-de/powershell/module/microsoft.powershell.core/about/about_execution_policies?view=powershell-7
$ Get-ExecutionPolicy
$ Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
3) Login 
$ firebase login
=> Redirect to browser login
4) Setup and select the options you want to edit
$ firebase init
5) Change the index.js accordingly
6) Apply temporary work around: https://stackoverflow.com/a/62852321/8524651
6) $ firebase deploy --only functions:sendNotificationForMessage

Firebase will need to upgrade the plan within the next year....
https://stackoverflow.com/questions/62852071/cloud-functions-deployment-requires-the-pay-as-you-go-blaze-billing-plan
