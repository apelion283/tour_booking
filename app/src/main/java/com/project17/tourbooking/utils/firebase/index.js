const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.setCustomUserClaims = functions.https.onCall(async (data, context) => {
    const uid = data.uid;
    const role = data.role;

    try {
        await admin.auth().setCustomUserClaims(uid, { role: role });
        return { message: `Custom claim set for user ${uid}` };
    } catch (error) {
        return { error: error.message };
    }
});
