/*global cordova, module*/

module.exports = {

    database: {
        once: function(path) {
            return new Promise(function(resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseDatabase", "once", [path]);
            });
        },
        on: function(path, successCallback, errorCallback) {
            cordova.exec(successCallback, errorCallback, "FirebaseDatabase", "on", [path]);
        },
        off: function(path, successCallback, errorCallback) {
            cordova.exec(successCallback, errorCallback, "FirebaseDatabase", "off", [path]);
        },
        generateKey: function(path) {
            return new Promise(function(resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseDatabase", "generateKey", [path]);
            });
        },
        push: function(path, value) {
            return new Promise(function(resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseDatabase", "push", [path, value]);
            });
        },
        set: function(path, value) {
            return new Promise(function(resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseDatabase", "set", [path, value]);
            });
        },
        update: function(path, value) {
            return new Promise(function(resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseDatabase", "update", [path, value]);
            });
        },
        remove: function(path) {
            return new Promise(function(resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseDatabase", "remove", [path]);
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
        signInWithFacebook: function () {
            return new Promise(function (resolve, reject) {
                cordova.exec(resolve, reject, "FirebaseFacebookAuth", "signIn", []);
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
