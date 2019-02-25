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
    test: function() {
        return new Promise(function(resolve, reject) {
            cordova.exec(resolve, reject, "Hello", "test", []);
        });
    }
};
