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
    push: function(path, value, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "FirebaseNative", "push", [path, value]);
    }
};
