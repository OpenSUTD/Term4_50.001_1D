# 3D Printer Mate - README
3D printing jobs continue to have a high rate of failure. Until technology can minimise such occurrences, the uncertainty of a successful printing job continues to led to inefficient usage of resources by multiple stakeholders. Our team recognises and attempts to eliminate this uncertainty by creating an app that notifies users whenever the 3D printer stops moving, and provides updated images of the print to verify the success of the print.


------
### Android App set-up:
* The application has to be authorised to access the Firebase Database to retrieve values from it. To do so you have to add the SHA1 key of the android app into Firebase.
  * To check the SHA1 key of your android app, use this link: https://stackoverflow.com/questions/15727912/sha-1-fingerprint-of-keystore-certificate
  * Add the SHA1 key into your Firebase through Project Settings > Your apps > Add App.
* Log in with the sample 3D Printer ID: 001
* Can access side bar menu to check webcam for status of print. Note that the Rpi is currently not running, hence we would observe the status to be “Stopped” and the photo obtained from the webcam will not be updated.

### Hardware set-up:
* Open terminal on an RPI
* Navigate to the base folder of the hardware portion: 'javac -cp "libs/" -d "build/" src/'
* Then open another terminal: 'python3 test_serial.py'
* On the terminal where you compiled java: 'cd build/' 'java DatabaseProcessor'

### Expected Behavior:
* After logging in, the user can view the status of the 3D printer, which is constantly updated. The index of the 3D printer will be saved in the phone to allow automatic login for the user, until the user explicitly logs out.
* The picture from the web camera, just like the status of the 3D printer, will be updated once every 15 seconds.
* Whenever the status of the printer changes from “Online” to “Stopped”, the user will receive a notification from the application, regardless of whether the application is opened or not.
