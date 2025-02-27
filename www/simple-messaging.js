/* global cordova, module */

module.exports = {
  fetchPermissions: function (successCallback, errorCallback) {
    console.log('fetchPermissions')

    cordova.exec(successCallback, errorCallback, 'MessagingUtils', 'fetchPermissions')
  },
  fetchDeviceToken: function (successCallback, errorCallback) {
    console.log('fetchDeviceToken')

    cordova.exec(successCallback, errorCallback, 'MessagingUtils', 'fetchDeviceToken')
  },
  configureEndpoint: function (endpointUrl, username, successCallback, errorCallback) {
    var storage = window.localStorage;

    storage.setItem('SimpleMessaging.endpointUrl', endpointUrl)
    storage.setItem('SimpleMessaging.username', username)

    this.fetchPermissions(function(successful) {
      console.log('[0] fetchPermissions: ' + successful)

      if (successful) {
        console.log('[1] fetched permissions.')

        cordova.exec(successCallback, errorCallback, 'MessagingUtils', 'fetchDeviceTokenAndTransmit', [endpointUrl, username])
      } else {
        errorCallback('Unable to fetch permissions. 1.0')
      }
    }, function(errored) {
      errorCallback('Unable to fetch permissions. 2.0')
    })
  },
  fetchUsername: function(responseCallback) {
    var username = storage.getItem('SimpleMessaging.username')

    responseCallback(username)
  },
  fetchEndpointUrl: function(responseCallback) {
    var endpointUrl = storage.getItem('SimpleMessaging.endpointUrl')

    responseCallback(endpointUrl)
  }
}
