/**
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// https://firebase.google.com/docs/cloud-messaging/auth-server
admin.initializeApp({
  credential: admin.credential.applicationDefault(),
});

const getData = (targetUserNotificationId: string, count: number): TokenMessage => {
  let title: string;
  if (count === 0) {
    throw new Error("There cannot be 0 messages");
  } else if (count === 1) {
    title = "New Message available";
  } else {
    title = count + " new messages available";
  }

  return {
    token: targetUserNotificationId,
    notification: {
      title,
      body: "open the app to receive it!",
    },
    data: {
      // Add additional data if needed..
    },
  };
};

import {onValueCreated} from "firebase-functions/v2/database";
import * as logger from "firebase-functions/logger";
import {TokenMessage} from "firebase-admin/lib/messaging/messaging-api";

const ref = {
  ref: "/users/{id}/messages/{message}",
  // https://console.firebase.google.com/project/parannoying-dev/database/parannoying-dev-default-rtdb/data
  // Datenbankspeicherort: Belgien (europe-west1)
  region: "europe-west1",
};
export const createPushNotificationForNewCreatedMessage = onValueCreated(ref, async (event) => {
  const messageRef = event.data.ref.parent!.ref;
  let onlineUserId: string;
  let messagesForUserCount: number;
  return messageRef.once("value").then((datasnapshot) => {
    const numberOfChildren = datasnapshot.numChildren();
    messagesForUserCount = numberOfChildren;
    onlineUserId = event.params.id;
    const notificationIdPromise = messageRef.parent!.child("notificationId").once("value");
    return notificationIdPromise;
  }).then((notificationIdSnapshot) => {
    const notificationId = notificationIdSnapshot.val();
    if (notificationId) {
      const message = getData(notificationId, messagesForUserCount);
      admin.messaging().send(message);
      logger.debug("Message " + message.notification?.title + " sent to " + notificationId + " (onlineid: " + onlineUserId + ")");
    }
  });
});

export const TestSendNotificationToDeviceA = functions.database.ref("users/{id}/messages/{message}").onCreate((snapshot, context) => {
  console.log("TestSendNotificationToDeviceA: V1 Code is deprecated and should be removed");
  return Promise.resolve();
});
