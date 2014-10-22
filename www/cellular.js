/*
 * Custom cellular object to get and store the values of advanced cellular information returned from native code
 * Properties include:
 * 1. CellID latched to
 * 2. LAC of the current CellID
 * 3. PSC On a UMTS network, returns the primary scrambling code of the serving cell. 
 * 4. IMEI
 * 5. Operator Name
 * 6. Neighboring Cell Sites
 */

var exec    = require('cordova/exec'),
    cordova = require('cordova');

var cellular = function() {
    this.cellID = null;
    this.lac = null;
    this.psc = null;
    this.imei = null;
    this.operator = null;
    this.neighbors = {};
    // Create new event handlers on the window (returns a channel instance)
    this.channels = {
        watchingnetwork: cordova.addWindowEventHandler("watchingnetwork")
    };
    for (var key in this.channels) {
        this.channels[key].onHasSubscribersChange = Cellular.onHasSubscribersChange;
    }

};

Cellular.onHasSubscribersChange = function() {
    exec(cellular.status, cellular.error, "Cellular", "getCellularInfo", []);
};

/**
 * Callback for cellular initiated
 *
 * @param {Object} info            keys: imei, isPlugged
 */
Cellular.prototype.status = function(info) {
    cordova.fireWindowEvent("watchingnetwork", info);
    if (info) {
        if (cellular.imei !== info.imei || cellular.operator !== info.operator) {

            if (info.imei === null && cellular.imei !== null) {
                return; // special case where callback is called because we stopped listening to the native side.
            }

            // Something changed. Fire watching network event

            cellular.imei = info.imei;
            cellular.operator = info.operator;
            cellular.cellID = info.cellID;
            cellular.lac = info.lac;
            cellular.neighbors = info.neighbors;
        }
    }
};

/**
 * Error callback for cellular initiated
 */
cellular.prototype.error = function(e) {
    console.log("Error initializing advanced network plugin: " + e);
};

var cellular = new Cellular();

module.exports = cellular;
