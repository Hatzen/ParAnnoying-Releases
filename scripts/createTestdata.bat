@ECHO OFF
cd ../app/uploadedDeviceFiles/
START "" /B python testdatareceiver-server.py
REM so github wont start and shutdown unnecessaryly
REM run only edge case test to generate data: https://stackoverflow.com/a/36726368/8524651

cd ../../
gradlew --info app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=de.hartz.software.parannoying.app.large.tests.e2e.SimulatedEdgeCaseE2ETest -Pandroid.testInstrumentationRunnerArguments.uploadTestFiles=true
REM https://superuser.com/a/1615337/1209812
wmic process where "name like '%%python%%' and commandline like '%%testdatareceiver-server.py%%'" delete
echo Test finished successfully
pause