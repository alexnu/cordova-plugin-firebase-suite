/*global cordova, module*/

module.exports = {

    database: {
        once: function(path) {
            return new Promise(function(resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseNative", "once", [path]);
            });
        },
        on: function(path, successCallback, errorCallback) {
            cordova.exec(successCallback, errorCallback, "FirebaseNative", "on", [path]);
        },
        off: function(path, successCallback, errorCallback) {
            cordova.exec(successCallback, errorCallback, "FirebaseNative", "off", [path]);
        },
        generateKey: function(path) {
            return new Promise(function(resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseNative", "generateKey", [path]);
            });
        },
        push: function(path, value) {
            return new Promise(function(resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseNative", "push", [path, value]);
            });
        },
        set: function(path, value) {
            return new Promise(function(resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseNative", "set", [path, value]);
            });
        },
        update: function(path, value) {
            return new Promise(function(resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseNative", "update", [path, value]);
            });
        },
        remove: function(path) {
            return new Promise(function(resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseNative", "remove", [path]);
            });
        }
    },

    auth: {
        onAuthStateChanged: function (successCallback, errorCallback) {
            cordova.exec(successCallback, errorCallback, "FirebaseAuth", "addAuthStateListener", []);

            return function () {
                cordova.exec(successCallback, errorCallback, "FirebaseAuth", "removeAuthStateListener", []);
            };
        },
        signInWithEmailAndPassword: function (email, password) {
            return new Promise(function (resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseAuth", "signInWithEmailAndPassword", [email, password]);
            });
        },
        createUserWithEmailAndPassword: function (email, password) {
            return new Promise(function (resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseAuth", "createUserWithEmailAndPassword", [email, password]);
            });
        },
        signInWithGoogle: function () {
            return new Promise(function (resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseGoogleAuth", "signIn", []);
            });
        },
        getTokenId: function () {
            return new Promise(function (resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseAuth", "getTokenId", []);
            });
        },
        signOut: function (successCallback, errorCallback) {
            cordova.exec(successCallback, errorCallback, "FirebaseAuth", "signOut", []);
        }
    }
};
