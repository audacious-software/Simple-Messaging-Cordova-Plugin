/* global cordova, module */

module.exports = {
  fetchPermissions: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'MessagingUtils', 'fetchPermissions')
  }
}
