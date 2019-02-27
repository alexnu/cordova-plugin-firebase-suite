/*global cordova, module*/

module.exports = {
    greet: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Hello", "greet", [name]);
    },
    once: function(ref) {
        return new Promise(function(resolve, reject) {
            cordova.exec(resolve, reject, "Hello", "once", [ref]);
        });
    },
    on: function(ref, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Hello", "on", [ref]);
    },
    test: function() {
        return new Promise(function(resolve, reject) {
            cordova.exec(resolve, reject, "Hello", "test", []);
        });
    },
    push: function(ref, value, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Hello", "set", [ref, value]);
    }
};
