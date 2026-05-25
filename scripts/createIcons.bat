@ECHO OFF
cd ../app/uploadedDeviceFiles/
START "" /B python testdatareceiver-server.py
REM so github wont start and shutdown unnecessaryly
REM run only edge case test to generate data: https://stackoverflow.com/a/36726368/8524651

REM echo "If cant connect see emulators wifi settings enabled https://stackoverflow.com/a/1722427/8524651"
REM pause

cd ../../
gradlew --info app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=de.hartz.software.parannoying.app.GenerateIconsTest -Pandroid.testInstrumentationRunnerArguments.uploadTestFiles=true
REM https://superuser.com/a/1615337/1209812
wmic process where "name like '%%python%%' and commandline like '%%testdatareceiver-server.py%%'" delete
echo Test finished successfully
pause