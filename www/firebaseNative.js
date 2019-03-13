/*global cordova, module*/

module.exports = {
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
    },

    signInWithEmailAndPassword: function(email, password) {
        return new Promise(function(resolve, reject) {
            cordova.exec(resolve, reject, "FirebaseNative", "signInWithEmailAndPassword", [email, password]);
        });
    }
};
